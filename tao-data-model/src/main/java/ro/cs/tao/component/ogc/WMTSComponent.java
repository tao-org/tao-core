package ro.cs.tao.component.ogc;

import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.docker.Container;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XmlRootElement(name = "wmstComponent")
public class WMTSComponent extends TaoComponent {
    private String remoteAddress;
    private String capabilityName;
    private List<ParameterDescriptor> parameters;
    private ProcessingComponentVisibility visibility;
    private boolean active;
    private String owner;
    private Container service;

    public WMTSComponent() { }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getCapabilityName() {
        return capabilityName;
    }

    public void setCapabilityName(String capabilityName) {
        this.capabilityName = capabilityName;
    }

    public List<ParameterDescriptor> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        return parameters;
    }

    public void setParameters(List<ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }

    public ProcessingComponentVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(ProcessingComponentVisibility visibility) {
        this.visibility = visibility;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Container getService() {
        return service;
    }

    public void setService(Container service) {
        this.service = service;
    }

    /**
     * Validates the parameter values against the parameter descriptors.
     */
    public void validate(Map<String, Object> parameterValues) throws ValidationException {
        if (parameterValues != null) {
            final List<ParameterDescriptor> parameterDescriptors =
                    getParameters().stream()
                                   .filter(d -> parameterValues.containsKey(d.getId()))
                                   .collect(Collectors.toList());
            for (ParameterDescriptor descriptor : parameterDescriptors) {
                descriptor.validate(parameterValues.get(descriptor.getId()));
            }
        }
    }

    @Override
    public WMTSComponent clone() throws CloneNotSupportedException {
        final WMTSComponent cloned = new WMTSComponent();
        cloned.label = this.label + " (copy)";
        cloned.version = this.version;
        cloned.description = this.description;
        cloned.authors = this.authors;
        cloned.copyright = this.copyright;
        cloned.nodeAffinity = this.nodeAffinity;
        cloned.remoteAddress = this.remoteAddress;
        cloned.capabilityName = this.capabilityName;
        if (this.parameters != null) {
            cloned.parameters = new ArrayList<>();
            for (ParameterDescriptor parameterDescriptor : this.parameters) {
                cloned.parameters.add(parameterDescriptor.clone());
            }
        }
        cloned.visibility = this.visibility;
        cloned.active = this.active;
        cloned.owner = this.owner;
        cloned.service = this.service;
        if (this.sources != null) {
            List<SourceDescriptor> clonedSources = new ArrayList<>(this.sources.size());
            for (SourceDescriptor sourceDescriptor : this.sources) {
                SourceDescriptor dClone = sourceDescriptor.clone();
                dClone.setParentId(cloned.getId());
                clonedSources.add(dClone);
            }
            cloned.setSources(clonedSources);
        }
        if (this.targets != null) {
            List<TargetDescriptor> clonedTargets = new ArrayList<>(this.targets.size());
            for (TargetDescriptor targetDescriptor : this.targets) {
                TargetDescriptor dClone = targetDescriptor.clone();
                dClone.setParentId(cloned.getId());
                clonedTargets.add(dClone);
            }
            cloned.setTargets(clonedTargets);
        }
        if (this.tags != null) {
            cloned.tags = new ArrayList<>(this.tags);
        }
        return cloned;
    }
}
