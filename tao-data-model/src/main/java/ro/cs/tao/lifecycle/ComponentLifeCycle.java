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
package ro.cs.tao.lifecycle;

/**
 * An interface (activator) that defines actions to be taken by a module at different system execution points.
 *
 * @author Cosmin Cara
 */
public interface ComponentLifeCycle {
    /**
     * The priority of this module (the lower the higher).
     * The priority dictates the order in which the modules will be treated.
     */
    int priority();

    /**
     * Life cycle method called when the system starts up.
     */
    void onStartUp();

    /**
     * Life cycle method called when the system is about to be shut down.
     */
    void onShutdown();
}
