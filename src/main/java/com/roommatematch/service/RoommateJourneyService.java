package com.roommatematch.service;

import com.roommatematch.dto.response.ChatRoomResponse;
import com.roommatematch.dto.response.ListingResponse;
import com.roommatematch.dto.response.MatchResponse;
import com.roommatematch.dto.response.MessageResponse;
import com.roommatematch.dto.response.UserSummary;
import com.roommatematch.exception.BusinessLogicException;
import com.roommatematch.exception.DuplicateResourceException;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.model.entity.ChatRoom;
import com.roommatematch.model.entity.Listing;
import com.roommatematch.model.entity.ListingInterest;
import com.roommatematch.model.entity.Match;
import com.roommatematch.model.entity.Message;
import com.roommatematch.model.entity.User;
import com.roommatematch.model.enums.ListingStatus;
import com.roommatematch.model.enums.MatchStatus;
import com.roommatematch.model.enums.MessageStatus;
import com.roommatematch.repository.ChatRoomRepository;
import com.roommatematch.repository.ListingInterestRepository;
import com.roommatematch.repository.ListingRepository;
import com.roommatematch.repository.MatchRepository;
import com.roommatematch.repository.MessageRepository;
import com.roommatematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoommateJourneyService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ListingInterestRepository listingInterestRepository;
    private final ListingRepository listingRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    public MatchResponse confirmRoommate(Long matchId, Long currentUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.ACCEPTED) {
            throw new BusinessLogicException("You can only confirm a roommate from an accepted match");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isRequester = match.getRequester().getId().equals(currentUserId);
        boolean isReceiver = match.getReceiver().getId().equals(currentUserId);

        if (!isRequester && !isReceiver) {
            throw new BusinessLogicException("You are not part of this match");
        }

        match.setStatus(MatchStatus.ROOMMATE_CONFIRMED);
        match = matchRepository.save(match);

        User otherUser = isRequester ? match.getReceiver() : match.getRequester();
        notificationService.createNotification(
                otherUser.getId(),
                "Roommate confirmed! 🏠",
                currentUser.getFirstName() + " confirmed you as their roommate. You can now browse listings together!",
                "ROOMMATE_CONFIRMED",
                match.getId()
        );

        chatRoomRepository.findByMatchAndChatType(match, "ROOMMATE")
                .ifPresent(chatRoom -> {
                    Message confirmMessage = Message.builder()
                            .chatRoom(chatRoom)
                            .sender(currentUser)
                            .content("You both confirmed as roommates! You can now browse listings together.")
                            .status(MessageStatus.SENT)
                            .build();
                    messageRepository.save(confirmMessage);

                    MessageResponse messageResponse = mapToMessageResponse(confirmMessage, currentUserId);
                    messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), messageResponse);

                    Message offersMessage = Message.builder()
                            .chatRoom(chatRoom)
                            .sender(currentUser)
                            .content("As confirmed roommates, you have access to exclusive partner offers! Check the Offers tab.")
                            .status(MessageStatus.SENT)
                            .build();
                    messageRepository.save(offersMessage);

                    MessageResponse offersResponse = mapToMessageResponse(offersMessage, currentUserId);
                    messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), offersResponse);
                });

        log.info("Roommate confirmed for match: {}", matchId);

        return mapToMatchResponse(match, currentUserId);
    }

    public List<ListingResponse> getListingsForRoommates(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Match> confirmedMatches = matchRepository.findByRequesterOrReceiver(user, user)
                .stream()
                .filter(m -> m.getStatus() == MatchStatus.ROOMMATE_CONFIRMED)
                .collect(Collectors.toList());

        if (confirmedMatches.isEmpty()) {
            throw new BusinessLogicException("You need a confirmed roommate to browse listings");
        }

        Match activeMatch = confirmedMatches.get(0);
        User otherUser = activeMatch.getRequester().getId().equals(userId)
                ? activeMatch.getReceiver()
                : activeMatch.getRequester();

        List<Listing> listings = listingRepository.findByStatusAndIsVerifiedOrderByCreatedAtDesc(
                ListingStatus.ACTIVE, true);

        return listings.stream()
                .map(listing -> mapToListingResponseWithInterest(listing, user, otherUser, activeMatch))
                .collect(Collectors.toList());
    }

    @Transactional
    public String expressInterestInListing(Long userId, Long listingId, Long matchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        if (match.getStatus() != MatchStatus.ROOMMATE_CONFIRMED) {
            throw new BusinessLogicException("Match must be confirmed to express interest");
        }

        boolean isRequester = match.getRequester().getId().equals(userId);
        boolean isReceiver = match.getReceiver().getId().equals(userId);

        if (!isRequester && !isReceiver) {
            throw new BusinessLogicException("You are not part of this match");
        }

        if (listingInterestRepository.existsByUserAndListing(user, listing)) {
            throw new DuplicateResourceException("You already expressed interest in this listing");
        }

        ListingInterest interest = ListingInterest.builder()
                .user(user)
                .listing(listing)
                .match(match)
                .build();
        listingInterestRepository.save(interest);

        log.info("User {} interested in listing {}", userId, listingId);

        User otherUser = isRequester ? match.getReceiver() : match.getRequester();

        if (listingInterestRepository.existsByUserAndListing(otherUser, listing)) {
            ChatRoom landlordChat = ChatRoom.builder()
                    .participant1(match.getRequester())
                    .participant2(match.getReceiver())
                    .landlord(listing.getLandlord())
                    .chatType("LANDLORD")
                    .match(match)
                    .listing(listing)
                    .build();
            landlordChat = chatRoomRepository.save(landlordChat);

            Message welcomeMessage = Message.builder()
                    .chatRoom(landlordChat)
                    .sender(user)
                    .content("Both roommates are interested in: " + listing.getTitle() +
                            ". The landlord has been notified and will join this chat shortly.")
                    .status(MessageStatus.SENT)
                    .build();
            messageRepository.save(welcomeMessage);

            MessageResponse messageResponse = mapToMessageResponse(welcomeMessage, userId);
            messagingTemplate.convertAndSend("/topic/chat/" + landlordChat.getId(), messageResponse);

            notificationService.createNotification(
                    otherUser.getId(),
                    "Your roommate likes the same listing! 👀",
                    "You and " + user.getFirstName() + " are both interested in: " + listing.getTitle() +
                            ". A landlord chat has been created!",
                    "LANDLORD_CHAT_CREATED",
                    landlordChat.getId()
            );

            notificationService.createNotification(
                    user.getId(),
                    "Landlord chat created! 🏡",
                    "Both you and your roommate are interested in: " + listing.getTitle() +
                            ". Check your new landlord chat!",
                    "LANDLORD_CHAT_CREATED",
                    landlordChat.getId()
            );

            log.info("Landlord chat created for listing {} between users {} and {}",
                    listingId, userId, otherUser.getId());

            return "Both interested! Landlord chat created.";
        }

        return "Interest registered. Waiting for your roommate to also like this listing.";
    }

    public List<ChatRoomResponse> getLandlordChats(Long landlordId) {
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new ResourceNotFoundException("Landlord not found"));

        List<ChatRoom> chats = chatRoomRepository.findByLandlord(landlord);

        return chats.stream()
                .map(chat -> mapToLandlordChatRoomResponse(chat, landlordId))
                .collect(Collectors.toList());
    }

    public List<ListingResponse> getMyInterests(Long userId, Long matchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        User otherUser = match.getRequester().getId().equals(userId)
                ? match.getReceiver()
                : match.getRequester();

        List<ListingInterest> interests = listingInterestRepository.findByUserAndMatch(user, match);

        return interests.stream()
                .map(interest -> mapToListingResponseWithInterest(
                        interest.getListing(), user, otherUser, match))
                .collect(Collectors.toList());
    }

    private MatchResponse mapToMatchResponse(Match match, Long currentUserId) {
        User otherUser = match.getRequester().getId().equals(currentUserId)
                ? match.getReceiver()
                : match.getRequester();

        return MatchResponse.builder()
                .matchId(match.getId())
                .compatibilityScore(match.getCompatibilityScore())
                .status(match.getStatus())
                .otherUser(UserSummary.builder()
                        .userId(otherUser.getId())
                        .firstName(otherUser.getFirstName())
                        .lastName(otherUser.getLastName())
                        .profilePhotoUrl(otherUser.getProfilePhotoUrl())
                        .city(otherUser.getPreferences() != null ?
                                otherUser.getPreferences().getCity() : null)
                        .build())
                .createdAt(match.getCreatedAt().format(FORMATTER))
                .build();
    }

    private MessageResponse mapToMessageResponse(Message message, Long currentUserId) {
        User sender = message.getSender();
        return MessageResponse.builder()
                .messageId(message.getId())
                .content(message.getContent())
                .sentAt(message.getSentAt().format(FORMATTER))
                .status(message.getStatus())
                .sender(UserSummary.builder()
                        .userId(sender.getId())
                        .firstName(sender.getFirstName())
                        .lastName(sender.getLastName())
                        .profilePhotoUrl(sender.getProfilePhotoUrl())
                        .build())
                .isOwnMessage(sender.getId().equals(currentUserId))
                .build();
    }

    private ListingResponse mapToListingResponseWithInterest(Listing listing, User currentUser,
                                                              User otherUser, Match match) {
        boolean currentUserInterested = listingInterestRepository
                .existsByUserAndListing(currentUser, listing);
        boolean otherUserInterested = listingInterestRepository
                .existsByUserAndListing(otherUser, listing);
        boolean bothInterested = currentUserInterested && otherUserInterested;

        Long landlordChatId = null;
        if (bothInterested) {
            landlordChatId = chatRoomRepository.findByListingAndChatType(listing, "LANDLORD")
                    .map(ChatRoom::getId)
                    .orElse(null);
        }

        List<String> photoUrls = listing.getPhotoUrls() != null && !listing.getPhotoUrls().isEmpty()
                ? Arrays.asList(listing.getPhotoUrls().split(","))
                : List.of();

        return ListingResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .address(listing.getAddress())
                .city(listing.getCity())
                .country(listing.getCountry())
                .monthlyRent(listing.getMonthlyRent())
                .availableFrom(listing.getAvailableFrom())
                .totalRooms(listing.getTotalRooms())
                .availableRooms(listing.getAvailableRooms())
                .petsAllowed(listing.isPetsAllowed())
                .smokingAllowed(listing.isSmokingAllowed())
                .photoUrls(photoUrls)
                .status(listing.getStatus())
                .isVerified(listing.isVerified())
                .landlordName(listing.getLandlord().getFirstName() + " " +
                        listing.getLandlord().getLastName())
                .landlordEmail(listing.getLandlord().getEmail())
                .landlordPhone(listing.getLandlord().getPhoneNumber())
                .createdAt(listing.getCreatedAt().format(FORMATTER))
                .currentUserInterested(currentUserInterested)
                .bothRoommatesInterested(bothInterested)
                .landlordChatId(landlordChatId)
                .build();
    }

    private ChatRoomResponse mapToLandlordChatRoomResponse(ChatRoom chat, Long landlordId) {
        Message lastMessage = chat.getMessages() != null && !chat.getMessages().isEmpty()
                ? chat.getMessages().get(chat.getMessages().size() - 1)
                : null;

        UserSummary participant = UserSummary.builder()
                .userId(chat.getParticipant1().getId())
                .firstName(chat.getParticipant1().getFirstName() + " & " +
                        chat.getParticipant2().getFirstName())
                .lastName("")
                .build();

        return ChatRoomResponse.builder()
                .chatRoomId(chat.getId())
                .otherParticipant(participant)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageTime(lastMessage != null ?
                        lastMessage.getSentAt().format(FORMATTER) : null)
                .chatType(chat.getChatType())
                .listingTitle(chat.getListing() != null ? chat.getListing().getTitle() : null)
                .listingId(chat.getListing() != null ? chat.getListing().getId() : null)
                .landlordName(chat.getLandlord() != null ?
                        chat.getLandlord().getFirstName() + " " + chat.getLandlord().getLastName()
                        : null)
                .build();
    }
}
