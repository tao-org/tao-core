package ro.cs.tao.utils.executors.monitoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class RollingFile {
    private final Path base;
    private final long limit;
    private final Logger logger;

    public RollingFile(Path base, long maxBytes) {
        if (base == null) {
            throw new NullPointerException("[base]");
        }
        if (!Files.isDirectory(base)) {
            throw new IllegalArgumentException(String.format("%s is not a directory", base));
        }
        this.base = base;
        this.limit = maxBytes;
        this.logger = Logger.getLogger(RollingFile.class.getName());
    }

    public void write(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        Path currentFile = currentFile(bytes.length);
        logger.finest(String.format("Current log file is %s", currentFile));
        if (!Files.exists(currentFile)) {
            Files.createDirectories(currentFile.getParent());
            Files.write(currentFile, bytes, StandardOpenOption.CREATE);
        } else {
            Files.write(currentFile, bytes, StandardOpenOption.APPEND);
        }
    }

    public Path getBase() { return base; }

    private Path currentFile(int currentLength) throws IOException {
        String fileName = "diskLog.log";
        Path file = this.base.resolve(fileName);
        if (!Files.exists(file)) {
            return file;
        }
        if (Files.size(file) + currentLength > this.limit) {
            Path backedUpFile = this.base.resolve(fileName + "." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
            Files.move(file, backedUpFile);
            logger.finest(String.format("Rolled from previous log file %s", backedUpFile));
        }
        return file;
    }
}
