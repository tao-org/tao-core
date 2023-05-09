package ro.cs.tao.utils.executors;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.apache.commons.lang3.StringUtils;
import ro.cs.tao.utils.FileUtilities;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory class for providing wrappers over file and process local or remote operations.
 *
 * @author Cosmin Cara
 */
public class FileProcessFactory implements AutoCloseable {
    private static final String localhost;
    private final String host;
    private final String user;
    private final String token;
    private final boolean useCertificate;
    private Integer uid;
    private Integer gid;

    private final FileManager fileManager;
    private final ProcessManager processManager;

    static {
        String name;
        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            name = "localhost";
        }
        localhost = name;
    }

    /**
     * Creates a factory for local file and process operations
     */
    public static FileProcessFactory createLocal() {
        return new FileProcessFactory();
    }

    /**
     * Creates a factory for file and process operations.
     * If the [host] value is the local host, it will return a local factory, otherwise it will return a remote factory.
     */
    public static FileProcessFactory createFor(String host, String user, String password, boolean certificate, boolean keepAlive) throws IOException {
        if (localhost.equalsIgnoreCase(host)) {
            return new FileProcessFactory();
        } else {
            return new FileProcessFactory(host, user, password, certificate, keepAlive);
        }
    }

    private FileProcessFactory(String host, String user, String token, boolean certificate, boolean keepAlive) throws IOException {
        this.host = host;
        this.user = user;
        this.token = token;
        this.useCertificate = certificate;
        this.fileManager = new RemoteFileManager(keepAlive);
        this.processManager = new RemoteProcessManager();
    }

    private FileProcessFactory() {
        this.host = localhost;
        this.user = System.getProperty("user.name");
        this.token = null;
        this.useCertificate = false;
        this.fileManager = new LocalFileManager();
        this.processManager = new LocalProcessManager();
    }

    /**
     * Returns the host name for which this factory is intended
     */
    public String getHost() { return host; }

    /**
     * Returns the wrapper for file operations
     */
    public FileManager fileManager() { return fileManager; }

    /**
     * Returns the wrapper for process operations
     */
    public ProcessManager processManager() { return processManager; }

    public void setUserId(int userId) { this.uid = userId; }

    public void setGroupId(int groupId) { this.gid = groupId; }

    @Override
    public void close() throws Exception {
        this.fileManager.close();
    }

    /**
     * Interface for wrappers for file operations
     */
    public interface FileManager {
        boolean exists(Path path);
        boolean isDirectory(Path path);
        void ensureExists(Path path) throws IOException;
        void move(Path source, Path destination) throws IOException;
        void copyAndDelete(Path source, Path destination) throws IOException;
        boolean deleteIfExists(Path file) throws IOException;
        void deleteFile(Path file) throws IOException;
        void deleteFolder(Path folder) throws IOException;
        void cleanup(Path folder, Set<String> excludedExtensions, String... patterns) throws IOException;
        long size(Path path) throws IOException;
        long folderSize(Path folder) throws IOException;
        long availableBytes(Path folder) throws IOException;
        List<String> readAllLines(Path file) throws IOException;
        void write(Path file, byte[] bytes) throws IOException;
        void ensurePermissions(Path path) throws IOException;
        void close();
        List<Path> list(Path folder) throws IOException;
    }

    /**
     * Interface for wrappers for process operations
     */
    public interface ProcessManager {
        /**
         * Creates an execution unit (i.e. what can be executed by an executor) given a list of command line arguments
         * @param arguments The commands with/without arguments to be wrapped by the execution unit
         */
        ExecutionUnit createUnit(List<String> arguments);

        /**
         * Creates an executor for the given list of command line arguments
         * @param arguments The commands with/without arguments to be executed by the executor
         */
        Executor<?> createExecutor(List<String> arguments);
    }

    /**
     * Implementation for a local file operations wrapper.
     * The operations are delegated to java.nio.Files and ro.cs.tao.utils.FileUtilities
     */
    private static class LocalFileManager implements FileManager {

        @Override
        public boolean exists(Path path) {
            return Files.exists(path);
        }

        @Override
        public boolean isDirectory(Path path) {
            return Files.isDirectory(path);
        }

        @Override
        public void ensureExists(Path path) throws IOException {
            FileUtilities.ensureExists(path);
        }

        @Override
        public void move(Path source, Path destination) throws IOException {
            Files.move(source, destination);
        }

        @Override
        public void copyAndDelete(Path source, Path destination) throws IOException {
            FileUtilities.copyAndDelete(source, destination);
        }

        @Override
        public boolean deleteIfExists(Path file) throws IOException {
            return Files.deleteIfExists(file);
        }

        @Override
        public void deleteFile(Path file) throws IOException {
            Files.deleteIfExists(file);
        }

        @Override
        public void deleteFolder(Path folder) throws IOException {
            FileUtilities.deleteTree(folder);
        }

        @Override
        public void cleanup(Path folder, Set<String> excludedExtensions, String... patterns) throws IOException {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if ((patterns != null && Arrays.stream(patterns).anyMatch(p -> file.getFileName().toString().contains(p)))
                            || !excludedExtensions.contains(FileUtilities.getExtension(file.toFile()))) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (patterns != null && Arrays.stream(patterns).anyMatch(p -> dir.getFileName().toString().contains(p))) {
                        FileUtilities.deleteTree(dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return excludedExtensions.contains(FileUtilities.getExtension(dir.toFile()))
                               ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // propagate the exception if not null
                    if (exc != null) {
                        throw exc;
                    }
                    // remove the directory if not empty
                    boolean hasChildren;
                    try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
                        hasChildren = dirStream.iterator().hasNext();
                    }
                    if (!hasChildren) {
                        Files.delete(dir);
                        //cleanup(dir, excludedExtensions, patterns);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        @Override
        public long size(Path path) throws IOException {
            return Files.size(path);
        }

        @Override
        public long folderSize(Path folder) throws IOException {
            return FileUtilities.folderSize(folder);
        }

        @Override
        public long availableBytes(Path folder) throws IOException {
            return folder.toFile().getUsableSpace();
        }

        @Override
        public List<String> readAllLines(Path file) throws IOException {
            return Files.readAllLines(file);
        }

        @Override
        public void write(Path file, byte[] bytes) throws IOException {
            Files.write(file, bytes);
        }

        @Override
        public void ensurePermissions(Path path) throws IOException {
            FileUtilities.ensurePermissions(path);
        }


        @Override
        public void close() {
            //NOOP
        }

        @Override
        public List<Path> list(Path folder) throws IOException {
            try (Stream<Path> stream = Files.list(folder)) {
                return stream.collect(Collectors.toList());
            }
        }
    }
    /**
     * Implementation for a remote file operations wrapper.
     * The operations are delegated to JSch.
     */
    private class RemoteFileManager implements FileManager {
        private final SSHExecutor executor;
        private final OutputAccumulator accumulator;

        private RemoteFileManager(boolean keepAlive) throws IOException {
            this.executor = new SSHExecutor(host, new ArrayList<>(), false, SSHMode.EXEC);
            this.accumulator = new OutputAccumulator();
            this.executor.setUser(user);
            this.executor.setPassword(token);
            this.executor.setOutputConsumer(this.accumulator);
            if (useCertificate) {
                this.executor.setCertificate(token);
            }
            this.executor.setKeepAlive(keepAlive);
            if (!this.executor.canConnect()) {
                throw new IOException("Cannot connect to " + host);
            }
        }

        public void deleteFile(Path file) throws IOException {
            //execute("/bin/bash", "-c", "\"rm -f " + file.toString() + "\"");
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                channel.getChannel().cd(file.getParent().toString());
                channel.getChannel().rm(file.getFileName().toString());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public void deleteFolder(Path folder) throws IOException {
            //execute("/bin/bash", "-c", "\"rm -rf " + folder.toString() + "\"");
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                deleteFolderInner(folder.toString(), channel.getChannel());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void cleanup(Path folder, Set<String> excludedExtensions, String... patterns) throws IOException {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                cleanup(folder.toString(), excludedExtensions, channel.getChannel(), patterns);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private void deleteFolderInner(String path, ChannelSftp channel) throws Exception {
            channel.cd(path);
            Vector<ChannelSftp.LsEntry> fileAndFolderList = channel.ls(path);
            for (ChannelSftp.LsEntry item : fileAndFolderList) {
                if (!item.getAttrs().isDir()) {
                    channel.rm(path + "/" + item.getFilename());
                } else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) { // If it is a subdir.
                    try {
                        channel.rmdir(path + "/" + item.getFilename());
                    } catch (Exception e) { // If subdir is not empty and error occurs.
                        deleteFolderInner(path + "/" + item.getFilename(), channel);
                    }
                }
            }
            channel.rmdir(path); // delete the parent directory after empty
        }

        private void cleanup(String path, Set<String> excludedExtensions, ChannelSftp channel, String... patterns) throws Exception {
            channel.cd(path);
            Vector<ChannelSftp.LsEntry> fileAndFolderList = channel.ls(path);
            for (ChannelSftp.LsEntry item : fileAndFolderList) {
                if (!item.getAttrs().isDir()) {
                    if (patterns == null && excludedExtensions == null) {
                        channel.rm(path + "/" + item.getFilename());
                    } else if ((patterns != null && Arrays.stream(patterns).anyMatch(p -> item.getFilename().contains(p)))
                            || !excludedExtensions.contains(item.getFilename().substring(item.getFilename().lastIndexOf('.')))) {
                        channel.rm(path + "/" + item.getFilename());
                    }
                } else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) { // If it is a subdir.
                    try {
                        channel.rmdir(path + "/" + item.getFilename());
                    } catch (Exception e) { // If subdir is not empty and error occurs.
                        cleanup(path + "/" + item.getFilename(), excludedExtensions, channel, patterns);
                    }
                }
            }
        }

        public void move(Path source, Path destination) throws IOException {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                channel.getChannel().rename(source.toString(), destination.toString());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public boolean exists(Path path) {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                channel.getChannel().ls(path.toString());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean isDirectory(Path path) {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                channel.getChannel().cd(path.getParent().toString());
                final String fileName = path.getFileName().toString();
                final SftpATTRS attrs = channel.getChannel().lstat(fileName);
                return attrs.isDir();
            } catch (Exception e) {
                return false;
            }
        }

        public void ensureExists(Path path) throws IOException {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                Path current = path.getRoot().resolve(path.getName(0));
                channel.getChannel().cd(current.toString());
                for (int i = 1; i < path.getNameCount(); i++) {
                    current = current.resolve(path.getName(i));
                    try {
                        channel.getChannel().ls(current.toString());
                    } catch (SftpException e) {
                        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                            channel.getChannel().mkdir(current.getFileName().toString());
                        } else {
                            throw e;
                        }
                    }
                    channel.getChannel().cd(current.toString());
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public void copyAndDelete(Path source, Path destination) throws IOException {
            try {
                execute("cp", "-rfv", source.toString(), destination.toString());
            } catch (Exception e) {
                throw new IOException("Copy failed", e);
            }
            try {
                deleteFolder(source);
            } catch (IOException e) {
                throw new IOException("Delete failed", e);
            }
        }

        private void copyFile(Path source, Path destination, ChannelSftp uploadChannel) throws Exception {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                channel.getChannel().put(uploadChannel.get(source.toString()), destination.toString());
            }
        }

        @Override
        public boolean deleteIfExists(Path file) throws IOException {
            try {
                execute("rm", "-f", file.toString());
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public long size(Path path) throws IOException {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                return FileUtilities.size(path, channel.getChannel());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public long folderSize(Path folder) throws IOException {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                return FileUtilities.size(folder, channel.getChannel());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public long availableBytes(Path folder) throws IOException {
            final String out = execute("df", "-h", folder.toString());
            if (StringUtils.isNotEmpty(out)) {
                final List<String> lines = Arrays.stream(out.split(" ")).filter(e -> e.length() > 0).collect(Collectors.toList());
                int shift = 30;
                int idx = lines.get(9).indexOf('G');
                if (idx < 0) {
                    idx = lines.get(9).indexOf('T');
                    if (idx < 0) {
                        idx = lines.get(9).indexOf('P');
                        shift += 20;
                    } else {
                        shift += 10;
                    }
                }
                final String value = lines.get(9).substring(0, idx).trim();
                return (value.contains(".") ? (long) Double.parseDouble(value) : Long.parseLong(value)) << shift;
            } else {
                return 0;
            }
        }

        @Override
        public List<String> readAllLines(Path file) throws IOException {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                channel.getChannel().cd(file.getParent().toString());
                try (InputStream stream = channel.getChannel().get(file.getFileName().toString());
                     BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                    final List<String> result = new ArrayList<>();
                    for (; ; ) {
                        String line = reader.readLine();
                        if (line == null)
                            break;
                        result.add(line);
                    }
                    return result;
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void write(Path file, byte[] bytes) throws IOException {
            try (InputStream stm = new ByteArrayInputStream(bytes);
                 ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))){
                //ensureExists(file.getParent());
                channel.getChannel().cd(file.getParent().toString());
                channel.getChannel().put(stm, file.getFileName().toString());
                channel.getChannel().chmod(Integer.parseInt("777", 8), file.getFileName().toString());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void ensurePermissions(Path path) throws IOException {
            try (ChannelWrapper<ChannelSftp> channel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                channel.getChannel().cd(path.getParent().toString());
                final String filePart = path.getFileName().toString();
                /*if (uid != null) {
                    this.channel.chown(uid, filePart);
                }
                if (gid != null) {
                    this.channel.chgrp(gid, filePart);
                }*/
                channel.getChannel().chmod(Integer.parseInt("777", 8), filePart);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void close() {
            this.executor.stop();
            this.executor.close();
        }

        @Override
        public List<Path> list(Path folder) throws IOException {
            try (ChannelWrapper<ChannelSftp> sftpChannel = new ChannelWrapper<>(this.executor.open(SSHMode.SFTP))) {
                final List<Path> files = new ArrayList<>();
                ChannelSftp channel = sftpChannel.getChannel();
                final String path = folder.toString();
                channel.cd(path);
                Vector<ChannelSftp.LsEntry> fileAndFolderList = channel.ls(path);
                for (ChannelSftp.LsEntry item : fileAndFolderList) {
                    if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) {
                        files.add(folder.resolve(item.getFilename()));
                    }
                }
                return files;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private String execute(String...commands) throws IOException {
            this.executor.arguments.clear();
            this.accumulator.reset();
            this.executor.isStopped = false;
            this.executor.arguments.addAll(Arrays.asList(commands));
            try {
                final int retCode = this.executor.execute(false);
                if (retCode != 0) {
                    throw new IOException("[code " + retCode + "] " + this.accumulator.getOutput());
                }
                return this.accumulator.getOutput();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
    /**
     * Implementation for a local process operations wrapper.
     */
    private class LocalProcessManager implements ProcessManager {

        @Override
        public ExecutionUnit createUnit(List<String> arguments) {
            return new ExecutionUnit(ExecutorType.PROCESS, host, user, token, arguments, false, SSHMode.EXEC);
        }

        @Override
        public Executor<?> createExecutor(List<String> arguments) {
            return Executor.create(ExecutorType.PROCESS, host, arguments, false);
        }
    }
    /**
     * Implementation for a remote process operations wrapper.
     */
    private class RemoteProcessManager implements ProcessManager {

        @Override
        public ExecutionUnit createUnit(List<String> arguments) {
            final ExecutionUnit unit = new ExecutionUnit(ExecutorType.SSH2, host, user, token, arguments, false, SSHMode.EXEC);
            if (useCertificate) {
                unit.setCertificate(token);
            }
            return unit;
        }

        @Override
        public Executor<?> createExecutor(List<String> arguments) {
            final Executor<?> executor = Executor.create(ExecutorType.SSH2, host, arguments, false);
            executor.setUser(user);
            executor.setPassword(token);
            if (useCertificate) {
                executor.setCertificate(token);
            }
            return executor;
        }
    }

    private static class ChannelWrapper<T extends Channel> implements AutoCloseable {
        private final T channel;

        public ChannelWrapper(Channel channel) {
            this.channel = (T) channel;
        }

        public T getChannel() {
            return channel;
        }

        @Override
        public void close() throws Exception {
            if (channel != null) {
                try {
                    channel.disconnect();
                    Logger.getLogger(FileProcessFactory.class.getName()).finest(String.format("Channel %d disconnected", channel.getId()));
                } catch (Exception ignored) {
                }
            }
        }
    }
}
