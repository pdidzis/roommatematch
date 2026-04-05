package com.roommatematch.service;

import com.roommatematch.dto.response.AdminStatsResponse;
import com.roommatematch.model.entity.Match;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.enums.ListingStatus;
import com.roommatematch.model.enums.MatchStatus;
import com.roommatematch.model.enums.UserRole;
import com.roommatematch.repository.ChatRoomRepository;
import com.roommatematch.repository.ListingRepository;
import com.roommatematch.repository.MatchRepository;
import com.roommatematch.repository.MessageRepository;
import com.roommatematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public AdminStatsResponse getStats() {
        // Count users
        long totalUsers = userRepository.count();

        // Users by role
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> usersByRole = Arrays.stream(UserRole.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        role -> allUsers.stream()
                                .filter(u -> u.getRole() == role)
                                .count()
                ));

        // Count listings
        long totalListings = listingRepository.count();

        // Listings by status
        Map<String, Long> listingsByStatus = new HashMap<>();
        for (ListingStatus status : ListingStatus.values()) {
            long count = listingRepository.findByStatus(status).size();
            listingsByStatus.put(status.name(), count);
        }

        // Count matches
        List<Match> allMatches = matchRepository.findAll();
        long totalMatches = allMatches.size();

        // Matches by status
        Map<String, Long> matchesByStatus = Arrays.stream(MatchStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        status -> allMatches.stream()
                                .filter(m -> m.getStatus() == status)
                                .count()
                ));

        // Total messages
        long totalMessages = messageRepository.count();

        // Confirmed roommates count
        long totalConfirmedRoommates = allMatches.stream()
                .filter(m -> m.getStatus() == MatchStatus.ROOMMATE_CONFIRMED)
                .count();

        // Landlord chats count
        long totalLandlordChats = chatRoomRepository.countByChatType("LANDLORD");

        // Average compatibility score
        Double averageCompatibilityScore = allMatches.stream()
                .filter(m -> m.getCompatibilityScore() != null)
                .mapToDouble(Match::getCompatibilityScore)
                .average()
                .orElse(0.0);

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .usersByRole(usersByRole)
                .totalListings(totalListings)
                .listingsByStatus(listingsByStatus)
                .totalMatches(totalMatches)
                .matchesByStatus(matchesByStatus)
                .totalMessages(totalMessages)
                .totalConfirmedRoommates(totalConfirmedRoommates)
                .totalLandlordChats(totalLandlordChats)
                .averageCompatibilityScore(Math.round(averageCompatibilityScore * 100.0) / 100.0)
                .build();
    }
}
