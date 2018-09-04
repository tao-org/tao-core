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

package ro.cs.tao.persistence.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.SortDirection;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.NodeRepository;
import ro.cs.tao.persistence.repository.ServiceRepository;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeServiceStatus;
import ro.cs.tao.topology.ServiceDescription;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("nodeManager")
public class NodeManager extends EntityManager<NodeDescription, String, NodeRepository> {

    /** CRUD Repository for ServiceDescription entities */
    @Autowired
    private ServiceRepository serviceRepository;

    @Transactional
    public List<NodeDescription> listActive() {
        return list(ro.cs.tao.Sort.by(identifier(), SortDirection.ASC))
                .stream()
                .filter(NodeDescription::getActive)
                .collect(Collectors.toList());
    }

    @Transactional
    public NodeDescription getNodeByHostName(final String hostName) throws PersistenceException {
        // check method parameters
        if (hostName == null || hostName.isEmpty()) {
            throw new PersistenceException("Invalid parameters were provided for searching execution node by host name ("+ String.valueOf(hostName) +") !");
        }

        // retrieve NodeDescription after its host name
        return repository.findById(hostName).orElse(null);
    }

    @Transactional
    public NodeDescription saveExecutionNode(NodeDescription node) throws PersistenceException {
        // check method parameters
        if(!checkEntity(node)) {
            throw new PersistenceException("Invalid parameters were provided for adding new execution node!");
        }

        // check if there is already another node with the same host name
        final Optional<NodeDescription> nodeWithSameHostName = repository.findById(node.getId());
        if (nodeWithSameHostName.isPresent()) {
            throw new PersistenceException("There is already another node with the host name: " + node.getId());
        }

        // save the services first
        for(NodeServiceStatus serviceStatus: node.getServicesStatus()) {
            ServiceDescription serviceDescription = serviceStatus.getServiceDescription();
            if (!exists(serviceDescription.getName(), serviceDescription.getVersion())) {
                serviceRepository.save(serviceDescription);
            } else {
                // retrieve the existent entities and associate them on the node
                ServiceDescription existingService =
                        serviceRepository.findByNameAndVersion(serviceDescription.getName(),
                                                               serviceDescription.getVersion());
                serviceStatus.setServiceDescription(existingService);
            }
        }

        // save the new NodeDescription entity and return it
        return repository.save(node);
    }

    @Transactional
    public NodeDescription deleteExecutionNode(final String hostName) throws PersistenceException {
        // check method parameters
        if (hostName == null || hostName.isEmpty()) {
            throw new PersistenceException("Invalid parameters were provided for deleting execution node (host name \""+ String.valueOf(hostName) +"\") !");
        }

        // retrieve NodeDescription after its host name
        final NodeDescription nodeEnt = repository.findById(hostName).orElse(null);
        if (nodeEnt == null) {
            throw new PersistenceException("There is no execution node with the specified host name: " + hostName);
        }
        nodeEnt.setActive(false);
        // save it
        return repository.save(nodeEnt);
    }

    @Transactional
    public ServiceDescription saveServiceDescription(ServiceDescription service) throws PersistenceException {
        // check method parameters
        if(!checkServiceDescription(service)) {
            throw new PersistenceException("Invalid parameters were provided for adding new service!");
        }

        // check if there is already another service with the same name and version
        final ServiceDescription serviceWithSameName = serviceRepository.findByNameAndVersion(service.getName(), service.getVersion());
        if (serviceWithSameName != null) {
            throw new PersistenceException("There is already another service with the name: " + service.getName());
        }

        // save the new ServiceDescription entity and return it
        return serviceRepository.save(service);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && !entityId.isEmpty();
    }

    @Override
    protected boolean checkEntity(NodeDescription entity) {
        return entity.getUserName() != null && entity.getUserPass() != null &&
               entity.getProcessorCount() > 0 && entity.getDiskSpaceSizeGB() > 0 && entity.getMemorySizeGB() > 0;
    }

    private boolean checkServiceDescription(ServiceDescription service) {
        return service != null && service.getName() != null && !service.getName().isEmpty() &&
                service.getVersion() != null && !service.getVersion().isEmpty();
    }

    @Transactional
    private boolean exists(final String serviceName, final String serviceVersion) {
        boolean result = false;
        if (serviceName != null && !serviceName.isEmpty()) {
            // try to retrieve ServiceDescription after its name
            final ServiceDescription serviceEnt = serviceRepository.findByNameAndVersion(serviceName, serviceVersion);
            if (serviceEnt != null) {
                result = true;
            }
        }

        return result;
    }
}
