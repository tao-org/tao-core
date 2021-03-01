package ro.cs.tao.utils.executors;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import ro.cs.tao.utils.FileUtilities;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static FileProcessFactory createLocal() {
        return new FileProcessFactory();
    }

    public static FileProcessFactory createFor(String host, String user, String password, boolean certificate) throws IOException {
        if (localhost.equalsIgnoreCase(host)) {
            return new FileProcessFactory();
        } else {
            return new FileProcessFactory(host, user, password, certificate);
        }
    }

    private FileProcessFactory(String host, String user, String token, boolean certificate) throws IOException {
        this.host = host;
        this.user = user;
        this.token = token;
        this.useCertificate = certificate;
        this.fileManager = new RemoteFileManager();
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

    public String getHost() { return host; }

    public FileManager fileManager() { return fileManager; }

    public ProcessManager processManager() { return processManager; }

    public void setUserId(int userId) { this.uid = userId; }

    public void setGroupId(int groupId) { this.gid = groupId; }

    @Override
    public void close() throws Exception {
        this.fileManager.close();
    }

    public interface FileManager {
        boolean exists(Path path);
        boolean isDirectory(Path path);
        void ensureExists(Path path) throws IOException;
        void move(Path source, Path destination) throws IOException;
        void copyAndDelete(Path source, Path destination) throws IOException;
        boolean deleteIfExists(Path file) throws IOException;
        void deleteFile(Path file) throws IOException;
        void deleteFolder(Path folder) throws IOException;
        long size(Path path) throws IOException;
        long folderSize(Path folder) throws IOException;
        List<String> readAllLines(Path file) throws IOException;
        void write(Path file, byte[] bytes) throws IOException;
        void ensurePermissions(Path path) throws IOException;
        void close();
    }

    public interface ProcessManager {
        ExecutionUnit createUnit(List<String> arguments);
        Executor<?> createExecutor(List<String> arguments);
    }

    private class LocalFileManager implements FileManager {

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
        public long size(Path path) throws IOException {
            return Files.size(path);
        }

        @Override
        public long folderSize(Path folder) throws IOException {
            return FileUtilities.folderSize(folder);
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
    }

    private class RemoteFileManager implements FileManager {
        private final SSHExecutor executor;
        private final OutputAccumulator accumulator;

        private RemoteFileManager() throws IOException {
            this.executor = new SSHExecutor(host, new ArrayList<>(), false, SSHMode.EXEC);
            this.accumulator = new OutputAccumulator();
            this.executor.setUser(user);
            this.executor.setPassword(token);
            if (useCertificate) {
                this.executor.setCertificate(token);
            }
            this.executor.setKeepAlive(true);
            if (!this.executor.canConnect()) {
                throw new RuntimeException("Cannot connect to " + host);
            }
        }

        public void deleteFile(Path file) throws IOException {
            execute("rm","-f", file.toString());
        }

        public void deleteFolder(Path folder) throws IOException {
            execute("rm", "-rf", folder.toString());
        }

        public void move(Path source, Path destination) throws IOException {
            execute("mv", source.toString(), destination.toString());
        }

        public boolean exists(Path path) {
            try {
                execute("ls", path.toString());
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public boolean isDirectory(Path path) {
            try {
                ChannelSftp channel = (ChannelSftp) this.executor.open(SSHMode.SFTP);
                channel.cd(path.getParent().toString());
                final String fileName = path.getFileName().toString();
                final SftpATTRS attrs = channel.lstat(fileName);
                return attrs.isDir();
            } catch (Exception e) {
                return false;
            }
        }

        public void ensureExists(Path path) throws IOException {
            this.accumulator.reset();
            this.executor.arguments.addAll(Arrays.asList("mkdir", "-p", path.toString()));
            try {
                this.executor.execute(false);
            } catch (JSchException | SftpException e) {
                throw new IOException(e);
            }
        }

        public void copyAndDelete(Path source, Path destination) throws IOException {
            execute("cp", "-r", source.toString(), destination.toString());
            deleteFolder(source);
        }

        @Override
        public boolean deleteIfExists(Path file) throws IOException {
            execute("rm","-f", file.toString());
            return true;
        }

        @Override
        public long size(Path path) throws IOException {
            ChannelSftp channel = null;
            try {
                channel = (ChannelSftp) this.executor.open(SSHMode.SFTP);
                return FileUtilities.size(path, channel);
            } finally {
                if (channel != null) {
                    try {
                        channel.disconnect();
                        channel.getSession().disconnect();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        @Override
        public long folderSize(Path folder) throws IOException {
            ChannelSftp channel = null;
            try {
                channel = (ChannelSftp) this.executor.open(SSHMode.SFTP);
                return FileUtilities.size(folder, channel);
            } finally {
                if (channel != null) {
                    try {
                        channel.disconnect();
                        channel.getSession().disconnect();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        @Override
        public List<String> readAllLines(Path file) throws IOException {
            ChannelSftp channel = null;
            try {
                channel = (ChannelSftp) this.executor.open(SSHMode.SFTP);
                channel.cd(file.getParent().toString());
                try (InputStream stream = channel.get(file.getFileName().toString());
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
            } catch (SftpException e) {
                throw new IOException(e);
            } finally {
                if (channel != null) {
                    try {
                        channel.disconnect();
                        channel.getSession().disconnect();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        @Override
        public void write(Path file, byte[] bytes) throws IOException {
            ChannelSftp channel = null;
            try (InputStream stm = new ByteArrayInputStream(bytes)){
                ensureExists(file.getParent());
                channel = (ChannelSftp) this.executor.open(SSHMode.SFTP);
                channel.cd(file.getParent().toString());
                channel.put(stm, file.getFileName().toString());
                ensurePermissions(file);
            } catch (SftpException e) {
                throw new IOException(e);
            } finally {
                if (channel != null) {
                    try {
                        channel.disconnect();
                        channel.getSession().disconnect();
                    } catch (Exception ignored) {
                    }
                }
            }

        }

        @Override
        public void ensurePermissions(Path path) throws IOException {
            ChannelSftp channel = null;
            try {
                channel = (ChannelSftp) this.executor.open(SSHMode.SFTP);
                channel.cd(path.getParent().toString());
                final String filePart = path.getFileName().toString();
                /*if (uid != null) {
                    this.channel.chown(uid, filePart);
                }
                if (gid != null) {
                    this.channel.chgrp(gid, filePart);
                }*/
                channel.chmod(Integer.parseInt("777", 8), filePart);
            } catch (SftpException e) {
                throw new IOException(e);
            } finally {
                if (channel != null) {
                    try {
                        channel.disconnect();
                        channel.getSession().disconnect();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        @Override
        public void close() {
            this.executor.stop();
        }

        private void execute(String...commands) throws IOException {
            this.executor.arguments.clear();
            this.accumulator.reset();
            this.executor.arguments.addAll(Arrays.asList(commands));
            try {
                if (this.executor.execute(false) != 0) {
                    throw new IOException(this.accumulator.getOutput());
                }
            } catch (JSchException | SftpException e) {
                throw new IOException(e);
            }
        }
    }

    class LocalProcessManager implements ProcessManager {

        @Override
        public ExecutionUnit createUnit(List<String> arguments) {
            return new ExecutionUnit(ExecutorType.PROCESS, host, user, token, arguments, false, SSHMode.EXEC);
        }

        @Override
        public Executor<?> createExecutor(List<String> arguments) {
            return Executor.create(ExecutorType.PROCESS, host, arguments, false);
        }
    }

    class RemoteProcessManager implements ProcessManager {

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
}
