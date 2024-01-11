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

import lombok.extern.slf4j.Slf4j;
import org.grad.eNav.vdesCtrl.components.Vdes1000Advertiser;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The VDES-1000 Service Class
 *
 * This class implements a handler for the AtoN messages coming into a Spring
 * Integration channel. It will then translate the content using JAXB and
 * generate the UDP sentences to be sent down to the VDES-1000 stations.
 *
 * @author Nikolaos Vastardis
 */
@Service
@Slf4j
public class VDES1000Service {

    /**
     * The Application Context
     */
    @Autowired
    ApplicationContext applicationContext;

    /**
     * The Station Service.
     */
    @Autowired
    StationService stationService;

    // Service Variables
    protected List<Vdes1000Advertiser> vdes1000Advertisers;
    protected boolean reloading;

    /**
     * The service post-construct operations where the handler auto-registers
     * it-self to the aton publication channel. Once successful, it will then
     * monitor the channel for all inputs coming through the REST API.
     */
    @PostConstruct
    public void init() {
        log.info("VDES-1000 Service is booting up...");

        // Get all the stations to be monitored
        final List<Station> stations = Optional.of(StationType.VDES_1000)
                .map(this.stationService::findAllByType)
                .orElseGet(Collections::emptyList);

        // Initialise the VDES-1000 Advertisers, one per each station
        this.vdes1000Advertisers = stations.stream()
                .map(station -> {
                    Vdes1000Advertiser vdes1000Advertiser = this.applicationContext.getBean(Vdes1000Advertiser.class);
                    try {
                        vdes1000Advertiser.init(station);
                    } catch (SocketException | UnknownHostException ex) {
                        log.error(ex.getMessage());
                        return null;
                    }
                    return vdes1000Advertiser;
                })
                .collect(Collectors.toList());
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        Optional.ofNullable(this.vdes1000Advertisers)
                .orElse(Collections.emptyList())
                .forEach(Vdes1000Advertiser::destroy);
    }

    /**
     * Whenever we get changes in the stations' configuration, we will need
     * to reload the VDES-1000 listeners and advertisers for each station,
     * so basically we need to call the init function again.
     */
    public void reload() {
        // Same as destroy - but set a flag to be safe
        this.reloading = true;
        this.destroy();
        this.reloading = false;

        // And re-initialise the service
        this.init();
    }

    /**
     * This is a scheduled task performed by the service. The fixed delay
     * scheduler is used to execute the tasks at a specific time. The
     * advertisement tasks run on asynchronous separate threads so there isn't
     * really a reason to make sure the previous run has been completed before
     * proceeding. Also, since the VDES-1000 basic operation is used, we should
     * control the periodic transmissions manually.
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 1000)
    public void advertiseAtons() {
        // Protection against advertisements while reloading
        if(reloading) {
            return;
        }
        // Otherwise, let the advertisers do their job
        Optional.ofNullable(this.vdes1000Advertisers)
                .orElse(Collections.emptyList())
                .forEach(Vdes1000Advertiser::advertiseAtons);
    }

}
