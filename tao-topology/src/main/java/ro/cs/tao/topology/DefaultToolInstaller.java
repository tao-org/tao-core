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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.topology.xml.ToolInstallersConfigHandler;
import ro.cs.tao.topology.xml.ToolInstallersConfigParser;
import ro.cs.tao.utils.executors.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cosmin Udroiu
 */
public class DefaultToolInstaller extends TopologyToolInstaller {
    private List<ToolInstallConfig> toolInstallConfigs;
    private String installToolsRootPath;
    private NodeDescription masterNodeInfo;
    protected static Logger logger = Logger.getLogger(DefaultToolInstaller.class.getName());

    public DefaultToolInstaller() {
        String taoWorkingDir = getTaoWorkingDir();
        // extract the tools script dir to tao working dir
        extractResourceDir(taoWorkingDir, "tools_scripts");

        final String toolInstallCfgFile = ConfigurationManager.getInstance().getValue("topology.tool_install_config");
        InputStream is = this.getClass().getResourceAsStream(toolInstallCfgFile);
        this.toolInstallConfigs = ToolInstallersConfigParser.parse(is,
                new ToolInstallersConfigHandler("tool_install_configurations"));
        this.installToolsRootPath = taoWorkingDir + "/tools_scripts/";
    }

    @Override
    public String defaultId() { return "DefaultInstaller"; }

    @Override
    public void setMasterNodeDescription(NodeDescription masterNodeInfo) {
        this.masterNodeInfo = masterNodeInfo;
    }

    @Override
    public ToolInstallStatus installNewNode(NodeDescription info) throws TopologyException {
        ToolInstallStatus installStatus = new ToolInstallStatus();
        for (ToolInstallConfig toolCfg : toolInstallConfigs) {
            try {
                installStatus.setToolName(toolCfg.getName());
                invokeSteps(info, toolCfg, false);
                installStatus.setStatus(ServiceStatus.INSTALLED);
            } catch (TopologyException tex) {
                installStatus.setStatus(ServiceStatus.ERROR);
                installStatus.setReason(tex.getMessage());
            }
            info.addServiceStatus(new NodeServiceStatus(new ServiceDescription(installStatus.getToolName(),
                toolCfg.getVersion(),
                toolCfg.getDescription()),
              installStatus.getStatus()));
        }
        return installStatus;
    }

    @Override
    public ToolInstallStatus uninstallNode(NodeDescription info) throws TopologyException {
        ToolInstallStatus installStatus = new ToolInstallStatus();
        for (ToolInstallConfig toolCfg : toolInstallConfigs) {
            try {
                installStatus.setToolName(toolCfg.getName());
                invokeSteps(info, toolCfg, true);
                installStatus.setStatus(ServiceStatus.UNINSTALLED);
            } catch (TopologyException tex) {
                installStatus.setStatus(ServiceStatus.ERROR);
                installStatus.setReason(tex.getMessage());
            }
        }
        return installStatus;
    }

    @Override
    public void editNode(NodeDescription nodeInfo) throws TopologyException {
        // TODO:
    }

    private void invokeSteps(NodeDescription info, ToolInstallConfig toolCfg, boolean uninstall) throws TopologyException {
        List<ToolInstallStep> steps = uninstall ? toolCfg.getUninstallSteps() : toolCfg.getInstallSteps();
        for (ToolInstallStep step: steps) {
            int retCode = doStepInvocation(info, steps, step);
            if (retCode != ToolInvocationCodes.OK) {
                if (step.getIgnoreErr()) {
                    logger.warning("Step [[" + step.getName() + "]] was not successful but the failure is ignored as configured!");
                } else {
                    throw new TopologyException(String.format("Tool %s installation failed with code %d",
                                                              toolCfg.getName(), retCode)) {{
                        addAdditionalInfo("Node", info);
                        addAdditionalInfo("Code", retCode);
                    }};
                }
            }
        }
    }

