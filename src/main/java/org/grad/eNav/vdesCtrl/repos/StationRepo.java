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

package org.grad.eNav.vdesCtrl.repos;

import org.grad.eNav.vdesCtrl.models.domain.SNode;
import org.grad.eNav.vdesCtrl.models.domain.Station;
import org.grad.eNav.vdesCtrl.models.domain.StationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

/**
 * Spring Data JPA repository for the Station entity.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public interface StationRepo  extends JpaRepository<Station, BigInteger> {

    /**
     * Find all stations of a specific type
     *
     * @return the list of stations of that type
     */
    List<Station> findByType(StationType stationType);

    /**
     * Find one with eager relationships design.
     *
     * @param id The id
     * @return the matching station
     */
    @Query("select distinct station " +
            " from Station station " +
            " left join fetch station.nodes " +
            " where station.id =:id")
    Station findOneWithEagerRelationships(@Param("id") BigInteger id);

}
