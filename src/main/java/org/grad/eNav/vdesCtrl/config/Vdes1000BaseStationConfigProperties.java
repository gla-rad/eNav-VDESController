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
import org.grad.vdes1000.formats.generic.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The VDES1000 Base Stations Configuration Properties Class.
 *
 * This class contains the configuration parameters that being picked up from
 * the configuration properties and used to setup the AIS base-stations
 * configuration of the VDES-1000 modules.
 * <p>
 * There is no reason why other devices cannot also be supported, but perhaps
 * a more generic set of configuration parameters is required./
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Component
@ConfigurationProperties(prefix = "gla.rad.vdes-ctrl.vdes-1000-advertiser.base-station")
public class Vdes1000BaseStationConfigProperties {

    // Class Variables
    private String uniqueId;
    private Integer rxChannelA;
    private Integer rxChannelB;
    private Integer txChannelA;
    private Integer txChannelB;
    private VHFChannelPower txPowerA;
    private VHFChannelPower txPowerB;
    private VdlMessageRetries vdlMessageRetries;
    private VdlMessageRepeatIndicator vdlMessageRepeatIndicator;
    private RATDMAControl ratdmaControl;
    private UTCSynchronisationSource utcSynchronisationSource;
    private Integer adsInterval;

    /**
     * Gets unique id.
     *
     * @return the unique id
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Sets unique id.
     *
     * @param uniqueId the unique id
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * Gets RX channel A.
     *
     * @return the RX channel A
     */
    public Integer getRxChannelA() {
        return rxChannelA;
    }

    /**
     * Sets RX channel A.
     *
     * @param rxChannelA the RX channel A
     */
    public void setRxChannelA(Integer rxChannelA) {
        this.rxChannelA = rxChannelA;
    }

    /**
     * Gets RX channel B.
     *
     * @return the RX channel B
     */
    public Integer getRxChannelB() {
        return rxChannelB;
    }

    /**
     * Sets RX channel B.
     *
     * @param rxChannelB the RX channel B
     */
    public void setRxChannelB(Integer rxChannelB) {
        this.rxChannelB = rxChannelB;
    }

    /**
     * Gets TX channel A.
     *
     * @return the TX channel A
     */
    public Integer getTxChannelA() {
        return txChannelA;
    }

    /**
     * Sets TX channel A.
     *
     * @param txChannelA the TX channel A
     */
    public void setTxChannelA(Integer txChannelA) {
        this.txChannelA = txChannelA;
    }

    /**
     * Gets TX channel B.
     *
     * @return the TX channel B
     */
    public Integer getTxChannelB() {
        return txChannelB;
    }

    /**
     * Sets TX channel B.
     *
     * @param txChannelB the TX channel B
     */
    public void setTxChannelB(Integer txChannelB) {
        this.txChannelB = txChannelB;
    }

    /**
     * Gets TX power A.
     *
     * @return the TX power A
     */
    public VHFChannelPower getTxPowerA() {
        return txPowerA;
    }

    /**
     * Sets TX power A.
     *
     * @param txPowerA the TX power A
     */
    public void setTxPowerA(VHFChannelPower txPowerA) {
        this.txPowerA = txPowerA;
    }

    /**
     * Gets TX power B.
     *
     * @return the TX power B
     */
    public VHFChannelPower getTxPowerB() {
        return txPowerB;
    }

    /**
     * Sets TX power B.
     *
     * @param txPowerB the TX power B
     */
    public void setTxPowerB(VHFChannelPower txPowerB) {
        this.txPowerB = txPowerB;
    }

    /**
     * Gets vdl message retries.
     *
     * @return the vdl message retries
     */
    public VdlMessageRetries getVdlMessageRetries() {
        return vdlMessageRetries;
    }

    /**
     * Sets vdl message retries.
     *
     * @param vdlMessageRetries the vdl message retries
     */
    public void setVdlMessageRetries(VdlMessageRetries vdlMessageRetries) {
        this.vdlMessageRetries = vdlMessageRetries;
    }

    /**
     * Gets vdl message repeat indicator.
     *
     * @return the vdl message repeat indicator
     */
    public VdlMessageRepeatIndicator getVdlMessageRepeatIndicator() {
        return vdlMessageRepeatIndicator;
    }

    /**
     * Sets vdl message repeat indicator.
     *
     * @param vdlMessageRepeatIndicator the vdl message repeat indicator
     */
    public void setVdlMessageRepeatIndicator(VdlMessageRepeatIndicator vdlMessageRepeatIndicator) {
        this.vdlMessageRepeatIndicator = vdlMessageRepeatIndicator;
    }

    /**
     * Gets ratdma control.
     *
     * @return the ratdma control
     */
    public RATDMAControl getRatdmaControl() {
        return ratdmaControl;
    }

    /**
     * Sets ratdma control.
     *
     * @param ratdmaControl the ratdma control
     */
    public void setRatdmaControl(RATDMAControl ratdmaControl) {
        this.ratdmaControl = ratdmaControl;
    }

    /**
     * Gets utc synchronisation source.
     *
     * @return the utc synchronisation source
     */
    public UTCSynchronisationSource getUtcSynchronisationSource() {
        return utcSynchronisationSource;
    }

    /**
     * Sets utc synchronisation source.
     *
     * @param utcSynchronisationSource the utc synchronisation source
     */
    public void setUtcSynchronisationSource(UTCSynchronisationSource utcSynchronisationSource) {
        this.utcSynchronisationSource = utcSynchronisationSource;
    }

    /**
     * Gets ads interval.
     *
     * @return the ads interval
     */
    public Integer getAdsInterval() {
        return adsInterval;
    }

    /**
     * Sets ads interval.
     *
     * @param adsInterval the ads interval
     */
    public void setAdsInterval(Integer adsInterval) {
        this.adsInterval = adsInterval;
    }

    /**
     * The key element of the configuration is the unique ID of the base station
     * and this function will make this check before returning the configuration.
     *
     * @return whether the current base station configuration is valid
     */
    public boolean isValid() {
        return Optional.ofNullable(this.uniqueId).isPresent();
    }

    /**
     * Translates this configuration into a proper VDES1000Lib AIS Base Station
     * configuration object. At this point in time the two are identical, so
     * the translation is quite straightforward.
     *
     *
     * @return the populated VDES1000 AIS Base Station configuration
     */
    public VDES1000BaseStationConfiguration getVdesBaseStationConfig() {
        // Create the configuration
        VDES1000BaseStationConfiguration config = new VDES1000BaseStationConfiguration();

        // Populate it
        config.setUniqueId(this.uniqueId);
        config.setRxChannelA(this.rxChannelA);
        config.setRxChannelB(this.rxChannelB);
        config.setTxChannelA(this.txChannelA);
        config.setTxChannelB(this.txChannelB);
        config.setTxPowerA(this.txPowerA);
        config.setTxPowerB(this.txPowerB);
        config.setVdlMessageRetries(this.vdlMessageRetries);
        config.setVdlMessageRepeatIndicator(this.vdlMessageRepeatIndicator);
        config.setRatdmaControl(this.ratdmaControl);
        config.setUtcSynchronisationSource(this.utcSynchronisationSource);
        config.setAdsInterval(this.adsInterval);

        // And return it
        return config;
    }

}