    private int doStepInvocation(NodeDescription nodeDescr, List<ToolInstallStep> allSteps, ToolInstallStep curStep) throws TopologyException {
        ExecutorType invokeType = curStep.getInvocationType();
        List<String> argsList = new ArrayList<>();
        String stepInvocationCmd = curStep.getInvocationCommand();
        // Replace the potential tokens in the command
        stepInvocationCmd = replaceTokensInCmd(stepInvocationCmd, nodeDescr, allSteps);

        // split the command but preserving the entities between double quotes
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(stepInvocationCmd);
        while (m.find()) {
            argsList.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
        }

        String hostName = curStep.getHostName();
        String user = curStep.getUser();
        String pass = curStep.getPass();
        if (invokeType != ExecutorType.PROCESS) {
            if (hostName == null && nodeDescr.getId() != null) {
                hostName = nodeDescr.getId();
            }
            if (user == null && nodeDescr.getUserName() != null) {
                user = nodeDescr.getUserName();
            }
            if (pass == null && nodeDescr.getUserPass() != null) {
                pass = nodeDescr.getUserPass();
            }
        } else {
            // For process invocation type normally we will have always localhost
            hostName = "localhost";
            if (user == null || "".equals(user)) {
                user = masterNodeInfo.getUserName();
            }
            if (pass == null || "".equals(pass)) {
                pass = masterNodeInfo.getUserPass();
            }
        }

        ExecutionUnit job = null;
        switch (invokeType) {
            case SSH2:
                job = new ExecutionUnit(ExecutorType.SSH2,
                        hostName, user, pass,
                        argsList, curStep.getExecutionModeMode().value(),
                        curStep.getSshMode());
                break;
            case PROCESS:
                job = new ExecutionUnit(ExecutorType.PROCESS,
                        hostName, user, pass,
                        argsList, curStep.getExecutionModeMode().value(),
                        SSHMode.EXEC);
                break;
            default:
                break;
        }
        if (job != null) {
            OutputConsumer consumer = new StepExecutionOutputConsumer(curStep);
            // wait for execution timeout
            return Executor.execute(consumer, curStep.getExecutionTimeout(), job);
        }
        return ToolInvocationCodes.INVALID_INVOCATION_TYPE;
    }

    /**
     * Replaces the tokens in the command with the ones supported by the application
     * @param cmd
     * @return
     */
    private String replaceTokensInCmd(String cmd, NodeDescription info, List<ToolInstallStep> allSteps) throws TopologyException {
        List<String> tokens = ToolCommandsTokens.getDefinedTokensList();
        for (String token: tokens) {
            if(cmd.contains(token)) {
                String replacementStr = null;
                switch (token) {
                    case ToolCommandsTokens.MASTER_HOSTNAME:
                        replacementStr = masterNodeInfo.getId();
                        break;
                    case ToolCommandsTokens.MASTER_USER:
                        replacementStr = masterNodeInfo.getUserName();
                        break;
                    case ToolCommandsTokens.MASTER_PASS:
                        replacementStr = masterNodeInfo.getUserPass();
                        break;
                    case ToolCommandsTokens.NODE_HOSTNAME:
                        replacementStr = info.getId();
                        break;
                    case ToolCommandsTokens.NODE_USER:
                        replacementStr = info.getUserName();
                        break;
                    case ToolCommandsTokens.NODE_PASSWORD:
                        replacementStr = info.getUserPass();
                        break;
                    case ToolCommandsTokens.NODE_PROCESSORS_CNT:
                        replacementStr = String.valueOf(info.getProcessorCount());
                        break;
                    case ToolCommandsTokens.INSTALL_SCRIPTS_ROOT_PATH:
                        replacementStr = installToolsRootPath;
                        break;
                    case ToolCommandsTokens.STEP_OUTPUT:
                        cmd = handleStepOutputReplacements(cmd, allSteps);
                        break;
                }
                if(replacementStr != null) {
                    replacementStr = replacementStr.replace("\\", "\\\\");
                    cmd = cmd.replaceAll(token, replacementStr);
                }
            }
        }

        return cmd;
    }

