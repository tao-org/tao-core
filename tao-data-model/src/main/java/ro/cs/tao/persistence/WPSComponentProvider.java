package ro.cs.tao.persistence;

import ro.cs.tao.Sort;
import ro.cs.tao.component.WPSComponent;

import java.util.List;

public interface WPSComponentProvider extends EntityProvider<WPSComponent, String> {

    WPSComponent get(String id);
    List<WPSComponent> list(int pageNumber, int pageSize, Sort sort);

}
