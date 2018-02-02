package ro.cs.tao.component;

import ro.cs.tao.eodata.EOData;
import ro.cs.tao.security.SecurityContext;
import ro.cs.tao.security.SystemSecurityContext;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;

/**
 * Base class for TAO components. Components can be:
 * - processing components
 * - data source components
 *
 * @author Cosmin Cara
 */
public abstract class TaoComponent extends Identifiable {
    protected String label;
    protected String version;
    protected String description;
    protected String authors;
    protected String copyright;
    protected String nodeAffinity;

    protected SourceDescriptor[] sources;
    protected TargetDescriptor[] targets;

    private SecurityContext securityContext;

    /**
     * Returns the display friendly name of this component
     */
    public String getLabel() {
        return label;
    }
    /**
     * Sets the display friendly name of this component
     */
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * Returns the version of this component
     */
    public String getVersion() {
        return version;
    }
    /**
     * Sets the version of this component
     */
    public void setVersion(String version) {
        this.version = version;
    }
    /**
     * Returns the description of this component
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Returns the authors of this component
     */
    public String getAuthors() {
        return authors;
    }
    /**
     * Sets the authors of this component
     */
    public void setAuthors(String authors) {
        this.authors = authors;
    }
    /**
     * Returns the copyright of this component
     */
    public String getCopyright() {
        return copyright;
    }
    /**
     * Sets the copyright of this component
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    /**
     * Returns the node name for which this component has affinity.
     */
    public String getNodeAffinity() { return nodeAffinity; }
    /**
     * Sets the node for which this component has affinity
     * @param nodeAffinity  The name of the topology node
     */
    public void setNodeAffinity(String nodeAffinity) { this.nodeAffinity = nodeAffinity; }
    /**
     * Returns the inputs of this component
     */
    @XmlElementWrapper(name = "inputs")
    public SourceDescriptor[] getSources() {
        return sources;
    }
    /**
     * Sets the inputs of this component
     * @param sources   An array of source (input) descriptors
     */
    public void setSources(SourceDescriptor[] sources) { this.sources = sources; }
    /**
     * Sets the number of inputs of this component. If some inputs already exist, the array will be resized (either
     * truncated or expanded) to the new value, potentially keeping existing values.
     *
     * @param value     The new dimension for the inputs array
     */
    public void setSourcesCount(int value) {
        if (this.sources == null) {
            this.sources = new SourceDescriptor[value];
        } else {
            this.sources = Arrays.copyOf(this.sources, value);
        }
    }
    /**
     * Adds an input to this component
     */
    public void addSource(SourceDescriptor source) {
        source.setParent(this);
        if (this.sources != null) {
            this.sources = Arrays.copyOf(this.sources, this.sources.length + 1);
            this.sources[this.sources.length - 1] = source;
        } else {
            this.sources = new SourceDescriptor[] { source };
        }
    }
    /**
     * Removes an input of this component
     */
    public void removeSource(SourceDescriptor source) {
        source.setParent(null);
        if (this.sources != null) {
            this.sources = Arrays.stream(this.sources)
                    .filter(s -> {
                        EOData src = s.getData();
                        EOData ref = source.getData();
                        return (src.getId() != null && !src.getId().equals(ref.getId())) ||
                                (src.getName() != null && !src.getName().equals(ref.getName()));
                    }).toArray(SourceDescriptor[]::new);
        }
    }
    /**
     * Returns the outputs of this component
     */
    @XmlElementWrapper(name = "outputs")
    public TargetDescriptor[] getTargets() {
        return targets;
    }
    /**
     * Sets the outputs of this component
     *
     * @param targets   An array of target (output) descriptors
     */
    public void setTargets(TargetDescriptor[] targets) { this.targets = targets; }
    /**
     * Sets the number of outputs of this component. If some outputs already exist, the array will be resized (either
     * truncated or expanded) to the new value, potentially keeping existing values.
     *
     * @param value     The new dimension for the outputs array
     */
    @XmlTransient
    public void setTargetCount(int value) {
        if (this.targets == null) {
            this.targets = new TargetDescriptor[value];
        } else {
            this.targets = Arrays.copyOf(this.targets, value);
        }
    }
    /**
     * Adds an output to this component.
     *
     * @param target    The output descriptor to be added
     */
    public void addTarget(TargetDescriptor target) {
        target.setParent(this);
        if (this.targets != null) {
            this.targets = Arrays.copyOf(this.targets, this.targets.length + 1);
            this.targets[this.targets.length - 1] = target;
        } else {
            this.targets = new TargetDescriptor[] { target };
        }
    }
    /**
     * Removes an output of this component
     *
     * @param target    The output descriptor to be removed
     */
    public void removeTarget(TargetDescriptor target) {
        target.setParent(null);
        if (this.targets != null) {
            this.targets = Arrays.stream(this.targets)
                    .filter(t -> {
                        EOData tar = t.getData();
                        EOData ref = target.getData();
                        return (tar.getId() != null && !tar.getId().equals(ref.getId())) ||
                                (tar.getName() != null && !tar.getName().equals(ref.getName()));
                    })
                    .toArray(TargetDescriptor[]::new);
        }
    }
    /**
     * Returns the security context of this component
     */
    @XmlTransient
    public SecurityContext securityContext() { return this.securityContext == null ?
            SystemSecurityContext.instance() : this.securityContext; }
    public void attachSecurityContext(SecurityContext context) { this.securityContext = context; }
    /**
     * Returns a copy (clone) of this component
     */
    @Override
    public TaoComponent clone() throws CloneNotSupportedException {
        TaoComponent clone = (TaoComponent) super.clone();
        if (this.sources != null) {
            clone.setSources(Arrays.copyOf(this.sources, this.sources.length));
        }
        if (this.targets != null) {
            clone.setTargets(Arrays.copyOf(this.targets, this.targets.length));
        }
        clone.attachSecurityContext(this.securityContext);
        return clone;
    }
}
