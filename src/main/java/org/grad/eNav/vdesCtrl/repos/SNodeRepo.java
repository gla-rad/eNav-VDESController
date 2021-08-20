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

package org.grad.eNav.vdesCtrl.repos;

import org.grad.eNav.vdesCtrl.models.domain.SNode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

/**
 * Spring Data JPA repository for the SNode entity.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public interface SNodeRepo extends JpaRepository<SNode, BigInteger> {

    /**
     * Find all with eager relationships list.
     *
     * @return The complete station list
     */
    SNode findByUid(String uid);

    /**
     * Find one with eager relationships design.
     *
     * @param id The id
     * @return The matching station
     */
    @Query("select distinct snode " +
            " from SNode snode " +
            " left join fetch snode.stations " +
            " where snode.id = :id")
    SNode findOneWithEagerRelationships(@Param("id") BigInteger id);

}
