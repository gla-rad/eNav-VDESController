/*
 * Copyright (c) 2021 GLA Research and Development Directorate
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

import org.apache.commons.io.IOUtils;
import org.grad.eNav.vdesCtrl.exceptions.DataNotFoundException;
import org.grad.eNav.vdesCtrl.models.domain.*;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.repos.SNodeRepo;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SNodeServiceTest {

    /**
     * The Tested Service.
     */
    @InjectMocks
    @Spy
    SNodeService sNodeService;

    /**
     * The Station Service mock.
     */
    @Mock
    StationService stationService;


    /**
     * The Station Node Repository Mock.
     */
    @Mock
    SNodeRepo sNodeRepo;

    // Test Variables
    private List<SNode> nodes;
    private Pageable pageable;
    private SNode newNode;
    private SNode existingNode;
    private Station station;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setup() {
        // Create a temp geometry factory to get a test geometries
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        // Initialise the station nodes list
        this.nodes = new ArrayList<>();
        for(long i=0; i<10; i++) {
            SNode node = new SNode();
            node.setId(BigInteger.valueOf(i));
            node.setUid("UID" + i);
            node.setType(SNodeType.S125);
            node.setMessage("Node Message No" + i);
            this.nodes.add(node);
        }

        // Create a pageable definition
        this.pageable = PageRequest.of(0, 5);

        // Create a new station node
        this.newNode = new SNode();
        this.newNode.setUid("UID1");
        this.newNode.setType(SNodeType.S125);
        this.newNode.setMessage("Node Message");

        // Create a station node with an ID
        this.existingNode = new SNode();
        this.existingNode.setId(BigInteger.TEN);
        this.existingNode.setUid("UID10");
        this.existingNode.setType(SNodeType.S125);
        this.existingNode.setMessage("Node Message");

        // Create a station
        this.station = new Station();
        this.station.setId(BigInteger.ONE);
        this.station.setName("New Station Name");
        this.station.setChannel(NMEAChannel.A);
        this.station.setMmsi("111111111");
        this.station.setIpAddress("10.0.0.1");
        this.station.setPiSeqNo(1L);
        this.station.setType(StationType.VDES_1000);
        this.station.setPort(8001);
        this.station.setGeometry(factory.createPoint(new Coordinate(52.001, 1.002)));
        this.station.setNodes(new HashSet<>(this.nodes));
    }

    /**
     * Test that we can retrieve all the station nodes currently present in the
     * database.
     */
    @Test
    void testFindAll() {
        // Created a result page to be returned by the mocked repository
        doReturn(this.nodes).when(this.sNodeRepo).findAll();

        // Perform the service call
        List<SNode> result = this.sNodeService.findAll();

        // Test the result
        assertEquals(this.nodes.size(), result.size());

        // Test each of the result entries
        for(int i=0; i < result.size(); i++){
            assertEquals(this.nodes.get(i), result.get(i));
        }
    }

    /**
     * Test that we can retrieve all the station nodes currently present in the
     * database through a paged call.
     */
    @Test
    void testFindAllPaged() {
        // Created a result page to be returned by the mocked repository
        Page<SNode> page = new PageImpl<>(this.nodes.subList(0, 5), this.pageable, this.nodes.size());
        doReturn(page).when(this.sNodeRepo).findAll(this.pageable);

        // Perform the service call
        Page<SNode> result = this.sNodeService.findAll(pageable);

        // Test the result
        assertEquals(page.getSize(), result.getSize());

        // Test each of the result entries
        for(int i=0; i < result.getSize(); i++){
            assertEquals(this.nodes.get(i), result.getContent().get(i));
        }
    }

    /**
     * Test that we can retrieve a single station node entry based on the
     * station node ID and all the eager relationships are loaded.
     */
    @Test
    void testFindOne() {
        doReturn(this.existingNode).when(this.sNodeRepo).findOneWithEagerRelationships(this.existingNode.getId());

        // Perform the service call
        SNode result = this.sNodeService.findOne(this.existingNode.getId());

        // Make sure the eager relationships repo call was called
        verify(this.sNodeRepo, times(1)).findOneWithEagerRelationships(this.existingNode.getId());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingNode.getId(), result.getId());
        assertEquals(this.existingNode.getUid(), result.getUid());
        assertEquals(this.existingNode.getType(), result.getType());
        assertEquals(this.existingNode.getMessage(), result.getMessage());
    }

    /**
     * Test that we if the provided station node ID does NOT exist, then when
     * trying to retrieve the respective station node will return a
     * DataNotFoundException.
     */
    @Test
    void testFindOneNotFound() {
        doReturn(null).when(this.sNodeRepo).findOneWithEagerRelationships(this.existingNode.getId());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.sNodeService.findOne(this.existingNode.getId())
        );
    }

    /**
     * Test that we can retrieve a single station node entry based on the
     * station node UID and all the eager relationships are loaded.
     */
    @Test
    void testFindOneByUid() {
        doReturn(this.existingNode).when(this.sNodeRepo).findByUid(this.existingNode.getUid());

        // Perform the service call
        SNode result = this.sNodeService.findOneByUid(this.existingNode.getUid());

        // Make sure the eager relationships repo call was called
        verify(this.sNodeRepo, times(1)).findByUid(this.existingNode.getUid());

        // Test the result
        assertNotNull(result);
        assertEquals(this.existingNode.getId(), result.getId());
        assertEquals(this.existingNode.getUid(), result.getUid());
        assertEquals(this.existingNode.getType(), result.getType());
        assertEquals(this.existingNode.getMessage(), result.getMessage());
    }

    /**
     * Test that we if the provided station node UID does NOT exist, then when
     * trying to retrieve the respective station node will return a
     * DataNotFoundException.
     */
    @Test
    void testFindOneByUidNotFound() {
        doReturn(null).when(this.sNodeRepo).findByUid(this.existingNode.getUid());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.sNodeService.findOneByUid(this.existingNode.getUid())
        );
    }

    /**
     * Test that we can save correctly a new or existing station node if all
     * the validation checks are successful.
     */
    @Test
    void testSave() {
        doReturn(this.newNode).when(this.sNodeRepo).save(any());

        // Perform the service call
        SNode result = this.sNodeService.save(this.newNode);

        // Test the result
        assertNotNull(result);
        assertEquals(this.newNode.getId(), result.getId());
        assertEquals(this.newNode.getUid(), result.getUid());
        assertEquals(this.newNode.getType(), result.getType());
        assertEquals(this.newNode.getMessage(), result.getMessage());

        // Also that a saving call took place in the repository
        verify(this.sNodeRepo, times(1)).save(this.newNode);
    }

    /**
     * Test that we can successfully delete an existing station node.
     */
    @Test
    void testDelete() throws DataNotFoundException {
        // Add a station to the node to be deleted
        this.existingNode.setStations(new HashSet<>());
        this.existingNode.getStations().add(new Station());

        doReturn(Boolean.TRUE).when(this.sNodeRepo).existsById(this.existingNode.getId());
        doReturn(this.existingNode).when(this.sNodeRepo).findOneWithEagerRelationships(this.existingNode.getId());
        doNothing().when(this.sNodeRepo).deleteById(this.existingNode.getId());

        // Perform the service call
        this.sNodeService.delete(this.existingNode.getId());

        // Verify that a deletion call took place in the repository
        verify(this.sNodeRepo, times(1)).deleteById(this.existingNode.getId());
    }

    /**
     * Test that if we try to delete a non-existing station node then a
     * DataNotFoundException will be thrown.
     */
    @Test
    void testDeleteNotFound() {
        doReturn(Boolean.FALSE).when(this.sNodeRepo).existsById(this.existingNode.getId());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.sNodeService.delete(this.existingNode.getId())
        );
    }

    /**
     * Test that we can successfully delete an existing station node by its UID.
     */
    @Test
    void testDeleteByUid() {
        doReturn(this.existingNode).when(this.sNodeRepo).findByUid(this.existingNode.getUid());
        doNothing().when(this.sNodeService).delete(this.existingNode.getId());

        // Perform the service call
        this.sNodeService.deleteByUid(this.existingNode.getUid());

        // Verify that a deletion call took place in the repository
        verify(this.sNodeService, times(1)).delete(this.existingNode.getId());
    }

    /**
     * Test that if we try to delete a non-existing station node by its UID then
     * a DataNotFoundException will be thrown.
     */
    @Test
    void testDeleteByUidNotFound() {
        doReturn(null).when(this.sNodeRepo).findByUid(this.existingNode.getUid());

        // Perform the service call
        assertThrows(DataNotFoundException.class, () ->
                this.sNodeService.deleteByUid(this.existingNode.getUid())
        );
    }

    /**
     * Test that we can retrieve all the station nodes assigned to a sigle
     * station as S125 objects.
     */
    @Test
    void testFindAllForStationDto() throws IOException {
        // First set a valid S125 content to the station nodes' messages
        InputStream in = new ClassPathResource("s125-msg.xml").getInputStream();
        String xml = IOUtils.toString(in, StandardCharsets.UTF_8.name());
        for(int i=0; i<this.nodes.size(); i++) {
            this.nodes.get(i).setMessage(xml);
        }

        doReturn(this.station).when(this.stationService).findOne(this.station.getId());

        // Perform the service call
        List<S125Node> result = this.sNodeService.findAllForStationDto(this.station.getId());

        assertNotNull(result);
        assertEquals(this.nodes.size(), result.size());

        // Test each of the result entries
        for(int i=0; i < result.size(); i++) {
            S125Node resultNode = result.get(i);
            SNode matchingNode = this.nodes.stream()
                    .filter(node -> node.getUid().equals(resultNode.getAtonUID()))
                    .findAny()
                    .orElse(null);
            assertNotNull(matchingNode);
            assertEquals(matchingNode.getUid(), resultNode.getAtonUID());
            assertEquals(matchingNode.getMessage(), resultNode.getContent());
        }
    }
}