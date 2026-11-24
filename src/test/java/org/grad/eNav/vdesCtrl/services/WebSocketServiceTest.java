/*
 * Copyright (c) 2024 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.vdesCtrl.services;

import org.grad.eNav.vdesCtrl.models.PubSubMsgHeaders;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    WebSocketService webSocketService;

    /**
     * The Publish Subscribe Channel mock.
     */
    @Mock
    PublishSubscribeChannel publishSubscribeChannel;

    /**
     * The Web Socket mock.
     */
    @Mock
    SimpMessagingTemplate webSocket;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() throws IOException {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Also set the web-socket service topic prefix
        this.webSocketService.prefix = "topic";
    }

    /**
     * Test that the S125 web-socket service gets initialised correctly,
     * and it subscribes to the AtoN publish subscribe channel.
     */
    @Test
    void testInit() {
        // Perform the service call
        this.webSocketService.init();

        verify(this.publishSubscribeChannel, times(1)).subscribe(this.webSocketService);
    }

    /**
     * Test that the S125 web-socket service gets destroyed correctly,
     * and it un-subscribes from the AtoN publish subscribe channel.
     */
    @Test
    void testDestroy() {
        // Perform the service call
        this.webSocketService.destroy();

        verify(this.publishSubscribeChannel, times(1)).destroy();
    }

    /**
     * Test that the Web-Socket controlling service can process correctly the
     * binary messages published in the publish-subscribe channel.
     */
    @Test
    void testHandleBinaryMessage() throws IOException {
        // Create a message to be handled
        Message<?> message = Optional.of(new byte[] {0x01, 0x02, 0x0E, 0x0F}).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, byte[].class))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), "127.0.0.1"))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PORT.getHeader(), 8000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.MMSI.getHeader(), "111111111"))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.webSocketService.handleMessage(message);

        // Verify that we send a packet to the VDES port and get that packet
        ArgumentCaptor<String> topicArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payLoadArgument = ArgumentCaptor.forClass(String.class);
        verify(this.webSocket, times(1)).convertAndSend(topicArgument.capture(), payLoadArgument.capture());

        // Verify the packet
        assertEquals("/topic/messages/127.0.0.1:8000", topicArgument.getValue());
        assertEquals("01020E0F", payLoadArgument.getValue());
    }

    /**
     * Test that the Web-Socket controlling service can process correctly the
     * simple string messages published in the publish-subscribe channel.
     */
    @Test
    void testHandleStringMessage() throws IOException {
        // Create a message to be handled
        Message<?> message = Optional.of("This is a simple message").map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, String.class))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), "127.0.0.1"))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PORT.getHeader(), 8000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.MMSI.getHeader(), "111111111"))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.webSocketService.handleMessage(message);

        // Verify that we send a packet to the VDES port and get that packet
        ArgumentCaptor<String> topicArgument = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payLoadArgument = ArgumentCaptor.forClass(String.class);
        verify(this.webSocket, times(1)).convertAndSend(topicArgument.capture(), payLoadArgument.capture());

        // Verify the packet
        assertEquals("/topic/messages/127.0.0.1:8000", topicArgument.getValue());
        assertEquals("This is a simple message", payLoadArgument.getValue());
    }

    /**
     * Test that we can only send valid messages down to the web-socket.
     */
    @Test
    void testHandleMessageWrongPayload() throws IOException {
        // Change the message content type to something else
        Message<?> message = Optional.of(Integer.MAX_VALUE).map(MessageBuilder::withPayload)
                .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, Object.class))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), "127.0.0.1"))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.PORT.getHeader(), 8000))
                .map(builder -> builder.setHeader(PubSubMsgHeaders.MMSI.getHeader(), "111111111"))
                .map(MessageBuilder::build)
                .orElse(null);

        // Perform the service call
        this.webSocketService.handleMessage(message);

        // Verify that we didn't send any packets to the VDES port
        verify(this.webSocket, never()).convertAndSend(any(String.class), any(Object.class));
    }

}