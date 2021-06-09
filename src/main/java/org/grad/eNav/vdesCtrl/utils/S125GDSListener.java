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

package org.grad.eNav.vdesCtrl.utils;

import lombok.extern.slf4j.Slf4j;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.simple.SimpleFeatureSource;
import org.grad.eNav.vdesCtrl.models.GeomesaData;
import org.grad.eNav.vdesCtrl.models.GeomesaS125;
import org.grad.eNav.vdesCtrl.models.PubSubMsgHeaders;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.grad.eNav.vdesCtrl.models.domain.SNode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.services.SNodeService;
import org.grad.eNav.vdesCtrl.services.StationService;
import org.locationtech.geomesa.kafka.utils.KafkaFeatureEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The AtoN Geomesa Data Store Listener Class
 *
 * This class defines the main operation of the AtoN listening operation
 * on the Geomesa Kafka Data Store.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Slf4j
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class S125GDSListener {

    /**
     * The AtoN Data Channel to publish the incoming data to.
     */
    @Autowired
    @Qualifier("atonPublishChannel")
    private PublishSubscribeChannel atonPublishChannel;

    /**
     * The Station Service.
     */
    @Autowired
    StationService stationService;

    /**
     * The SNode Service.
     */
    @Autowired
    SNodeService sNodeService;

    // Component Variables
    private DataStore consumer;
    private FeatureListener listener;
    private GeomesaData geomesaData;
    private Station station;
    private List<Double> listenerArea;
    private SimpleFeatureSource featureSource;
    private String dataChannelTopic;

    /**
     * Once the listener has been initialised, it will create a consumer of
     * the data store provided and publish the incoming messages into the
     * AtoN data channel.
     *
     * @param consumer      The data store to consume the messages from
     */
    public void init(DataStore consumer,
                     GeomesaData geomesaData,
                     Station station) throws IOException {
        this.consumer = consumer;
        this.geomesaData = geomesaData;
        this.station = station;
        this.dataChannelTopic = String.format("%s:%d", station.getIpAddress(), station.getPort());
        this.listenerArea = Optional.ofNullable(listenerArea).orElse(Collections.emptyList());
        this.listener = (this::listenToEvents);

        // And add the feature listener to start reading
        this.featureSource = this.consumer.getFeatureSource(this.geomesaData.getTypeName());
        this.featureSource.addFeatureListener(listener);

        // Log an information message
        log.info(String.format("Initialised AtoN listener for VDES at %s:%d for area: %s",
                station.getIpAddress(),
                station.getPort(),
                this.listenerArea.stream().map(String::valueOf).collect(Collectors.joining(","))));
    }

    /**
     * When shutting down the application we need to make sure that all
     * threads have been gracefully shutdown as well.
     */
    @PreDestroy
    public void destroy() {
        log.info("AtoN Data Listener is shutting down...");
        this.featureSource.removeFeatureListener(this.listener);
    }

    /**
     * The main data store listener operation where events are being handled.
     *
     * @param featureEvent      The feature event that took place
     */
    private void listenToEvents(FeatureEvent featureEvent) {
        // We are only interested in Kafka Feature Messages, otherwise don't bother
        if(!(featureEvent instanceof  KafkaFeatureEvent)) {
            return;
        }

        // For feature additions/changes
        if (featureEvent.getType() == FeatureEvent.Type.CHANGED) {
            // Extract the S-125 message and send it
            Optional.of(featureEvent)
                    .filter(KafkaFeatureEvent.KafkaFeatureChanged.class::isInstance)
                    .map(KafkaFeatureEvent.KafkaFeatureChanged.class::cast)
                    .map(KafkaFeatureEvent.KafkaFeatureChanged::feature)
                    .filter(this.geomesaData.getSubsetFilter()::evaluate)
                    .map(Collections::singletonList)
                    .map(sl -> new GeomesaS125().retrieveData(sl))
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(this::saveSNode)
                    .map(MessageBuilder::withPayload)
                    .map(builder -> builder.setHeader(MessageHeaders.CONTENT_TYPE, this.dataChannelTopic))
                    .map(builder -> builder.setHeader(PubSubMsgHeaders.ADDRESS.getHeader(), station.getIpAddress()))
                    .map(builder -> builder.setHeader(PubSubMsgHeaders.PORT.getHeader(), station.getPort()))
                    .map(builder -> builder.setHeader(PubSubMsgHeaders.PI_SEQ_NO.getHeader(), station.getPiSeqNo()))
                    .map(builder -> builder.setHeader(PubSubMsgHeaders.MMSI.getHeader(), station.getMmsi()))
                    .map(MessageBuilder::build)
                    .forEach(this.atonPublishChannel::send);
        }
        // For feature deletions,
        else if (featureEvent.getType() == FeatureEvent.Type.REMOVED) {
            // Extract the S-125 message and just log it
            Optional.of(featureEvent)
                    .filter(KafkaFeatureEvent.KafkaFeatureChanged.class::isInstance)
                    .map(KafkaFeatureEvent.KafkaFeatureChanged.class::cast)
                    .map(KafkaFeatureEvent.KafkaFeatureChanged::feature)
                    .filter(this.geomesaData.getSubsetFilter()::evaluate)
                    .map(Collections::singletonList)
                    .map(sl -> new GeomesaS125().retrieveData(sl))
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(S125Node::getAtonUID)
                    .forEach(uid -> log.info("Received Delete for AtoN: " + uid));
        }
    }

    /**
     * A helper that processes the S125Node entry provided and stored it in the
     * database for future reference.
     *
     * @param s125Node  the S125Node to be saved
     */
    @Transactional
    private S125Node saveSNode(S125Node s125Node){
        // Create a new SNode entry
        SNode sNode =Optional.ofNullable(this.sNodeService.findOneByUid(s125Node.getAtonUID())).orElseGet(() -> new SNode(s125Node));
        sNode.setMessage(s125Node.getContent());

        // Save the SNode
        this.sNodeService.save(sNode);

        // Save the new SNode for the station
        Station station = this.stationService.findOne(this.station.getId());
        station.getNodes().add(sNode);
        this.stationService.save(station);

        // And return to continue a possible stream?
        return s125Node;
    }

}
