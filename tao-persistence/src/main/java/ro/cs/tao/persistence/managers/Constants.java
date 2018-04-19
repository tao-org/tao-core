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

package ro.cs.tao.persistence.managers;

public class Constants {
    /** Constant for the identifier member name of execution node entity */
    static final String NODE_IDENTIFIER_PROPERTY_NAME = "hostName";

    /** Constant for the identifier member id of container entity */
    static final String CONTAINER_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for the identifier member name of processing component entity */
    static final String COMPONENT_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for the identifier member name of EOProduct entity */
    static final String DATA_PRODUCT_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for the identifier member name of execution job entity */
    static final String JOB_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for the identifier member name of execution task entity */
    static final String TASK_IDENTIFIER_PROPERTY_NAME = "id";

    /** Constant for user messages page size */
    static final int MESSAGES_PAGE_SIZE = 10;

    /** Constant for the identifier member name of EOProduct entity */
    static final String MESSAGE_TIMESTAMP_PROPERTY_NAME = "timestamp";

    /** Constant for the identifier member name of workflow entity */
    static final String WORKFLOW_IDENTIFIER_PROPERTY_NAME = "id";
}
