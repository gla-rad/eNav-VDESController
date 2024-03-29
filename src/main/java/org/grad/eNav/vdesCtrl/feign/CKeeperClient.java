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

package org.grad.eNav.vdesCtrl.feign;

import org.grad.eNav.vdesCtrl.config.FeignClientConfig;
import org.grad.eNav.vdesCtrl.models.domain.McpEntityType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * The Feign Interface For the CKeeper Client.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@FeignClient(name = "ckeeper", configuration = FeignClientConfig.class)
public interface CKeeperClient {

    @RequestMapping(method = RequestMethod.POST, value = "/api/signature/entity/generate/{entityName}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    byte[] generateEntitySignature(@PathVariable String entityName,
                                   @RequestParam("mmsi") String mmsi,
                                   @RequestParam("algorithm") String algorithm,
                                   @RequestParam("entityType") String entityType,
                                   @RequestBody byte[] signaturePayload);

}
