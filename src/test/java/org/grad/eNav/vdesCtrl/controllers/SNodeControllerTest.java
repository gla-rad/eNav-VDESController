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
import org.grad.eNav.vdesCtrl.models.domain.SNode;
import org.grad.eNav.vdesCtrl.models.domain.SNodeType;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.*;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SNodeController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class SNodeControllerTest {

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
     * The Station Node Service mock.
     */
    @MockBean
    SNodeService sNodeService;

    // Test Variables
    private List<SNode> nodes;
    private Pageable pageable;
    private SNode existingNode;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
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

        // Create a station node with an ID
        existingNode = new SNode();
        existingNode.setId(BigInteger.valueOf(1));
        existingNode.setUid("UID1");
        existingNode.setType(SNodeType.S125);
        existingNode.setMessage("Node Message");
    }

    /**
     * Test that we can retrieve all the station nodes currently in the database
     * in a paged result.
     */
    @Test
    void testGetAllNodes() throws Exception {
        // Created a result page to be returned by the mocked service
        Page<SNode> page = new PageImpl<>(this.nodes.subList(0, 5), this.pageable, this.nodes.size());
        doReturn(page).when(this.sNodeService).findAll(any());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(get("/api/snodes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        // Parse and validate the response
        SNode[] result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SNode[].class);
        assertEquals(5, Arrays.asList(result).size());
    }

    /**
     * Test that the API supports the jQuery Datatables server-side paging
     * and search requests.
     */
    @Test
    void testGetNodesForDatatables() throws Exception {
        // Create a test datatables paging request
        DtColumn dtColumn = new DtColumn("id");
        dtColumn.setName("ID");
        dtColumn.setOrderable(true);
        DtOrder dtOrder = new DtOrder();
        dtOrder.setColumn(0);
        dtOrder.setDir(DtDirection.asc);
        DtPagingRequest dtPagingRequest = new DtPagingRequest();
        dtPagingRequest.setStart(0);
        dtPagingRequest.setLength(this.nodes.size());
        dtPagingRequest.setDraw(1);
        dtPagingRequest.setSearch(new DtSearch());
        dtPagingRequest.setOrder(Collections.singletonList(dtOrder));
        dtPagingRequest.setColumns(Collections.singletonList(dtColumn));

        // Create a mocked datatables paging response
        DtPage<SNode> dtPage = new DtPage<>();
        dtPage.setData(this.nodes);
        dtPage.setDraw(1);
        dtPage.setRecordsFiltered(this.nodes.size());
        dtPage.setRecordsTotal(this.nodes.size());

        // Mock the service call for creating a new instance
        doReturn(dtPage).when(this.sNodeService).handleDatatablesPagingRequest(any());

        // Perform the MVC request
        MvcResult mvcResult = this.mockMvc.perform(post("/api/snodes/dt")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(this.objectMapper.writeValueAsString(dtPagingRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Parse and validate the response
        DtPage<SNode> result = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DtPage.class);
        assertEquals(this.nodes.size(), result.getData().size());
    }

    /**
     * Test that we can correctly delete an existing station  node by using a
     * valid ID.
     */
    @Test
    void testDeleteSNode() throws Exception {
        // Perform the MVC request
        this.mockMvc.perform(delete("/api/snodes/{id}", this.existingNode.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * Test that if we do NOT find the station node we are trying to delete, an
     * HTTP NOT_FOUND response will be returned.
     */
    @Test
    void testDeleteSNodeNotFound() throws Exception {
        doThrow(DataNotFoundException.class).when(this.sNodeService).delete(any());

        // Perform the MVC request
        this.mockMvc.perform(delete("/api/snodes/{id}", this.existingNode.getId()))
                .andExpect(status().isNotFound());
    }

    /**
     * Test that we can correctly delete an existing station node by using a
     * valid UID.
     */
    @Test
    void testDeleteSNodeByUid() throws Exception {
        // Perform the MVC request
        this.mockMvc.perform(delete("/api/snodes/uid/{uid}", this.existingNode.getUid())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }

    /**
     * Test that if we do NOT find the station node we are trying to delete, an
     * HTTP NOT_FOUND response will be returned.
     */
    @Test
    void testDeleteSNodeByUidNotFound() throws Exception {
        doThrow(DataNotFoundException.class).when(this.sNodeService).deleteByUid(any());

        // Perform the MVC request
        this.mockMvc.perform(delete("/api/snodes/uid/{id}", this.existingNode.getId()))
                .andExpect(status().isNotFound());
    }

}