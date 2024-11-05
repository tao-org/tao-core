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
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.SortDirection;
import ro.cs.tao.persistence.NodeDBProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.NodeFlavorRepository;
import ro.cs.tao.persistence.repository.NodeRepository;
import ro.cs.tao.persistence.repository.ServiceRepository;
import ro.cs.tao.topology.*;
import ro.cs.tao.utils.Crypto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("nodeManager")
public class NodeDBManager extends EntityManager<NodeDescription, String, NodeRepository> implements NodeDBProvider {

    /** CRUD Repository for ServiceDescription entities */
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private NodeFlavorRepository nodeFlavorRepository;

    @Override
    public List<NodeDescription> list(boolean active) {
        return list(ro.cs.tao.Sort.by(identifier(), SortDirection.ASC))
                .stream()
                .filter(n -> n.getActive() != null && n.getActive() == active)
                .collect(Collectors.toList());
    }

    @Override
    public List<NodeDescription> getByFlavor(NodeFlavor flavor) {
        return repository.findByFlavor(flavor);
    }

    @Override
    public List<NodeDescription> getByUserWithFlavor(String userId, String flavorId) {
        return repository.getNodesByUserAndFlavor(userId, flavorId);
    }

    @Override
    public int countUsableNodes(String userId) {
        return repository.countUsableNodes(userId);
    }

    @Override
    public NodeDescription save(NodeDescription node) throws PersistenceException {
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

    @Override
    public NodeDescription update(NodeDescription entity) throws TopologyException {
        // check if there is already another node with the same host name
        final Optional<NodeDescription> nodeWithSameHostName = repository.findById(entity.getId());
        if (nodeWithSameHostName.isEmpty()) {
            throw new TopologyException(String.format("Entity %s does not exist. use 'saveExecutionode' instead.",
                                                         entity.getId()));
        }
        // save the services first
        for( NodeServiceStatus serviceStatus: entity.getServicesStatus()) {
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
        if (entity.getFlavor() == null) {
            throw new TopologyException(String.format("Node flavor not defined for node '%s'", entity.getId()));
        } else {
            nodeFlavorRepository.save(entity.getFlavor());
        }
        // save the new NodeDescription entity and return it
        return repository.save(entity);
    }

    @Override
    public void delete(final String hostName) throws PersistenceException {
        // check method parameters
        if (hostName == null || hostName.isEmpty()) {
            throw new PersistenceException("Invalid parameters were provided for deleting execution node (host name \""+ String.valueOf(hostName) +"\") !");
        }

        // retrieve NodeDescription after its host name
        final NodeDescription nodeEnt = repository.findById(hostName).orElse(null);
        if (nodeEnt == null) {
            throw new PersistenceException("There is no execution node with the specified host name: " + hostName);
        }
        if ((nodeEnt.getVolatile() != null && nodeEnt.getVolatile()) || nodeEnt.getServerId() != null) {
            repository.delete(nodeEnt);
        } else {
            nodeEnt.setActive(false);
            repository.save(nodeEnt);
        }
    }

    @Override
    public ServiceDescription getServiceDescription(String name, String version) {
        return serviceRepository.findByNameAndVersion(name, version);
    }

    @Override
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
        return entity.getUserName() != null && entity.getFlavor() != null;
    }

    private boolean checkServiceDescription(ServiceDescription service) {
        return service != null && service.getName() != null && !service.getName().isEmpty() &&
                service.getVersion() != null && !service.getVersion().isEmpty();
    }

    private void ensurePasswordEncrypted(final NodeDescription node) {
        if (node.getUserName() != null && node.getUserPass() != null) {
            node.setUserPass(Crypto.encrypt(node.getUserPass(), node.getUserName()));
        }
    }

    private void ensurePasswordDecrypted(final NodeDescription node) {
        if (node.getUserName() != null && node.getUserPass() != null) {
            node.setUserPass(Crypto.decrypt(node.getUserPass(), node.getUserName()));
        }
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
