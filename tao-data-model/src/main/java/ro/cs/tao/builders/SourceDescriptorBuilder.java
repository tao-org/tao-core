package ro.cs.tao.builders;

import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.SensorType;

import java.awt.*;

public class SourceDescriptorBuilder extends AggregatedBuilder<SourceDescriptor, ProcessingComponentBuilder> {

    public SourceDescriptorBuilder(ProcessingComponentBuilder parent) {
        super(SourceDescriptor.class, parent);
    }

    public SourceDescriptorBuilder withName(String name) {
        entity.setName(name);
        return this;
    }

    public SourceDescriptorBuilder withCardinality(int value) {
        entity.setCardinality(value);
        return this;
    }

    public SourceDescriptorBuilder withDataDescriptor(DataFormat format, String geometryWKT, String crsCode,
                                                      SensorType sensorType, Integer width, Integer height,
                                                      String location) {
        DataDescriptor descriptor = new DataDescriptor();
        descriptor.setFormatType(format);
        descriptor.setGeometry(geometryWKT);
        descriptor.setCrs(crsCode);
        descriptor.setSensorType(sensorType);
        if (width != null && height != null) {
            descriptor.setDimension(new Dimension(width, height));
        }
        descriptor.setLocation(location);
        entity.setDataDescriptor(descriptor);
        return this;
    }

    public SourceDescriptorBuilder withConstraints(String...constraints) {
        if (constraints != null && constraints.length > 0) {
            for (String constraint : constraints) {
                entity.addConstraint(constraint);
            }
        }
        return this;
    }

    public SourceDescriptorBuilder withId(String id) {
        entity.setId(id);
        return this;
    }

    @Override
    protected SourceDescriptor addToParent(SourceDescriptor builtEntity) {
        builtEntity.setParentId(this.parent.entity.getId());
        this.parent.entity.addSource(builtEntity);
        return builtEntity;
    }
}
