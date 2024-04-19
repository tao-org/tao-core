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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Udroiu
 */
public class ToolCommandsTokens {
    static final String MASTER_HOSTNAME = "#master_hostname#";
    static final String MASTER_USER = "#master_user#";
    static final String MASTER_PASS = "#master_pass#";
    static final String MASTER_SHARE = "#master_share#";
    static final String NODE_HOSTNAME = "#node_hostname#";
    static final String NODE_USER = "#node_user#";
    static final String NODE_PASSWORD = "#node_pass#";
    static final String NODE_SHARE = "#node_share#";
    static final String NODE_PROCESSORS_CNT = "#procs_cnt#";
    static final String INSTALL_SCRIPTS_ROOT_PATH = "#scripts_root_path#";
    static final String STEP_OUTPUT = "#step_output#";
    static final String DOCKER_REGISTRY = "#docker_registry#";

    private static final List<String> tokensList;

    static {
         tokensList = new ArrayList<>() {{
             add(MASTER_HOSTNAME); add(MASTER_USER); add(MASTER_PASS); add(MASTER_SHARE);
             add(NODE_HOSTNAME); add(NODE_USER); add(NODE_PASSWORD); add(NODE_SHARE); add(NODE_PROCESSORS_CNT);
             add(INSTALL_SCRIPTS_ROOT_PATH); add(STEP_OUTPUT); add(DOCKER_REGISTRY);
         }};
    }

    public static List<String> getDefinedTokensList() { return tokensList; }
}
