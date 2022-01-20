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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grad.eNav.vdesCtrl.exceptions.DataNotFoundException;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.models.dtos.S100AbstractNode;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.*;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.GeometryJSONConverter;
import org.grad.vdes1000.generic.AISChannelPref;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = StationController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class StationControllerTest {

    /**
     * The Mock MVC.
     */
    @Autowired
    MockMvc mockMvc;

    /**
     * The JSON Object Mapper.
     */
    @Autowired
    ObjectMapper objectMapper;

    /**
     * The Station Service mock.
     */
    @MockBean
    StationService stationService;

    // Test Variables
    private List<Station> stations;
    private List<S100AbstractNode> messages;
    private Pageable pageable;
    private Station newStation;
    private Station existingStation;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Initialise the station messages list
        this.messages = new ArrayList<>();
        for(long i=0; i<2; i++) {
            S125Node message = new S125Node();
            message.setAtonUID("UID" + i);
            message.setBbox(GeometryJSONConverter.convertFromGeometry(factory.createPoint(new Coordinate(1.594 + i, 53.6 + i))));
            message.setContent("Node Message");
            this.messages.add(message);
        }

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
            this.stations.add(station);
        }

        // Create a pageable definition
        this.pageable = PageRequest.of(0, 5);

        // Create a new instance
        this.newStation = new Station();
        this.newStation.setName("New Station Name");
        this.newStation.setChannel(AISChannelPref.A);
        this.newStation.setMmsi("111111111");
        this.newStation.setIpAddress("10.0.0.1");
        this.newStation.setType(StationType.VDES_1000);
        this.newStation.setPort(8001);
        this.newStation.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));

        // Create an instance with an ID
        this.existingStation = new Station();
        this.existingStation.setId(BigInteger.ONE);
        this.existingStation.setName("Existing Station Name");
        this.existingStation.setChannel(AISChannelPref.B);
        this.existingStation.setMmsi("222222222");
        this.existingStation.setIpAddress("10.0.0.2");
        this.existingStation.setType(StationType.GNU_RADIO);
        this.existingStation.setPort(8002);
        this.existingStation.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
    }

    /**
     * Test that we can retrieve all the stations currently in the database in
     * a paged result.
     */
    @Test
    void testGetAllStations() throws Exception {
        // Created a result page to be returned by the mocked service
        Page<Station> page = new PageImpl<>(this.stations.subList(0, 5), this.pageable, this.stations.size());
        doReturn(page).when(this.stationService).findAll(any());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        // Parse and validate the response
        Station[] result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Station[].class);
        assertEquals(5, Arrays.asList(result).size());
    }

    /**
     * Test that the API supports the jQuery Datatables server-side paging
     * and search requests.
     */
    @Test
    void testGetStationsForDatatables() throws Exception {
        // Create a test datatables paging request
        DtColumn dtColumn = new DtColumn("id");
        dtColumn.setName("ID");
        dtColumn.setOrderable(true);
        DtOrder dtOrder = new DtOrder();
        dtOrder.setColumn(0);
        dtOrder.setDir(DtDirection.asc);
        DtPagingRequest dtPagingRequest = new DtPagingRequest();
        dtPagingRequest.setStart(0);
        dtPagingRequest.setLength(this.stations.size());
        dtPagingRequest.setDraw(1);
        dtPagingRequest.setSearch(new DtSearch());
        dtPagingRequest.setOrder(Collections.singletonList(dtOrder));
        dtPagingRequest.setColumns(Collections.singletonList(dtColumn));

        // Create a mocked datatables paging response
        DtPage<Station> dtPage = new DtPage<>();
        dtPage.setData(this.stations);
        dtPage.setDraw(1);
        dtPage.setRecordsFiltered(this.stations.size());
        dtPage.setRecordsTotal(this.stations.size());

        // Mock the service call for creating a new instance
        doReturn(dtPage).when(this.stationService).handleDatatablesPagingRequest(any());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(post("/api/stations/dt")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(this.objectMapper.writeValueAsString(dtPagingRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Parse and validate the response
        DtPage<Station> result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DtPage.class);
        assertEquals(this.stations.size(), result.getData().size());
    }

    /**
     * Test that we can correctly retrieve a single station based on the
     * provided entry ID.
     */
    @Test
    void testGetStation() throws Exception {
        doReturn(this.existingStation).when(this.stationService).findOne(this.existingStation.getId());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(get("/api/stations/{id}", this.existingStation.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        // Parse and validate the response
        Station result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Station.class);
        assertEquals(this.existingStation, result);
    }

    /**
     * Test that if we do NOT find the station we are looking for, an HTTP
     * NOT_FOUND response will be returned.
     */
    @Test
    void testGetStationNotFound() throws Exception {
        Long id = 0L;
        doThrow(DataNotFoundException.class).when(this.stationService).findOne(any());

        // Perform the MVC request
        this.mockMvc.perform(get("/api/stations/{id}", id))
                .andExpect(status().isNotFound());
    }

    /**
     * Test that we can create a new station correctly through a POST request.
     * The incoming station should NOT have an ID, while the returned
     * value will have the ID field populated.
     */
    @Test
    void testCreateStation() throws Exception {
        // Mock the service call for creating a new instance
        doReturn(this.existingStation).when(this.stationService).save(any());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(post("/api/stations")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(this.objectMapper.writeValueAsString(this.newStation)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        // Parse and validate the response
        Station result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Station.class);
        assertEquals(this.existingStation, result);
    }

    /**
     * Test that if we try to create a station with an existing ID field,
     * an HTTP BAD_REQUEST response will be returns, with a description of
     * the error in the header.
     */
    @Test
    void testCreateStationWithId() throws Exception {
        // Perform the MVC request
        this.mockMvc.perform(post("/api/stations")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(this.objectMapper.writeValueAsString(this.existingStation)))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-vdesCtrl-error"))
                .andExpect(header().exists("X-vdesCtrl-params"))
                .andReturn();
    }

    /**
     * Test that we can update an existing station correctly through a PUT
     * request. The incoming station should always have an ID.
     */
    @Test
    void testUpdateStation() throws Exception {
        // Mock the service call for updating an existing instance
        doReturn(this.existingStation).when(this.stationService).save(any());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(put("/api/stations/{id}", this.existingStation.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(this.objectMapper.writeValueAsString(this.existingStation)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        // Parse and validate the response
        Station result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Station.class);
        assertEquals(this.existingStation, result);
    }

    /**
     * Test that if we fail to update the provided station due to a general
     * error, an HTTP BAD_REQUEST response will be returned, with a description
     * of the error in the header.
     */
    @Test
    void testUpdateStationFailure() throws Exception {
        // Mock a general Exception when saving the instance
        doThrow(RuntimeException.class).when(this.stationService).save(any());

        // Perform the MVC request
        this.mockMvc.perform(put("/api/stations/{id}", this.existingStation.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(this.objectMapper.writeValueAsString(this.existingStation)))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-vdesCtrl-error"))
                .andExpect(header().exists("X-vdesCtrl-params"))
                .andReturn();
    }

    /**
     * Test that we can correctly delete an existing station by using a valid
     * ID.
     */
    @Test
    void testDeleteStation() throws Exception {
        // Perform the MVC request
        this.mockMvc.perform(delete("/api/stations/{id}", this.existingStation.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * Test that if we do NOT find the station we are trying to delete, an HTTP
     * NOT_FOUND response will be returned.
     */
    @Test
    void testDeleteStationNotFound() throws Exception {
        doThrow(DataNotFoundException.class).when(this.stationService).delete(any());

        // Perform the MVC request
        this.mockMvc.perform(delete("/api/stations/{id}", this.existingStation.getId()))
                .andExpect(status().isNotFound());
    }

    /**
     * Test that we can retrieve all the AtoN messages linked to a specific
     * station.
     */
    @Test
    void testGetStationMessages() throws Exception {
        // Created a result page to be returned by the mocked service
        doReturn(this.messages).when(this.stationService).findMessagesForStation(this.existingStation.getId());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(get("/api/stations/" + this.existingStation.getId()+ "/nodes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        // Parse and validate the response
        S125Node[] result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), S125Node[].class);
        assertEquals(this.messages.size(), Arrays.asList(result).size());
    }

}