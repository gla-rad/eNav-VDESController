/*
 * Copyright (c) 2021 Maritime Connectivity Platform Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.vdesCtrl.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * Utility class for HTTP headers creation.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class HeaderUtil {

    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

    /**
     * Create alert http headers.
     *
     * @param message the message
     * @param param   the param
     * @return the http headers
     */
    public static HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-vdesCtrl-alert", message);
        headers.add("X-vdesCtrl-params", param);
        return headers;
    }

    /**
     * Create failure alert http headers.
     *
     * @param entityName     the entity name
     * @param errorKey       the error key
     * @param defaultMessage the default message
     * @return the http headers
     */
    public static HttpHeaders createFailureAlert(String entityName, String errorKey, String defaultMessage) {
        log.error("Entity creation failed, {}", defaultMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-vdesCtrl-error", "error." + errorKey);
        headers.add("X-vdesCtrl-params", entityName);
        return headers;
    }

    /**
     * Create entity creation alert http headers.
     *
     * @param entityName the entity name
     * @param param      the param
     * @return the http headers
     */
    public static HttpHeaders createEntityCreationAlert(String entityName, String param) {
        return createAlert("vdesCtrl." + entityName + ".created", param);
    }

    /**
     * Create entity update alert http headers.
     *
     * @param entityName the entity name
     * @param param      the param
     * @return the http headers
     */
    public static HttpHeaders createEntityUpdateAlert(String entityName, String param) {
        return createAlert("vdesCtrl." + entityName + ".updated", param);
    }

    /**
     * Create entity deletion alert http headers.
     *
     * @param entityName the entity name
     * @param param      the param
     * @return the http headers
     */
    public static HttpHeaders createEntityDeletionAlert(String entityName, String param) {
        return createAlert("vdesCtrl." + entityName + ".deleted", param);
    }

}
