package ro.cs.eo.gdal.dataio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

final class GDALTileCacheMap extends LinkedHashMap<String, Long> {

    private static final BlockingQueue<String> evictionQueue = new LinkedBlockingQueue<>();

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
    protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
        final boolean removeEldestEntry = this.usedCacheSize.get() / GDALTileCache.MB >= this.cacheSize;
        if(removeEldestEntry){
            evictTileFromCache(eldest.getKey(), eldest.getValue());
        }
        return removeEldestEntry;
    }

    void putTileInCache(String key, long tileSize) {
        this.usedCacheSize.getAndAdd(tileSize);
        super.put(key, tileSize);
    }

    private void evictTileFromCache(String tilePath, Long tileSize) {
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
                final String tileFileToEvict = evictionQueue.take();
                final Path tileFileToEvictPath = Paths.get(tileFileToEvict);
                Files.deleteIfExists(tileFileToEvictPath);
            } catch (IOException | InterruptedException e) {
                System.err.println("Fail to delete file from GDAL tile cache.");
            }
        }

    }
}
