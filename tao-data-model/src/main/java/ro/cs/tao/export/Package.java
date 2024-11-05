package ro.cs.tao.export;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.docker.Container;
import ro.cs.tao.serialization.BaseSerializer;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.SerializerFactory;
import ro.cs.tao.utils.DockerHelper;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.Zipper;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Package {
    private final String name;
    private Container containerDescriptor;
    private List<ProcessingComponent> componentDescriptors;
    private ProgressListener progressListener;

    public Package(String name) {
        this.name = name;
    }

    public List<ProcessingComponent> getComponentDescriptors() {
        return componentDescriptors;
    }

    public void setComponentDescriptors(List<ProcessingComponent> componentDescriptors) {
        this.componentDescriptors = componentDescriptors;
    }

    public Container getContainerDescriptor() {
        return containerDescriptor;
    }

    public void setContainerDescriptor(Container containerDescriptor) {
        this.containerDescriptor = containerDescriptor;
    }

    public String getName() {
        return name;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public Path toFile(Path targetDirectory, boolean includeImageFile) throws IOException, SerializationException {
        final String validFileName = FileUtilities.ensureValidFileName(name);
        Path workDir = targetDirectory.resolve(validFileName);
        FileUtilities.createDirectories(workDir);
        if (includeImageFile) {
            final Path imageFile = workDir.resolve(validFileName + ".tar");
            DockerHelper.saveImage(name, imageFile);
        }
        final BaseSerializer<Container> containerBaseSerializer = SerializerFactory.create(Container.class, MediaType.JSON);
        Files.writeString(workDir.resolve("container.json"), containerBaseSerializer.serialize(containerDescriptor));
        final BaseSerializer<ProcessingComponent> componentSerializer = SerializerFactory.create(ProcessingComponent.class, MediaType.JSON);
        Files.writeString(workDir.resolve("components.json"), componentSerializer.serialize(componentDescriptors, "components"));
        Zipper.compress(workDir, validFileName, true, progressListener);
        return workDir;
    }

    public static Package fromFile(Path file, Path targetFolder) throws IOException, SerializationException {
        Package pack = null;
        if (Files.exists(file)) {
            Zipper.decompressZip(file, targetFolder, null);
            final Path containerFile = targetFolder.resolve("container.json");
            if (Files.exists(containerFile)) {
                final BaseSerializer<Container> containerBaseSerializer = SerializerFactory.create(Container.class, MediaType.JSON);
                final Container container = containerBaseSerializer.deserialize(Files.readString(containerFile));
                pack = new Package(container.getName());
                pack.setContainerDescriptor(container);
                final Path componentsFile = targetFolder.resolve("components.json");
                if (Files.exists(componentsFile)) {
                    final BaseSerializer<ProcessingComponent> componentSerializer = SerializerFactory.create(ProcessingComponent.class, MediaType.JSON);
                    pack.setComponentDescriptors(componentSerializer.deserialize(ProcessingComponent.class, Files.readString(componentsFile)));
                }
                final Path imageFile = targetFolder.resolve(file.getFileName().toString().replace(".zip", ".tar"));
                if (Files.exists(imageFile)) {
                    DockerHelper.loadImage(imageFile);
                }
            }
        }
        return pack;
    }
}
