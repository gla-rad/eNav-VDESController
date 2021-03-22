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

package org.grad.eNav.vdesCtrl.models.S125;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * The S125 DataSet Class
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@XmlRootElement
public class DataSet {

    // Dataset Members
    @XmlElement
    List<Member> members;
    @XmlElement
    List<IMember> iMembers;

    /**
     * Sets new members.
     *
     * @param members New value of members.
     */
    public void setMembers(List<Member> members) {
        this.members = members;
    }

    /**
     * Sets new iMembers.
     *
     * @param iMembers New value of iMembers.
     */
    public void setIMembers(List<IMember> iMembers) {
        this.iMembers = iMembers;
    }

    /**
     * Gets iMembers.
     *
     * @return Value of iMembers.
     */
    public List<IMember> getIMembers() {
        return iMembers;
    }

    /**
     * Gets members.
     *
     * @return Value of members.
     */
    public List<Member> getMembers() {
        return members;
    }
}
