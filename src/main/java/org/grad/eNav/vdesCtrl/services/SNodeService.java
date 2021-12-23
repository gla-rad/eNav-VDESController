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

package org.grad.eNav.vdesCtrl.services;

import _int.iho.s125.gml._0.DataSet;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.exceptions.DataNotFoundException;
import org.grad.eNav.vdesCtrl.models.domain.SNode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.dtos.S100AbstractNode;
import org.grad.eNav.vdesCtrl.models.dtos.S124Node;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.repos.SNodeRepo;
import org.grad.eNav.vdesCtrl.utils.GeoJSONUtils;
import org.grad.eNav.vdesCtrl.utils.S100Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Station Node Service Class
 *
 * Service Implementation for managing Station SNodes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
@Transactional
public class SNodeService {

    /**
     * The Station Service.
     */
    @Autowired
    StationService stationService;

    /**
     * The SNode Repo.
     */
    @Autowired
    SNodeRepo sNodeRepo;

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
     * Get one node by ID.
     *
     * @param id the ID of the node
     * @return the node
     */
    @Transactional(readOnly = true)
    public SNode findOne(BigInteger id) {
        log.debug("Request to get Node : {}", id);
        return Optional.ofNullable(id)
                .map(this.sNodeRepo::findOneWithEagerRelationships)
                .orElseThrow(() ->
                        new DataNotFoundException(String.format("No station node found for the provided ID: %d", id))
                );
    }

    /**
     * Get one node by UID.
     *
     * @param uid the UID of the node
     * @return the node
     */
    @Transactional(readOnly = true)
    public SNode findOneByUid(String uid) {
        log.debug("Request to get Node with UID : {}", uid);
        return Optional.ofNullable(uid)
                .map(this.sNodeRepo::findByUid)
                .orElseThrow(() ->
                        new DataNotFoundException(String.format("No station node found for the provided UID: %s", uid))
                );
    }

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
     * Delete the node by ID.
     *
     * @param id the ID of the node
     */
    public void delete(BigInteger id) {
        log.debug("Request to delete Station Node : {}", id);
        if(this.sNodeRepo.existsById(id)) {
            // Get the station node with all relationships
            SNode sNode = this.sNodeRepo.findOneWithEagerRelationships(id);
            // Remove it from all linked stations
            Optional.of(sNode)
                    .map(SNode::getStations)
                    .orElse(Collections.emptySet())
                    .stream()
                    .forEach(station -> {
                        station.getNodes().remove(sNode);
                        this.stationService.save(station);
                    });
            // Finally delete the station node
            this.sNodeRepo.deleteById(id);
        } else {
            throw new DataNotFoundException(String.format("No station node found for the provided ID: %d", id));
        }
    }

    /**
     * Delete the node by UID.
     *
     * @param uid the UID the node
     */
    public void deleteByUid(String uid) throws DataNotFoundException {
        log.debug("Request to delete Node with UID : {}", uid);
        BigInteger id = Optional.ofNullable(uid)
                .map(this.sNodeRepo::findByUid)
                .map(SNode::getId)
                .orElseThrow(() ->
                        new DataNotFoundException(String.format("No station node found for the provided UID: %s", uid))
                );
        this.delete(id);
    }

    /**
     * Get all the nodes of a specific station in a pageable search.
     *
     * @param stationId the station ID to retrieve the nodes for
     * @return the list of nodes
     */
    @Transactional(readOnly = true)
    public List<S100AbstractNode> findAllForStationDto(BigInteger stationId) {
        log.debug("Request to get all Nodes for Station: {}", stationId);
        return Optional.ofNullable(stationId)
                .map(this.stationService::findOne)
                .map(Station::getNodes)
                .map(l -> l.stream().map(S100Utils::toS100Dto).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

}
