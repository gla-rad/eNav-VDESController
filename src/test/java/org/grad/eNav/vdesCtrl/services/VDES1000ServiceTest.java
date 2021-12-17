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

import org.grad.eNav.vdesCtrl.components.Vdes1000Advertiser;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.vdes1000.generic.AISChannel;
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
class VDES1000ServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    VDES1000Service vdes1000Service;

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
            station.setChannel(AISChannel.A);
            station.setMmsi("12345678" + i);
            station.setIpAddress("10.0.0." + i);
            station.setPiSeqNo(i);
            station.setType(StationType.VDES_1000);
            station.setPort(8000 + (int)i);
            station.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
            station.setNodes(new HashSet<>());
            this.stations.add(station);
        }
    }

    /**
     * Test that the VDES-1000 service can initialise correctly and register
     * the VDES-1000 advertisers for each of the stations present in the
     * database.
     */
    @Test
    void testInit() {
        doReturn(this.stations).when(this.stationService).findAllByType(StationType.VDES_1000);

        // Create a mock Datastore Listener to be returned by the listener initialisation
        Vdes1000Advertiser mockAdvertiser = mock(Vdes1000Advertiser.class);
        doReturn(mockAdvertiser).when(this.applicationContext).getBean(Vdes1000Advertiser.class);

        // Perform the service call
        this.vdes1000Service.init();

        // Assert all the GNURadio Advertisers were created as expected
        List<Station> advertisers = this.stations.stream()
                .filter(s -> s.getType() == StationType.VDES_1000)
                .collect(Collectors.toList());
        assertEquals(stations.size(), this.vdes1000Service.vdes1000Advertisers.size());
    }

    /**
     * Test that the VDES-1000 service can be destroyed gracefully, and it
     * destroys all the running GNURadio AIS advertisers.
     */
    @Test
    void testDestroy() {
        doReturn(this.stations).when(this.stationService).findAllByType(StationType.VDES_1000);

        // Create a mock Datastore Listener to be returned by the listener initialisation
        Vdes1000Advertiser mockAdvertiser = mock(Vdes1000Advertiser.class);
        doReturn(mockAdvertiser).when(this.applicationContext).getBean(Vdes1000Advertiser.class);

        // First initialise the service to pick up the advertisers
        this.vdes1000Service.init();

        // Perform the service call
        this.vdes1000Service.destroy();

        // Make sure the advertisers got destroyed
        verify(mockAdvertiser, times(this.stations.size())).destroy();
    }

}