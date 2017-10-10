package ro.cs.tao.topology;

/**
 * @author Cosmin Cara
 */
public class ToolInstallStatus {
    private String toolName;
    private ServiceStatus status;
    private String reason;

    public ToolInstallStatus() {
        this.status = ServiceStatus.NOT_FOUND;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
