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

package ro.cs.tao.persistence.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.eodata.AuxiliaryData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.VectorData;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.AuxDataRepository;
import ro.cs.tao.persistence.repository.EOProductRepository;
import ro.cs.tao.persistence.repository.VectorDataRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("productManager")
public class ProductManager {
    private Logger logger = Logger.getLogger(ProductManager.class.getName());

    /** CRUD Repository for EOProduct entities */
    @Autowired
    private EOProductRepository eoProductRepository;

    /** CRUD Repository for VectorData entities */
    @Autowired
    private VectorDataRepository vectorDataRepository;

    @Autowired
    private AuxDataRepository auxDataRepository;

    /**
     * Retrieve all EOProduct
     */
    @Transactional
    public List<EOProduct> getEOProducts() {
        // retrieve products
        return new ArrayList<>(((List<EOProduct>)
                eoProductRepository.findAll(new Sort(Sort.Direction.ASC,
                                                     Constants.DATA_PRODUCT_IDENTIFIER_PROPERTY_NAME))));
    }

    @Transactional
    public List<EOProduct> getPublicEOProducts() {
        return eoProductRepository.getPublicProducts();
    }

    @Transactional
    public List<EOProduct> getEOProducts(Set<String> locations) {
        return eoProductRepository.getProductsByLocation(locations);
    }

    @Transactional
    public List<EOProduct> getEOProducts(String location) {
        return eoProductRepository.getProductsByLocation(location);
    }

    /**
     * Retrieve all VectorData
     */
    @Transactional
    public List<VectorData> getVectorDataProducts() {
        // retrieve products
        return new ArrayList<>(((List<VectorData>)
                vectorDataRepository.findAll(new Sort(Sort.Direction.ASC,
                                                      Constants.DATA_PRODUCT_IDENTIFIER_PROPERTY_NAME))));
    }

    @Transactional
    public List<VectorData> getVectorDataProducts(Set<String> locations) {
        return vectorDataRepository.getProductsByLocation(locations);
    }

    @Transactional
    public List<AuxiliaryData> getAuxiliaryData(String userName) {
        return auxDataRepository.getAuxiliaryDataByUser(userName);
    }

    @Transactional
    public List<AuxiliaryData> getAuxiliaryData(String userName, Set<String> locations) {
        return auxDataRepository.getAuxiliaryDataByLocation(userName, locations);
    }

    @Transactional
    public EOProduct saveEOProduct(EOProduct eoProduct) throws PersistenceException {
        // check method parameters
        if (!checkEOProduct(eoProduct)) {
            throw new PersistenceException("Invalid parameters were provided for adding new EO data product!");
        }
        // save the EOProduct entity
        EOProduct savedEOProduct = eoProductRepository.save(eoProduct);
        if (savedEOProduct.getId() == null) {
            throw new PersistenceException("Error saving EO data product with name: " + eoProduct.getName());
        }
        return savedEOProduct;
    }

    @Transactional
    public VectorData saveVectorDataProduct(VectorData vectorDataProduct) throws PersistenceException {
        // check method parameters
        if (!checkVectorData(vectorDataProduct)) {
            throw new PersistenceException("Invalid parameters were provided for adding new vector data product!");
        }
        // save the VectorData entity
        VectorData savedVectorData = vectorDataRepository.save(vectorDataProduct);
        if (savedVectorData.getId() == null) {
            throw new PersistenceException("Error saving vector data product with name: " + vectorDataProduct.getName());
        }
        return savedVectorData;
    }

    @Transactional
    public AuxiliaryData saveAuxiliaryData(AuxiliaryData data) throws PersistenceException {
        // check method parameters
        if (!checkAuxData(data)) {
            throw new PersistenceException("Invalid parameters were provided for adding new auxiliary data!");
        }
        return auxDataRepository.save(data);
    }

    @Transactional
    public void removeAuxiliaryData(String location) {
        auxDataRepository.delete(location);
    }

    @Transactional
    public void removeAuxiliaryData(AuxiliaryData data) {
        auxDataRepository.delete(data);
    }

    private boolean checkEOProduct(EOProduct eoProduct) {
        return eoProduct != null && eoProduct.getId() != null && !eoProduct.getId().isEmpty() &&
                eoProduct.getName() != null && eoProduct.getGeometry() != null && eoProduct.getProductType() != null &&
                eoProduct.getLocation() != null && eoProduct.getSensorType() != null && eoProduct.getPixelType() != null;
    }

    private boolean checkVectorData(VectorData vectorDataProduct) {
        return vectorDataProduct != null && vectorDataProduct.getId() != null && !vectorDataProduct.getId().isEmpty() &&
                vectorDataProduct.getName() != null && vectorDataProduct.getGeometry() != null &&
                vectorDataProduct.getLocation() != null;
    }

    private boolean checkAuxData(AuxiliaryData auxiliaryData) {
        return auxiliaryData != null && auxiliaryData.getLocation() != null && !auxiliaryData.getLocation().isEmpty() &&
                auxiliaryData.getDescription() != null && !auxiliaryData.getDescription().isEmpty() &&
                auxiliaryData.getUserName() != null && !auxiliaryData.getUserName().isEmpty();
    }
}
