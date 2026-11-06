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

package ro.cs.tao.orchestration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A provider of an {@link ExecutorService} delegate that allows creating
 * instances of ExecutorService with a specified number of threads.
 *
 * This interface provides a default implementation for creating a fixed-size
 * thread pool using the {@link Executors#newFixedThreadPool} method.
 */
public interface ExecutorServiceDelegateProvider {

    /**
     * Creates an {@link ExecutorService} with a fixed number of threads.
     * The created {@link ExecutorService} uses a thread pool of the specified size.
     *
     * @param threads the number of threads in the fixed thread pool
     * @return an {@link ExecutorService} instance with a fixed thread pool
     */
    default ExecutorService createExecutor(int threads) {
        return Executors.newFixedThreadPool(threads);
    }
}
