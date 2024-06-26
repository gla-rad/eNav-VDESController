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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import org.grad.eNav.vdesCtrl.utils.GeometryJSONDeserializer;
import org.grad.eNav.vdesCtrl.utils.GeometryJSONSerializer;
import org.grad.vdes1000.formats.generic.AISChannelPref;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.locationtech.jts.geom.Geometry;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Set;

/**
 * The type Station.
 */
@SuppressWarnings("LombokSetterMayBeUsed")
@Entity
@Table(name = "station")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Station implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_generator")
    @SequenceGenerator(name="station_generator", sequenceName = "station_seq", allocationSize=1)
    private BigInteger id;

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "ipAddress", nullable = false)
    private String ipAddress;

    @NotNull
    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "broadcastPort")
    private Integer broadcastPort;

    @Column(name = "fwdIpAddress")
    private String fwdIpAddress;

    @Column(name = "fwdPort")
    private Integer fwdPort;

    @NotNull
    @Column(name = "mmsi", nullable = false)
    private String mmsi;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "varchar(30) default 'VDES-1000'")
    private StationType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", columnDefinition = "varchar(4) default 'A'")
    private AISChannelPref channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "signatureMode", columnDefinition = "varchar(30) default 'NONE'")
    private SignatureMode signatureMode;

    @JsonSerialize(using = GeometryJSONSerializer.class)
    @JsonDeserialize(using = GeometryJSONDeserializer.class)
    @Column(name = "geometry")
    private Geometry geometry;

    @ElementCollection
    private Set<String> blacklistedUids;

    /**
     * Instantiates a new Station.
     */
    public Station() {
        // Empty constructor
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(BigInteger id) {
        this.id = id;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets ip address.
     *
     * @return the ip address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets ip address.
     *
     * @param ipAddress the ip address
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets port.
     *
     * @param port the port
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Gets broadcast port.
     *
     * @return the broadcast port
     */
    public Integer getBroadcastPort() {
        return broadcastPort;
    }

    /**
     * Sets broadcast port.
     *
     * @param broadcastPort the broadcast port
     */
    public void setBroadcastPort(Integer broadcastPort) {
        this.broadcastPort = broadcastPort;
    }

    /**
     * Gets fwd ip address.
     *
     * @return the fwd ip address
     */
    public String getFwdIpAddress() {
        return fwdIpAddress;
    }

    /**
     * Sets fwd ip address.
     *
     * @param fwdIpAddress the fwd ip address
     */
    public void setFwdIpAddress(String fwdIpAddress) {
        this.fwdIpAddress = fwdIpAddress;
    }

    /**
     * Gets fwd port.
     *
     * @return the fwd port
     */
    public Integer getFwdPort() {
        return fwdPort;
    }

    /**
     * Sets fwd port.
     *
     * @param fwdPort the fwd port
     */
    public void setFwdPort(Integer fwdPort) {
        this.fwdPort = fwdPort;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public StationType getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(StationType type) {
        this.type = type;
    }

    /**
     * Gets channel.
     *
     * @return the channel
     */
    public AISChannelPref getChannel() {
        return channel;
    }

    /**
     * Sets channel.
     *
     * @param channel the channel
     */
    public void setChannel(AISChannelPref channel) {
        this.channel = channel;
    }

    /**
     * Gets signature mode.
     *
     * @return the signature mode
     */
    public SignatureMode getSignatureMode() {
        return signatureMode;
    }

    /**
     * Sets signature mode.
     *
     * @param signatureMode the signature mode
     */
    public void setSignatureMode(SignatureMode signatureMode) {
        this.signatureMode = signatureMode;
    }

    /**
     * Gets geometry.
     *
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Sets geometry.
     *
     * @param geometry the geometry
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Gets blacklisted uids.
     *
     * @return the blacklisted uids
     */
    public Set<String> getBlacklistedUids() {
        return blacklistedUids;
    }

    /**
     * Sets blacklisted uids.
     *
     * @param blacklistedUids the blacklisted uids
     */
    public void setBlacklistedUids(Set<String> blacklistedUids) {
        this.blacklistedUids = blacklistedUids;
    }

    /**
     * Gets mmsi.
     *
     * @return the mmsi
     */
    public String getMmsi() {
        return mmsi;
    }

    /**
     * Sets mmsi.
     *
     * @param mmsi the mmsi
     */
    public void setMmsi(String mmsi) {
        this.mmsi = mmsi;
    }

    /**
     * Overrides the equality operator of the class.
     *
     * @param o the object to check the equality
     * @return whether the two objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station station = (Station) o;
        return id.equals(station.id);
    }

    /**
     * Overrides the hashcode generation of the object.
     *
     * @return the generated hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
