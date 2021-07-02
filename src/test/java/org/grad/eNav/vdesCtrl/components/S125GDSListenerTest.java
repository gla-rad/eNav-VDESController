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

package org.grad.eNav.vdesCtrl.components;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQLException;
import org.grad.eNav.vdesCtrl.models.GeomesaS125;
import org.grad.eNav.vdesCtrl.models.domain.NMEAChannel;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.GeoJSONUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.geomesa.kafka.utils.KafkaFeatureEvent;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S125GDSListenerTest {

    /**
     * The Tested Component.
     */
    @InjectMocks
    @Spy
    S125GDSListener s125GDSListener;

    /**
     * The AtoN Data Channel mock.
     */
    @Mock
    PublishSubscribeChannel atonPublishChannel;

    /**
     * The Station Service mock.
     */
    @Mock
    StationService stationService;

    /**
     * The Station Node Service mock.
     */
    @Mock
    SNodeService sNodeService;

    // Test Variables
    private Station station;
    private S125Node s125Node;

    // Geomesa Variables
    private GeometryFactory geometryFactory;
    private GeomesaS125 geomesaData;
    private SimpleFeatureSource featureSource;
    private DataStore consumer;
    private Set<FeatureListener> featureListeners;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() throws IOException, CQLException {
        // Create a temp geometry factory to get a test geometries
        this.geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Create a station with an ID
        this.station = new Station();
        this.station.setId(BigInteger.ONE);
        this.station.setName("Existing Station Name");
        this.station.setChannel(NMEAChannel.B);
        this.station.setMmsi("222222222");
        this.station.setIpAddress("10.0.0.2");
        this.station.setPiSeqNo(2L);
        this.station.setType(StationType.GNU_RADIO);
        this.station.setPort(8002);
        this.station.setGeometry(geometryFactory.createPolygon(new Coordinate[] {
                new Coordinate(-180, -90),
                new Coordinate(-180, 90),
                new Coordinate(180, 90),
                new Coordinate(180, -90),
                new Coordinate(-180, -90)
        }));
        this.station.setNodes(new HashSet<>());

        // Read a valid S125 content to generate the S125Node message for.
        InputStream in = new ClassPathResource("s125-msg.xml").getInputStream();
        String xml = IOUtils.toString(in, StandardCharsets.UTF_8.name());

        // Also create a GeoJSON point geometry for our S125 message
        JsonNode point = GeoJSONUtils.createGeoJSONPoint(53.61, 1.594);

        // Now create the S125 node object
        this.s125Node = new S125Node("test_aton", point, xml);
        this.geomesaData = new GeomesaS125(this.station.getGeometry());

        // Also mock the GeoMesa DataStore data and consumer
        this.featureSource = mock(SimpleFeatureSource.class);
        this.consumer = mock(DataStore.class);

        // Specify the mock behaviour
        doReturn(this.featureSource).when(this.consumer).getFeatureSource("S125");

        // Keep a local record of all added feature listeners
        featureListeners = new HashSet<>();
        doAnswer((inv) -> {
            this.featureListeners.add(inv.getArgument(0));
            return null;
        }).when(this.featureSource).addFeatureListener(any(FeatureListener.class));
    }

    /**
     * Test that the S125 Geomesa DataStore Listener can initialise correctly.
     */
    @Test
    void testInit() throws IOException {
        // Init the component
        this.s125GDSListener.init(this.consumer, this.geomesaData, this.station);

        // Make sure the initialisation was successful
        assertEquals(this.s125GDSListener.consumer, this.consumer);
        assertEquals(this.s125GDSListener.geomesaData, this.geomesaData);
        assertEquals(this.s125GDSListener.station, this.station);
        assertTrue(this.featureListeners.size() == 1);
    }

    /**
     * Test that when the S125 Geomesa DataStore Listener gets destroyed, then
     * the feature listeners are cleared from the object's feature source
     */
    @Test
    void testDestroy() throws IOException {
       // Don't forget the removal of the listener from the local list
        doAnswer((inv) -> {
            this.featureListeners.remove(inv.getArgument(0));
            return null;
        }).when(this.featureSource).removeFeatureListener(any(FeatureListener.class));

        // Init and perform the component call
        this.s125GDSListener.init(this.consumer, this.geomesaData, this.station);
        this.s125GDSListener.destroy();

        // Assert that the feature listeners list is empty
        assertTrue(this.featureListeners.size() == 0);
    }

    /**
     * Test that the S125 Geomesa Listener can correctly handle the incoming
     * S125 Geomesa change events, and if so, it will save the S125 message
     * in the database and send it to the AtoN publish-subscribe channel
     */
    @Test
    void testListenToEventsChangedSendAndSaved() throws IOException {
        // Translate our S125Node to a feature list
        List<SimpleFeature> simpleFeatureList = this.geomesaData.getFeatureData(Collections.singletonList(this.s125Node));

        // Mock a new event
        KafkaFeatureEvent.KafkaFeatureChanged featureEvent = mock(KafkaFeatureEvent.KafkaFeatureChanged.class);
        doReturn(FeatureEvent.Type.CHANGED).when(featureEvent).getType();
        doReturn(simpleFeatureList.stream().findFirst().orElse(null)).when(featureEvent).feature();

        // Also mock the saving operation
        doReturn(this.station).when(this.stationService).findOne(this.station.getId());

        // Init and perform the component call
        this.s125GDSListener.init(this.consumer, this.geomesaData, this.station);
        this.s125GDSListener.listenToEvents(featureEvent);

        // Verify that our message was saved and sent
        verify(this.stationService, times(1)).save(any(Station.class));
        verify(atonPublishChannel, times(1)).send(any(Message.class));
    }

    /**
     * Test that the S125 Geomesa Listener can correctly handle the incoming
     * S125 Geomesa change events, but it will not act on them if the fall
     * outside the station's coverage area.
     */
    @Test
    void testListenToEventsChangedOutsideStationArea() throws IOException {
        // Change the stations coverage area
        this.station.setGeometry(geometryFactory.createPolygon(new Coordinate[] {
                new Coordinate(-0, -0),
                new Coordinate(-0, 0),
                new Coordinate(0, 0),
        }));
        this.geomesaData = new GeomesaS125(this.station.getGeometry());

        // Translate our S125Node to a feature list
        List<SimpleFeature> simpleFeatureList = this.geomesaData.getFeatureData(Collections.singletonList(this.s125Node));

        // Mock a new event
        KafkaFeatureEvent.KafkaFeatureChanged featureEvent = mock(KafkaFeatureEvent.KafkaFeatureChanged.class);
        doReturn(FeatureEvent.Type.CHANGED).when(featureEvent).getType();
        doReturn(simpleFeatureList.stream().findFirst().orElse(null)).when(featureEvent).feature();

        // Init and perform the component call
        this.s125GDSListener.init(this.consumer, this.geomesaData, this.station);
        this.s125GDSListener.listenToEvents(featureEvent);

        // Verify that our message was not saved or sent
        verify(this.stationService, never()).save(any(Station.class));
        verify(atonPublishChannel, never()).send(any(Message.class));
    }

    /**
     * Currently the removed Geomesa feature event doesn't do much so a simple
     * test just to make sure it doesn't break.
     */
    @Test
    void testListenToEventsRemovedDoesNothing() throws IOException {
        // Translate our S125Node to a feature list
        List<SimpleFeature> simpleFeatureList = this.geomesaData.getFeatureData(Collections.singletonList(this.s125Node));

        // Mock a new event
        KafkaFeatureEvent.KafkaFeatureRemoved featureEvent = mock(KafkaFeatureEvent.KafkaFeatureRemoved.class);
        doReturn(FeatureEvent.Type.REMOVED).when(featureEvent).getType();
        doReturn(simpleFeatureList.stream().findFirst().orElse(null)).when(featureEvent).feature();

        // Create a Geomesa data spy to view the calls
        GeomesaS125 geomesaDataSpy = spy(this.geomesaData);

        // Init and perform the component call
        this.s125GDSListener.init(this.consumer, geomesaDataSpy, this.station);
        this.s125GDSListener.listenToEvents(featureEvent);

        // Make sure the evaluation works
        verify(geomesaDataSpy, times(1)).getSubsetFilter();
    }

}