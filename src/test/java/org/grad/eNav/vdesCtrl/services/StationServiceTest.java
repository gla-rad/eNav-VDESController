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

package org.grad.eNav.vdesCtrl.services;

import org.grad.eNav.vdesCtrl.exceptions.DataNotFoundException;
import org.grad.eNav.vdesCtrl.models.domain.*;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.*;
import org.grad.eNav.vdesCtrl.repos.StationRepo;
import org.hibernate.search.jpa.FullTextQuery;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    StationService stationService;

    /**
     * The Entity Manager mock.
     */
    @Mock
    EntityManager entityManager;

    /**
     * The Station Repository Mock.
     */
    @Mock
    private StationRepo stationRepo;

    // Test Variables
    private List<Station> stations;
    private List<SNode> nodes;
    private Pageable pageable;
    private Station newStation;
    private Station existingStation;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Initialise the station nodes list
        this.nodes = new ArrayList<>();
        for(long i=0; i<2; i++) {
            SNode node = new SNode();
            node.setId(BigInteger.valueOf(i));
            node.setUid("UID" + i);
            node.setType(SNodeType.S125);
            node.setMessage("Node Message");
            this.nodes.add(node);
        }

        // Initialise the stations list
        this.stations = new ArrayList<>();
        for(long i=0; i<10; i++) {
            Station station = new Station();
            station.setId(BigInteger.valueOf(i));
            station.setName("Station Name");
            station.setChannel(NMEAChannel.A);
            station.setMmsi("12345678" + i);
            station.setIpAddress("10.0.0." + i);
            station.setPiSeqNo(i);
            station.setType(StationType.VDES_1000);
            station.setPort(8000 + (int)i);
            station.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
            station.setNodes(new HashSet<>());
            station.getNodes().addAll(this.nodes);
            this.stations.add(station);
        }

        // Create a pageable definition
        this.pageable = PageRequest.of(0, 5);

        // Create a new station
        this.newStation = new Station();
        this.newStation.setName("New Station Name");
        this.newStation.setChannel(NMEAChannel.A);
        this.newStation.setMmsi("111111111");
        this.newStation.setIpAddress("10.0.0.1");
        this.newStation.setPiSeqNo(1L);
        this.newStation.setType(StationType.VDES_1000);
        this.newStation.setPort(8001);
        this.newStation.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));

        // Create a station with an ID
        this.existingStation = new Station();
        this.existingStation.setId(BigInteger.ONE);
        this.existingStation.setName("Existing Station Name");
        this.existingStation.setChannel(NMEAChannel.B);
        this.existingStation.setMmsi("222222222");
        this.existingStation.setIpAddress("10.0.0.2");
        this.existingStation.setPiSeqNo(2L);
        this.existingStation.setType(StationType.GNU_RADIO);
        this.existingStation.setPort(8002);
        this.existingStation.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
    }

    /**
     * Test that we can retrieve all the stations currently present in the
     * database.
     */
    @Test
    void testFindAll() {
        // Created a result page to be returned by the mocked repository
        doReturn(this.stations).when(this.stationRepo).findAll();

        // Perform the service call
        List<Station> result = this.stationService.findAll();

        // Test the result
        assertEquals(this.stations.size(), result.size());

        // Test each of the result entries
        for(int i=0; i < result.size(); i++){
            assertEquals(this.stations.get(i), result.get(i));
        }
    }

    /**
     * Test that we can retrieve all the stations currently present in the
     * database through a paged call.
     */
    @Test
    void testFindAllPaged() {
        // Created a result page to be returned by the mocked repository
        Page<Station> page = new PageImpl<>(this.stations.subList(0, 5), this.pageable, this.stations.size());
        doReturn(page).when(this.stationRepo).findAll(this.pageable);

        // Perform the service call
        Page<Station> result = this.stationService.findAll(pageable);

        // Test the result
        assertEquals(page.getSize(), result.getSize());

        // Test each of the result entries
        for(int i=0; i < result.getSize(); i++){
            assertEquals(this.stations.get(i), result.getContent().get(i));
        }
    }

    /**
     * Test that we can retrieve all the stations currently present in the
     * database for a specified station type.
     */
    @Test
    void testFindAllByType() {
        // Created a result page to be returned by the mocked repository
        doReturn(this.stations).when(this.stationRepo).findByType(StationType.VDES_1000);

        // Perform the service call
        List<Station> result = this.stationService.findAllByType(StationType.VDES_1000);

        // Test the result
        assertEquals(result.size(), result.size());

        // Test each of the result entries
        for(int i=0; i < result.size(); i++){
            assertEquals(this.stations.get(i), result.get(i));
        }
    }

    /**
     * Test that we can retrieve a single station entry based on the station
     * ID and all the eager relationships are loaded.
     */
    @Test
    void testFindOne() {
        doReturn(this.existingStation).when(this.stationRepo).findOneWithEagerRelationships(this.existingStation.getId());

        // Perform the service call
        Station result = this.stationService.findOne(this.existingStation.getId());

        // Make sure the eager relationships repo call was called
        verify(this.stationRepo, times(1)).findOneWithEagerRelationships(this.existingStation.getId());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingStation.getId(), result.getId());
        assertEquals(this.existingStation.getName(), result.getName());
        assertEquals(this.existingStation.getType(), result.getType());
        assertEquals(this.existingStation.getIpAddress(), result.getIpAddress());
        assertEquals(this.existingStation.getPort(), result.getPort());
        assertEquals(this.existingStation.getMmsi(), result.getMmsi());
        assertEquals(this.existingStation.getChannel(), result.getChannel());
        assertEquals(this.existingStation.getGeometry(), result.getGeometry());
        assertEquals(this.existingStation.getNodes(), result.getNodes());
    }

    /**
     * Test that we if the provided station ID does NOT exist, then when trying
     * to retrieve the respective station will return a DataNotFoundException.
     */
    @Test
    void testFindOneNotFound() {
        doReturn(null).when(this.stationRepo).findOneWithEagerRelationships(this.existingStation.getId());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.stationService.findOne(this.existingStation.getId())
        );
    }

    /**
     * Test that we can save correctly a new or existing station if all
     * the validation checks are successful.
     */
    @Test
    void testSave() {
        doReturn(this.newStation).when(this.stationRepo).save(any());

        // Perform the service call
        Station result = this.stationService.save(this.newStation);

        // Test the result
        assertEquals(this.newStation.getId(), result.getId());
        assertEquals(this.newStation.getName(), result.getName());
        assertEquals(this.newStation.getType(), result.getType());
        assertEquals(this.newStation.getIpAddress(), result.getIpAddress());
        assertEquals(this.newStation.getPort(), result.getPort());
        assertEquals(this.newStation.getMmsi(), result.getMmsi());
        assertEquals(this.newStation.getChannel(), result.getChannel());
        assertEquals(this.newStation.getGeometry(), result.getGeometry());
        assertEquals(this.newStation.getNodes(), result.getNodes());

        // Also that a saving call took place in the repository
        verify(this.stationRepo, times(1)).save(this.newStation);
    }

    /**
     * Test that we can successfully delete an existing station.
     */
    @Test
    void testDelete() {
        doReturn(Boolean.TRUE).when(this.stationRepo).existsById(this.existingStation.getId());
        doNothing().when(this.stationRepo).deleteById(this.existingStation.getId());

        // Perform the service call
        this.stationService.delete(this.existingStation.getId());

        // Verify that a deletion call took place in the repository
        verify(this.stationRepo, times(1)).deleteById(this.existingStation.getId());
    }

    /**
     * Test that if we try to delete a non-existing station then a
     * DataNotFoundException will be thrown.
     */
    @Test
    void testDeleteNotFound() {
        doReturn(Boolean.FALSE).when(this.stationRepo).existsById(this.existingStation.getId());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.stationService.delete(this.existingStation.getId())
        );
    }

    /**
     * Test that we can retrieve the paged list of stations for a Datatables
     * pagination request (which by the way also includes search and sorting
     * definitions).
     */
    @Test
    void testGetStationsForDatatables() {
        // First create the pagination request
        DtPagingRequest dtPagingRequest = new DtPagingRequest();
        dtPagingRequest.setStart(0);
        dtPagingRequest.setLength(5);

        // Set the pagination request columns
        dtPagingRequest.setColumns(new ArrayList());
        Stream.of("id", "name", "ipAddress", "mmsi")
                .map(DtColumn::new)
                .forEach(dtPagingRequest.getColumns()::add);

        // Set the pagination request ordering
        DtOrder dtOrder = new DtOrder();
        dtOrder.setColumn(0);
        dtOrder.setDir(DtDirection.asc);
        dtPagingRequest.setOrder(Collections.singletonList(dtOrder));

        // Set the pagination searchg
        DtSearch dtSearch = new DtSearch();
        dtSearch.setValue("search-term");
        dtPagingRequest.setSearch(dtSearch);

        // Mock the full text query
        FullTextQuery mockedQuery = mock(FullTextQuery.class);
        doReturn(this.stations.subList(0, 5)).when(mockedQuery).getResultList();
        doReturn(mockedQuery).when(this.stationService).searchStationsQuery(any());

        // Perform the service call
        DtPage<Station> result = this.stationService.getStationsForDatatables(dtPagingRequest);

        // Validate the result
        assertNotNull(result);
        assertEquals(5, result.getRecordsFiltered());

        // Test each of the result entries
        for(int i=0; i < result.getRecordsFiltered(); i++){
            assertEquals(result.getData().get(i), this.stations.get(i));
        }
    }

}