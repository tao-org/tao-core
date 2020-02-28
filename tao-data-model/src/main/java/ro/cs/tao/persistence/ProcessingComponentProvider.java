package ro.cs.tao.persistence;

import ro.cs.tao.Sort;
import ro.cs.tao.component.ProcessingComponent;

import java.util.List;

public interface ProcessingComponentProvider extends EntityProvider<ProcessingComponent, String> {

    ProcessingComponent get(String id, String containerId);
    List<ProcessingComponent> list(int pageNumber, int pageSize, Sort sort);
    List<ProcessingComponent> listUserProcessingComponents(String userName);
    List<ProcessingComponent> listUserScriptComponents(String userName);
    List<ProcessingComponent> listByLabel(String label);

}
