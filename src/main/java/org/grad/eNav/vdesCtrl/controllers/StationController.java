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

package org.grad.eNav.vdesCtrl.controllers;

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.dtos.AtonMessageDto;
import org.grad.eNav.vdesCtrl.models.dtos.S100AbstractNode;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.DtPage;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * REST controller for managing Stations.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RestController
@RequestMapping("/api/stations")
@Slf4j
public class StationController {

    /**
     * The Station Service.
     */
    @Autowired
    StationService stationService;

    /**
     * GET /api/stations : Returns a paged list of all current stations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @ResponseStatus
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Station>> getStations(Pageable pageable) {
        log.debug("REST request to get page of Stations");
        Page<Station> stationPage = this.stationService.findAll(pageable);
        return ResponseEntity.ok()
                .body(stationPage.getContent());
    }

    /**
     * POST /api/stations/dt : Returns a paged list of all current stations.
     *
     * @param dtPagingRequest the datatables paging request
     * @return the ResponseEntity with status 200 (OK) and the list of stations in body
     */
    @PostMapping(value = "/dt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DtPage<Station>> getStationsForDatatables(@RequestBody DtPagingRequest dtPagingRequest) {
        log.debug("REST request to get page of Stations for datatables");
        return ResponseEntity.ok()
                .body(this.stationService.handleDatatablesPagingRequest(dtPagingRequest));
    }

    /**
     * GET /api/stations/:id : get the "ID" station.
     *
     * @param id the ID of the station to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the station, or with status 404 (Not Found)
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Station> getStation(@PathVariable BigInteger id) {
        log.debug("REST request to get Station : {}", id);
        return ResponseEntity.ok()
                .body(this.stationService.findOne(id));
    }

    /**
     * POST /api/stations : Create a new station.
     *
     * @param station the station to create
     * @return the ResponseEntity with status 201 (Created) and with body the new station, or with status 400 (Bad Request) if the station has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
            station = this.stationService.save(station);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("station", e.getMessage(), e.toString()))
                    .body(station);
        }

        // Build the response
        return ResponseEntity.created(new URI("/api/stations/" + station.getId())).body(station);
    }

    /**
     * PUT /api/stations : Update an existing station.
     *
     * @param id the ID of the station to update
     * @param station the station to update
     * @return the ResponseEntity with status 201 (Created) and with body the new station, or with status 400 (Bad Request) if the station has already an ID
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
     * DELETE /api/stations/:id : Delete the "ID" station.
     *
     * @param id the ID of the station to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteStation(@PathVariable BigInteger id) {
        log.debug("REST request to delete Station : {}", id);
        this.stationService.delete(id);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("station", id.toString()))
                .build();
    }

    /**
     * GET /api/stations/{id}/messages : Returns a paged list of all messages
     * assigned to a specified station.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of nodes in body
     */
    @GetMapping(value = "/{id}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AtonMessageDto>> getStationMessages(@PathVariable BigInteger id) {
        log.debug("REST request to get the messages for Station : {}", id);
        return ResponseEntity.ok()
                .body(this.stationService.findMessagesForStation(id));
    }

    /**
     * PUT /api/stations/{id}/messages/{atonNumber}/blacklist : Add a specific
     * AtoN Number in the blacklist for a given station.
     *
     * @return the ResponseEntity with status 200 (OK)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}/messages/{atonNumber}/blacklist", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<S100AbstractNode>> addBlacklistedAtoN(@PathVariable BigInteger id, @PathVariable String atonNumber) {
        log.debug("REST request to blacklist AtoN Number {} for Station : {}", atonNumber, id);
        this.stationService.addBlacklistAtonNumber(id, atonNumber);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/stations/{atonNumber}/messages/{uid}/blacklist : Removes a
     * specific AtoN Number from the blacklist for a given station.
     *
     * @return the ResponseEntity with status 200 (OK)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}/messages/{atonNumber}/blacklist", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<S100AbstractNode>> deleteBlacklistedAtoN(@PathVariable BigInteger id, @PathVariable String atonNumber) {
        log.debug("REST request to blacklist AtoN Number {} for Station : {}", atonNumber, id);
        this.stationService.removeBlacklisAtonNumber(id, atonNumber);
        return ResponseEntity.ok().build();
    }

}
