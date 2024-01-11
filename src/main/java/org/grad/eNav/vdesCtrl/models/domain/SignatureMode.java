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
package org.grad.eNav.vdesCtrl.models.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * The Signature Mode Enum.
 * <p/>
 * The enumeration that describes the supported mode in which the advertised
 * messages can provide an associated signature message. This is usually
 * provided through the cKeeper microservice.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public enum SignatureMode {
    NONE("NONE"),
    AIS("AIS"),
    VDE("VDE");

    // Enum Variables
    private final String value;

    /**
     * Enum Constructor
     *
     * @param value the enum value
     */
    SignatureMode(final String value) {
        this.value = value;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    @JsonValue
    public String getValue() { return value; }

    /**
     * Find the enum entry that corresponds to the provided value.
     *
     * @param value the enum value
     * @return The respective enum entry
     */
    public static SignatureMode fromValue(String value) {
        return Arrays.stream(SignatureMode.values())
                .filter(t -> t.getValue().compareTo(value)==0)
                .findFirst()
                .orElse(null);
    }

}
