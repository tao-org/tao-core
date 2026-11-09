package ro.cs.tao.utils.executors.monitoring;

import ro.cs.tao.utils.FileUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * The RollingFile class handles writing log data to a file with a size limit.
 * When the size limit is reached, the current log file is archived with a timestamp,
 * and a new log file is created.
 */
public class RollingFile {
    private final Path base;
    private final long limit;
    private final Logger logger;

    /**
     * Constructs a RollingFile instance that handles writing logs to disk with a size limit.
     * When the size of the file exceeds the specified limit, the existing file is archived
     * and a new one is created.
     *
     * @param base      The base directory where the log files will be stored. Must not be null and must point to an existing directory.
     * @param maxBytes  The maximum size in bytes a log file is allowed before being rolled over.
     * @throws NullPointerException     If the base path is null.
     * @throws IllegalArgumentException If the base path is not an existing directory.
     */
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

    /**
     * Writes the specified byte array to the log file. If the log file does not exist, it will be created.
     * If the log file size exceeds the predefined limit, the existing file will be archived,
     * and a new log file will be created before writing.
     *
     * @param bytes the byte array of data to be written to the log file. If null or empty, the method performs no action.
     * @throws IOException if an I/O error occurs during the file operations.
     */
    public void write(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        Path currentFile = currentFile(bytes.length);
        logger.finest(String.format("Current log file is %s", currentFile));
        if (!Files.exists(currentFile)) {
            FileUtilities.createDirectories(currentFile.getParent());
            Files.write(currentFile, bytes, StandardOpenOption.CREATE);
        } else {
            Files.write(currentFile, bytes, StandardOpenOption.APPEND);
        }
    }

    /**
     * Retrieves the base path where log files are stored.
     *
     * @return the base directory as a {@link Path} object.
     */
    public Path getBase() { return base; }

    /**
     * Determines the current log file to be used. If the specified log file exceeds the size
     * limit after adding the content length, it archives the existing log file by renaming
     * it with a timestamp and prepares a new log file.
     *
     * @param currentLength the length of the content to be written to the file, in bytes.
     *                      This value is used to check if adding the content will exceed
     *                      the size limit for the log file.
     * @return the {@code Path} to the current log file that should be used for writing.
     * @throws IOException if an I/O error occurs during file operations, such as checking
     *                     file size or moving existing files.
     */
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
