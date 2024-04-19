package ro.cs.tao.execution.persistence;

import ro.cs.tao.execution.model.ResourceUsageReport;
import ro.cs.tao.persistence.EntityProvider;

public interface ResourceUsageReportProvider extends EntityProvider<ResourceUsageReport, Long> {

    ResourceUsageReport getByUserId(String userId);

}
