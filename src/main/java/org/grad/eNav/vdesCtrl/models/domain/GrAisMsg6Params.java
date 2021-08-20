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

package org.grad.eNav.vdesCtrl.models.domain;

/**
 * The GR-AIS Message 6 Parameters Class.
 * <p>
 * This class contains all the required parameters for generating an AIS binary
 * message through the GrAisUtils class function.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class GrAisMsg6Params {

    // Class Variables
    private Integer mmsi;
    private Integer destMmsi;
    private byte[] message;

    /**
     * Empty Constructor.
     */
    public GrAisMsg6Params() {
        this.mmsi = null;
        this.destMmsi = null;
        this.message = new byte[0];
    }

    /**
     * Instantiates a new GGR-AIS Message 6 Parameters class with arguments.
     *
     * @param mmsi     the mmsi
     * @param destMmsi the dest mmsi
     * @param message  the message
     */
    public GrAisMsg6Params(Integer mmsi, Integer destMmsi, byte[] message) {
        this.mmsi = mmsi;
        this.destMmsi = destMmsi;
        this.message = message;
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
     * Gets dest mmsi.
     *
     * @return the dest mmsi
     */
    public Integer getDestMmsi() {
        return destMmsi;
    }

    /**
     * Sets dest mmsi.
     *
     * @param destMmsi the dest mmsi
     */
    public void setDestMmsi(Integer destMmsi) {
        this.destMmsi = destMmsi;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(byte[] message) {
        this.message = message;
    }
}
