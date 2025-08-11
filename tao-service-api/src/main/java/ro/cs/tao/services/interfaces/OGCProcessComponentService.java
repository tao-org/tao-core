package ro.cs.tao.services.interfaces;

import ro.cs.tao.Sort;
import ro.cs.tao.component.ogc.OGCProcessComponent;
import ro.cs.tao.services.model.component.OGCProcessComponentInfo;

import java.util.List;

public interface OGCProcessComponentService extends CRUDService<OGCProcessComponent, String> {

    List<OGCProcessComponentInfo> getOGCProcessComponents();

    List<OGCProcessComponentInfo> getOGCProcessComponents(int pageNumber, int pageSize, Sort sort);

}
