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
import org.grad.eNav.vdesCtrl.services.StationService;
import org.grad.eNav.vdesCtrl.utils.HeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Station.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RequestMapping("/stations")
@Slf4j
public class StationController {

    /**
     * The Station Service
     */
    @Autowired
    private StationService stationService;

    /**
     * GET /stations : Returns a paged list of all current stations.
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
     * GET  /instances/:id : get the "id" instance.
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
     * DELETE  /stations/:id : delete the "id" station.
     *
     * @param id the id of the station to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteInstance(@PathVariable BigInteger id) {
        log.debug("REST request to delete Instance : {}", id);
        this.stationService.delete(id);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert("station", id.toString()))
                .build();
    }
}
