package ro.cs.tao.serialization;

import ro.cs.tao.component.constraints.IOConstraint;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Cosmin Cara
 */
public class ConstraintAdapter extends XmlAdapter<IOConstraint, String> {
    @Override
    public String unmarshal(IOConstraint v) throws Exception {
        return v == null ? null : v.getClass().getName();
    }

    @Override
    public IOConstraint marshal(String v) throws Exception {
        return v == null ? null : (IOConstraint) Class.forName(v).newInstance();
    }
}
