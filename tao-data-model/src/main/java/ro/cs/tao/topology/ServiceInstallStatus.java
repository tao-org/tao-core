/*
 * Copyright (C) 2018 CS ROMANIA
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

/**
 * Represents the installation status of a service, including its name,
 * current status, and the reason for its state.
 *
 * This class is used to encapsulate information about whether a service
 * is installed, uninstalled, or in another state, and provides methods
 * for setting and retrieving its attributes.
 *
 * The following statuses are predefined:
 * - NODE_ADDED: Indicates that the service "Node" has been installed.
 * - NODE_REMOVED: Indicates that the service "Node" has been uninstalled.
 *
 * Attributes:
 * - serviceName: The name of the service whose status is being tracked.
 * - status: The current status of the service (e.g., INSTALLED, UNINSTALLED, NOT_FOUND).
 * - reason: A description or message providing additional details about the service's status.
 *
 * @author Cosmin Cara
 */
public class ServiceInstallStatus {
    public static final ServiceInstallStatus NODE_ADDED = new ServiceInstallStatus() {{
        setServiceName("Node");
        setStatus(ServiceStatus.INSTALLED);
    }};
    public static final ServiceInstallStatus NODE_REMOVED = new ServiceInstallStatus() {{
        setServiceName("Node");
        setStatus(ServiceStatus.UNINSTALLED);
    }};
    private String serviceName;
    private ServiceStatus status;
    private String reason;

    public ServiceInstallStatus() {
        this.status = ServiceStatus.NOT_FOUND;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
