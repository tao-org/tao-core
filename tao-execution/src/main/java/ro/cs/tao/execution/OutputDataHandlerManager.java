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

package ro.cs.tao.execution;

import ro.cs.tao.eodata.DataHandlingException;
import ro.cs.tao.eodata.OutputDataHandler;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class is responsible with applying any defined output data handlers on a list of output items.
 *
 * @author Cosmin Cara
 */
public class OutputDataHandlerManager {
    private static final OutputDataHandlerManager instance;
    private final Map<Class<?>, List<OutputDataHandler>> handlers;
    private final Logger logger;

    static {
        instance = new OutputDataHandlerManager();
    }

    /**
     * Returns the only instance of this class
     */
    public static OutputDataHandlerManager getInstance() { return instance; }

    private OutputDataHandlerManager() {
        this.logger = Logger.getLogger(OutputDataHandlerManager.class.getName());
        ServiceRegistry<OutputDataHandler> registry = ServiceRegistryManager.getInstance()
                                                                        .getServiceRegistry(OutputDataHandler.class);
        Set<OutputDataHandler> services = registry.getServices();
        List<OutputDataHandler> allHandlers = services.stream()
                                                  .sorted(Comparator.comparingInt(OutputDataHandler::getPriority))
                                                  .collect(Collectors.toList());
        this.handlers = new HashMap<>();
        for (OutputDataHandler handler : allHandlers) {
            Class<?> clazz = handler.isIntendedFor();
            if (!this.handlers.containsKey(clazz)) {
                this.handlers.put(clazz, new ArrayList<>());
            }
            this.handlers.get(clazz).add(handler);
        }
        for (Class<?> key : this.handlers.keySet()) {
            this.handlers.get(key).sort(Comparator.comparingInt(OutputDataHandler::getPriority));
        }

    }

    public <T> T applyHandlers(T item) throws DataHandlingException {
        if (item == null) {
            throw new DataHandlingException("Unhandled null value");
        }
        List<OutputDataHandler> handlers = this.handlers.get(item.getClass());
        if (handlers != null) {
            for (OutputDataHandler handler : handlers) {
                item = (T) handler.handle(item);
            }
        } else {
            logger.warning(String.format("No output handler defined for type %s", item.getClass()));
            return null;
        }
        return item;
    }

    /**
     * Invokes the handlers registered for on the items of the given type and returns a list of updated items.
     * The order of handler invocation is given by the handler defined priority, in the ascending order of values.
     * If no handler is registered for the given type, the input list is returned.
     *
     * @param list      The items to be handled
     * @param <T>       The type of the items to be handled
     *
     */
    public <T> List<T> applyHandlers(List<T> list) throws DataHandlingException {
        if (list == null) {
            return null;
        }
        if (list.size() > 0) {
            T element = list.get(0);
            if (element == null) {
                throw new DataHandlingException("Unhandled null value");
            }
            List<OutputDataHandler> handlers = this.handlers.get(element.getClass());
            if (handlers != null) {
                for (OutputDataHandler handler : handlers) {
                    list = handler.handle(list);
                }
            } else {
                logger.warning(String.format("No output handler defined for type %s", element.getClass()));
            }
        }
        return list;
    }
}
