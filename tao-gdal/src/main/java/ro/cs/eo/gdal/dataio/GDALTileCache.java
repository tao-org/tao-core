package ro.cs.eo.gdal.dataio;

import ro.cs.tao.utils.FileUtilities;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class GDALTileCache {

    private final GDALTileCacheMap cachedTiles;

    private final boolean cacheEnabled;
    private Path cacheDirPath;
    private final boolean cacheClearAtStartup;


    public GDALTileCache(boolean enabled, Path cachePath, int size, boolean cleanOnStrartup) {
        this.cacheEnabled = enabled;
        this.cacheDirPath = cachePath;
        this.cacheClearAtStartup = cleanOnStrartup;
        if (this.cacheEnabled) {
            this.cachedTiles = new GDALTileCacheMap(size);
            initCache();
        } else {
            this.cachedTiles = null;
        }
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    private void initCache() {
        if (this.cacheClearAtStartup) {
            clearCache();
        } else {
            loadCache();
        }
        if (!Files.exists(this.cacheDirPath)) {
            try {
                FileUtilities.createDirectories(this.cacheDirPath);
            } catch (IOException e) {
                System.err.println("Fail to init cache from: " + this.cacheDirPath);
            }
        }
    }

    private void clearCache() {
        if (Files.exists(this.cacheDirPath)) {
            try {
                FileUtilities.deleteTree(this.cacheDirPath);
            } catch (IOException e) {
                System.err.println("Fail to clear cache from: " + this.cacheDirPath);
            }
        }
    }

    private void loadCache() {
        if (Files.exists(this.cacheDirPath)) {
            try (Stream<Path> paths = Files.walk(this.cacheDirPath, FileVisitOption.FOLLOW_LINKS)) {
                paths.filter(Files::isRegularFile).forEach(tilePath -> {
                    try {
                        final long fileSize = Files.size(tilePath);
                        this.cachedTiles.putTileInCache(tilePath, fileSize);
                    } catch (IOException e) {
                        System.err.println("Fail to load file from cache: " + tilePath);
                    }
                });
            } catch (IOException e) {
                System.err.println("Fail to load cache from: " + this.cacheDirPath);
            }
        }
    }

    public void setCacheDirPath(Path cacheDirPath) {
        this.cacheDirPath = cacheDirPath;
    }

    public void putTile(Path tileFilePath, long tileSize) {
        this.cachedTiles.putTileInCache(tileFilePath, tileSize);
    }

    public Path getTilePath(String imageFileName, int x, int y, byte z, int pixelMinScale, int pixelMaxScale){
        return this.cacheDirPath.resolve(imageFileName).resolve("" + z).resolve("" + x).resolve(y + "-" + pixelMinScale + "_" + pixelMaxScale + ".PNG");
    }


}
