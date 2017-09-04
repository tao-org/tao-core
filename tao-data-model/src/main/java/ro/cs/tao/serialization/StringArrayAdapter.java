package ro.cs.tao.serialization;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Cosmin Cara
 */
public class StringArrayAdapter extends XmlAdapter<String[], String> {
    @Override
    public String unmarshal(String[] v) throws Exception {
        return v == null ? null : "[" + String.join(",", v) + "]";
    }

    @Override
    public String[] marshal(String v) throws Exception {
        if (v == null || v.isEmpty()) {
            return null;
        }
        v = v.replace("[", "").replace("]", "");
        return v.split(",");
    }
}
