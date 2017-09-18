package ro.cs.tao.topology;

import org.apache.commons.io.IOUtils;
import ro.cs.tao.topology.xml.ToolInstallersConfigHandler;
import ro.cs.tao.topology.xml.ToolInstallersConfigParser;
import ro.cs.tao.utils.Platform;
import ro.cs.tao.utils.executors.ExecutionUnit;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.OutputConsumer;
import ro.cs.tao.utils.executors.SSHMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cosmin on 8/7/2017.
 */
public class DefaultToolInstaller implements ITopologyToolInstaller {
    private List<ToolInstallConfig> toolInstallConfigs;
    private String installToolsRootPath;
    private NodeDescription masterNodeInfo;
    Logger logger;

    public DefaultToolInstaller() {
        String taoWorkingDir = getTaoWorkingDir();
        // extract the tools script dir to tao working dir
        extractResourceDir(taoWorkingDir, "tools_scripts");

        InputStream is = this.getClass().getResourceAsStream("/DefaultToolInstallConfig.xml");
        this.toolInstallConfigs = ToolInstallersConfigParser.parse(is,
                new ToolInstallersConfigHandler("tool_install_configurations"));
        this.installToolsRootPath = taoWorkingDir + "/tools_scripts/";
    }

    @Override
    public void setMasterNodeDescription(NodeDescription masterNodeInfo) {
        this.masterNodeInfo = masterNodeInfo;
    }

    @Override
    public void installNewNode(NodeDescription info) throws TopologyException {
        for(ToolInstallConfig toolCfg: toolInstallConfigs) {
            invokeSteps(info, toolCfg, false);
        }
    }

    @Override
    public void uninstallNode(NodeDescription info) throws TopologyException {
        for(ToolInstallConfig toolCfg: toolInstallConfigs) {
            invokeSteps(info, toolCfg, true);
        }
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
                    logger.info("Step [[" + step.getName() + "]] was not successful but the failure is ignored as configured!");
                } else {
                    throw new TopologyException("Tool " + toolCfg.getName() + " installation failed installation failed") {{
                        addAdditionalInfo("Node", info);
                        addAdditionalInfo("Code", retCode);
                    }};
                }
            }
        }
    }

    private int doStepInvocation(NodeDescription info, List<ToolInstallStep> allSteps, ToolInstallStep curStep) throws TopologyException {
        ExecutorType invokeType = curStep.getInvocationType();
        List<String> argsList = new ArrayList<>();
        String stepInvocationCmd = curStep.getInvocationCommand();
        // Replace the potential tokens in the command
        stepInvocationCmd = replaceTokensInCmd(stepInvocationCmd, info, allSteps);

        // split the command but preserving the entities between double quotes
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(stepInvocationCmd);
        while (m.find()) {
            argsList.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
        }

        System.out.println(argsList);

        String hostName = curStep.getHostName() == null ? info.getHostName() : curStep.getHostName();
        String user = curStep.getUser() == null ? info.getUserName() : curStep.getUser();
        String pass = curStep.getPass() == null ? info.getUserPass() : curStep.getPass();

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
            return Executor.execute(consumer, 10, job);
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
                        replacementStr = masterNodeInfo.getHostName();
                        break;
                    case ToolCommandsTokens.NODE_HOSTNAME:
                        replacementStr = info.getHostName();
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
        Platform platform = Platform.getCurrentPlatform();
        String workingDirectory;
        if(platform.getId() == Platform.ID.win) {
            workingDirectory = System.getenv("AppData");
        } else {
            workingDirectory = System.getProperty("user.home");
        }
        String taoUserDir = workingDirectory + "/TAO";
        File taoUserDirFile = new File(taoUserDir);
        if (!taoUserDirFile.exists()) {
            taoUserDirFile.mkdir();
            System.out.println("Directory created :: " + taoUserDir);
        }
        return taoUserDir;
    }

    private static void copyFolder(File sourceFolder, File destinationFolder) throws IOException
    {
        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (sourceFolder.isDirectory())
        {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists())
            {
                destinationFolder.mkdir();
                System.out.println("Directory created :: " + destinationFolder);
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            for (String file : files)
            {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                //Recursive function call
                copyFolder(srcFile, destFile);
            }
        }
        else
        {
            //Copy the file content from one place to another
            Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied :: " + destinationFolder);
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
