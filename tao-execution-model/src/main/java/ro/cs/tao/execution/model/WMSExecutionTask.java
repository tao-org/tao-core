package ro.cs.tao.execution.model;

import ro.cs.tao.component.*;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.utils.FileUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WMSExecutionTask extends ExecutionTask {

    private WMSComponent component;

    public WMSExecutionTask() { super(); }

    public WMSComponent getComponent() { return component; }

    public void setComponent(WMSComponent component) { this.component = component; }

    @Override
    public void setInputParameterValue(String parameterId, String value) {
        boolean descriptorExists = false;
        List<ParameterDescriptor> descriptorList = this.component.getParameters();
        for (ParameterDescriptor descriptor : descriptorList) {
            if (descriptor.getId().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        List<SourceDescriptor> sources = this.component.getSources();
        for (SourceDescriptor source : sources) {
            if (source.getName().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        List<TargetDescriptor> targets = this.component.getTargets();
        for (TargetDescriptor target : targets) {
            if (target.getName().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        if (!descriptorExists) {
            throw new ValidationException(String.format("The parameter ID [%s] does not exists in the component '%s'",
                                                        parameterId, component.getLabel()));
        }
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        Variable variable = this.inputParameterValues.stream()
                                                     .filter(v -> v.getKey().equals(parameterId))
                                                     .findFirst()
                                                     .orElse(null);
        SourceDescriptor sourceDescriptor = this.component.getSources().stream()
                                                          .filter(s -> s.getName().equals(parameterId))
                                                          .findFirst()
                                                          .orElse(null);
        final int cardinality = sourceDescriptor != null ? sourceDescriptor.getCardinality() : 1;
        if (variable != null) {
            if (cardinality == 1) {
                variable.setValue(value);
            } else {
                variable.setValue(appendValueToList(variable.getValue(), value));
            }
        } else {
            Variable var;
            if (cardinality == 1) {
                var = new Variable(parameterId, value);
            } else {
                String newValue = appendValueToList(null, value);
                var = new Variable(parameterId, newValue);
            }
            this.inputParameterValues.add(var);
        }
    }

    @Override
    public void setOutputParameterValue(String parameterId, String value) {
        if (this.outputParameterValues == null) {
            this.outputParameterValues = new ArrayList<>();
        }
        this.outputParameterValues.add(new Variable(parameterId, value));
    }

    @Override
    public String buildExecutionCommand() {
        return null;
    }

    public String buildOutputPath() {
        if (this.instanceTargetOutput == null) {
            String location = null;
            for (TargetDescriptor descriptor : this.component.getTargets()) {
                Variable variable = outputParameterValues.stream()
                                                         .filter(v -> descriptor.getName().equals(v.getKey())).findFirst().orElse(null);
                if (variable != null) {
                    location = variable.getValue() != null ? variable.getValue() : descriptor.getDataDescriptor().getLocation();
                    if (location != null) {
                        break;
                    }
                }
            }
            if (location != null) {
                Path path = Paths.get(location);
                final Path jobPath = Paths.get(getJob().getJobOutputPath());
                if (!path.isAbsolute()) {
                    Path relPath = jobPath.getName(jobPath.getNameCount() - 2).resolve(jobPath.getFileName());
                    path = Paths.get(ExecutionConfiguration.getWorkerContainerVolumeMap().getContainerWorkspaceFolder())
                                .resolve(relPath).resolve(path); //context.getNetSpace().resolve(path);
                }
                String fileName = path.getFileName().toString();
                String folderName = String.format(nameTemplate,
                                                  this.getJob().getId(),
                                                  this.getId(),
                                                  this.internalState == null ? "" : this.internalState + "-",
                                                  FileUtilities.getFilenameWithoutExtension(fileName));
                path = jobPath.resolve(folderName);
                if (!Files.exists(path)) {
                    try {
                        Files.createDirectories(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                this.instanceTargetOutput = FileUtilities.asUnixPath(path.resolve(fileName), true);
            }
        }
        return this.instanceTargetOutput;
    }
}
