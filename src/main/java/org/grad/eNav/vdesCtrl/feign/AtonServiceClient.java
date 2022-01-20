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

package org.grad.eNav.vdesCtrl.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.grad.eNav.vdesCtrl.config.FeignClientConfig;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Feign Interface For the AtoN Service Client.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@FeignClient(name = "aton-service", configuration = FeignClientConfig.class)
public interface AtonServiceClient {

    @RequestMapping(method = RequestMethod.GET, value = "/api/messages")
    Page<S125Node> getMessagesForGeometry(@RequestParam("geometry") JsonNode geometry);

}
