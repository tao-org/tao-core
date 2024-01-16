package ro.cs.tao.services.interfaces;

import ro.cs.tao.Sort;
import ro.cs.tao.component.ogc.WMSComponent;
import ro.cs.tao.services.model.component.WMSComponentInfo;

import java.util.List;

public interface WMSComponentService extends CRUDService<WMSComponent, String> {

    List<WMSComponentInfo> getWPSComponents();

    List<WMSComponentInfo> getWPSComponents(int pageNumber, int pageSize, Sort sort);

}
