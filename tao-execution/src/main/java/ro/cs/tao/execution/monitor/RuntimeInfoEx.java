package ro.cs.tao.execution.monitor;

/**
 * Extends the RuntimeInfo class to include additional information about
 * the runtime environment. Provides detailed statistics about system
 * memory usage, including buffer and cache memory, as well as swap memory
 * totals and free space.
 *
 * This class inherits all the properties and methods of the RuntimeInfo
 * class while introducing the following additional properties:
 *
 * - Buffers: Represents the memory used by the operating system for buffers.
 * - Cached: Represents the memory in use for caching file system data.
 * - SwapTotal: Represents the total amount of swap space available.
 * - SwapFree: Represents the unused portion of swap space.
 *
 * Provides getters and setters for these additional properties to manage
 * and retrieve the extended runtime information.
 */
public class RuntimeInfoEx extends RuntimeInfo {
    private long buffers;
    private long cached;
    private long swapTotal;
    private long swapFree;

    public RuntimeInfoEx() {
        super();
    }

    public long getBuffers() {
        return buffers;
    }

    public void setBuffers(long buffers) {
        this.buffers = buffers;
    }

    public long getCached() {
        return cached;
    }

    public void setCached(long cached) {
        this.cached = cached;
    }

    public long getSwapTotal() {
        return swapTotal;
    }

    public void setSwapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
    }

    public long getSwapFree() {
        return swapFree;
    }

    public void setSwapFree(long swapFree) {
        this.swapFree = swapFree;
    }
}
