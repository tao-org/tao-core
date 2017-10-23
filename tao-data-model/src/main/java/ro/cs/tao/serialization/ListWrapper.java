package ro.cs.tao.serialization;

import javax.xml.bind.annotation.XmlAnyElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class ListWrapper<T> {

    private List<T> items;

    public ListWrapper() {
        items = new ArrayList<>();
    }

    public ListWrapper(List<T> items) {
        this.items = items;
    }

    @XmlAnyElement(lax=true)
    public List<T> getItems() {
        return items;
    }

}
