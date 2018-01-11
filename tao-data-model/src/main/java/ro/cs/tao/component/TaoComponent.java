package ro.cs.tao.component;

import ro.cs.tao.eodata.EOData;
import ro.cs.tao.security.SecurityContext;
import ro.cs.tao.security.SystemSecurityContext;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Arrays;

/**
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getNodeAffinity() { return nodeAffinity; }

    public void setNodeAffinity(String nodeAffinity) { this.nodeAffinity = nodeAffinity; }

    @XmlElementWrapper(name = "inputs")
    public SourceDescriptor[] getSources() {
        return sources;
    }

    public void setSources(SourceDescriptor[] sources) { this.sources = sources; }

    @XmlTransient
    public void setSourcesCount(int value) {
        if (this.sources == null) {
            this.sources = new SourceDescriptor[value];
        } else {
            this.sources = Arrays.copyOf(this.sources, value);
        }
    }

    public void addSource(SourceDescriptor source) {
        if (this.sources != null) {
            this.sources = Arrays.copyOf(this.sources, this.sources.length + 1);
            this.sources[this.sources.length - 1] = source;
        } else {
            this.sources = new SourceDescriptor[] { source };
        }
    }

    public void removeSource(TargetDescriptor source) {
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

    @XmlElementWrapper(name = "outputs")
    public TargetDescriptor[] getTargets() {
        return targets;
    }

    public void setTargets(TargetDescriptor[] targets) { this.targets = targets; }

    @XmlTransient
    public void setTargetCount(int value) {
        if (this.targets == null) {
            this.targets = new TargetDescriptor[value];
        } else {
            this.targets = Arrays.copyOf(this.targets, value);
        }
    }

    public void addTarget(TargetDescriptor target) {
        if (this.targets != null) {
            this.targets = Arrays.copyOf(this.targets, this.targets.length + 1);
            this.targets[this.targets.length - 1] = target;
        } else {
            this.targets = new TargetDescriptor[] { target };
        }
    }

    public void removeTarget(TargetDescriptor target) {
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

    @XmlTransient
    public SecurityContext getSecurityContext() { return this.securityContext == null ?
            SystemSecurityContext.instance() : this.securityContext; }
    public void setSecurityContext(SecurityContext context) { this.securityContext = context; }

    @Override
    public TaoComponent clone() throws CloneNotSupportedException {
        TaoComponent clone = (TaoComponent) super.clone();
        if (this.sources != null) {
            clone.setSources(Arrays.copyOf(this.sources, this.sources.length));
        }
        if (this.targets != null) {
            clone.setTargets(Arrays.copyOf(this.targets, this.targets.length));
        }
        clone.setSecurityContext(this.securityContext);
        return clone;
    }
}
