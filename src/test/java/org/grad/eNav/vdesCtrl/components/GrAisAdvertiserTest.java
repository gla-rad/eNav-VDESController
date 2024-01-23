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

package org.grad.eNav.vdesCtrl.components;

import com.fasterxml.jackson.databind.JsonNode;
import org.grad.eNav.vdesCtrl.feign.CKeeperClient;
import org.grad.eNav.vdesCtrl.models.domain.McpEntityType;
import org.grad.eNav.vdesCtrl.models.domain.SignatureMode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.models.dtos.AtonMessageDto;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.GeoJSONUtils;
import org.grad.vdes1000.formats.generic.AISChannelPref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrAisAdvertiserTest {

    /**
     * The Tested Component.
     */
    @InjectMocks
    @Spy
    GrAisAdvertiser grAisAdvertiser;

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
    private DatagramSocket gnuRadioSocket;

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
        this.station.setType(StationType.GNU_RADIO);
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

        // Also mock a UDP socket that does nothing, to be used in the tests
        this.gnuRadioSocket = mock(DatagramSocket.class);
    }

    /**
     * Test that the GNURadio AIS advertiser can initialise correctly.
     */
    @Test
    void testInit() throws SocketException {
        // Perform the component call
        this.grAisAdvertiser.init(this.station);

        assertEquals(this.station, this.grAisAdvertiser.station);
        assertNotNull(this.grAisAdvertiser.gnuRadioSocket);
    }

    /**
     * Test that the GNURadio AIS advertiser can be destroyed gracefully and
     * will close its UDP connection to the GNURadio device.
     */
    @Test
    void testDestroy() {
        // Initialise the advertiser
        this.grAisAdvertiser.gnuRadioSocket = this.gnuRadioSocket;

        // Perform the service class
        this.grAisAdvertiser.destroy();

        // Assert that the UDP socket was closed
        verify(this.gnuRadioSocket, times(1)).close();
    }

    /**
     * Test that the GNURadio AIS advertiser can actually read the station
     * messages from the message service and advertise the connected GNURadio
     * station.
     */
    @Test
    void testAdvertiseAtons() throws IOException {
        doReturn(Collections.singletonList(this.atonMessageDto)).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));

        // Initialise the advertiser and perform the component call
        this.grAisAdvertiser.station = this.station;
        this.grAisAdvertiser.gnuRadioSocket = this.gnuRadioSocket;
        this.grAisAdvertiser.aisInterval = 1000L;
        this.grAisAdvertiser.signatureAlgorithm = "algorithm";
        this.grAisAdvertiser.signatureDestMmmsi = 123456789;
        this.grAisAdvertiser.advertiseAtons();

        // Make sure the UDP packet was sent to the GRURadio station
        verify(this.gnuRadioSocket, times(1)).send(any());
    }

    /**
     * Test that the  GNURadio AIS advertiser can actually read the station
     * messages from the message service but will not advertise the ones that
     * have been blacklisted.
     */
    @Test
    void testAdvertiseAtonsBlacklisted() throws IOException {
        doReturn(Collections.emptyList()).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));

        // Initialise the advertiser and perform the component call
        this.grAisAdvertiser.station = this.station;
        this.grAisAdvertiser.gnuRadioSocket = this.gnuRadioSocket;
        this.grAisAdvertiser.aisInterval = 1000L;
        this.grAisAdvertiser.signatureAlgorithm = "algorithm";
        this.grAisAdvertiser.signatureDestMmmsi = 123456789;
        this.grAisAdvertiser.advertiseAtons();

        // Make sure the UDP packet was sent to the GRURadio station
        verify(this.gnuRadioSocket, never()).send(any());
    }

    /**
     * Test that the GNURadio AIS advertiser can actually read the station
     * messages from the message service and advertise the connected GNURadio
     * station. It will also send a second message over AIS containing the
     * signature of the first, if that feature is enabled.
     */
    @Test
    void testAdvertiseAtonsWithSignatureAIS() throws IOException {
        // Enable AIS signatures for this station
        this.station.setSignatureMode(SignatureMode.AIS);

        doReturn(Collections.singletonList(this.atonMessageDto)).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));
        doReturn(this.signature).when(this.cKeeperClient).generateEntitySignature(any(String.class), any(String.class), any(String.class), eq(McpEntityType.DEVICE.getValue()), any(byte[].class));

        // Initialise the advertiser and perform the component call
        this.grAisAdvertiser.station = this.station;
        this.grAisAdvertiser.gnuRadioSocket = this.gnuRadioSocket;
        this.grAisAdvertiser.aisInterval = 1000L;
        this.grAisAdvertiser.signatureAlgorithm = "algorithm";
        this.grAisAdvertiser.signatureDestMmmsi = 123456789;
        this.grAisAdvertiser.advertiseAtons();

        // Make sure the UDP packet was sent to the GRURadio station
        verify(this.gnuRadioSocket, times(2)).send(any());
    }

    /**
     * Test that the GNURadio AIS advertiser can actually read the station
     * messages from the message service and try to advertise the connected
     * GNURadio station. If however a VDE signature is selected, an error
     * will be thrown and no signature will be transmitted since this station
     * type only supported AIS signatures.
     */
    @Test
    void testAdvertiseAtonsWithSignatureVDE() throws IOException {
        // Enable VDE signature for this station
        this.station.setSignatureMode(SignatureMode.VDE);

        doReturn(Collections.singletonList(this.atonMessageDto)).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));
        doReturn(this.signature).when(this.cKeeperClient).generateEntitySignature(any(String.class), any(String.class), any(String.class), eq(McpEntityType.DEVICE.getValue()), any(byte[].class));

        // Initialise the advertiser and perform the component call
        this.grAisAdvertiser.station = this.station;
        this.grAisAdvertiser.gnuRadioSocket = this.gnuRadioSocket;
        this.grAisAdvertiser.aisInterval = 1000L;
        this.grAisAdvertiser.signatureAlgorithm = "algorithm";
        this.grAisAdvertiser.signatureDestMmmsi = 123456789;
        this.grAisAdvertiser.advertiseAtons();

        // Make sure the UDP packet was sent to the GRURadio station
        verify(this.gnuRadioSocket, times(1)).send(any());
    }

    /**
     * Test that the GNURadio AIS advertiser will not actually send anything
     * if an empty/null S125 message is received
     */
    @Test
    void testAdvertiseAtonsEmptyMessage() throws IOException {
        // Enable signature for this station
        this.station.setSignatureMode(SignatureMode.AIS);

        this.atonMessageDto.setContent(null);
        doReturn(Collections.singletonList(null)).when(this.stationService).findMessagesForStation(eq(this.station.getId()), eq(Boolean.FALSE));

        // Initialise the advertiser and perform the component call
        this.grAisAdvertiser.station = this.station;
        this.grAisAdvertiser.gnuRadioSocket = this.gnuRadioSocket;
        this.grAisAdvertiser.aisInterval = 1000L;
        this.grAisAdvertiser.signatureAlgorithm = "algorithm";
        this.grAisAdvertiser.signatureDestMmmsi = 123456789;

        // Perform the component call
        this.grAisAdvertiser.advertiseAtons();

        // Make sure no UDP packet was sent to the GRURadio station
        verify(this.gnuRadioSocket, never()).send(any());
    }

}