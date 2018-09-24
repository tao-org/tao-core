package ro.cs.tao.builders;

import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.SensorType;

import java.awt.*;

public class TargetDescriptorBuilder extends AggregatedBuilder<TargetDescriptor, ProcessingComponentBuilder> {

    public TargetDescriptorBuilder(ProcessingComponentBuilder parent) {
        super(TargetDescriptor.class, parent);
    }

    public TargetDescriptorBuilder withParentId(String parent) {
        entity.setParentId(parent);
        return this;
    }

    public TargetDescriptorBuilder withName(String name) {
        entity.setName(name);
        return this;
    }

    public TargetDescriptorBuilder withCardinality(int value) {
        entity.setCardinality(value);
        return this;
    }

    public TargetDescriptorBuilder withDataDescriptor(DataFormat format, String geometryWKT, String crsCode,
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

    public TargetDescriptorBuilder withConstraints(String...constraints) {
        if (constraints != null && constraints.length > 0) {
            for (String constraint : constraints) {
                entity.addConstraint(constraint);
            }
        }
        return this;
    }

    public TargetDescriptorBuilder withId(String id) {
        entity.setId(id);
        return this;
    }

    @Override
    protected TargetDescriptor addToParent(TargetDescriptor builtEntity) {
        builtEntity.setParentId(this.parent.entity.getId());
        this.parent.entity.addTarget(builtEntity);
        return builtEntity;
    }
}
