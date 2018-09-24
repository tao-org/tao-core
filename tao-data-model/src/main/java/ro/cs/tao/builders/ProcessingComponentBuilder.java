package ro.cs.tao.builders;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProcessingComponentBuilder extends AbstractBuilder<ProcessingComponent> {

    private final List<ProcessingParameterBuilder> parameterBuilders;
    private final List<SourceDescriptorBuilder> sourceDescriptorBuilders;
    private final List<TargetDescriptorBuilder> targetDescriptorBuilders;

    public ProcessingComponentBuilder() {
        super(ProcessingComponent.class);
        this.parameterBuilders = new ArrayList<>();
        this.sourceDescriptorBuilders = new ArrayList<>();
        this.targetDescriptorBuilders = new ArrayList<>();
    }

    public ProcessingComponentBuilder withFileLocation(String fileLocation) {
        entity.setFileLocation(fileLocation);
        return this;
    }

    public ProcessingComponentBuilder withWorkingDirectory(String workingDirectory) {
        entity.setWorkingDirectory(workingDirectory);
        return this;
    }

    public ProcessingComponentBuilder withTemplate(Template template) throws TemplateException {
        entity.setTemplate(template);
        return this;
    }

    public ProcessingComponentBuilder withVariable(Variable variable) {
        if (entity.getVariables() == null) {
            entity.setVariables(new HashSet<>());
        }
        entity.getVariables().add(variable);
        return this;
    }

    public ProcessingParameterBuilder newParameter() {
        ProcessingParameterBuilder builder = new ProcessingParameterBuilder(this);
        this.parameterBuilders.add(builder);
        return builder;
    }

    public ProcessingComponentBuilder withMultiThread(boolean multiThread) {
        entity.setMultiThread(multiThread);
        return this;
    }

    public ProcessingComponentBuilder withVisibility(ProcessingComponentVisibility visibility) {
        entity.setVisibility(visibility);
        return this;
    }

    public ProcessingComponentBuilder withActive(boolean active) {
        entity.setActive(active);
        return this;
    }

    public ProcessingComponentBuilder withTemplateType(TemplateType templateType) {
        entity.setTemplateType(templateType);
        return this;
    }

    public ProcessingComponentBuilder setTemplateContents(String contents) {
        entity.setTemplateContents(contents);
        return this;
    }

    public ProcessingComponentBuilder withContainerId(String containerId) {
        entity.setContainerId(containerId);
        return this;
    }

    public ProcessingComponentBuilder withComponentType(ProcessingComponentType entityType) {
        entity.setComponentType(entityType);
        return this;
    }

    public ProcessingComponentBuilder withOwner(String owner) {
        entity.setOwner(owner);
        return this;
    }

    public ProcessingComponentBuilder withLabel(String label) {
        entity.setLabel(label);
        return this;
    }

    public ProcessingComponentBuilder withVersion(String version) {
        entity.setVersion(version);
        return this;
    }

    public ProcessingComponentBuilder withDescription(String description) {
        entity.setDescription(description);
        return this;
    }

    public ProcessingComponentBuilder withAuthors(String authors) {
        entity.setAuthors(authors);
        return this;
    }

    public ProcessingComponentBuilder withCopyright(String copyright) {
        entity.setCopyright(copyright);
        return this;
    }

    public ProcessingComponentBuilder withNodeAffinity(String nodeAffinity) {
        entity.setNodeAffinity(nodeAffinity);
        return this;
    }

    public SourceDescriptorBuilder newSource() {
        SourceDescriptorBuilder builder = new SourceDescriptorBuilder(this);
        this.sourceDescriptorBuilders.add(builder);
        return builder;
    }

    public TargetDescriptorBuilder newTarget() {
        TargetDescriptorBuilder builder = new TargetDescriptorBuilder(this);
        this.targetDescriptorBuilders.add(builder);
        return builder;
    }

    public ProcessingComponentBuilder withId(String id) {
        entity.setId(id);
        return this;
    }

    public ProcessingComponent build() {
        if (this.entity.getId() == null) {
            this.entity.setId(UUID.randomUUID().toString());
        }
        this.entity.setParameterDescriptors(this.parameterBuilders.stream()
                                                    .map(AbstractBuilder::build).collect(Collectors.toList()));
        this.entity.setSources(this.sourceDescriptorBuilders.stream()
                                                    .map(AbstractBuilder::build).collect(Collectors.toList()));
        this.entity.setTargets(this.targetDescriptorBuilders.stream()
                                                    .map(AbstractBuilder::build).collect(Collectors.toList()));
        return this.entity;
    }
}
