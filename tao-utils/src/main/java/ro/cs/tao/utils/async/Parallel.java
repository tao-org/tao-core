package ro.cs.tao.utils.async;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Cosmin Cara
 */
public class Parallel {
    static final int PARALLELISM = Math.max(Runtime.getRuntime().availableProcessors() - 1, 2);
    /**
     * The loop code wrapper
     */
    public interface Runnable<T> {
        void run(T i);
    }

    public interface Cancellable<T> {
        void run(T i, Signal cancelSignal);
    }

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
        ForEach(params, PARALLELISM, code);
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
    public static <T> void ForEach(Iterable <T> params, int parallelism, final Runnable<T> code) {
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        List<Future<?>> tasks  = new LinkedList<>();
        for (final T param : params) {
            tasks.add(executor.submit(() -> code.run(param)));
        }
        tasks.forEach(task -> {
            try   { task.get(); }
            catch (InterruptedException | ExecutionException ignored) { }
        });
        executor.shutdown();
    }

    /**
     * Runs a for-loop in parallel.
     *
     * @param start     The start index
     * @param stop      The end index
     * @param code      The loop code
     */
    public static void For(int start, int stop, final Runnable<Integer> code) {
        For(start, stop, PARALLELISM, code);
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
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        List<Future<?>> tasks  = new LinkedList<>();
        for (int i = start; i < stop; i++) {
            final Integer index = i;
            tasks.add(executor.submit(() -> code.run(index)));
        }
        tasks.forEach(task -> {
            try   { task.get(); }
            catch (InterruptedException | ExecutionException ignored) { }
        });
        executor.shutdown();
    }

    public static void For(int start, int stop, final Cancellable<Integer> code) {
        For(start, stop, PARALLELISM, code);
    }

    public static void For(int start, int stop, int parallelism, final Cancellable<Integer> code) {
        final ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        final List<Future<?>> tasks  = new LinkedList<>();
        final Signal cancelSignal = new Signal(tasks);
        for (int i = start; i < stop; i++) {
            final Integer index = i;
            tasks.add(executor.submit(() -> code.run(index, cancelSignal)));
        }
        tasks.forEach(task -> {
            try   { task.get(); }
            catch (InterruptedException | ExecutionException | CancellationException ignored) { }
        });
        executor.shutdown();
    }
}
