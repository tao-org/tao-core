/*
 * Copyright (C) 2017 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.topology;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
