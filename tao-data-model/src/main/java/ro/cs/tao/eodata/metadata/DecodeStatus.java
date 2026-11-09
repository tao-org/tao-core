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

package ro.cs.tao.eodata.metadata;

/**
 * Represents the status of a decoding process.
 * This enum is used to indicate the result or applicability
 * of decoding an entity or data.
 *
 * The possible statuses include:
 * - INTENDED: Indicates that the decoding process is intended or expected to occur.
 * - SUITABLE: Denotes that the decoding process is suitable and valid for the entity.
 * - UNABLE: Suggests that decoding cannot be performed or is not applicable.
 */
public enum DecodeStatus {
    INTENDED,
    SUITABLE,
    UNABLE
}
