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

package ro.cs.tao.execution;

import ro.cs.tao.eodata.*;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.*;
import java.util.stream.Collectors;

public class EODataHandlerManager {
    private static final EODataHandlerManager instance;
    private final Map<Class<? extends EOData>, List<EODataHandler>> handlers;


    static {
        instance = new EODataHandlerManager();
    }

    public static EODataHandlerManager getInstance() { return instance; }

    private EODataHandlerManager() {
        ServiceRegistry<EODataHandler> registry = ServiceRegistryManager.getInstance()
                                                                        .getServiceRegistry(EODataHandler.class);
        Set<EODataHandler> services = registry.getServices();
        List<EODataHandler> allHandlers = services.stream()
                                                  .sorted(Comparator.comparingInt(EODataHandler::getPriority))
                                                  .collect(Collectors.toList());
        this.handlers = new HashMap<>();
        for (EODataHandler handler : allHandlers) {
            Class<? extends EOData> clazz = handler.isIntendedFor();
            if (!this.handlers.containsKey(clazz)) {
                this.handlers.put(clazz, new ArrayList<>());
            }
            this.handlers.get(clazz).add(handler);
        }
    }

    public <T extends EOData> List<T> applyHandlers(List<T> list) throws DataHandlingException {
        if (list == null) {
            return null;
        }
        if (list.size() > 0) {
            T element = list.get(0);
            if (element == null) {
                throw new DataHandlingException("Unhandled null value");
            }
            if (!(element instanceof EOProduct) && !(element instanceof VectorData)) {
                throw new DataHandlingException(String.format("Unhandled type: %s", element.getClass().getName()));
            }
            List<EODataHandler> handlers = this.handlers.get(element.getClass());
            for (EODataHandler handler : handlers) {
                list = handler.handle(list);
            }
        }
        return list;
    }
}
