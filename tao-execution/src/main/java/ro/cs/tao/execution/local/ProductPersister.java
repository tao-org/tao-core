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

package ro.cs.tao.execution.local;

import ro.cs.tao.eodata.DataHandlingException;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.OutputDataHandler;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.eodata.enums.ProductStatus;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.eodata.util.Conversions;
import ro.cs.tao.persistence.EOProductProvider;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.FileUtilities;

import java.awt.geom.Point2D;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * Specialization of an output data handler that persists EOProducts to the database.
 *
 * @author Cosmin Cara
 */
public class ProductPersister implements OutputDataHandler<EOProduct> {
        private final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public Class<EOProduct> isIntendedFor() { return EOProduct.class; }

    @Override
    public int getPriority() { return 0; }

    @Override
    public List<EOProduct> handle(List<EOProduct> list) throws DataHandlingException {
        String utmCode;
        for (EOProduct product : list) {
            try {
                utmCode = product.getAttributeValue("utmCode");
                if (utmCode != null) {
                    Polygon2D footprint = Polygon2D.fromWKT(product.getGeometry());
                    Polygon2D footprintUTM = new Polygon2D();
                    List<Point2D> points = footprint.getPoints();
                    for (Point2D point : points) {
                        double[] values = Conversions.utmToDegrees(product.getCrs(), point.getX(), point.getY());
                        footprintUTM.append(values[0], values[1]);
                    }
                    product.setGeometry(footprintUTM.toWKT());
                }
                product.setVisibility(Visibility.PRIVATE);
                product.setProductStatus(ProductStatus.PRODUCED);
                product.addReference(SessionStore.currentContext().getPrincipal().getName());
                product = SpringContextBridge.services().getService(EOProductProvider.class).save(product);
                final String location = product.getLocation();
                Path productPath = FileUtilities.isURI(location) ? Path.of(URI.create(location)) : Path.of(location);
                final String entryPoint = product.getEntryPoint();
                if (entryPoint != null) {
                    productPath = productPath.resolve(entryPoint);
                }
                logger.fine("Computing product SHA-1 hash");
                Files.writeString(productPath.getParent().resolve(FileUtilities.getFilenameWithoutExtension(productPath) + ".sha"),
                                  FileUtilities.computeHash(productPath, "SHA-1"));
            } catch (Exception e) {
                logger.severe(String.format("Product %s could not be written to database: %s",
                                            product.getName(), e.getMessage()));
            }
        }
        return list;
    }
}
