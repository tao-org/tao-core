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
package ro.cs.tao.persistence.convert;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Converter for <code>List&lt;String&gt;</code> stored values
 *
 * @author Oana H.
 */
public class ConstraintsConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        String result = null;

        if (attribute != null && !attribute.isEmpty()) {
            result = "";
            for (String constraint : attribute) {
                result = result.concat(constraint).concat(";");
            }
            result = result.substring(0, result.lastIndexOf(";"));
        }

        return result;
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        List<String> result = null;

        if (!StringUtils.isEmpty(dbData)) {
            result = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(dbData, ";");
            while (st.hasMoreElements()) {
                result.add(st.nextToken());
            }
        }

        return result;
    }
}
