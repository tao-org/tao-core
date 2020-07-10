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

package ro.cs.tao.utils.executors;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadPoolExecutor extends ThreadPoolExecutor {
    private static final String THREAD_NAME = "%s-%d";
    private String poolName;
    private long timeout = -1;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    public NamedThreadPoolExecutor(String poolName, int maxThreads) {
        this(maxThreads, maxThreads, 1L, TimeUnit.SECONDS, poolName);
        this.poolName = poolName;
    }

    public void setTimeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return new TimeoutFuture<>(super.submit(task), this.timeout, this.timeUnit);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return new TimeoutFuture<>(super.submit(task, result), this.timeout, this.timeUnit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return new TimeoutFuture<>(super.submit(task), this.timeout, this.timeUnit);
    }

    String getPoolName() {  return this.poolName; }

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
              },
              new RejectedTaskHandler());
        allowCoreThreadTimeOut(false);
    }

    /**
     * Credits to Igor Kromin
     */
    public class TimeoutFuture<T> implements Future<T> {
        private final Future<T> delegate;
        private final long timeout;
        private final TimeUnit timeUnit;

        TimeoutFuture(Future<T> delegate, long timeout, TimeUnit timeUnit) {
            this.delegate = delegate;
            this.timeout = timeout;
            this.timeUnit = timeUnit;
        }
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }
        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }
        @Override
        public boolean isDone() {
            return delegate.isDone();
        }
        @Override
        public T get() throws InterruptedException, ExecutionException {
            try {
                if (timeout > 0) {
                    return delegate.get(timeout, timeUnit);
                }
                return delegate.get();
            }
            catch (TimeoutException e) {
                this.cancel(true);
                throw new ExecutionException(
                        "Forced timeout after " + timeout + " " + timeUnit.name(), null);
            }
        }
        @Override
        public T get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            try {
                return delegate.get(timeout, unit);
            }
            catch (TimeoutException e) {
                this.cancel(true);
                throw new ExecutionException(
                        "Timeout after " + timeout + " " + unit.name(), null);
            }
        }
    }
}
