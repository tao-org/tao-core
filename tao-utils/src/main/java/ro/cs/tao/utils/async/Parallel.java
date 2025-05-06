package ro.cs.tao.utils.async;

import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Utility class for running parallel iterations over a range of integers or collections.
 * It provides static methods for parallel loops with customizable levels of parallelism.
 *
 * @author Cosmin Cara
 */
public class Parallel {
    static final int PARALLELISM = Math.max(Runtime.getRuntime().availableProcessors() - 1, 2);
    private static final Logger logger = Logger.getLogger(NamedThreadPoolExecutor.class.getName());
    /**
     * The loop code wrapper
     */
    public interface Runnable<T> {
        void run(T i);
    }

    public interface Cancellable<T> {
        void run(T i, Signal cancelSignal);
    }

    /**
     * A utility class used to manage and control the execution of a collection of tasks represented as {@link Future} objects.
     * This class provides functionality to signal cancellation of all tasks that are not yet completed.
     */
    public static class Signal {
        private final List<Future<?>> tasks;

        public Signal(List<Future<?>> tasks) {
            this.tasks = tasks;
        }

        public void signal() {
            for (Future<?> task : tasks) {
                if (!task.isDone()) {
                    task.cancel(true);
                }
            }
        }
    }

    /**
     * Iterates an Iterable collection in parallel.
     *
     * @param params    The items to iterate
     * @param code      The loop code
     * @param <T>       The type of items
     */
    public static <T> void ForEach(Iterable <T> params, final Runnable<T> code) {
        ForEach(null, params, PARALLELISM, code);
    }

    /**
     * Iterates an Iterable collection in parallel.
     *
     * @param params    The items to iterate
     * @param parallelism   The degree of parallelism
     * @param code      The loop code
     * @param <T>       The type of items
     */
    public static <T> void ForEach(Iterable <T> params, int parallelism, final Runnable<T> code) {
        ForEach(null, params, parallelism, code);
    }

    /**
     * Iterates an Iterable collection in parallel with the given
     * degree of parallelism
     *
     * @param params    The items to iterate
     * @param parallelism The degree of parallelism
     * @param code      The loop code
     * @param <T>       The type of items
     */
    public static <T> void ForEach(String name, Iterable <T> params, int parallelism, final Runnable<T> code) {
        final NamedThreadPoolExecutor executor = new NamedThreadPoolExecutor("pi" + (name == null ? "" : "-" + name),
                                                                             Math.max(PARALLELISM, parallelism));
        try {
            List<Future<?>> tasks = new LinkedList<>();
            for (final T param : params) {
                tasks.add(executor.submit(() -> {
                    try {
                        code.run(param);
                    } catch (Exception e) {
                        logger.warning("Unhandled exception in Runnable code: " + ExceptionUtils.getStackTrace(logger, e));
                    }
                }));
            }
            tasks.forEach(task -> {
                try {
                    task.get();
                } catch (InterruptedException | ExecutionException ignored) {
                }
            });
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Runs a for-loop in parallel.
     *
     * @param start     The start index
     * @param stop      The end index
     * @param code      The loop code
     */
    public static void For(int start, int stop, final Runnable<Integer> code) {
        For(null, start, stop, PARALLELISM, code);
    }

    /**
     * Runs a for-loop in parallel.
     *
     * @param name      The name of the loop
     * @param start     The start index
     * @param stop      The end index
     * @param code      The loop code
     */
    public static void For(String name, int start, int stop, final Runnable<Integer> code) {
        For(name, start, stop, PARALLELISM, code);
    }

    /**
     * Runs a for-loop in parallel with the given degree of parallelism
     *
     * @param start     The start index
     * @param stop      The end index
     * @param parallelism The degree of parallelism
     * @param code      The loop code
     */
    public static void For(int start, int stop, int parallelism, final Runnable<Integer> code) {
        For(null, start, stop, parallelism, code);
    }

    /**
     * Runs a for-loop in parallel with the given degree of parallelism
     *
     * @param name      The name of the loop
     * @param start     The start index
     * @param stop      The end index
     * @param parallelism The degree of parallelism
     * @param code      The loop code
     */
    public static void For(String name, int start, int stop, int parallelism, final Runnable<Integer> code) {
        final NamedThreadPoolExecutor executor = new NamedThreadPoolExecutor((name == null ? "" : name + "-") + "pf",
                                                                             Math.max(PARALLELISM, parallelism));
        try {
            List<Future<?>> tasks  = new LinkedList<>();
            for (int i = start; i < stop; i++) {
                final Integer index = i;
                tasks.add(executor.submit(() -> {
                    try {
                        code.run(index);
                    } catch (Exception e) {
                        logger.warning("Unhandled exception in Runnable code: " + ExceptionUtils.getStackTrace(logger, e));
                    }
                }));
            }
            tasks.forEach(task -> {
                try   { task.get(); }
                catch (InterruptedException | ExecutionException ignored) { }
            });
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Executes a parallel for-loop from the specified start index to the specified stop index.
     *
     * @param start The start index of the loop (inclusive).
     * @param stop The stop index of the loop (exclusive).
     * @param code The loop code to execute, which accepts the current index and a signal for cancellation handling.
     */
    public static void For(int start, int stop, final Cancellable<Integer> code) {
        For(null, start, stop, PARALLELISM, code);
    }

    /**
     * Executes a parallel for-loop across a range of integers, with specified settings
     * including loop naming and degree of parallelism. This method provides support for
     * cancellation mechanisms via a {@link Signal} object.
     *
     * @param name        The name of the loop, useful for identification in thread pool configurations.
     * @param start       The start index of the loop (inclusive).
     * @param stop        The end index of the loop (exclusive).
     * @param parallelism The degree of parallelism defining how many threads can run concurrently.
     * @param code        The loop code to execute, which accepts the current index and a signal for
     *                    cancellation handling.
     */
    public static void For(String name, int start, int stop, int parallelism, final Cancellable<Integer> code) {
        final NamedThreadPoolExecutor executor = new NamedThreadPoolExecutor("pfc" + (name == null ? "" : "-" + name), Math.max(PARALLELISM, parallelism));
        try {
            final List<Future<?>> tasks = new LinkedList<>();
            final Signal cancelSignal = new Signal(tasks);
            for (int i = start; i < stop; i++) {
                final Integer index = i;
                tasks.add(executor.submit(() -> {
                    try {
                        code.run(index, cancelSignal);
                    } catch (Exception e) {
                        logger.warning("Unhandled exception in Runnable code: " + ExceptionUtils.getStackTrace(logger, e));
                    }
                }));
            }
            tasks.forEach(task -> {
                try {
                    task.get();
                } catch (InterruptedException | ExecutionException | CancellationException ignored) {
                }
            });
        } finally {
            executor.shutdown();
        }
    }
}
