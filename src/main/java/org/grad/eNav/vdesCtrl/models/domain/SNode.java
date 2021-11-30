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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.grad.eNav.vdesCtrl.models.dtos.S124Node;
import org.grad.eNav.vdesCtrl.models.dtos.S125Node;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The Station Node Class
 * <p></p>
 * This class defines the database structure of the station node entries. These
 * are any types of S-100/S-200 IALA product specification objects that are
 * attributed to a station at the current time. It can also be used to bootstrap
 * the station, when no previous messages has been received.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Entity
@Table(name = "node")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SNode {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;

    @NotNull
    @Column(name = "uid", unique = true)
    private String uid;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "varchar(30) default 'S_125'")
    private SNodeType type;

    @NotNull
    @Type(type="text")
    @Column(name = "message")
    private String message;

    @ManyToMany(mappedBy = "nodes", fetch = FetchType.LAZY)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Station> stations = new HashSet<>();

    /**
     * Instantiates a new Station Node.
     */
    public SNode() {
    }

    /**
     * Instantiates a new Station Node using an S124 object.
     *
     * @param s124Node the s124 node object
     */
    public SNode(S124Node s124Node) {
        this.uid = s124Node.getMessageId();
        this.type = SNodeType.S124;
        this.message = s124Node.getContent();
    }

    /**
     * Instantiates a new Station Node using an S125 object.
     *
     * @param s125Node the s125 node object
     */
    public SNode(S125Node s125Node) {
        this.uid = s125Node.getAtonUID();
        this.type = SNodeType.S125;
        this.message = s125Node.getContent();
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
     * Gets uid.
     *
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets uid.
     *
     * @param uid the uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public SNodeType getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(SNodeType type) {
        this.type = type;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets stations.
     *
     * @return the stations
     */
    public Set<Station> getStations() {
        return stations;
    }

    /**
     * Sets stations.
     *
     * @param stations the stations
     */
    public void setStations(Set<Station> stations) {
        this.stations = stations;
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
        if (!(o instanceof SNode)) return false;
        SNode SNode = (SNode) o;
        return Objects.equals(id, SNode.id);
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
