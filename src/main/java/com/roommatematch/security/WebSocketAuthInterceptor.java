package com.roommatematch.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                throw new MessageDeliveryException("Missing or invalid JWT token");
            }

            String authHeader = authHeaders.get(0);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessageDeliveryException("Missing or invalid JWT token");
            }

            String token = authHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                if (!jwtUtil.validateToken(token, userDetails)) {
                    throw new MessageDeliveryException("Invalid or expired JWT token");
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                accessor.setUser(authentication);
                log.info("WebSocket connected: {}", username);

            } catch (MessageDeliveryException e) {
                throw e;
            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage());
                throw new MessageDeliveryException("Invalid or expired JWT token");
            }
        }

        return message;
    }
}
