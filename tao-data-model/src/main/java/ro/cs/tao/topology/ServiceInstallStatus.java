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
