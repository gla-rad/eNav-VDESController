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

import lombok.extern.slf4j.Slf4j;
import org.geotools.data.DataStore;
import org.grad.eNav.vdesCtrl.components.S125GDSListener;
import org.grad.eNav.vdesCtrl.models.GeomesaS125;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The AtoN Geomesa Data Store Service Class
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Singleton
@Service
@Slf4j
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class S125GDSService {

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

    /**
     * The Geomesa Data Store.
     */
    @Autowired
    @Qualifier("gsDataStore")
    DataStore consumer;

    // Service Variables
    protected List<S125GDSListener> dsListeners;

    /**
     * Once the service has been initialised, we can that start the execution
     * of the kafka data store listeners as a separate components that will run
     * on independent threads. All incoming messages with then be consumed by
     * the same handler, but handled based on the topic.
     */
    @PostConstruct
    public void init() {
        log.info("Geomesa Data Store Service is booting up...");

        // Create the consumer
        if(this.consumer == null) {
            log.error("Unable to connect to data store");
            return;
        }

        // Get and initialise a the listener workers
        this.dsListeners = this.stationService.findAll()
                .stream()
                .map(station -> {
                    S125GDSListener dsListener = this.applicationContext.getBean(S125GDSListener.class);
                    try {
                        dsListener.init(this.consumer,
                                        new GeomesaS125(station.getGeometry()),
                                        station);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        return null;
                    }
                    return  dsListener;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("Geomesa Data Store is shutting down...");
        Optional.ofNullable(this.dsListeners).ifPresent(l -> l.forEach(S125GDSListener::destroy));
        this.consumer.dispose();
    }

    /**
     * Whenever we get changes in the stations configuration, we will need
     * to reload the S125GDSListeners for each stations, so basically we need
     * to call the init function again.
     */
    public void reload() {
        // Destroy all the previous listeners if they exist, to have a clean slate
        Optional.ofNullable(this.dsListeners).ifPresent(l -> l.forEach(S125GDSListener::destroy));

        // And re-initialise the service
        this.init();
    }

}
