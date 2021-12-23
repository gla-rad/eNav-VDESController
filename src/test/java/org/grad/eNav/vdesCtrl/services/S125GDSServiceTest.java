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
 */

package org.grad.eNav.vdesCtrl.services;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.grad.eNav.vdesCtrl.components.S125GDSListener;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.vdes1000.generic.AISChannelPref;
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
import org.springframework.context.ApplicationContext;
import org.springframework.integration.channel.PublishSubscribeChannel;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S125GDSServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    S125GDSService s125GDSService;

    /**
     * The Application Context mock.
     */
    @Mock
    ApplicationContext applicationContext;

    /**
     * The AtoN Publish Channel mock.
     */
    @Mock
    PublishSubscribeChannel atonPublishChannel;

    /**
     * The Station Service mock.
     */
    @Mock
    StationService stationService;

    /**
     * The Geomesa Data Store mock.
     */
    @Mock
    DataStore consumer;

    // Test Variables
    private List<Station> stations;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Initialise the stations list
        this.stations = new ArrayList<>();
        for(long i=0; i<10; i++) {
            Station station = new Station();
            station.setId(BigInteger.valueOf(i));
            station.setName("Station Name");
            station.setChannel(AISChannelPref.A);
            station.setMmsi("12345678" + i);
            station.setIpAddress("10.0.0." + i);
            station.setType(StationType.VDES_1000);
            station.setPort(8000 + (int)i);
            station.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
            station.setNodes(new HashSet<>());
            this.stations.add(station);
        }
    }

    /**
     * Test that the S125 Geomesa Datastore service can initialise correctly
     * and register a datastore listener for each of the stations present in
     * the database.
     */
    @Test
    void testInit() throws IOException {
        doReturn(this.stations).when(this.stationService).findAll();

        // Create a Datastore Listener to be returned by the listener initialisation
        doAnswer((invocation) -> new S125GDSListener()).when(this.applicationContext).getBean(S125GDSListener.class);
        SimpleFeatureSource featureSource = mock(SimpleFeatureSource.class);
        doReturn(featureSource).when(this.consumer).getFeatureSource(any(String.class));

        // Perform the service call
        this.s125GDSService.init();

        // Assert all the datastore listeners were created as expected
        assertEquals(this.stations.size(), this.s125GDSService.dsListeners.size());

        // Assert that only the one listener is set as a deletion handler
        assertTrue(this.s125GDSService.dsListeners
                .stream()
                .filter(S125GDSListener::isDeletionHandler).
                collect(Collectors.toList())
                .size() == 1);
    }

    /**
     * Test that the S125 Geomesa Datastore service will not initialise if a
     * valid Geomesa Datastore does NOT exist.
     */
    @Test
    void testInitNoDatastore() {
        // Cancel the consumer datastore
        this.s125GDSService.consumer = null;

        // Perform the service call
        this.s125GDSService.init();

        // Assert no listener were generated
        assertNull(this.s125GDSService.dsListeners);
    }

    /**
     * Test that the S125 Geomesa Datastore service can be destroyed gracefully,
     * and it disconnects from the connected Geomesa datastore.
     */
    @Test
    void testDestroy() {
        doReturn(this.stations).when(this.stationService).findAll();

        // Create a mock Datastore Listener to be returned by the listener initialisation
        S125GDSListener mockListener = mock(S125GDSListener.class);
        doReturn(mockListener).when(this.applicationContext).getBean(S125GDSListener.class);

        // First initialise the service to pick up the listeners
        this.s125GDSService.init();

        // Perform the service call
        this.s125GDSService.destroy();

        // Make sure the listeners and the Geomesa datastore gets disconnected
        verify(mockListener, times(this.stations.size())).destroy();
        verify(this.consumer, times(1)).dispose();
    }

    /**
     * Test that the S125 Geomesa Datastore service can be destroyed gracefully,
     * but if this happens during a reloading operation, the Geomesa DataStore
     * consumer will NOT be dropped.
     */
    @Test
    void testDestroyWhileReloading() {
        doReturn(this.stations).when(this.stationService).findAll();

        // Create a mock Datastore Listener to be returned by the listener initialisation
        S125GDSListener mockListener = mock(S125GDSListener.class);
        doReturn(mockListener).when(this.applicationContext).getBean(S125GDSListener.class);

        // First initialise the service to pick up the listeners
        this.s125GDSService.init();

        // Mock a reloading operation
        this.s125GDSService.reloading = true;

        // Perform the service call
        this.s125GDSService.destroy();

        // Make sure the listeners and the Geomesa datastore gets disconnected
        verify(mockListener, times(this.stations.size())).destroy();
        verify(this.consumer, never()).dispose();
    }

    /**
     * Test that the S125 Geomesa Datastore service can reload by calling the
     * initialisation procedure on demand.
     */
    @Test
    void testReload() {
        doReturn(this.stations).when(this.stationService).findAll();

        // Create a mock Datastore Listener to be returned by the listener initialisation
        S125GDSListener mockListener = mock(S125GDSListener.class);
        doReturn(mockListener).when(this.applicationContext).getBean(S125GDSListener.class);

        // Perform the service call
        this.s125GDSService.reload();

        // Assert all the datastore listeners were created as expected
        assertEquals(this.stations.size(), this.s125GDSService.dsListeners.size());
    }

}