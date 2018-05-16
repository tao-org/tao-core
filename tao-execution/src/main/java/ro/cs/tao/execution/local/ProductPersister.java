/*
 * Copyright (C) 2017 CS ROMANIA
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

package ro.cs.tao.execution.local;

import ro.cs.tao.eodata.DataHandlingException;
import ro.cs.tao.eodata.EODataHandler;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.eodata.util.Conversions;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.Logger;

public class ProductPersister implements EODataHandler<EOProduct> {
    private final PersistenceManager persistenceManager = SpringContextBridge.services().getPersistenceManager();
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public Class<EOProduct> isIntendedFor() { return EOProduct.class; }

    @Override
    public int getPriority() { return 0; }

    @Override
    public List<EOProduct> handle(List<EOProduct> list) throws DataHandlingException {
        String utmCode = null;
        for (EOProduct product : list) {
            try {
                utmCode = product.getAttributeValue("utmCode");
                if (utmCode != null) {
                    Polygon2D footprint = Polygon2D.fromWKT(product.getGeometry());
                    if (footprint != null) {
                        Polygon2D footprintUTM = new Polygon2D();
                        List<Point2D> points = footprint.getPoints();
                        for (Point2D point : points) {
                            double[] values = Conversions.utmToDegrees(product.getCrs(), point.getX(), point.getY());
                            footprintUTM.append(values[0], values[1]);
                        }
                        product.setGeometry(footprintUTM.toWKT());
                    }
                }
                product = persistenceManager.saveEOProduct(product);
            } catch (Exception e) {
                logger.severe(String.format("Product %s could not be written to database: %s",
                                            product.getName(), e.getMessage()));
            }
        }
        return list;
    }
}
