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
 *
 */

package org.grad.eNav.vdesCtrl.models.vdes;

import org.grad.eNav.vdesCtrl.utils.StringBinUtils;

/**
 * The Abstract Message Class.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public abstract class AbstractMessage {

    // Class Variables
    protected Integer mmsi;

    /**
     * Instantiates a new Abstract message.
     */
    public AbstractMessage() {
        this.mmsi = null;
    }

    /**
     * Gets mmsi.
     *
     * @return the mmsi
     */
    public Integer getMmsi() {
        return mmsi;
    }

    /**
     * Sets mmsi.
     *
     * @param mmsi the mmsi
     */
    public void setMmsi(Integer mmsi) {
        this.mmsi = mmsi;
    }

    /**
     * Get binary message byte array.
     *
     * @param convertTo6Bit whether to generate the binary message using 6bit encoding
     * @return the binary message byte array
     */
    public byte[] getBinaryMessage(boolean convertTo6Bit) {
        return StringBinUtils.convertBinaryStringToBytes(this.getBinaryMessageString(), convertTo6Bit);
    }

    /**
     * Gets binary message string.
     *
     * @return the binary message string
     */
    public abstract String getBinaryMessageString();
}
