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
package ro.cs.tao.spi;

import ro.cs.tao.component.Identifiable;
import ro.cs.tao.utils.Assert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * {@inheritDoc}
 */
public class DefaultServiceRegistry<T> implements ServiceRegistry<T> {

    private final Class<T> serviceType;
    private final HashMap<String, T> services;
    private final HashMap<String, String> serviceIds;
    private final ArrayList<ServiceRegistryListener<T>> listeners;

    public DefaultServiceRegistry(Class<T> serviceType) {
        Assert.notNull(serviceType, "serviceType");
        this.serviceType = serviceType;
        this.services = new HashMap<>(10);
        this.serviceIds = new HashMap<>(10);
        this.listeners = new ArrayList<>(3);
        ServiceLoader.loadServices(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getServiceType() {
        return serviceType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<T> getServices() {
        return new HashSet<>(services.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<T> getServices(Filter filter) {
    	FilterIterator<T> iter = new FilterIterator<>(services.values().iterator(), filter);
    	return StreamSupport.stream(
    			Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false).
    			collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getService(Class<? extends T> tClass) {
        return services.get(tClass.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getService(String serviceName) {
        String serviceId = serviceIds.get(serviceName);
        return services.get(serviceId != null ? serviceId : serviceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addService(T service) {
        Assert.notNull(service, "service");
        final T existingService = services.put(service.getClass().getName(), service);
        if (existingService != null && existingService.getClass().equals(service.getClass())) {
            return false;
        } else {
            if (service instanceof Identifiable) {
                serviceIds.put(((Identifiable) service).getId(), service.getClass().getName());
            }
        }
        for (ServiceRegistryListener<T> listener : listeners) {
            listener.serviceAdded(this, service);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeService(T service) {
        Assert.notNull(service, "service");
        final T existingService = services.remove(service.getClass().getName());
        if (existingService != service) {
            return false;
        } else {
            if (service instanceof Identifiable) {
                serviceIds.remove(((Identifiable) service).getId());
            }
        }
        for (ServiceRegistryListener<T> listener : listeners) {
            listener.serviceRemoved(this, service);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceRegistryListener<T>> getListeners() {
        return (List<ServiceRegistryListener<T>>) listeners.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(ServiceRegistryListener<T> listener) {
        Assert.notNull(listener, "listener");
        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(ServiceRegistryListener<T> listener) {
        Assert.notNull(listener, "listener");
        listeners.remove(listener);
    }
    
    /** {@collect.stats}
     * A class for wrapping <code>Iterators</code> with a filter function. 
     * This provides an iterator for a subset without duplication. 
     */ 
    class FilterIterator<V> implements Iterator<V> { 
     
        private Iterator<V> iter; 
        private ServiceRegistry.Filter filter; 
     
        private V next = null; 
     
        public FilterIterator(Iterator<V> iter, 
                              ServiceRegistry.Filter filter) { 
            this.iter = iter; 
            this.filter = filter; 
            advance(); 
        } 
     
        private void advance() { 
            while (iter.hasNext()) { 
                V elt = iter.next(); 
                if (filter.filter(elt)) { 
                    next = elt; 
                    return; 
                } 
            } 
     
            next = null; 
        } 
     
        public boolean hasNext() { 
            return next != null; 
        } 
     
        public V next() { 
            if (next == null) { 
                throw new NoSuchElementException(); 
            } 
            V o = next; 
            advance(); 
            return o; 
        } 
     
        public void remove() { 
            throw new UnsupportedOperationException(); 
        } 
    }

}
