/*
 * Copyright (c) 2021 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.grad.eNav.vdesCtrl.components;

import com.fasterxml.jackson.databind.JsonNode;
import org.grad.eNav.vdesCtrl.feign.CKeeperClient;
import org.grad.eNav.vdesCtrl.models.PubSubMsgHeaders;
import org.grad.eNav.vdesCtrl.models.domain.McpEntityType;
import org.grad.eNav.vdesCtrl.models.domain.SignatureMode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.models.dtos.AtonMessageDto;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.GeoJSONUtils;
import org.grad.vdes1000.comm.VDES1000Conn;
import org.grad.vdes1000.exceptions.VDES1000ConnException;
import org.grad.vdes1000.generic.AISChannelPref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Vdes1000AdvertiserTest {

    /**
     * The Tested Component.
     */
    @InjectMocks
    @Spy
    Vdes1000Advertiser vdes1000Advertiser;

    /**
     * The Publish-Subscribe Channel mock.
     */
    @Mock
    PublishSubscribeChannel publishSubscribeChannel;

    /**
     * The CKeeper Client mock.
     */
    @Mock
    CKeeperClient cKeeperClient;

    /**
     * The Station Service mock.
     */
    @Mock
    StationService stationService;

    // Test Variables
    private Station station;
    private AtonMessageDto atonMessageDto;
    private byte[] signature;
    private VDES1000Conn vdes1000Conn;
    private DatagramSocket fwdSocket;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() throws IOException, NoSuchAlgorithmException {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Create a station with an ID
        this.station = new Station();
        this.station.setId(BigInteger.ONE);
        this.station.setName("Existing Station Name");
        this.station.setChannel(AISChannelPref.B);
        this.station.setSignatureMode(SignatureMode.NONE);
        this.station.setMmsi("222222222");
        this.station.setIpAddress("10.0.0.2");
        this.station.setType(StationType.VDES_1000);
        this.station.setPort(8002);
        this.station.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));

        // Read a valid S125 content to generate the S125Node message for.
        InputStream in = new ClassPathResource("s125-msg.xml").getInputStream();
        String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        // Also create a GeoJSON point geometry for our S125 message
        JsonNode point = GeoJSONUtils.createGeoJSONPoint(53.61, 1.594);

        // Now create the S125 node object
        this.atonMessageDto = new AtonMessageDto(new S125Node("test_aton", point, xml), false);

        // Mock a signature
        this.signature = MessageDigest.getInstance("SHA-256").digest(("That's the signature?").getBytes());

        // Mock the VDES-1000 connection, to be used in the tests
        this.vdes1000Conn = mock(VDES1000Conn.class);

        // Finally moch the VDES-1000 UDP forward socket
        this.fwdSocket = mock(DatagramSocket.class);
    }

    /**
     * Test that the VDES-1000 advertiser can initialise correctly.
     */
    @Test
    void testInit() throws SocketException, UnknownHostException {
        // Perform the component call
        doReturn(this.vdes1000Conn).when(vdes1000Advertiser).getVdes1000Conn();
        this.vdes1000Advertiser.init(this.station);

        assertEquals(this.station, this.vdes1000Advertiser.station);
        assertNotNull(this.vdes1000Advertiser.vdes1000Conn);

        // Make sure the monitoring will attempt to start
        verify(this.vdes1000Conn, times(1)).startMonitoring();
    }

    /**
     * Test that the VDES-1000 advertiser can be destroyed gracefully and
     * will close its UDP connection to the GNURadio device.
     */
    @Test
    void testDestroy() throws SocketException, UnknownHostException, InterruptedException {
        // Initialise the advertiser
        doReturn(this.vdes1000Conn).when(vdes1000Advertiser).getVdes1000Conn();
        this.vdes1000Advertiser.init(this.station);

        // Perform the service class
        this.vdes1000Advertiser.destroy();

        // Assert that the UDP socket was closed
        verify(this.vdes1000Conn, times(1)).close();
    }

    /**
     * Test that the VDES-1000 advertiser can actually read the station
     * messages from the message service and advertise the connected VDES-1000
     * station.
     */
    @Test
    void testAdvertiseAtons() throws VDES1000ConnException {
        doReturn(this.vdes1000Conn).when(vdes1000Advertiser).getVdes1000Conn();
        doReturn(Collections.singletonList(this.atonMessageDto)).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));

        // Initialise the advertiser and perform the component call
        this.vdes1000Advertiser.station = this.station;
        this.vdes1000Advertiser.signatureAlgorithm = "algorithm";
        this.vdes1000Advertiser.signatureDestMmmsi = 123456789;
        this.vdes1000Advertiser.advertiseAtons();

        // Make sure the UDP packet was sent to the GRURadio station
        verify(this.vdes1000Conn, times(1)).sendMessage(any(), eq(this.station.getChannel()));
        verify(this.vdes1000Conn, never()).sendMessageWithBBM(any(), eq(this.station.getChannel()));
    }

    /**
     * Test that the VDES-1000 advertiser can actually read the station
     * messages from the message service but will not advertise the ones that
     * have been blacklisted.
     */
    @Test
    void testAdvertiseAtonsBlacklisted() throws VDES1000ConnException {
        doReturn(Collections.emptyList()).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));

        // Initialise the advertiser and perform the component call
        this.vdes1000Advertiser.station = this.station;
        this.vdes1000Advertiser.signatureAlgorithm = "algorithm";
        this.vdes1000Advertiser.signatureDestMmmsi = 123456789;
        this.vdes1000Advertiser.advertiseAtons();

        // Make sure the UDP packet was sent to the GRURadio station
        verify(this.vdes1000Conn, never()).sendMessage(any(), eq(this.station.getChannel()));
        verify(this.vdes1000Conn, never()).sendMessageWithBBM(any(), eq(this.station.getChannel()));
    }

    /**
     * Test that the VDES-1000 advertiser can actually read the station
     * messages from the message service and advertise the connected VDES-1000
     * station. It will also send a second message over AIS, containing the
     * signature of the first, if that feature is enabled.
     */
    @Test
    void testAdvertiseAtonsWithSignatureAIS() throws VDES1000ConnException {
        // Enable AIS signatures for this station
        this.station.setSignatureMode(SignatureMode.AIS);

        doReturn(this.vdes1000Conn).when(vdes1000Advertiser).getVdes1000Conn();
        doReturn(Collections.singletonList(this.atonMessageDto)).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));
        doReturn(this.signature).when(this.cKeeperClient).generateEntitySignature(any(String.class), any(String.class), any(String.class), eq(McpEntityType.DEVICE.getValue()), any(byte[].class));

        // Initialise the advertiser and perform the component call
        this.vdes1000Advertiser.station = this.station;
        this.vdes1000Advertiser.signatureAlgorithm = "algorithm";
        this.vdes1000Advertiser.signatureDestMmmsi = 123456789;
        this.vdes1000Advertiser.advertiseAtons();

        // Make sure the UDP packet was sent to the GRURadio station
        verify(this.vdes1000Conn, times(1)).sendMessage(any(), eq(this.station.getChannel()));
        verify(this.vdes1000Conn, times(1)).sendMessageWithBBM(any(), eq(this.station.getChannel()));
    }

    /**
     * Test that the VDES-1000 advertiser can actually read the station
     * messages from the message service and advertise the connected VDES-1000
     * station. It will also send a second message over VDE, containing the
     * signature of the first, if that feature is enabled.
     */
    @Test
    void testAdvertiseAtonsWithSignatureVDE() throws VDES1000ConnException {
        // Enable VDE signatures for this station
        this.station.setSignatureMode(SignatureMode.VDE);

        doReturn(this.vdes1000Conn).when(vdes1000Advertiser).getVdes1000Conn();
        doReturn(Collections.singletonList(this.atonMessageDto)).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));
        doReturn(this.signature).when(this.cKeeperClient).generateEntitySignature(any(String.class), any(String.class), any(String.class), eq(McpEntityType.DEVICE.getValue()), any(byte[].class));

        // Initialise the advertiser and perform the component call
        this.vdes1000Advertiser.station = this.station;
        this.vdes1000Advertiser.signatureAlgorithm = "algorithm";
        this.vdes1000Advertiser.signatureDestMmmsi = 123456789;
        this.vdes1000Advertiser.advertiseAtons();

        // Make sure the UDP packet was sent to the GRURadio station
        verify(this.vdes1000Conn, times(1)).sendMessage(any(), eq(this.station.getChannel()));
        verify(this.vdes1000Conn, times(0)).sendMessageWithBBM(any(), eq(this.station.getChannel()));
    }

    /**
     * Test that the VDES-1000 advertiser will not actually send anything
     * if an empty/null S125 message is received
     */
    @Test
    void testAdvertiseAtonsEmptyMessage() throws VDES1000ConnException {
        // Enable signature for this station
        this.station.setSignatureMode(SignatureMode.AIS);

        this.atonMessageDto.setContent(null);
        doReturn(Collections.singletonList(null)).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));

        // Initialise the advertiser and perform the component call
        this.vdes1000Advertiser.station = this.station;
        this.vdes1000Advertiser.signatureAlgorithm = "algorithm";
        this.vdes1000Advertiser.signatureDestMmmsi = 123456789;

        // Perform the component call
        this.vdes1000Advertiser.advertiseAtons();

        // Make sure no UDP packet was sent to the GRURadio station
        verify(this.vdes1000Conn, never()).sendMessage(any(), eq(this.station.getChannel()));
    }

    /**
     * Test that the VDES-1000 connection monitoring handler will pick up
     * the received messages and attempt to publish them in the publish
     * subscribe channel.
     */
    @Test
    void testHandleMessage() throws IOException {
        // Initialise the advertiser
        doReturn(this.vdes1000Conn).when(vdes1000Advertiser).getVdes1000Conn();
        this.vdes1000Advertiser.init(this.station);

        // Spy on the publish/subscribe channel to pick up the published message
        ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);

        // Perform the component call
        this.vdes1000Advertiser.handleMessage("This is a test message");

        // Verify that a correct message was published
        verify(this.publishSubscribeChannel, times(1)).send(messageArgument.capture());
        assertEquals(this.station.getType(), messageArgument.getValue().getHeaders().get(MessageHeaders.CONTENT_TYPE));
        assertEquals(this.station.getIpAddress(), messageArgument.getValue().getHeaders().get(PubSubMsgHeaders.ADDRESS.getHeader()));
        assertEquals(this.station.getBroadcastPort(), messageArgument.getValue().getHeaders().get(PubSubMsgHeaders.PORT.getHeader()));
        assertEquals(this.station.getMmsi(), messageArgument.getValue().getHeaders().get(PubSubMsgHeaders.MMSI.getHeader()));
        assertEquals("This is a test message", messageArgument.getValue().getPayload());

        // Verify that no forwarding took place
        verify(this.fwdSocket, never()).send(any());
    }

    /**
     * Test that the VDES-1000 connection monitoring handler will pick up
     * the received messages and attempt to publish them in the publish
     * subscribe channel, as well as forward them in the forward port if that
     * has been defined.
     */
    @Test
    void testHandleMessageWithForward() throws IOException {
        // Turn on the forwarding
        this.station.setFwdIpAddress("10.0.0.2");
        this.station.setFwdPort(8003);

        // Initialise the advertiser
        doReturn(this.vdes1000Conn).when(vdes1000Advertiser).getVdes1000Conn();
        doReturn(this.fwdSocket).when(vdes1000Advertiser).getFwdSocket();
        this.vdes1000Advertiser.init(this.station);

        // Spy on the publish/subscribe channel to pick up the published message
        ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);

        // Spy on the forward
        ArgumentCaptor<DatagramPacket> datagramPacketArgument = ArgumentCaptor.forClass(DatagramPacket.class);

        // Perform the component call
        this.vdes1000Advertiser.handleMessage("This is a test message");

        // Verify that a correct message was published
        verify(this.publishSubscribeChannel, times(1)).send(messageArgument.capture());
        assertEquals(this.station.getType(), messageArgument.getValue().getHeaders().get(MessageHeaders.CONTENT_TYPE));
        assertEquals(this.station.getIpAddress(), messageArgument.getValue().getHeaders().get(PubSubMsgHeaders.ADDRESS.getHeader()));
        assertEquals(this.station.getBroadcastPort(), messageArgument.getValue().getHeaders().get(PubSubMsgHeaders.PORT.getHeader()));
        assertEquals(this.station.getMmsi(), messageArgument.getValue().getHeaders().get(PubSubMsgHeaders.MMSI.getHeader()));
        assertEquals("This is a test message", messageArgument.getValue().getPayload());

        // Verify that the correcy datagram packet was sent
        verify(this.fwdSocket, times(1)).send(datagramPacketArgument.capture());
        assertEquals(this.station.getFwdIpAddress(), datagramPacketArgument.getValue().getAddress().getHostAddress());
        assertEquals(this.station.getFwdPort(), datagramPacketArgument.getValue().getPort());
        assertEquals("This is a test message", new String(datagramPacketArgument.getValue().getData()));
    }

}