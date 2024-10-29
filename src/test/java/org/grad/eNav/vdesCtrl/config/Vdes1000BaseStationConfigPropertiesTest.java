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

package org.grad.eNav.vdesCtrl.config;

import org.grad.vdes1000.comm.VDES1000BaseStationConfiguration;
import org.grad.vdes1000.formats.generic.RATDMAControl;
import org.grad.vdes1000.formats.generic.VHFChannelPower;
import org.grad.vdes1000.formats.generic.VdlMessageRepeatIndicator;
import org.grad.vdes1000.formats.generic.VdlMessageRetries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vdes1000BaseStationConfigPropertiesTest {

    //Test Variables
    Vdes1000BaseStationConfigProperties config;

    /**
     * Setup some base data.
     */
    @BeforeEach
    void setup() {
        this.config = new Vdes1000BaseStationConfigProperties();
    }

    /**
     * Test that the setter and getter operations are working properly for
     * the AIS Base-Station Configuration Class.
     */
    @Test
    public void testSettersGetters() {
        // Set the configuration parameters
        this.config.setUniqueId("uniqueId");
        this.config.setRxChannelA(2000);
        this.config.setRxChannelB(2001);
        this.config.setTxChannelA(2002);
        this.config.setTxChannelB(2003);
        this.config.setTxPowerA(VHFChannelPower.HIGH);
        this.config.setTxPowerB(VHFChannelPower.LOW);
        this.config.setVdlMessageRetries(VdlMessageRetries.NO_REBROADCAST);
        this.config.setVdlMessageRepeatIndicator(VdlMessageRepeatIndicator.DO_NOT_REPEAT);
        this.config.setRatdmaControl(RATDMAControl.OFF);
        this.config.setAdsInterval(100);

        // And now check the getters
        assertEquals("uniqueId", this.config.getUniqueId());
        assertEquals(2000, this.config.getRxChannelA());
        assertEquals(2001, this.config.getRxChannelB());
        assertEquals(2002, this.config.getTxChannelA());
        assertEquals(2003, this.config.getTxChannelB());
        assertEquals(VHFChannelPower.HIGH, this.config.getTxPowerA());
        assertEquals(VHFChannelPower.LOW, this.config.getTxPowerB());
        assertEquals(VdlMessageRetries.NO_REBROADCAST, this.config.getVdlMessageRetries());
        assertEquals(VdlMessageRepeatIndicator.DO_NOT_REPEAT, this.config.getVdlMessageRepeatIndicator());
        assertEquals(RATDMAControl.OFF, this.config.getRatdmaControl());
        assertEquals(100, this.config.getAdsInterval());
    }

    /**
     * Test that we can correctly translate the domain AIS Base Station
     * configuration supplied through the service application properties, into
     * a VDES1000Lib configuration compatible with the CML VDES-1000 stations.
     */
    @Test
    public void testGetVdesBaseStationConfig() {

        // Set the configuration parameters
        this.config.setUniqueId("uniqueId");
        this.config.setRxChannelA(2000);
        this.config.setRxChannelB(2001);
        this.config.setTxChannelA(2002);
        this.config.setTxChannelB(2003);
        this.config.setTxPowerA(VHFChannelPower.HIGH);
        this.config.setTxPowerB(VHFChannelPower.LOW);
        this.config.setVdlMessageRetries(VdlMessageRetries.NO_REBROADCAST);
        this.config.setVdlMessageRepeatIndicator(VdlMessageRepeatIndicator.DO_NOT_REPEAT);
        this.config.setRatdmaControl(RATDMAControl.OFF);
        this.config.setAdsInterval(100);

        // Get the VDES1000Lib configuration object
        VDES1000BaseStationConfiguration vdesConfig = this.config.getVdesBaseStationConfig();

        // And now check the getters
        assertEquals("uniqueId", vdesConfig.getUniqueId());
        assertEquals(2000, vdesConfig.getRxChannelA());
        assertEquals(2001, vdesConfig.getRxChannelB());
        assertEquals(2002, vdesConfig.getTxChannelA());
        assertEquals(2003, vdesConfig.getTxChannelB());
        assertEquals(VHFChannelPower.HIGH, vdesConfig.getTxPowerA());
        assertEquals(VHFChannelPower.LOW, vdesConfig.getTxPowerB());
        assertEquals(VdlMessageRetries.NO_REBROADCAST, vdesConfig.getVdlMessageRetries());
        assertEquals(VdlMessageRepeatIndicator.DO_NOT_REPEAT, vdesConfig.getVdlMessageRepeatIndicator());
        assertEquals(RATDMAControl.OFF, vdesConfig.getRatdmaControl());
        assertEquals(100, vdesConfig.getAdsInterval());
    }

}