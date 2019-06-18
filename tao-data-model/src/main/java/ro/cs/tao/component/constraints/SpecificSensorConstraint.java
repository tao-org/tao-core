package ro.cs.tao.component.constraints;

import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.serialization.ConstraintAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Constraint that indicates that several data descriptors should have the same sensor value (i.e. Sentinel2)
 *
 * @author Cosmin Cara
 * @see DataDescriptor
 */
@Constraint(name = "Same sensor")
@XmlJavaTypeAdapter(ConstraintAdapter.class)
public class SpecificSensorConstraint extends IOConstraint {

    public SpecificSensorConstraint(String value) {
        this.value = value;
    }

    @Override
    public boolean check(DataDescriptor... args) { return true; }

    @Override
    public boolean check(SourceDescriptor source, TargetDescriptor target) {
        return source != null && target != null &&
                (source.getConstraints() == null ||
                 source.getConstraints().stream().anyMatch(c -> {
                     IOConstraint constraint = ConstraintFactory.create(c);
                     return constraint != null && this.value.equals(constraint.getValue());
                 }));
    }
}
