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
import org.apache.lucene.search.Query;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.models.dtos.DtPage;
import org.grad.eNav.vdesCtrl.models.dtos.DtPagingRequest;
import org.grad.eNav.vdesCtrl.repos.StationRepo;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing Stations.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Service
@Slf4j
@Transactional
public class StationService {

    /**
     * The Entity Manager.
     */
    @Autowired
    EntityManager entityManager;

    /**
     * The Station Repository.
     */
    @Autowired
    StationRepo stationRepo;

    // Service Variables
    private static final Comparator<Station> EMPTY_COMPARATOR = (e1, e2) -> 0;

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
     * Get all the stations of a specific type.
     *
     * @return the list of stations of that type
     */
    @Transactional(readOnly = true)
    public List<Station> findAllByType(StationType stationType) {
        log.debug("Request to get all Stations by type : {}", stationType.name());
        return this.stationRepo.findByType(stationType);
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
        return this.stationRepo.findOneWithEagerRelationships(id);
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

    /**
     * Handles a datatables pagination request and returns the results list in
     * an appropriate format to be viewed by a datatables jQuery table.
     *
     * @param pagingRequest the Datatables pagination request
     * @return the Datatables paged response
     */
    public DtPage<Station> getStationsForDatatables(DtPagingRequest pagingRequest) {
        // Create the search query
        FullTextQuery searchQuery = this.searchStationsQuery(pagingRequest.getSearch().getValue());
        searchQuery.setFirstResult(pagingRequest.getStart());
        searchQuery.setMaxResults(pagingRequest.getLength());

        // Add sorting if requested
        Optional.of(pagingRequest)
                .map(DtPagingRequest::getLucenceSort)
                .filter(ls -> ls.getSort().length > 0)
                .ifPresent(searchQuery::setSort);

        return (DtPage<Station>) Optional.of(searchQuery)
                .map(FullTextQuery::getResultList)
                .map(stations -> new PageImpl<>(stations, pagingRequest.toPageRequest(), searchQuery.getResultSize()))
                .map(page -> new DtPage((Page<Station>) page, pagingRequest))
                .orElseGet(DtPage::new);
    }

    /**
     * Constructs a hibernate search query using Lucene based on the provided
     * search test. This query will be based solely on the stations table and
     * will include the following fields:
     * - Name
     * - IP Address
     * - MMSI
     *
     * @param searchText the text to be searched
     * @return the full text query
     */
    private FullTextQuery searchStationsQuery(String searchText) {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Station.class)
                .get();

        Query luceneQuery = queryBuilder
                .keyword()
                .wildcard()
                .onFields("name", "ipAddress", "mmsi")
                .matching(searchText + "*")
                .createQuery();

        return fullTextEntityManager.createFullTextQuery(luceneQuery, Station.class);
    }

}
