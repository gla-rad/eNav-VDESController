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

package org.grad.eNav.vdesCtrl.config;

import org.grad.eNav.vdesCtrl.components.PubSubErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;

/**
 * The PubSubChannelConfig Class
 *
 * This class establishes a publish-subscribe channel when the internal
 * components of this application can exchange data messages. This will
 * be used to transfer the incoming radar data to various consumers.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
@EnableIntegration
public class PubSubChannelConfig {

    /**
     * Defining a publish subscribe Spring Integration channel to exchange
     * the incoming AtoN data between the application components.
     *
     * @return The publish subscribe message channel
     */
    @Bean
    public PublishSubscribeChannel atonPublishChannel() {
        PublishSubscribeChannel pubsubChannel = new PublishSubscribeChannel();
        pubsubChannel.setErrorHandler(new PubSubErrorHandler());
        return pubsubChannel;
    }

}
