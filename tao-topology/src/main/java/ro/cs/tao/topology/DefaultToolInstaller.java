package ro.cs.tao.topology;

import ro.cs.tao.topology.xml.ToolInstallersConfigHandler;
import ro.cs.tao.topology.xml.ToolInstallersConfigParser;
import ro.cs.tao.utils.executors.ExecutionUnit;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.OutputConsumer;
import ro.cs.tao.utils.executors.SSHMode;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        // read the tool install configurations
        URL cfgUrl = this.getClass().getResource("/DefaultToolInstallConfig.xml");
        this.toolInstallConfigs = ToolInstallersConfigParser.parse(cfgUrl.getFile(),
                new ToolInstallersConfigHandler("tool_install_configurations"));
        try {
            this.installToolsRootPath = new File(cfgUrl.toURI()).getParent();
            this.installToolsRootPath += "/tools_scripts/";
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setMasterNodeDescription(NodeDescription masterNodeInfo) {
        this.masterNodeInfo = masterNodeInfo;
    }

    @Override
    public void installNewNode(NodeDescription info) {
        for(ToolInstallConfig toolCfg: toolInstallConfigs) {
            invokeSteps(info, toolCfg, false);
        }
    }

    @Override
    public void uninstallNode(NodeDescription info) {
        for(ToolInstallConfig toolCfg: toolInstallConfigs) {
            invokeSteps(info, toolCfg, true);
        }
    }

    @Override
    public void editNode(NodeDescription nodeInfo) {
        // TODO:
    }

    private void invokeSteps(NodeDescription info, ToolInstallConfig toolCfg, boolean uninstall) {
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

    private int doStepInvocation(NodeDescription info, List<ToolInstallStep> allSteps, ToolInstallStep curStep) {
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

        String ipAddr = curStep.getIp() == null ? info.getIpAddr() : curStep.getIp();
        String user = curStep.getUser() == null ? info.getUserName() : curStep.getUser();
        String pass = curStep.getPass() == null ? info.getUserPass() : curStep.getPass();

        ExecutionUnit job = null;
        switch (invokeType) {
            case SSH2:
                job = new ExecutionUnit(ExecutorType.SSH2,
                        ipAddr, user, pass,
                        argsList, curStep.getExecutionModeMode().value(),
                        curStep.getSshMode());
                break;
            case PROCESS:
                job = new ExecutionUnit(ExecutorType.PROCESS,
                        ipAddr, user, pass,
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
    private String replaceTokensInCmd(String cmd, NodeDescription info, List<ToolInstallStep> allSteps) {
        List<String> tokens = ToolCommandsTokens.getDefinedTokensList();
        for (String token: tokens) {
            if(cmd.contains(token)) {
                String replacementStr = null;
                switch (token) {
                    case ToolCommandsTokens.MASTER_HOSTNAME:
                        replacementStr = masterNodeInfo.getHostName();
                        break;
                    case ToolCommandsTokens.MASTER_IP_ADDR:
                        replacementStr = masterNodeInfo.getIpAddr();
                        break;
                    case ToolCommandsTokens.NODE_HOSTNAME:
                        replacementStr = info.getHostName();
                        break;
                    case ToolCommandsTokens.NODE_IP_ADDR:
                        replacementStr = info.getIpAddr();
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

    private String handleStepOutputReplacements(String cmd, List<ToolInstallStep> allSteps) {
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

    private ToolInstallStep getStepByName(String stepName, List<ToolInstallStep> allSteps) {
        for(ToolInstallStep step: allSteps) {
            if(step.getName().equals(stepName)) {
                return step;
            }
        }
        throw new TopologyException("No such referenced step name " + stepName);
    }
}
