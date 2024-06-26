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

package org.grad.eNav.vdesCtrl.services;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.grad.eNav.vdesCtrl.exceptions.DataNotFoundException;
import org.grad.eNav.vdesCtrl.exceptions.ValidationException;
import org.grad.eNav.vdesCtrl.feign.AtonServiceClient;
import org.grad.eNav.vdesCtrl.models.domain.SignatureMode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.models.dtos.AtonMessageDto;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.DtPage;
import org.grad.eNav.vdesCtrl.models.dtos.datatables.DtPagingRequest;
import org.grad.eNav.vdesCtrl.repos.StationRepo;
import org.grad.eNav.vdesCtrl.utils.GeometryJSONConverter;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

/**
 * The Station Service Class
 *
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
     * The AtoN Service Client
     */
    @Autowired
    AtonServiceClient atonServiceClient;

    /**
     * The GNURadio AIS Service.
     */
    @Autowired
    @Lazy
    GrAisService grAisService;

    /**
     * The VDES-1000 Service.
     */
    @Autowired
    @Lazy
    VDES1000Service vdes1000Service;

    /**
     * The Station Repository.
     */
    @Autowired
    StationRepo stationRepo;

    // Service Variables
    private final String[] searchFields = new String[] {
            "name",
            "ipAddress",
            "mmsi"
    };

    /**
     * Get all the stations.
     *
     * @return the list of stations
     */
    @Transactional(readOnly = true)
    public List<Station> findAll() {
        log.debug("Request to get all Stations in a list");
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
        return this.stationRepo.findAll(pageable);
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
        return this.stationRepo.findById(id)
                .orElseThrow(() ->
                        new DataNotFoundException(String.format("No station found for the provided ID: %d", id))
                );
    }

    /**
     * Save a station.
     *
     * @param station the station to save
     * @return the persisted station
     */
    public Station save(Station station) {
        log.debug("Request to save Station : {}", station);

        // Validate the signature mode - no VDE is GNURadio
        if(station.getType() == StationType.GNU_RADIO && station.getSignatureMode() == SignatureMode.VDE) {
            throw new ValidationException("VDE is not a valid mode for GNURadio-based stations.");
        }

        // Copy the existing blacklist IDs if available
        Optional.of(station)
                .map(Station::getId)
                .map(this.stationRepo::getReferenceById)
                .map(Station::getBlacklistedUids)
                .ifPresent(station::setBlacklistedUids);

        // Save the updated station
        Station savedStation = this.stationRepo.save(station);

        // And ask the geomesa datastore services to reload
        this.grAisService.reload();
        this.vdes1000Service.reload();

        // Finally, return the saved value
        return savedStation;
    }

    /**
     * Delete the station by id.
     *
     * @param id the id of the station
     */
    public void delete(BigInteger id) {
        log.debug("Request to delete Station : {}", id);
        // Make sure the station exists
        if(!this.stationRepo.existsById(id)) {
            throw new DataNotFoundException(String.format("No station found for the provided ID: %d", id));
        }

        // Now delete the station
        this.stationRepo.deleteById(id);

        // And ask the geomesa datastore services to reload
        this.grAisService.reload();
        this.vdes1000Service.reload();
    }

    /**
     * Get all the allocated messages for a specific station in a pageable
     * search. The result will include all all the blacklisted messages.
     *
     * @param stationId the station ID to retrieve the messages for
     * @return the list of messages
     */
    @Transactional(readOnly = true)
    public List<AtonMessageDto> findMessagesForStation(BigInteger stationId) {
        return this.findMessagesForStation(stationId, true);
    }

    /**
     * Get all the allocated messages for a specific station in a pageable
     * search. An additional parameter can be used to indicate whether the
     * blacklisted messages should me omitted.
     *
     * @param stationId the station ID to retrieve the messages for
     * @return the list of messages
     */
    @Transactional(readOnly = true)
    public List<AtonMessageDto> findMessagesForStation(BigInteger stationId, boolean includeBlacklisted) {
        log.debug("Request to get all messages for Station: {}", stationId);
        // First access the station information
        final Station station = this.findOne(stationId);
        // Now query for the non-blacklisted AtoN messages
        return Optional.of(station)
                .map(Station::getGeometry)
                .filter(Objects::nonNull)
                .filter(not(Geometry::isEmpty))
                .map(GeometryJSONConverter::convertFromGeometry)
                .map(JsonNode::toString)
                .map(this.atonServiceClient::getMessagesForGeometry)
                .map(Page::getContent)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(s125 -> new AtonMessageDto(s125, station.getBlacklistedUids().contains(s125.getIdCode())))
                .filter(msg -> includeBlacklisted || !msg.isBlacklisted())
                .collect(Collectors.toList());
    }

    /**
     * Handles a datatables pagination request and returns the results list in
     * an appropriate format to be viewed by a datatables jQuery table.
     *
     * @param dtPagingRequest the Datatables pagination request
     * @return the Datatables paged response
     */
    @Transactional(readOnly = true)
    public DtPage<Station> handleDatatablesPagingRequest(DtPagingRequest dtPagingRequest) {
        // Create the search query
        TypedQuery<Station> searchQuery = this.searchStationsQuery(
                dtPagingRequest.getSearch().getValue()
        );

        // Set the pagination
        final long totalCount = this.stationRepo.count();
        final PageRequest pageRequest = dtPagingRequest.toPageRequest();
        searchQuery.setFirstResult(pageRequest.getPageNumber()*pageRequest.getPageSize());
        searchQuery.setMaxResults(pageRequest.getPageSize());

        // Get the search query results
        return Optional.of(searchQuery)
                .map(TypedQuery::getResultList)
                .map(resultList -> new PageImpl<>(resultList, dtPagingRequest.toPageRequest(), totalCount))
                .map(page -> new DtPage<>(page, dtPagingRequest))
                .orElseGet(DtPage::new);
    }

    /**
     * Add the provided AtoN Number UID into the specified station's blacklist.
     *
     * @param id the ID of the station to add the blacklist entry
     * @param atonNumber the AtoN Number of the entry to be added into the blacklist
     */
    public void addBlacklistAtonNumber(BigInteger id, String atonNumber) {
        // First get the specified stations
        Station station = this.findOne(id);

        // Add the specified UID
        station.getBlacklistedUids().add(atonNumber);

        // And save the list
        this.save(station);
    }

    /**
     * Removes a specific AtoN Number from the given station's blacklist.
     *
     * @param id the ID of the station to remove the blacklist entry
     * @param atonNumber the AtoN Number of the entry be removed from the blacklist
     */
    public void removeBlacklisAtonNumber(BigInteger id, String atonNumber) {
        // First get the specified stations
        Station station = this.findOne(id);

        // Remove the specified UID
        station.getBlacklistedUids().remove(atonNumber);

        // And save the list
        this.save(station);
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
    protected TypedQuery<Station> searchStationsQuery(String searchText) {
        final CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        final CriteriaQuery<Station> criteriaQuery = criteriaBuilder.createQuery(Station.class);
        final Root<Station> stationRoot = criteriaQuery.from(Station.class);

        // Create the query predicates
        final Predicate searchPredicate = Optional.ofNullable(searchText)
                        .filter(StringUtils::isNotBlank)
                        .map(input -> criteriaBuilder.or(Stream.of(this.searchFields)
                                        .map(field -> criteriaBuilder.like(stationRoot.get(field), "%" + searchText + "%"))
                                        .toArray(Predicate[]::new)))
                        .orElseGet(criteriaBuilder::and);

        // Now create the final criteria query
        final CriteriaQuery<Station> searchQuery = criteriaQuery.where(searchPredicate);

        // And return the final query
        return this.entityManager.createQuery(searchQuery);
    }

}
