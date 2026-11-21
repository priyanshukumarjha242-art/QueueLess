package com.queueless.service;

import com.queueless.dto.TokenDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketPublisherService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPublisherService.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPublisherService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishTokenUpdate(TokenDto tokenDto) {
        try {
            // Personal token topic
            if (tokenDto.getId() != null) {
                messagingTemplate.convertAndSend("/topic/token/" + tokenDto.getId(), tokenDto);
            }
            if (tokenDto.getTokenNumber() != null) {
                messagingTemplate.convertAndSend("/topic/token-num/" + tokenDto.getTokenNumber(), tokenDto);
            }

            // Service queue topic
            if (tokenDto.getServiceId() != null) {
                messagingTemplate.convertAndSend("/topic/service/" + tokenDto.getServiceId(), tokenDto);
            }

            // Organization board topic
            if (tokenDto.getOrganizationId() != null) {
                messagingTemplate.convertAndSend("/topic/org/" + tokenDto.getOrganizationId(), tokenDto);
            }

            // Counter topic
            if (tokenDto.getCounterId() != null) {
                messagingTemplate.convertAndSend("/topic/counter/" + tokenDto.getCounterId(), tokenDto);
            }
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message for token: {}", tokenDto.getTokenNumber(), e);
        }
    }

    public void publishBroadcast(String destination, Object payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            log.error("Failed to broadcast message to {}", destination, e);
        }
    }
}
