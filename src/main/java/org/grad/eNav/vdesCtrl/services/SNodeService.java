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

import _int.iho.s125.gml._0.DatasetType;
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
     * Get one node by ID.
     *
     * @param id the ID of the node
     * @return the node
     */
    @Transactional(readOnly = true)
    public SNode findOne(BigInteger id) throws DataNotFoundException {
        log.debug("Request to get Node : {}", id);
        return Optional.ofNullable(id)
                .map(this.sNodeRepo::findOneWithEagerRelationships)
                .orElseThrow(() -> new DataNotFoundException("No station node found for the provided ID", null));
    }

    /**
     * Get one node by UDI.
     *
     * @param uid the UID of the node
     * @return the node
     */
    @Transactional(readOnly = true)
    public SNode findOneByUid(String uid) throws DataNotFoundException {
        log.debug("Request to get Node with UID : {}", uid);
        return Optional.ofNullable(uid)
                .map(this.sNodeRepo::findByUid)
                .orElseThrow(() -> new DataNotFoundException("No station node found for the provided ID", null));
    }

    /**
     * Delete the node by ID.
     *
     * @param id the ID of the node
     */
    public void delete(BigInteger id) throws DataNotFoundException {
        log.debug("Request to delete Station Node : {}", id);
        if(this.sNodeRepo.existsById(id)) {
            // Get the station node with all relationships
            SNode sNode = this.sNodeRepo.findOneWithEagerRelationships(id);
            // Remove the station node from all stations
            for(Station s: sNode.getStations()) {
                s.getNodes().remove(sNode);
                this.stationService.save(s);
            }
            // Finally delete the station node
            this.sNodeRepo.deleteById(id);
        } else {
            throw new DataNotFoundException("No station node found for the provided ID", null);
        }
    }

    /**
     * Delete the node by ID.
     *
     * @param uid the UID the node
     */
    public void deleteByUid(String uid) throws DataNotFoundException {
        log.debug("Request to delete Node with UID : {}", uid);
        BigInteger id = Optional.ofNullable(uid)
                .map(this.sNodeRepo::findByUid)
                .map(SNode::getId)
                .orElse(null);
        this.delete(id);
    }

    /**
     * Get all the nodes of a specific station in a pageable search.
     *
     * @param stationId the station ID to retrieve the nodes for
     * @return the list of nodes
     */
    @Transactional(readOnly = true)
    public List<S125Node> findAllForStationDto(BigInteger stationId) {
        log.debug("Request to get all Nodes for Station: {}", stationId);
        return Optional.ofNullable(stationId)
                .map(this.stationService::findOne)
                .map(Station::getNodes)
                .map(l -> l.stream().map(this::toS100Dto).map(S125Node.class::cast).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * This helper function translates the provided SNode domain object to
     * a S100AbstractNode implementing DTO. This can be used when the service
     * response to a client, rather than an internal component.
     *
     * @param snode the SNode object to be translated to a DTO
     * @return the DTO generated from the provided SNode object
     */
    private S100AbstractNode toS100Dto(SNode snode) {
        // Sanity check
        if(Objects.isNull(snode)) {
            return null;
        }

        // We first need to extract the bounding box of the snode message
        JsonNode bbox = null;
        try {
            DatasetType s125Dataset = S100Utils.unmarshallS125(snode.getMessage());
            List<Double> point = s125Dataset.getBoundedBy().getEnvelope().getLowerCorner().getValue();
            String crsName = s125Dataset.getBoundedBy().getEnvelope().getSrsName();
            Integer srid = Optional.ofNullable(crsName).map(crs -> crs.split(":")[1]).map(Integer::valueOf).orElse(null);
            bbox = GeoJSONUtils.createGeoJSONPoint(point.get(0), point.get(1), srid);
        } catch (JAXBException | NumberFormatException ex) {
            log.error(ex.getMessage());
        }

        // Now construct the DTO based on the SNode type
        switch (snode.getType()) {
            case S124:
                return new S124Node(snode.getUid(), bbox, snode.getMessage());
            case S125:
                return new S125Node(snode.getUid(), bbox, snode.getMessage());
            default:
                return null;
        }
    }
}
