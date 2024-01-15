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

import org.grad.eNav.vdesCtrl.exceptions.DataNotFoundException;
import org.grad.eNav.vdesCtrl.exceptions.ValidationException;
import org.grad.eNav.vdesCtrl.feign.AtonServiceClient;
import org.grad.eNav.vdesCtrl.models.domain.SignatureMode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.models.dtos.AtonMessageDto;
import org.grad.eNav.vdesCtrl.models.dtos.S100AbstractNode;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.*;
import org.grad.eNav.vdesCtrl.repos.StationRepo;
import org.grad.eNav.vdesCtrl.utils.GeometryJSONConverter;
import org.grad.vdes1000.generic.AISChannelPref;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.SearchResultTotal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
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
     * The AtoN Service Client mock.
     */
    @Mock
    AtonServiceClient atonServiceClient;

    /**
     * The GNURadio AIS Service mock.
     */
    @Mock
    GrAisService grAisService;

    /**
     * The VDES-1000 Service mock.
     */
    @Mock
    VDES1000Service vdes1000Service;

    /**
     * The Station Repository mock.
     */
    @Mock
    private StationRepo stationRepo;

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
    void setup() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Initialise the station messages list
        this.messages = new ArrayList<>();
        for(long i=0; i<10; i++) {
            AtonMessageDto message = new AtonMessageDto();
            message.setAtonNumber("AtonNumber" + i);
            message.setGeometry(GeometryJSONConverter.convertFromGeometry(factory.createPoint(new Coordinate(1.594 + i, 53.6 + i))));
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
            station.setSignatureMode(SignatureMode.NONE);
            station.setMmsi("12345678" + i);
            station.setIpAddress("10.0.0." + i);
            station.setType(StationType.VDES_1000);
            station.setPort(8000 + (int)i);
            station.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
            this.stations.add(station);
        }

        // Create a pageable definition
        this.pageable = PageRequest.of(0, 5);

        // Create a new station
        this.newStation = new Station();
        this.newStation.setName("New Station Name");
        this.newStation.setChannel(AISChannelPref.A);
        this.newStation.setSignatureMode(SignatureMode.NONE);
        this.newStation.setMmsi("111111111");
        this.newStation.setIpAddress("10.0.0.1");
        this.newStation.setType(StationType.VDES_1000);
        this.newStation.setPort(8001);
        this.newStation.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
        this.newStation.setBlacklistedUids(new HashSet<>());

        // Create a station with an ID
        this.existingStation = new Station();
        this.existingStation.setId(BigInteger.ONE);
        this.existingStation.setName("Existing Station Name");
        this.existingStation.setChannel(AISChannelPref.B);
        this.existingStation.setSignatureMode(SignatureMode.NONE);
        this.existingStation.setMmsi("222222222");
        this.existingStation.setIpAddress("10.0.0.2");
        this.existingStation.setType(StationType.GNU_RADIO);
        this.existingStation.setPort(8002);
        this.existingStation.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
        this.existingStation.setBlacklistedUids(new HashSet<>());
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
        assertEquals(this.stations.size(), result.size());

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
        doReturn(Optional.of(this.existingStation)).when(this.stationRepo).findById(this.existingStation.getId());

        // Perform the service call
        Station result = this.stationService.findOne(this.existingStation.getId());

        // Make sure the eager relationships repo call was called
        verify(this.stationRepo, times(1)).findById(this.existingStation.getId());

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
    }

    /**
     * Test that we if the provided station ID does NOT exist, then when trying
     * to retrieve the respective station will return a DataNotFoundException.
     */
    @Test
    void testFindOneNotFound() {
        doReturn(Optional.empty()).when(this.stationRepo).findById(this.existingStation.getId());

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
        doAnswer(returnsFirstArg()).when(this.stationRepo).save(any());

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

        // Make sure all the relevant services have been reloaded
        verify(this.grAisService, times(1)).reload();
        verify(this.vdes1000Service, times(1)).reload();

        // Also that a saving call took place in the repository
        verify(this.stationRepo, times(1)).save(this.newStation);
    }

    /**
     * Test that we can save correctly a new or existing station and all the
     * already recorded station nodes will be correctly allocated if they match
     * the geometry of the station.
     */
    @Test
    void testSaveUpdateNodes() {
        doAnswer(returnsFirstArg()).when(this.stationRepo).save(any());

        // Perform the service call
        Station result = this.stationService.save(this.existingStation);

        // Test the result
        assertEquals(this.existingStation.getId(), result.getId());
        assertEquals(this.existingStation.getName(), result.getName());
        assertEquals(this.existingStation.getType(), result.getType());
        assertEquals(this.existingStation.getIpAddress(), result.getIpAddress());
        assertEquals(this.existingStation.getPort(), result.getPort());
        assertEquals(this.existingStation.getMmsi(), result.getMmsi());
        assertEquals(this.existingStation.getChannel(), result.getChannel());
        assertEquals(this.existingStation.getGeometry(), result.getGeometry());

        // Verify that a saving call took place in the repository
        verify(this.stationRepo, times(1)).save(this.existingStation);

        // Make sure all the relevant services have been reloaded
        verify(this.grAisService, times(1)).reload();
        verify(this.vdes1000Service, times(1)).reload();
    }

    /**
     * Test that if we try to assign a VDE mode to a GNURadio-based station
     * a ValidationException will be thrown since this is an invalid
     * configuration.
     */
    @Test
    void testSaveGNURadioWithVDEMode() {
        // Set the station as GNURadio with VDE mode
        this.newStation.setType(StationType.GNU_RADIO);
        this.newStation.setSignatureMode(SignatureMode.VDE);

        // Perform the service call
        assertThrows(ValidationException.class, () ->
                this.stationService.save(this.newStation)
        );
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

        // Make sure all the relevant services have been reloaded
        verify(this.grAisService, times(1)).reload();
        verify(this.vdes1000Service, times(1)).reload();
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

        // Make sure none of the relevant services have been reloaded
        verify(this.grAisService, never()).reload();
        verify(this.vdes1000Service, never()).reload();
    }

    /**
     * Test that we can correctly receive and process the S125 messages for
     * a specific station geometry through the AtoN service client.
     */
    @Test
    void testFindMessagesForStation() {
        Page<S125Node> page = new PageImpl<>(this.messages.subList(0, 5).stream().map(S125Node.class::cast).collect(Collectors.toList()), this.pageable, this.messages.size());
        doReturn(Optional.of(this.existingStation)).when(this.stationRepo).findById(this.existingStation.getId());
        doReturn(page).when(this.atonServiceClient).getMessagesForGeometry(any(String.class));

        // Perform the service call
        List<AtonMessageDto> result = this.stationService.findMessagesForStation(this.existingStation.getId());

        // Test the result
        assertEquals(page.getSize(), result.size());

        // Test each of the result entries
        for(int i=0; i < result.size(); i++){
            assertEquals(this.messages.get(i), result.get(i));
        }
    }

    /**
     * Test that we can correctly receive and process the S125 messages for
     * a specific station geometry through the AtoN service client. Make sure
     * however, that the blacklisted messages will NOT be included.
     */
    @Test
    void testFindMessagesForStationBlacklisted() {
        // Blacklist all messages
        this.existingStation.setBlacklistedUids(this.messages.stream()
                .map(AtonMessageDto.class::cast)
                .map(AtonMessageDto::getAtonNumber)
                .collect(Collectors.toSet()));

        Page<S125Node> page = new PageImpl<>(this.messages.subList(0, 5).stream().map(S125Node.class::cast).collect(Collectors.toList()), this.pageable, this.messages.size());
        doReturn(Optional.of(this.existingStation)).when(this.stationRepo).findById(this.existingStation.getId());
        doReturn(page).when(this.atonServiceClient).getMessagesForGeometry(any(String.class));

        // Perform the service call
        List<AtonMessageDto> result = this.stationService.findMessagesForStation(this.existingStation.getId(), false);

        // Test the result
        assertEquals(0, result.size());
    }

    /**
     * Test that if the requested station has no specified geometry, then no
     * linked AtoN messages will be return from the station message query.
     */
    @Test
    void testFindMessagesForStationNoGeometry() {
        this.existingStation.setGeometry(null);
        doReturn(Optional.of(this.existingStation)).when(this.stationRepo).findById(this.existingStation.getId());

        // Perform the service call
        List<AtonMessageDto> result = this.stationService.findMessagesForStation(this.existingStation.getId());

        // Test the result
        assertEquals(0, result.size());

        // Make sure the Feign call was never made
        verify(this.atonServiceClient, never()).getMessagesForGeometry(any(String.class));
    }

    /**
     * Test that we can retrieve the paged list of stations for a Datatables
     * pagination request (which by the way also includes search and sorting
     * definitions).
     */
    @Test
    void testHandleDatatablesPagingRequest() {
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

        // Set the pagination search
        DtSearch dtSearch = new DtSearch();
        dtSearch.setValue("search-term");
        dtPagingRequest.setSearch(dtSearch);

        // Mock the full text query
        SearchQuery mockedQuery = mock(SearchQuery.class);
        SearchResult mockedResult = mock(SearchResult.class);
        SearchResultTotal mockedResultTotal = mock(SearchResultTotal.class);
        doReturn(5L).when(mockedResultTotal).hitCount();
        doReturn(mockedResultTotal).when(mockedResult).total();
        doReturn(this.stations.subList(0, 5)).when(mockedResult).hits();
        doReturn(mockedResult).when(mockedQuery).fetch(any(), any());
        doReturn(mockedQuery).when(this.stationService).searchStationsQuery(any(), any());

        // Perform the service call
        DtPage<Station> result = this.stationService.handleDatatablesPagingRequest(dtPagingRequest);

        // Validate the result
        assertNotNull(result);
        assertEquals(5, result.getRecordsFiltered());

        // Test each of the result entries
        for(int i=0; i < result.getRecordsFiltered(); i++){
            assertEquals(this.stations.get(i), result.getData().get(i));
        }
    }

    /**
     * Test that we can add a new message UID into a station's blacklist.
     */
    @Test
    void testAddBlacklistUid() {
        doReturn(this.existingStation).when(this.stationService).findOne(this.existingStation.getId());

        // Perform the service call
        this.stationService.addBlacklistAtonNumber(this.existingStation.getId(), "test_message_uid");

        // Check that during the station update the blacklist was updated
        ArgumentCaptor<Station> stationsArgument = ArgumentCaptor.forClass(Station.class);
        verify(this.stationRepo, times(1)).save(stationsArgument.capture());
        assertNotNull(stationsArgument.getValue());
        assertNotNull(stationsArgument.getValue().getBlacklistedUids());
        assertEquals(1, stationsArgument.getValue().getBlacklistedUids().size());
        assertTrue(stationsArgument.getValue().getBlacklistedUids().contains("test_message_uid"));
    }

    /**
     * Test that we can remove an existing message UID from a station's
     * blacklist.
     */
    @Test
    void testRemoveBlacklistUid() {
        // Initialise the station with an existing blacklisted message UID
        this.existingStation.getBlacklistedUids().add("test_message_iod");

        doReturn(this.existingStation).when(this.stationService).findOne(this.existingStation.getId());

        // Perform the service call
        this.stationService.removeBlacklisAtonNumber(this.existingStation.getId(), "test_message_iod");

        // Check that during the station update the blacklist was updated
        ArgumentCaptor<Station> stationsArgument = ArgumentCaptor.forClass(Station.class);
        verify(this.stationRepo, times(1)).save(stationsArgument.capture());
        assertNotNull(stationsArgument.getValue());
        assertNotNull(stationsArgument.getValue().getBlacklistedUids());
        assertEquals(0, stationsArgument.getValue().getBlacklistedUids().size());
    }

}