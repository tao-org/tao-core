package ro.cs.tao.component.constraints;

import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.serialization.ConstraintAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Constraint that indicates that the given source and target descriptors should have
 * the same cardinality.
 *
 * @author Cosmin Cara
 */
@Constraint(name = "Same cardinality")
@XmlJavaTypeAdapter(ConstraintAdapter.class)
public class SelfCardinalityConstraint extends IOConstraint {
    @Override
    public boolean check(DataDescriptor... args) {
        return true;
    }

    @Override
    public boolean check(SourceDescriptor source, TargetDescriptor target) {
        return source != null && target != null && source.getCardinality() == target.getCardinality();
    }
}
