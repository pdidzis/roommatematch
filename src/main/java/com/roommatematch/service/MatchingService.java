package com.roommatematch.service;

import com.roommatematch.dto.response.MatchResponse;
import com.roommatematch.dto.response.UserSummary;
import com.roommatematch.exception.BusinessLogicException;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.Match;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.entity.UserPreferences;
import com.roommatematch.model.enums.MatchStatus;
import com.roommatematch.model.enums.UserRole;
import com.roommatematch.repository.MatchRepository;
import com.roommatematch.repository.UserPreferencesRepository;
import com.roommatematch.repository.UserRepository;
import com.roommatematch.service.matching.CompatibilityResult;
import com.roommatematch.service.matching.CompatibilityScorer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final CompatibilityScorer compatibilityScorer;
    private final NotificationService notificationService;

    public List<MatchResponse> findSuggestions(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        UserPreferences currentPrefs = currentUser.getPreferences();
        if (currentPrefs == null) {
            throw new BusinessLogicException("Please complete your preferences before viewing matches");
        }

        // Load all active TENANT users excluding current user
        List<User> candidates = userRepository.findByRoleAndIsActive(UserRole.TENANT, true)
                .stream()
                .filter(u -> !u.getId().equals(userId))
                .collect(Collectors.toList());

        // Filter out already matched users
        List<Match> existingMatches = matchRepository.findByRequesterOrReceiver(currentUser, currentUser);
        Set<Long> matchedUserIds = new HashSet<>();
        for (Match match : existingMatches) {
            if (match.getRequester().getId().equals(userId)) {
                matchedUserIds.add(match.getReceiver().getId());
            } else {
                matchedUserIds.add(match.getRequester().getId());
            }
        }
        candidates.removeIf(c -> matchedUserIds.contains(c.getId()));

        // Calculate compatibility scores
        List<MatchResponse> suggestions = new ArrayList<>();
        for (User candidate : candidates) {
            UserPreferences candidatePrefs = candidate.getPreferences();
            CompatibilityResult result = compatibilityScorer.calculateScore(currentPrefs, candidatePrefs);

            if (result.getFinalScore() >= 0.0) {
                suggestions.add(MatchResponse.builder()
                        .matchId(null)
                        .compatibilityScore(result.getFinalScore())
                        .confidenceScore(result.getConfidenceScore())
                        .status(null)
                        .otherUser(mapToUserSummary(candidate))
                        .scoreBreakdown(result.getBreakdown())
                        .createdAt(null)
                        .build());
            }
        }

        // Sort by score descending and limit to top 20
        suggestions.sort(Comparator.comparingDouble(MatchResponse::getCompatibilityScore).reversed());
        List<MatchResponse> topSuggestions = suggestions.stream().limit(20).collect(Collectors.toList());

        log.info("Found {} suggestions for user: {}", topSuggestions.size(), userId);
        return topSuggestions;
    }

    @Transactional
    public MatchResponse requestMatch(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new BusinessLogicException("Cannot send match request to yourself");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", requesterId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", receiverId));

        // Check for duplicate match in either direction
        if (matchRepository.existsByRequesterAndReceiver(requester, receiver) ||
            matchRepository.existsByRequesterAndReceiver(receiver, requester)) {
            throw new BusinessLogicException("Match request already exists between these users");
        }

        // Calculate compatibility score
        UserPreferences requesterPrefs = requester.getPreferences();
        UserPreferences receiverPrefs = receiver.getPreferences();
        CompatibilityResult result = compatibilityScorer.calculateScore(requesterPrefs, receiverPrefs);

        Match match = Match.builder()
                .requester(requester)
                .receiver(receiver)
                .compatibilityScore(result.getFinalScore())
                .status(MatchStatus.PENDING)
                .build();

        match = matchRepository.save(match);
        log.info("Match request created from user {} to user {}", requesterId, receiverId);

        notificationService.createNotification(
                receiver.getId(),
                requester.getFirstName() + " wants to be your roommate!",
                requester.getFirstName() + " " + requester.getLastName() + " sent you a match request.",
                "MATCH_REQUEST",
                match.getId()
        );

        return mapToMatchResponse(match, receiver, result);
    }

    @Transactional
    public MatchResponse respondToMatch(Long matchId, Long currentUserId, boolean accept) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId));

        if (!match.getReceiver().getId().equals(currentUserId)) {
            throw new BusinessLogicException("Only the receiver can respond to this match request");
        }

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new BusinessLogicException("This match request has already been responded to");
        }

        match.setStatus(accept ? MatchStatus.ACCEPTED : MatchStatus.DECLINED);
        match = matchRepository.save(match);

        log.info("Match {} {} by user {}", matchId, accept ? "accepted" : "declined", currentUserId);

        if (accept) {
            notificationService.createNotification(
                    match.getRequester().getId(),
                    "Match accepted! 🎊",
                    match.getReceiver().getFirstName() + " accepted your roommate request. Start chatting!",
                    "MATCH_ACCEPTED",
                    match.getId()
            );
        } else {
            notificationService.createNotification(
                    match.getRequester().getId(),
                    "Match update",
                    match.getReceiver().getFirstName() + " has declined your match request.",
                    "MATCH_DECLINED",
                    match.getId()
            );
        }

        User otherUser = match.getRequester();
        CompatibilityResult result = compatibilityScorer.calculateScore(
                match.getRequester().getPreferences(),
                match.getReceiver().getPreferences()
        );

        return mapToMatchResponse(match, otherUser, result);
    }

    public List<MatchResponse> getMyMatches(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<Match> matches = matchRepository.findByRequesterOrReceiver(user, user);

        return matches.stream().map(match -> {
            User otherUser = match.getRequester().getId().equals(userId)
                    ? match.getReceiver()
                    : match.getRequester();

            CompatibilityResult result = compatibilityScorer.calculateScore(
                    match.getRequester().getPreferences(),
                    match.getReceiver().getPreferences()
            );

            return mapToMatchResponse(match, otherUser, result);
        }).collect(Collectors.toList());
    }

    private MatchResponse mapToMatchResponse(Match match, User otherUser, CompatibilityResult result) {
        return MatchResponse.builder()
                .matchId(match.getId())
                .compatibilityScore(match.getCompatibilityScore())
                .confidenceScore(result != null ? result.getConfidenceScore() : null)
                .status(match.getStatus())
                .otherUser(mapToUserSummary(otherUser))
                .scoreBreakdown(result != null ? result.getBreakdown() : null)
                .createdAt(match.getCreatedAt() != null ? match.getCreatedAt().toString() : null)
                .build();
    }

    private UserSummary mapToUserSummary(User user) {
        String city = null;
        if (user.getPreferences() != null) {
            city = user.getPreferences().getCity();
        }

        return UserSummary.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .city(city)
                .build();
    }
}
