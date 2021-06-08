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

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.domain.SNode;
import org.grad.eNav.vdesCtrl.repos.SNodeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing Station SNodes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
@Transactional
public class SNodeService {

    /**
     * The SNode Repo.
     */
    @Autowired
    SNodeRepo sNodeRepo;

    /**
     * The Station Service.
     */
    @Autowired
    StationService stationService;

    /**
     * Save a node.
     *
     * @param SNode the entity to save
     * @return the persisted entity
     */
    public SNode save(SNode SNode) {
        log.debug("Request to save Node : {}", SNode);
        return this.sNodeRepo.save(SNode);
    }

    /**
     * Get all the nodes.
     *
     * @return the list of nodes
     */
    @Transactional(readOnly = true)
    public List<SNode> findAll() {
        log.debug("Request to get all Nodes");
        return this.sNodeRepo.findAll();
    }

    /**
     * Get all the nodes in a pageable search.
     *
     * @param pageable the pagination information
     * @return the list of nodes
     */
    @Transactional(readOnly = true)
    public Page<SNode> findAll(Pageable pageable) {
        log.debug("Request to get all Nodes in a pageable search");
        return this.sNodeRepo.findAll(pageable);
    }

    /**
     * Get all the nodes of a specific station in a pageable search.
     *
     * @param stationId the station ID to retrieve the nodes for
     * @return the list of nodes
     */
    @Transactional(readOnly = true)
    public Page<SNode> findAllForStation(BigInteger stationId, Pageable pageable) {
        log.debug("Request to get all Nodes for Station: {}", stationId);
        return Optional.ofNullable(stationId)
                .map(this.stationService::findOne)
                .map(s -> this.sNodeRepo.findByStations(s, pageable))
                .orElse(Page.empty());
    }

    /**
     * Get one node by ID.
     *
     * @param id the ID of the node
     * @return the node
     */
    @Transactional(readOnly = true)
    public SNode findOne(BigInteger id) {
        log.debug("Request to get Node : {}", id);
        return this.sNodeRepo.findOneWithEagerRelationships(id);
    }

    /**
     * Get one node by UDI.
     *
     * @param uid the UID of the node
     * @return the node
     */
    @Transactional(readOnly = true)
    public SNode findOneByUid(String uid) {
        log.debug("Request to get Node with UID : {}", uid);
        return Optional.ofNullable(uid)
                .map(this.sNodeRepo::findByUid)
                .orElse(null);
    }

    /**
     * Delete the node by ID.
     *
     * @param id the ID of the node
     */
    public void delete(BigInteger id) {
        log.debug("Request to delete Node : {}", id);
        this.sNodeRepo.deleteById(id);
    }

    /**
     * Delete the node by ID.
     *
     * @param uid the UID the node
     */
    public void deleteByUid(String uid) {
        log.debug("Request to delete Node with UID : {}", uid);
        Optional.ofNullable(uid)
                .map(this.sNodeRepo::findByUid)
                .map(SNode::getId)
                .ifPresent(this.sNodeRepo::deleteById);
    }
}
