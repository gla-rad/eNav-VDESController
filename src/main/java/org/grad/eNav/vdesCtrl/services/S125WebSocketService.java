/*
 * Copyright (c) 2021 GLA UK Research and Development Directive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.grad.eNav.vdesCtrl.services;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;

/**
 * The S125WebSocketService Class
 *
 * This class implements a handler for the AtoN messages coming into a Spring
 * Integration channel. It basically just publishes them to another channel,
 * which happens to be a web-socket implementation.
 *
 * @author Nikolaos Vastardis
 */
@Service
@Slf4j
public class S125WebSocketService implements MessageHandler {

    /**
     * The General Destination Prefix
     */
    @Value("${gla.rad.radar-service.web-socket.prefix:topic}")
    private String prefix;

    /**
     * The AtoN Publish Channel to listen the AtoN messages to.
     */
    @Autowired
    @Qualifier("atonPublishChannel")
    private PublishSubscribeChannel atonPublishChannel;

    /**
     * Attach the web-socket as a simple messaging template
     */
    @Autowired
    private SimpMessagingTemplate webSocket;

    /**
     * The service post-construct operations where the handler auto-registers
     * it-self to the aton publication channel. Once successful, it will then
     * monitor the channel for all inputs coming through the REST API.
     */
    @PostConstruct
    public void init() {
        log.info("AtoN Message Web Socket Service is booting up...");
        this.atonPublishChannel.subscribe(this);
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("AtoN Message Web Socket Service is shutting down...");
        if (this.atonPublishChannel != null) {
            this.atonPublishChannel.destroy();
        }
    }

    /**
     * This is a simple handler for the incoming messages. This is a generic
     * handler for any type of Spring Integration messages but it should really
     * only be used for the ones containing S-125 message payloads.
     *
     * @param message               The message to be handled
     * @throws MessagingException   The Messaging exceptions that might occur
     */
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        // Check that this seems ot be a valid message
        if(!(message.getPayload() instanceof S125Node)) {
            log.warn("Radar message handler received a message with erroneous format.");
            return;
        }

        // Get the header and payload of the incoming message
        String endpoint = Objects.toString(message.getHeaders().get(MessageHeaders.CONTENT_TYPE));
        S125Node s125Node = (S125Node) message.getPayload();

        // A simple debug message;
        log.debug(String.format("Received AtoN Message with UID: %s.", s125Node.getAtonUID()));

        // Now push the aton node down the web-socket stream
        this.pushAton(this.webSocket, String.format("/%s/%s", prefix, endpoint), s125Node);
    }

    /**
     * Pushes a new/updated AtoN node into the Web-Socket messaging template.
     *
     * @param messagingTemplate     The web-socket messaging template
     * @param topic                 The topic of the web-socket
     * @param payload               The payload to be pushed
     */
    private void pushAton(SimpMessagingTemplate messagingTemplate, String topic, Object payload) {
        messagingTemplate.convertAndSend(topic, payload);
    }

}
