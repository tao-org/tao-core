/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.datasource.remote.scihub.helpers;

import ro.cs.tao.datasource.remote.ProductHelper;

import java.util.regex.Pattern;

/**
 * @author Cosmin Cara
 */
public class Sentinel1ProductHelper extends ProductHelper {

    private static final Pattern S1Pattern =
            Pattern.compile("(S1[A-B])_(SM|IW|EW|WV)_(SLC_|GRDH|RAW_|OCN_)_([0-9A-Z]{4})_(\\d{8}T\\d{6})_(\\d{8}T\\d{6})_(\\d{6})_([0-9A-F]{6})_([0-9A-F]{4})(?:.SAFE)?");

    @Override
    public String getProductRelativePath() {
        return null;
    }

    @Override
    protected boolean verifyProductName(String name) {
        return S1Pattern.matcher(name).matches();
    }
}
