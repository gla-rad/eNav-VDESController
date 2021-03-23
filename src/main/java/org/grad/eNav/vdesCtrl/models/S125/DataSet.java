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
    Member[] members;
    IMember[] imembers;

    /**
     * Sets new members.
     *
     * @param members New value of members.
     */
    @XmlElement(name = "member")
    public void setMembers(Member[] members) {
        this.members = members;
    }

    /**
     * Gets members.
     *
     * @return Value of members.
     */
    public Member[] getMembers() {
        return members;
    }

    /**
     * Sets new iMembers.
     *
     * @param imembers New value of iMembers.
     */
    @XmlElement(name = "imember")
    public void setIMember(IMember[] imembers) {
        this.imembers = imembers;
    }

    /**
     * Gets iMembers.
     *
     * @return Value of iMembers.
     */
    public IMember[] getIMembers() {
        return imembers;
    }


}
