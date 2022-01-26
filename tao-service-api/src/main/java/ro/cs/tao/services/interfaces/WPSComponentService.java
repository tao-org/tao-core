package ro.cs.tao.services.interfaces;

import ro.cs.tao.Sort;
import ro.cs.tao.component.WPSComponent;
import ro.cs.tao.services.model.component.WPSComponentInfo;

import java.util.List;

public interface WPSComponentService extends CRUDService<WPSComponent, String> {

    List<WPSComponentInfo> getWPSComponents();

    List<WPSComponentInfo> getWPSComponents(int pageNumber, int pageSize, Sort sort);

}
