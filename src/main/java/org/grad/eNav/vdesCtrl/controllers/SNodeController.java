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

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.domain.SNode;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.DtPage;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.grad.eNav.vdesCtrl.utils.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Station Nodes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RestController
@RequestMapping("/api/snodes")
@Slf4j
public class SNodeController {

    /**
     * The Station Node Service.
     */
    @Autowired
    SNodeService sNodeService;

    /**
     * GET /api/snodes : Returns a paged list of all current stations.
     *
     * @param page the page number to be retrieved
     * @param size the number of entries on each page
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @ResponseStatus
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SNode>> getNodes(@RequestParam("page") Optional<Integer> page,
                                                @RequestParam("size") Optional<Integer> size) {
        log.debug("REST request to get page of Stations");
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        Page<SNode> nodePage = this.sNodeService.findAll(PageRequest.of(currentPage - 1, pageSize));
        return ResponseEntity.ok()
                .body(nodePage.getContent());
    }

    /**
     * POST /api/snodes/dt : Returns a paged list of all current stations.
     *
     * @param dtPagingRequest the datatables paging request
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @PostMapping(value = "/dt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DtPage<SNode>> getNodesForDatatables(@RequestBody DtPagingRequest dtPagingRequest) {
        log.debug("REST request to get page of Stations for datatables");
        return ResponseEntity.ok()
                .body(this.sNodeService.handleDatatablesPagingRequest(dtPagingRequest));
    }

    /**
     * DELETE /api/snodes/{id} : Delete the "id" station node.
     *
     * @param id the ID of the station node to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteNode(@PathVariable BigInteger id) {
        log.debug("REST request to delete Station Node : {}", id);
        this.sNodeService.delete(id);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("node", id.toString()))
                .build();
    }

    /**
     * DELETE /api/snodes/uid/{uid} : Delete the "UID" station node.
     *
     * @param uid the UID of the station node to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "/uid/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteNodeByUid(@PathVariable String uid) {
        log.debug("REST request to delete Station Node by UID : {}", uid);
        // First translate the UID into a station node ID
        this.sNodeService.deleteByUid(uid);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("node", uid))
                .build();
    }

}
