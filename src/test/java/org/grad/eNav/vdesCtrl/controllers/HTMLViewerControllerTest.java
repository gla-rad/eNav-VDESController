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

package org.grad.eNav.vdesCtrl.controllers;

import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.vdes1000.generic.AISChannelPref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = HTMLViewerController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class HTMLViewerControllerTest {

    /**
     * The Mock MVC.
     */
    @Autowired
    MockMvc mockMvc;

    /**
     * The Station Service mock.
     */
    @MockBean
    StationService stationService;

    // Test Variables
    private List<Station> stations;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Initialise the stations list
        this.stations = new ArrayList<>();
        for (long i = 0; i < 10; i++) {
            Station station = new Station();
            station.setId(BigInteger.valueOf(i));
            station.setName("Station Name");
            station.setChannel(AISChannelPref.A);
            station.setMmsi("12345678" + i);
            station.setIpAddress("10.0.0." + i);
            station.setType(StationType.VDES_1000);
            station.setPort(8000 + (int) i);
            station.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
            this.stations.add(station);
        }
    }

    /**
     * Test that we can access the main index HTML page.
     */
    @Test
    void testGetIndex() throws Exception {
        doReturn(this.stations).when(stationService).findAll();

        // Perform the MVC request
        this.mockMvc.perform(get("/index")
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

    /**
     * Test that we can access the about HTML page.
     */
    @Test
    void testGetAbout() throws Exception {
        // Perform the MVC request
        this.mockMvc.perform(get("/about")
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

    /**
     * Test that we can access the refresh session HTML page.
     */
    @Test
    void testGetRefresh() throws Exception {
        // Perform the MVC request
        this.mockMvc.perform(get("/refresh")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }

}