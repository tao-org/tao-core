package ro.cs.tao.builders;

import ro.cs.tao.component.ParameterDependency;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.enums.Condition;
import ro.cs.tao.component.enums.ParameterType;

public class ProcessingParameterBuilder extends AggregatedBuilder<ParameterDescriptor, ProcessingComponentBuilder> {

    public ProcessingParameterBuilder(ProcessingComponentBuilder parent) {
        super(ParameterDescriptor.class, parent);
    }

    public ProcessingParameterBuilder withName(String value) {
        entity.setName(value);
        return this;
    }

    public ProcessingParameterBuilder withType(ParameterType type) {
        entity.setType(type);
        return this;
    }

    public ProcessingParameterBuilder withDataType(Class<?> dataType) {
        entity.setDataType(dataType);
        return this;
    }

    public ProcessingParameterBuilder withDefaultValue(String defaultValue) {
        entity.setDefaultValue(defaultValue);
        return this;
    }

    public ProcessingParameterBuilder withDescription(String description) {
        entity.setDescription(description);
        return this;
    }

    public ProcessingParameterBuilder withLabel(String label) {
        entity.setLabel(label);
        return this;
    }

    public ProcessingParameterBuilder withUnit(String unit) {
        entity.setUnit(unit);
        return this;
    }

    public ProcessingParameterBuilder withValueSet(String... valueSet) {
        entity.setValueSet(valueSet);
        return this;
    }

    public ProcessingParameterBuilder withFormat(String format) {
        entity.setFormat(format);
        return this;
    }

    public ProcessingParameterBuilder withNotNull(boolean notNull) {
        entity.setNotNull(notNull);
        return this;
    }

    public ProcessingParameterBuilder withDependency(String paramId, Condition condition, String... values) {
        entity.addDependency(paramId, condition, values);
        return this;
    }

    public ProcessingParameterBuilder withId(String id) {
        entity.setId(id);
        return this;
    }

    @Override
    public ProcessingComponentBuilder and() {
        if (this.entity.getDependencies() != null) {
            for (ParameterDependency dependency : this.entity.getDependencies()) {
                if (this.parent.entity.getParameterDescriptors().stream().noneMatch(p -> p.getId().equals(dependency.getReferencedParameterId()))) {
                    throw new IllegalArgumentException(String.format("Unsatisfied dependency (non-existend parameter id '%s'",
                                                                     dependency.getReferencedParameterId()));
                }
            }

        }
        return super.and();
    }

    @Override
    protected ParameterDescriptor addToParent(ParameterDescriptor builtEntity) {
        this.parent.entity.getParameterDescriptors().add(builtEntity);
        return builtEntity;
    }
}
