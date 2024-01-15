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

import org.grad.eNav.vdesCtrl.components.GrAisAdvertiser;
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrAisServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    GrAisService grAisService;

    /**
     * The Application Context mock.
     */
    @Mock
    ApplicationContext applicationContext;

    /**
     * The Station Service mock.
     */
    @Mock
    StationService stationService;

    // Test Variables
    private List<Station> stations;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() throws IOException {
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
            station.setType(StationType.GNU_RADIO);
            station.setPort(8000 + (int)i);
            station.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
            this.stations.add(station);
        }
    }

    /**
     * Test that the GNURadio AIS service can initialise correctly and register
     * the GNURadio AIS advertisers for each of the stations present in the
     * database.
     */
    @Test
    void testInit() {
        doReturn(this.stations).when(this.stationService).findAllByType(StationType.GNU_RADIO);

        // Create a mock Datastore Listener to be returned by the listener initialisation
        GrAisAdvertiser mockAdvertiser = mock(GrAisAdvertiser.class);
        doReturn(mockAdvertiser).when(this.applicationContext).getBean(GrAisAdvertiser.class);

        // Perform the service call
        this.grAisService.init();

        // Assert all the GNURadio Advertisers were created as expected
        List<Station> advertisers = this.stations.stream()
                .filter(s -> s.getType() == StationType.GNU_RADIO)
                .collect(Collectors.toList());
        assertEquals(stations.size(), this.grAisService.grAisAdvertisers.size());
    }

    /**
     * Test that the SGNURadio AIS service can be destroyed gracefully, and it
     * destroys all the running GNURadio AIS advertisers.
     */
    @Test
    void testDestroy() {
        doReturn(this.stations).when(this.stationService).findAllByType(StationType.GNU_RADIO);

        // Create a mock Datastore Listener to be returned by the listener initialisation
        GrAisAdvertiser mockAdvertiser = mock(GrAisAdvertiser.class);
        doReturn(mockAdvertiser).when(this.applicationContext).getBean(GrAisAdvertiser.class);

        // First initialise the service to pick up the advertisers
        this.grAisService.init();

        // Perform the service call
        this.grAisService.destroy();

        // Make sure the advertisers got destroyed
        verify(mockAdvertiser, times(this.stations.size())).destroy();
    }

    /**
     * Test that we can call the service to reload all the advertisers.
     */
    @Test
    void testReload() {
        //Perform the service call
        this.grAisService.reload();

        // Make sure we first destroy and re-init the service
        verify(this.grAisService, times(1)).destroy();
        verify(this.grAisService, times(1)).init();
    }

    /**
     * Test that we can call all advertisers to publish their advertisements.
     */
    @Test
    void testAdvertise() {
        doReturn(this.stations).when(this.stationService).findAllByType(StationType.GNU_RADIO);

        // Create a mock Datastore Listener to be returned by the listener initialisation
        GrAisAdvertiser mockAdvertiser = mock(GrAisAdvertiser.class);
        doReturn(mockAdvertiser).when(this.applicationContext).getBean(GrAisAdvertiser.class);

        // First initialise the service to pick up the advertisers
        this.grAisService.init();

        // Perform the service call
        this.grAisService.advertiseAtons();

        // Make sure all advertisers were called
        verify(mockAdvertiser, times(this.stations.size())).advertiseAtons();
    }

    /**
     * Test that during the reloading operation, the advertisers will be
     * temporarily disabled so that we don't get any unwanted behaviour.
     */
    @Test
    void testAdvertiseWhileReloading() {
        doReturn(this.stations).when(this.stationService).findAllByType(StationType.GNU_RADIO);

        // Create a mock Datastore Listener to be returned by the listener initialisation
        GrAisAdvertiser mockAdvertiser = mock(GrAisAdvertiser.class);
        doReturn(mockAdvertiser).when(this.applicationContext).getBean(GrAisAdvertiser.class);

        // First initialise the service to pick up the advertisers
        this.grAisService.init();

        // Mock a reloading operation
        this.grAisService.reloading = true;

        // Perform the service call
        this.grAisService.advertiseAtons();

        // Make sure all advertisers were NOT called
        verify(mockAdvertiser, never()).advertiseAtons();
    }

}