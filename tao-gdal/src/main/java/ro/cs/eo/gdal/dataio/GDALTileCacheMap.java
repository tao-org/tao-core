package ro.cs.eo.gdal.dataio;

import ro.cs.tao.utils.executors.MemoryUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

final class GDALTileCacheMap extends LinkedHashMap<Path, Long> {

    private static final BlockingQueue<Path> evictionQueue = new LinkedBlockingQueue<>();

    private final int cacheSize;
    private final AtomicLong usedCacheSize = new AtomicLong(0);

    GDALTileCacheMap(int cacheSize) {
        super();
        this.cacheSize = cacheSize;
        TileEvictor tileEvictor = new TileEvictor();
        tileEvictor.setName("tile-cache-evictor");
        tileEvictor.start();
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Path, Long> eldest) {
        final boolean removeEldestEntry = this.usedCacheSize.get() / MemoryUnit.MB.value() >= this.cacheSize;
        if(removeEldestEntry){
            evictTileFromCache(eldest.getKey(), eldest.getValue());
        }
        return removeEldestEntry;
    }

    void putTileInCache(Path key, long tileSize) {
        this.usedCacheSize.getAndAdd(tileSize);
        super.put(key, tileSize);
    }

    private void evictTileFromCache(Path tilePath, Long tileSize) {
        this.usedCacheSize.getAndAdd(-tileSize);
        evictionQueue.add(tilePath);
    }

    private static class TileEvictor extends Thread {

        @Override
        public void run() {
            while (this.isAlive()) {
                evictTile();
            }
        }

        private void evictTile() {
            try {
                final Path tileFileToEvict = evictionQueue.take();
                Files.deleteIfExists(tileFileToEvict);
            } catch (IOException | InterruptedException e) {
                System.err.println("Fail to delete file from GDAL tile cache.");
            }
        }

    }
}
