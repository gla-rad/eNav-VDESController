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
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Station.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Controller
@RequestMapping("/api/stations")
@Slf4j
public class StationController {

    /**
     * The Station Service.
     */
    @Autowired
    private StationService stationService;

    /**
     * The SNode Service.
     */
    @Autowired
    private SNodeService sNodeService;

    /**
     * GET /api/stations : Returns a paged list of all current stations.
     *
     * @param page the page number to be retrieved
     * @param size the number of entries on each page
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Station>> getStations(@RequestParam("page") Optional<Integer> page,
                                                     @RequestParam("size") Optional<Integer> size) {
        log.debug("REST request to get page of Stations");
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        Page<Station> stationPage = stationService.findAll(PageRequest.of(currentPage - 1, pageSize));
        return new ResponseEntity<>(stationPage.getContent(), HttpStatus.OK);
    }

    /**
     * GET /api/instances/:id : get the "id" instance.
     *
     * @param id the id of the instance to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the station, or with status 404 (Not Found)
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Station> getStation(@PathVariable BigInteger id) {
        log.debug("REST request to get Station : {}", id);
        return Optional.ofNullable(this.stationService.findOne(id))
                .map(result -> ResponseEntity.ok().body(result))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /api/stations : Create a new station.
     *
     * @param station the instance to create
     * @return the ResponseEntity with status 201 (Created) and with body the new instance, or with status 400 (Bad Request) if the instance has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Station> createStation(@RequestBody Station station) throws Exception, URISyntaxException {
        log.debug("REST request to save Station : {}", station);
        if (station.getId() != null) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("station", "idexists", "A new station cannot already have an ID"))
                    .body(null);
        }

        // Save the station
        try {
            this.stationService.save(station);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("station", e.getMessage(), e.toString()))
                    .body(station);
        }

        // Build the response
        return ResponseEntity.created(new URI("/api/instances/" + station.getId())).body(station);
    }

    /**
     * PUT /api/stations : Update an existing station.
     *
     * @param station the instance to create
     * @return the ResponseEntity with status 201 (Created) and with body the new instance, or with status 400 (Bad Request) if the instance has already an ID
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Station> updateStation(@PathVariable BigInteger id,
                                                 @RequestBody Station station) {
        log.debug("REST request to update Station : {}", station);
        if (id == null) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("station", "noid", "Cannot update a station without an ID"))
                    .body(null);
        } else {
            station.setId(id);
        }

        // Save the station
        try {
            this.stationService.save(station);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("station", e.getMessage(), e.toString()))
                    .body(station);
        }

        // Build the response
        return ResponseEntity.ok().body(station);
    }

    /**
     * DELETE /api/stations/:id : Delete the "id" station.
     *
     * @param id the id of the station to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteInstance(@PathVariable BigInteger id) {
        log.debug("REST request to delete Instance : {}", id);
        this.stationService.delete(id);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("station", id.toString()))
                .build();
    }

    /**
     * GET /api/stations/{id}/nodes : Returns a paged list of all nodes in the
     * specified station.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of nodes in body
     */
    @GetMapping(value = "/{id}/nodes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<S125Node>> getStationSNodes(@PathVariable BigInteger id) {
        log.debug("REST request to get page of Station Nodes");
        List<S125Node > nodeList = sNodeService.findAllForStationDto(id);
        return new ResponseEntity<>(nodeList, HttpStatus.OK);
    }
}
