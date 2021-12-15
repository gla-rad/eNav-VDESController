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

package org.grad.eNav.vdesCtrl.models.domain;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * The AIS Channel Enum.
 * <p>
 * AIS sentences supports two channels, A and B.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public enum AISChannel {
    A("A"),
    B("B"),
    NONE("NONE"),
    BOTH("BOTH");

    // Enum Variables
    private String channel;
    private int index;

    /**
     * The NMEA Channel Constructor.
     *
     * @param channel the NMEA channel
     */
    AISChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Gets channel.
     *
     * @return the channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets channel.
     *
     * @param channel the channel
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Returns an integer representation of the AIS Channel selection that
     * includes the NONE and BOTH values.
     *
     * @return the integer representation of the AIS channel selection
     */
    public int getIndex() {
        switch (this.getChannel()) {
            case "A":
                return 1;
            case "B":
                return 2;
            case "BOTH":
                return 3;
            default:
                return 0;
        }
    }
}
