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

/**
 * Factory class for creating instances of {@link ExecutorService} with a configurable delegate provider.
 *
 * This class allows the customization of {@link ExecutorService} creation through a delegate provider supplied
 * by the user. If no delegate provider is set, a default implementation is used, which creates a fixed thread
 * pool ExecutorService.
 */
public class ExecutorServiceFactory {
    private static ExecutorServiceDelegateProvider delegateProvider;
    private static final ExecutorServiceDelegateProvider defaultProvider = new ExecutorServiceDelegateProvider() { };

    /**
     * Sets the delegate provider used for creating instances of {@link ExecutorService}.
     * The delegate provider allows customization of the {@link ExecutorService} creation process.
     * If no delegate provider is set, a default implementation will be used.
     *
     * @param delegateProvider an implementation of {@link ExecutorServiceDelegateProvider} to be used
     *                         for creating {@link ExecutorService} instances. If null, the previously
     *                         set delegate provider will be cleared.
     */
    public static void setDelegateProvider(ExecutorServiceDelegateProvider delegateProvider) {
        ExecutorServiceFactory.delegateProvider = delegateProvider;
    }

    /**
     * Creates an {@link ExecutorService} with the specified number of threads.
     *
     * The creation is delegated to a configurable delegate provider if set. If no delegate provider
     * is configured, a default implementation is used, which creates a fixed thread pool ExecutorService.
     *
     * @param threads the number of threads to allocate in the created ExecutorService.
     * @return an instance of {@link ExecutorService} configured with the specified number of threads.
     */
    public static ExecutorService createExecutor(int threads) {
        return delegateProvider != null ?
                delegateProvider.createExecutor(threads) : defaultProvider.createExecutor(threads);
    }
}
