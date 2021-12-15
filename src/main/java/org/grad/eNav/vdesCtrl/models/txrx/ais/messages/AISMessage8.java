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

package org.grad.eNav.vdesCtrl.models.txrx.ais.messages;

import org.grad.eNav.vdesCtrl.models.txrx.AbstractMessage;
import org.grad.eNav.vdesCtrl.utils.GrAisUtils;

/**
 * The GR-AIS Message 8 Parameters Class.
 * <p>
 * This class contains all the required parameters for generating an AIS binary
 * message through the GrAisUtils class function.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class AISMessage8 extends AbstractMessage  {

    // Class Variables
    private byte[] message;

    /**
     * Empty Constructor.
     */
    public AISMessage8() {
        super();
        this.message = new byte[0];
    }

    /**
     * Instantiates a  new GR-AIS Message 8 Parameters with arguments.
     *
     * @param mmsi    the mmsi
     * @param message the message
     */
    public AISMessage8(Integer mmsi, byte[] message) {
        super();
        this.mmsi = mmsi;
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

    /**
     * Gets binary message.
     *
     * @return the binary message
     */
    @Override
    public String getBinaryMessageString() {
        return GrAisUtils.encodeMsg8(this);
    }
}