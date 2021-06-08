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
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.repos.StationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

/**
 * Service Implementation for managing Stations.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
@Transactional
public class StationService {

    @Autowired
    StationRepo stationRepo;

    /**
     * Save a station.
     *
     * @param station the station to save
     * @return the persisted station
     */
    public Station save(Station station) {
        log.debug("Request to save Station : {}", station);
        return this.stationRepo.save(station);
    }

    /**
     * Get all the stations.
     *
     * @return the list of stations
     */
    @Transactional(readOnly = true)
    public List<Station> findAll() {
        log.debug("Request to get all Stations in a pageable search");
        return this.stationRepo.findAll();
    }

    /**
     * Get all the stations in a pageable search.
     *
     * @param pageable the pagination information
     * @return the list of stations
     */
    @Transactional(readOnly = true)
    public Page<Station> findAll(Pageable pageable) {
        log.debug("Request to get all Stations in a pageable search");
        Page<Station> result = this.stationRepo.findAll(pageable);
        return result;
    }

    /**
     * Get one station by id.
     *
     * @param id the id of the station
     * @return the station
     */
    @Transactional(readOnly = true)
    public Station findOne(BigInteger id) {
        log.debug("Request to get Station : {}", id);
        Station station = this.stationRepo.findOneWithEagerRelationships(id);
        return station;
    }

    /**
     * Delete the station by id.
     *
     * @param id the id of the station
     */
    public void delete(BigInteger id) {
        log.debug("Request to delete Station : {}", id);
        this.stationRepo.deleteById(id);
    }
}
