package com.roommatematch.service;

import com.roommatematch.dto.response.NotificationResponse;
import com.roommatematch.exception.ResourceNotFoundException;
import com.roommatematch.exception.UnauthorizedException;
import com.roommatematch.model.entity.Notification;
import com.roommatematch.model.entity.User;
import com.roommatematch.repository.NotificationRepository;
import com.roommatematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification createNotification(Long userId, String title, String message,
                                           String type, Long relatedId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot create notification: User not found with id {}", userId);
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .relatedId(relatedId)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                mapToResponse(savedNotification)
        );

        log.info("Notification created for user {}: {}", userId, title);

        return savedNotification;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        notificationRepository.markAllAsReadForUser(user);
        log.info("All notifications marked as read for user {}", userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not your notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        log.info("Notification {} marked as read for user {}", notificationId, userId);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.isRead())
                .relatedId(n.getRelatedId())
                .createdAt(n.getCreatedAt() != null ? n.getCreatedAt().toString() : null)
                .build();
    }
}
