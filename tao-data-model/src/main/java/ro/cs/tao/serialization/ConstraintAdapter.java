package ro.cs.tao.serialization;

import ro.cs.tao.component.constraints.IOConstraint;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Cosmin Cara
 */
public class ConstraintAdapter<T> extends XmlAdapter<IOConstraint<T>, String> {
    @Override
    public String unmarshal(IOConstraint<T> v) throws Exception {
        return v == null ? null : v.getClass().getName();
    }

    @Override
    public IOConstraint<T> marshal(String v) throws Exception {
        return v == null ? null : (IOConstraint<T>) Class.forName(v).newInstance();
    }
}
