/*
 * Copyright (c) 2020 - Department of Research & Development
 * General Lighthouse Authorities of the UK and Ireland's.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 */

package org.grad.eNav.vdesCtrl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * The WebSocketConfig Class
 *
 * This configuration class sets up the WebSocket for this app where remote
 * clients can monitor the incoming AtoN data.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * The WebSocket Name
     */
    @Value("${gla.rad.vdes-ctlr.web-socket.name:vdes-ctlr-websocket}")
    private String webSocketName;

    /**
     * The General Destination Prefix
     */
    @Value("${gla.rad.vdes-ctlr.web-socket.prefix:topic}")
    private String prefix;

    /**
     * The VDES Controller Data Endpoint of the WebSocket
     */
    @Value("${gla.rad.vdes-ctlr.web-socket.radar-data-endpoint:aton}")
    private String radarDataEndpoint;

    /**
     * This function implements the basic registration for our WebSocket message
     * broker. It basically set's the destination prefix and all endpoints.
     *
     * @param config    The message broker configuration
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/"+prefix);
        config.setApplicationDestinationPrefixes("/"+radarDataEndpoint);
    }

    /**
     * This is where the WebSocket is actually registered into the application
     * as an endpoint and become active.
     *
     * @param registry  The active WebSocket Registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/"+ webSocketName).withSockJS();
    }

}
