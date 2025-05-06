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

/**
 * A factory class for creating and wrapping {@link Runnable} instances. This class provides a mechanism
 * to delegate or modify the execution of {@link Runnable} objects by using a {@link RunnableDelegateProvider}.
 *
 * The factory allows for setting a custom {@link RunnableDelegateProvider} that determines how {@link Runnable}
 * instances are wrapped. If no delegate provider is set, the {@link Runnable} instance is returned as-is.
 */
public class RunnableContextFactory {
    private static RunnableDelegateProvider delegateProvider;

    /**
     * Sets the {@link RunnableDelegateProvider} used by the factory to wrap {@link Runnable} instances.
     * The provided delegate will determine how {@link Runnable} objects are modified or decorated during wrapping.
     * If set to {@code null}, the factory will stop modifying or wrapping {@link Runnable} instances.
     *
     * @param delegateProvider the {@link RunnableDelegateProvider} to be used for wrapping {@link Runnable} instances,
     *                         or {@code null} to disable wrapping behavior.
     */
    public static void setDelegateProvider(RunnableDelegateProvider delegateProvider) {
        RunnableContextFactory.delegateProvider = delegateProvider;
    }

    /**
     * Wraps a given {@link Runnable} instance using a {@link RunnableDelegateProvider} if one is set.
     * If no delegate provider is defined, the original {@link Runnable} instance is returned.
     *
     * @param runnable the {@link Runnable} instance to be wrapped
     * @return the wrapped {@link Runnable} if a {@link RunnableDelegateProvider} is set,
     *         otherwise the original {@link Runnable}
     */
    public static Runnable wrap(Runnable runnable) {
        return delegateProvider != null ? delegateProvider.wrap(runnable) : runnable;
    }

}
