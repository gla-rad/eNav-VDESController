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

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.grad.eNav.vdesCtrl.utils.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

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
    private SNodeService sNodeService;

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
