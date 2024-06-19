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

package org.grad.eNav.vdesCtrl.models.dtos;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * The S125 Node Class.
 *
 * This node extends the S-100 abstract node to implement the S-125 messages
 * including the AtoN Number value.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S125Node extends S100AbstractNode {

    // Class Variables
    private String idCode;

    /**
     * Empty Constructor
     */
    public S125Node() {

    }

    /**
     * The Fully Populated  Constructor.
     *
     * @param idCode        The AtoN ID code
     * @param geometry      The object geometry
     * @param content       The XML content
     */
    public S125Node(String idCode, JsonNode geometry, String content) {
        super(geometry, content);
        this.idCode = idCode;
    }

    /**
     * Gets AtoN ID code.
     *
     * @return the AtoN ID code
     */
    public String getIdCode() {
        return idCode;
    }

    /**
     * Sets the AtoN ID code.
     *
     * @param idCode the AtoN ID code
     */
    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S125Node)) return false;
        if (!super.equals(o)) return false;
        S125Node s125Node = (S125Node) o;
        return Objects.equals(idCode, s125Node.idCode);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idCode);
    }

}
