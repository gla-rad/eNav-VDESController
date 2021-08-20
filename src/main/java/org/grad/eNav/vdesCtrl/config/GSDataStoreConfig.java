/*
 * Copyright (c) 2021 GLA Research and Development Directorate
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

package org.grad.eNav.vdesCtrl.config;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The Geomesa Data Store Configuration
 *
 * A simple configuration that allows connections to the Geomesa Data Store.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
@ConditionalOnProperty({"kafka.zookeepers", "kafka.brokers"})
public class GSDataStoreConfig {

    /**
     * The Kafka Zookeepers addresses.
     */
    @Value("${kafka.zookeepers:localhost:2181}" )
    private String kafkaZookeepers;

    /**
     * The Kafka Brokers addresses.
     */
    @Value("${kafka.brokers:localhost:9092}" )
    private String kafkaBrokers;

    /**
     * The Number of Kafka Consumers.
     */
    @Value("${kafka.consumer.count:1}" )
    private Integer noKafkaConsumers;

    /**
     * Returns the Geomesa Data Store as a bean so that we can easily call it
     * anywhere we want.
     *
     * @return the Geomesa Data Store constructed according to the configuration parameters
     */
    @Bean
    DataStore gsDataStore() {
        // The the connection parameters
        Map<String, String> params = new HashMap<>();
        params.put("kafka.brokers", kafkaBrokers);
        params.put("kafka.zookeepers", kafkaZookeepers);
        params.put("kafka.consumer.count", Objects.toString(noKafkaConsumers));

        // And construct the data store
        try {
            return DataStoreFinder.getDataStore(params);
        } catch (IOException ex) {
            return null;
        }
    }
}
