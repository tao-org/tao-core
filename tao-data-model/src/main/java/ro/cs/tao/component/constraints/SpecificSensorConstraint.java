package ro.cs.tao.component.constraints;

import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.serialization.ConstraintAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Set;

/**
 * Constraint that indicates that several data descriptors should have the same sensor value (i.e. Sentinel2)
 *
 * @author Cosmin Cara
 * @see DataDescriptor
 */
@Constraint(name = "Specific sensor")
@XmlJavaTypeAdapter(ConstraintAdapter.class)
public class SpecificSensorConstraint extends IOConstraint {

    private Set<String> synonims;

    public SpecificSensorConstraint(String value) {
        this.value = value;
    }

    public void setSynonims(Set<String> synonims) {
        this.synonims = synonims;
    }

    @Override
    public boolean check(DataDescriptor... args) { return true; }

    @Override
    public boolean check(SourceDescriptor source, TargetDescriptor target) {
        return source != null && target != null &&
                (source.getConstraints() == null ||
                 source.getConstraints().stream().anyMatch(c -> {
                     IOConstraint constraint = ConstraintFactory.create(c);
                     return constraint != null
                             && (this.value.equals(constraint.getValue())
                                 || (this.synonims != null && this.synonims.contains(constraint.getValue())));
                 }));
    }

    @Override
    public String toString() {
        return super.toString() + "[" + value + "]";
    }
}
