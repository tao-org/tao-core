/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.eodata.AuxiliaryData;

import java.util.List;
import java.util.Set;

@Repository
@Qualifier(value = "auxDataRepository")
@Transactional
public interface AuxDataRepository extends PagingAndSortingRepository<AuxiliaryData, String> {

    AuxiliaryData findByLocation(String location);

    @Query(value = "SELECT * FROM tao.auxiliary_data WHERE username = :userName ORDER BY location",
            nativeQuery = true)
    List<AuxiliaryData> getAuxiliaryDataByUser(@Param("userName") String userName);

    @Query(value = "SELECT * FROM tao.auxiliary_data WHERE username = :userName AND location IN (:locations) " +
            "ORDER BY location",
            nativeQuery = true)
    List<AuxiliaryData> getAuxiliaryDataByLocation(@Param("userName") String userName,
                                                   @Param("locations") Set<String> locations);
}