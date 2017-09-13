/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */
package ro.cs.tao.topology.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import ro.cs.tao.topology.ToolInstallConfig;
import ro.cs.tao.topology.ToolInstallStep;
import ro.cs.tao.topology.TopologyException;
import ro.cs.tao.utils.executors.ExecutionMode;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.SSHMode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by cosmin on 8/8/2017.
 */
public class ToolInstallersConfigHandler extends DefaultHandler {

    private List<ToolInstallConfig> results;
    protected StringBuilder buffer;
    protected String recordElement;
    protected ToolInstallConfig currentCfg;
    protected ToolInstallStep currentStep;
    protected boolean rootElementFound;
    protected boolean installStepsElementFound;
    protected boolean uninstallStepsElementFound;
    private String identifiedElement;
    protected Logger logger = Logger.getLogger(ToolInstallersConfigHandler.class.getName());

    public ToolInstallersConfigHandler(String recordElementName) {
        super();
        this.recordElement = recordElementName;
    }

    List<ToolInstallConfig> getResults() {
        return results;
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            results = new ArrayList<>();
            buffer = new StringBuilder();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        buffer.append(new String(ch, start, length).replace("\n", ""));
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        buffer.setLength(0);
        if (this.recordElement.equals(qName)) {
            rootElementFound = true;
        }
        handleStartElement(qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (this.recordElement.equals(qName)) {
            rootElementFound = false;
            // order the results according to their priority
            results.sort((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()));
        }
        handleEndElement(qName);
        buffer.setLength(0);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        String error = e.getMessage();
        if (!error.contains("no grammar found")) {
            logger.warning(error);
        }
    }

    protected void handleStartElement(String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "tool_install_config":
                currentCfg = new ToolInstallConfig();
                break;
            case "name":
                if (currentCfg == null && currentStep == null) {
                    throw new SAXException("Invalid element name without config or step");
                }
                break;
            case "priority":
                if (currentCfg == null) {
                    throw new SAXException("Invalid element priority without config");
                }
                break;
            case "install_steps":
                if (currentCfg != null) {
                    installStepsElementFound = true;
                } else {
                    throw new SAXException("Invalid element steps without config");
                }
                break;
            case "uninstall_steps":
                if (currentCfg != null) {
                    uninstallStepsElementFound = true;
                } else {
                    throw new SAXException("Invalid element steps without config");
                }
                break;

            case "step":
                if (installStepsElementFound || uninstallStepsElementFound) {
                    currentStep = new ToolInstallStep();
                } else {
                    throw new SAXException("Invalid element step without steps");
                }
                break;
            case "order_id":
            case "invocation_type":
            case "ip":
            case "user":
            case "pass":
            case "ignore_err":
            case "command":
                if (currentStep == null) {
                    throw new SAXException("Invalid element " + qName + " without step");
                }
                break;
        }
    }

    protected void handleEndElement(String qName) {
        final String elementValue = buffer.toString().trim();
        switch (qName) {
            case "tool_install_config":
                results.add(currentCfg);
                currentCfg = null;
                break;
            case "name":
                if(currentStep != null) {
                    currentStep.setName(elementValue);
                } else {
                    currentCfg.setName(elementValue);
                }
                break;
            case "priority":
                currentCfg.setPriority(Integer.parseInt(elementValue));
                break;
            case "install_steps":
                checkStepsConsistency(currentCfg.getInstallSteps());
                installStepsElementFound = false;
                break;
            case "uninstall_steps":
                checkStepsConsistency(currentCfg.getUninstallSteps());
                uninstallStepsElementFound = false;
                break;
            case "step":
                if (installStepsElementFound) {
                    currentCfg.addToolInstallStep(currentStep);
                } else {
                    currentCfg.addToolUninstallStep(currentStep);
                }
                currentStep = null;
                break;
            case "order_id":
                currentStep.setOrderId(Integer.parseInt(elementValue));
                break;
            case "invocation_type":
                currentStep.setInvocationType(ExecutorType.valueOf(elementValue.toUpperCase()));
                break;
            case "ssh_mode":
                currentStep.setSshMode(SSHMode.valueOf(elementValue.toUpperCase()));
                break;
            case "execution_mode":
                currentStep.setExecutionModeMode(ExecutionMode.valueOf(elementValue.toUpperCase()));
                break;
            case "host":
                currentStep.setHostName(elementValue);
                break;
            case "user":
                currentStep.setUser(elementValue);
                break;
            case "pass":
                currentStep.setPass(elementValue);
                break;
            case "command":
                currentStep.setInvocationCommand(elementValue);
                break;
            case "ignore_err":
                currentStep.setIgnoreErr(Boolean.valueOf(elementValue));
                break;
        }
    }

    private void checkStepsConsistency(List<ToolInstallStep> steps) {
        for(ToolInstallStep step: steps) {
            int stepOrderId = step.getOrderId();
            for(ToolInstallStep step2: steps) {
                if(step != step2 && stepOrderId == step2.getOrderId()) {
                    throw new TopologyException("Step id " + stepOrderId + " is defined several times");
                }
            }
        }
    }
}
