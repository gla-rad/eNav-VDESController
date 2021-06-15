/*
 * Copyright (c) 2021 GLA UK Research and Development Directive
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.grad.eNav.vdesCtrl.utils.GeometryJSONDeserializer;
import org.grad.eNav.vdesCtrl.utils.GeometryJSONSerializer;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The type Station.
 */
@Entity
@Table(name = "station")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Station implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @NotNull
    @Column(name = "piSeqNo", nullable = false, columnDefinition = "bigint default 0")
    private Long piSeqNo;

    @NotNull
    @Column(name = "mmsi", nullable = false)
    private String mmsi;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "varchar(30) default 'VDES-1000'")
    private StationType type;

    @JsonSerialize(using = GeometryJSONSerializer.class)
    @JsonDeserialize(using = GeometryJSONDeserializer.class)
    @Column(name = "geometry")
    private Geometry geometry;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "station_nodes",
            joinColumns = @JoinColumn(name="station_id", referencedColumnName="ID"),
            inverseJoinColumns = @JoinColumn(name="node_id", referencedColumnName="ID"))
    private Set<SNode> nodes = new HashSet<>();

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
     * Gets pi seq no.
     *
     * @return the pi seq no
     */
    public Long getPiSeqNo() {
        return piSeqNo;
    }

    /**
     * Sets pi seq no.
     *
     * @param piSeqNo the pi seq no
     */
    public void setPiSeqNo(Long piSeqNo) {
        this.piSeqNo = piSeqNo;
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
     * Gets nodes.
     *
     * @return the nodes
     */
    public Set<SNode> getNodes() {
        return nodes;
    }

    /**
     * Sets nodes.
     *
     * @param nodes the nodes
     */
    public void setNodes(Set<SNode> nodes) {
        this.nodes = nodes;
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