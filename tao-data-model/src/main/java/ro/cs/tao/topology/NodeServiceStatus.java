package ro.cs.tao.topology;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Association between the execution nodes and the installed services, together with their status
 *
 * @author  Oana H.
 */
@XmlRootElement(name = "serviceStatus")
public class NodeServiceStatus implements Serializable {

    private ServiceDescription serviceDescription;
    private ServiceStatus status;

    public NodeServiceStatus() {
    }

    public NodeServiceStatus(ServiceDescription serviceDescription, ServiceStatus status) {
        this.serviceDescription = serviceDescription;
        this.status = status;
    }
    @XmlElement(name = "service")
    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    @XmlElement(name = "status")
    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }
}
