package com.roommatematch.service;

import com.roommatematch.dto.response.MatchResponse;
import com.roommatematch.exception.BusinessLogicException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @Mock
    private CompatibilityScorer compatibilityScorer;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MatchingService matchingService;

    private User currentUser;
    private UserPreferences currentUserPrefs;

    @BeforeEach
    void setUp() {
        currentUserPrefs = UserPreferences.builder()
                .id(1L)
                .minBudget(300)
                .maxBudget(600)
                .city("Riga")
                .petsAllowed(false)
                .smokingAllowed(false)
                .sleepSchedule(3)
                .cleanlinessLevel(3)
                .socialHabits(3)
                .noiseLevel(3)
                .guestFrequency(3)
                .workFromHome(3)
                .build();

        currentUser = User.builder()
                .id(1L)
                .email("current@test.com")
                .firstName("Current")
                .lastName("User")
                .role(UserRole.TENANT)
                .isActive(true)
                .preferences(currentUserPrefs)
                .build();

        currentUserPrefs.setUser(currentUser);
    }

    private User createCandidateUser(Long id) {
        UserPreferences prefs = UserPreferences.builder()
                .id(id)
                .minBudget(300)
                .maxBudget(600)
                .city("Riga")
                .petsAllowed(false)
                .smokingAllowed(false)
                .sleepSchedule(3)
                .cleanlinessLevel(3)
                .socialHabits(3)
                .noiseLevel(3)
                .guestFrequency(3)
                .workFromHome(3)
                .build();

        User user = User.builder()
                .id(id)
                .email("user" + id + "@test.com")
                .firstName("User" + id)
                .lastName("Test")
                .role(UserRole.TENANT)
                .isActive(true)
                .preferences(prefs)
                .build();

        prefs.setUser(user);
        return user;
    }

    @Test
    @DisplayName("findSuggestions returns max 20 results")
    void testFindSuggestionsReturnsMax20() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        List<User> candidates = new ArrayList<>();
        for (long i = 2L; i <= 26L; i++) {
            candidates.add(createCandidateUser(i));
        }
        when(userRepository.findByRoleAndIsActive(UserRole.TENANT, true)).thenReturn(candidates);
        when(matchRepository.findByRequesterOrReceiver(currentUser, currentUser))
                .thenReturn(Collections.emptyList());

        CompatibilityResult result = CompatibilityResult.builder()
                .finalScore(0.85)
                .confidenceScore(1.0)
                .breakdown(new HashMap<>())
                .build();
        when(compatibilityScorer.calculateScore(any(UserPreferences.class), any(UserPreferences.class)))
                .thenReturn(result);

        List<MatchResponse> suggestions = matchingService.findSuggestions(1L);

        assertEquals(20, suggestions.size());
    }

    @Test
    @DisplayName("findSuggestions excludes current user")
    void testFindSuggestionsExcludesCurrentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        List<User> candidates = new ArrayList<>();
        candidates.add(currentUser);
        candidates.add(createCandidateUser(2L));
        candidates.add(createCandidateUser(3L));
        when(userRepository.findByRoleAndIsActive(UserRole.TENANT, true)).thenReturn(candidates);
        when(matchRepository.findByRequesterOrReceiver(currentUser, currentUser))
                .thenReturn(Collections.emptyList());

        CompatibilityResult result = CompatibilityResult.builder()
                .finalScore(0.85)
                .confidenceScore(1.0)
                .breakdown(new HashMap<>())
                .build();
        when(compatibilityScorer.calculateScore(any(UserPreferences.class), any(UserPreferences.class)))
                .thenReturn(result);

        List<MatchResponse> suggestions = matchingService.findSuggestions(1L);

        assertEquals(2, suggestions.size());
        for (MatchResponse suggestion : suggestions) {
            assertNotEquals(1L, suggestion.getOtherUser().getUserId());
        }
    }

    @Test
    @DisplayName("findSuggestions excludes already matched users")
    void testFindSuggestionsExcludesAlreadyMatched() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        User user2 = createCandidateUser(2L);
        User user3 = createCandidateUser(3L);
        User user4 = createCandidateUser(4L);
        List<User> candidates = List.of(user2, user3, user4);
        when(userRepository.findByRoleAndIsActive(UserRole.TENANT, true)).thenReturn(new ArrayList<>(candidates));

        Match existingMatch = Match.builder()
                .id(1L)
                .requester(currentUser)
                .receiver(user2)
                .status(MatchStatus.PENDING)
                .build();
        when(matchRepository.findByRequesterOrReceiver(currentUser, currentUser))
                .thenReturn(List.of(existingMatch));

        CompatibilityResult result = CompatibilityResult.builder()
                .finalScore(0.85)
                .confidenceScore(1.0)
                .breakdown(new HashMap<>())
                .build();
        when(compatibilityScorer.calculateScore(any(UserPreferences.class), any(UserPreferences.class)))
                .thenReturn(result);

        List<MatchResponse> suggestions = matchingService.findSuggestions(1L);

        assertEquals(2, suggestions.size());
        for (MatchResponse suggestion : suggestions) {
            assertNotEquals(2L, suggestion.getOtherUser().getUserId());
        }
    }

    @Test
    @DisplayName("requestMatch saves with PENDING status")
    void testRequestMatchSavesWithPendingStatus() {
        User receiver = createCandidateUser(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(matchRepository.existsByRequesterAndReceiver(currentUser, receiver)).thenReturn(false);
        when(matchRepository.existsByRequesterAndReceiver(receiver, currentUser)).thenReturn(false);

        CompatibilityResult result = CompatibilityResult.builder()
                .finalScore(0.85)
                .confidenceScore(1.0)
                .breakdown(new HashMap<>())
                .build();
        when(compatibilityScorer.calculateScore(any(UserPreferences.class), any(UserPreferences.class)))
                .thenReturn(result);

        Match savedMatch = Match.builder()
                .id(1L)
                .requester(currentUser)
                .receiver(receiver)
                .compatibilityScore(0.85)
                .status(MatchStatus.PENDING)
                .build();
        when(matchRepository.save(any(Match.class))).thenReturn(savedMatch);

        MatchResponse response = matchingService.requestMatch(1L, 2L);

        verify(matchRepository, times(1)).save(any(Match.class));
        assertEquals(MatchStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("requestMatch throws exception for same user")
    void testRequestMatchThrowsForSameUser() {
        assertThrows(BusinessLogicException.class, () -> matchingService.requestMatch(1L, 1L));
    }

    @Test
    @DisplayName("requestMatch throws exception for duplicate match")
    void testRequestMatchThrowsForDuplicateMatch() {
        User receiver = createCandidateUser(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(matchRepository.existsByRequesterAndReceiver(currentUser, receiver)).thenReturn(true);

        assertThrows(BusinessLogicException.class, () -> matchingService.requestMatch(1L, 2L));
    }

    @Test
    @DisplayName("respondToMatch only receiver can respond")
    void testOnlyReceiverCanRespond() {
        User receiver = createCandidateUser(2L);
        Match match = Match.builder()
                .id(1L)
                .requester(currentUser)
                .receiver(receiver)
                .status(MatchStatus.PENDING)
                .build();
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(BusinessLogicException.class, () -> matchingService.respondToMatch(1L, 1L, true));
    }

    @Test
    @DisplayName("respondToMatch updates status to ACCEPTED")
    void testRespondToMatchAccepted() {
        User receiver = createCandidateUser(2L);
        Match match = Match.builder()
                .id(1L)
                .requester(currentUser)
                .receiver(receiver)
                .status(MatchStatus.PENDING)
                .compatibilityScore(0.85)
                .build();
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompatibilityResult result = CompatibilityResult.builder()
                .finalScore(0.85)
                .confidenceScore(1.0)
                .breakdown(new HashMap<>())
                .build();
        when(compatibilityScorer.calculateScore(any(UserPreferences.class), any(UserPreferences.class)))
                .thenReturn(result);

        MatchResponse response = matchingService.respondToMatch(1L, 2L, true);

        assertEquals(MatchStatus.ACCEPTED, response.getStatus());
        verify(matchRepository, times(1)).save(argThat(m -> m.getStatus() == MatchStatus.ACCEPTED));
    }
}
