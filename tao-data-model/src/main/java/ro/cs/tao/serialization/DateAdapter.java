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
package ro.cs.tao.serialization;

import ro.cs.tao.utils.DateUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class DateAdapter extends XmlAdapter<String, LocalDateTime> {
    protected Logger logger = Logger.getLogger(getClass().getName());
    private final DateTimeFormatter dateFormat = DateUtils.getFormatterAtUTC("yyyy-MM-dd'T'HH:mm:ss");
    private final DateTimeFormatter dateFormatZ = DateUtils.getFormatterAtUTC("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public LocalDateTime unmarshal(String v) throws Exception {
        LocalDateTime result = null;
        try {
            result = DateUtils.parseDateTime(v);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        return result;
    }

    @Override
    public String marshal(LocalDateTime v) throws Exception {
        return dateFormat.format(v);
    }
}
