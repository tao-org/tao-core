package ro.cs.tao.workflow;

public interface ExternalGraphConverter {
    WorkflowDescriptor convert(String jsonGraph, String container) throws Exception;
}
