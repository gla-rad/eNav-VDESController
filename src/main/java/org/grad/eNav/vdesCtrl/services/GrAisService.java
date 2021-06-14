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
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.grad.eNav.vdesCtrl.components.GrAisAdvertiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The GnuRadion AIS Service Class
 *
 * This class implements a handler for the AtoN messages coming into a Spring
 * Integration channel. It will then translate the content using JAXB and
 * generate the AIS sentences to be sent down to the GNURadio stations
 * running the GR-AIX module.
 *
 * More Info: https://github.com/gla-rad/ais
 *
 * @author Nikolaos Vastardis
 */
@Service
@Slf4j
public class GrAisService {

    /**
     * The Application Context
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * The Station Service.
     */
    @Autowired
    private StationService stationService;

    /**
     * The GNURadio UDP Connection.
     */
    private List<GrAisAdvertiser> grAisAdvertisers;

    /**
     * The service post-construct operations where the handler auto-registers
     * it-self to the aton publication channel. Once successful, it will then
     * monitor the channel for all inputs coming through the REST API.
     */
    @PostConstruct
    public void init() {
        log.info("GrAis Service is booting up...");

        // Initialise the GNURadio AIS Advertisers, one per each station
        this.grAisAdvertisers = Optional.of(StationType.GNU_RADIO)
                .map(this.stationService::findAllByType)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(station -> {
                    GrAisAdvertiser grAisAdvertiser = this.applicationContext.getBean(GrAisAdvertiser.class);
                    try {
                        grAisAdvertiser.init(station);
                    } catch (SocketException ex) {
                        log.error(ex.getMessage());
                        return null;
                    }
                    return grAisAdvertiser;
                })
                .collect(Collectors.toList());
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        this.grAisAdvertisers.forEach(GrAisAdvertiser::destroy);
    }

}
