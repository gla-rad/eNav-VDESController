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

package org.grad.eNav.vdesCtrl.models;

/**
 * The PubSubMsgHeaders Enum.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public enum PubSubMsgHeaders {
    ADDRESS("ADDRESS"),
    PORT("PORT"),
    PI_SEQ_NO("PI_SEQ_NO"),
    MMSI("MMSI");

    // Enum Variables
    private String header;

    /**
     * The PubSubMsgHeaders Enum Constructor.
     *
     * @param header        The PubSub Message header
     */
    PubSubMsgHeaders(String header) {
        this.header = header;
    }

    /**
     * Gets the PubSub Messages header.
     *
     * @return The PubSub Messages heade
     */
    public String getHeader() {
        return header;
    }
}
