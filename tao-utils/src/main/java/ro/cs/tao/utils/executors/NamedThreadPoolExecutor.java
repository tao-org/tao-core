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

package ro.cs.tao.utils.executors;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadPoolExecutor extends ThreadPoolExecutor {
    private static final String THREAD_NAME = "%s-%d";

    public NamedThreadPoolExecutor(String poolName, int maxThreads) {
        this(maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, poolName);
    }

    private NamedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, final TimeUnit unit,
                                    final String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<>(),
              new ThreadFactory() {
                  private final AtomicInteger counter = new AtomicInteger();
                  @Override
                  public Thread newThread(Runnable r) {
                      final String name = String.format(THREAD_NAME, poolName, counter.incrementAndGet());
                      return new Thread(r, name);
                  }
              });

    }
}