    private String handleStepOutputReplacements(String cmd, List<ToolInstallStep> allSteps) throws TopologyException {
        int curSearchIdx = 0;
        while (true) {
            int idx = cmd.indexOf(ToolCommandsTokens.STEP_OUTPUT);
            if (idx >= 0) {
                int stepLastDiezIdx = cmd.indexOf('#', idx + ToolCommandsTokens.STEP_OUTPUT.length());
                if(stepLastDiezIdx == -1) {
                    throw new TopologyException("Step output tag not specifying the step number followed by #");
                }
                curSearchIdx = stepLastDiezIdx+1;
                String stepName = cmd.substring(idx+ToolCommandsTokens.STEP_OUTPUT.length(), stepLastDiezIdx);
                ToolInstallStep sourceStep = getStepByName(stepName, allSteps);
                List<String> msgs = sourceStep.getExecutionMessages();
                if(msgs.size() > 0) {
                    cmd = cmd.substring(0, idx) + msgs.get(0).trim() + cmd.substring(stepLastDiezIdx+1);
                }
            } else {
                break;
            }
        }
        return cmd;
    }

    private ToolInstallStep getStepByName(String stepName, List<ToolInstallStep> allSteps) throws TopologyException {
        for(ToolInstallStep step: allSteps) {
            if(step.getName().equals(stepName)) {
                return step;
            }
        }
        throw new TopologyException("No such referenced step name " + stepName);
    }

    private String getTaoWorkingDir() {
        String workingDirectory;
        if (SystemUtils.IS_OS_WINDOWS) {
            workingDirectory = System.getenv("AppData");
        } else {
            try {
                workingDirectory = Files.createTempDirectory("TAO").toString();
                //workingDirectory = workingDirectory + "/TAO_" + new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date(System.currentTimeMillis()));
            } catch (IOException e) {
                throw new TopologyException("Cannot create TAO temporary directory for Topology scripts");
            }
        }
        String taoUserDir = workingDirectory + "/TAO";
        File taoUserDirFile = new File(taoUserDir);
        if (!taoUserDirFile.exists()) {
            if (taoUserDirFile.mkdir()) {
                logger.fine("TAO user working directory created :: " + taoUserDir);
            } else {
                logger.severe("TAO user working directory cannot be created in " + taoUserDir);
            }
        }
        return taoUserDir;
    }

    private static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (sourceFolder.isDirectory()) {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists()) {
                if (destinationFolder.mkdir()) {
                    logger.fine("Destination directory created :: " + destinationFolder);
                } else {
                    logger.severe("TAO destination directory cannot be created in " + destinationFolder);
                }
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();
            //Iterate over all files and copy them to destinationFolder one by one
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(sourceFolder, file);
                    File destFile = new File(destinationFolder, file);
                    //Recursive function call
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            //Copy the file content from one place to another only if not existing or different
            if (!destinationFolder.exists() || sourceFolder.length() != destinationFolder.length()) {
                Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.fine("File copied :: " + destinationFolder);
            }
        }
    }

    private void extractResourceDir(String targetDir, String dirNameToExtract) {
        try {
            // read the tool install configurations
            final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

            if(jarFile.isFile()) {  // Run with JAR file
                final JarFile jar = new JarFile(jarFile);
                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                String extractedDir = dirNameToExtract + "/";
                while(entries.hasMoreElements()) {
                    java.util.jar.JarEntry file = (java.util.jar.JarEntry) entries.nextElement();
                    if (file.getName().startsWith(extractedDir)) {
                        java.io.File f = new java.io.File(targetDir + java.io.File.separator + file.getName());
                        if (file.isDirectory()) { // if its a directory, create it
                            f.mkdir();
                            continue;
                        }
                        java.io.InputStream is = jar.getInputStream(file); // get the input stream
                        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                        IOUtils.copy(is,fos);
                        fos.close();
                        is.close();
                    }
                }
                jar.close();
            } else { // Run with IDE
                final URL url = getClass().getResource("/" + dirNameToExtract + "/");
                if (url != null) {
                    try {
                        copyFolder(new File(url.toURI()), new File(targetDir + File.separator + dirNameToExtract));
                    } catch (URISyntaxException ex) {
                        // never happens
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
