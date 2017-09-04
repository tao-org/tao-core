package ro.cs.tao.serialization;

import ro.cs.tao.component.constraints.Constraint;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Cosmin Cara
 */
public class ConstraintAdapter<T> extends XmlAdapter<Constraint<T>, String> {
    @Override
    public String unmarshal(Constraint<T> v) throws Exception {
        return v == null ? null : v.getClass().getName();
    }

    @Override
    public Constraint<T> marshal(String v) throws Exception {
        return v == null ? null : (Constraint<T>) Class.forName(v).newInstance();
    }
}
