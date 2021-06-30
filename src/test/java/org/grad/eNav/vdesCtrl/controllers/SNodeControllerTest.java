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

package org.grad.eNav.vdesCtrl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grad.eNav.vdesCtrl.exceptions.DataNotFoundException;
import org.grad.eNav.vdesCtrl.models.domain.SNode;
import org.grad.eNav.vdesCtrl.models.domain.SNodeType;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    private SNode existingNode;

    /**
     * Common setup for all the tests.
     */
    @BeforeEach
    void setUp() {
        existingNode = new SNode();
        existingNode.setId(BigInteger.valueOf(1));
        existingNode.setUid("UID1");
        existingNode.setType(SNodeType.S125);
        existingNode.setMessage("Node Message");
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