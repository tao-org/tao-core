package ro.cs.tao.component;

import ro.cs.tao.component.enums.ComponentCategory;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.eodata.enums.DataFormat;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.logging.Logger;

/**
 * A pass-through component is a descriptor for routing the input to the output.
 * It is useful for avoiding exposing multiple inputs that are supposed to receive the same value.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "passThroughComponent")
public class PassThroughComponent extends TaoComponent {
    public static final String COMPONENT_ID = "pass-through-component";
    private static final Logger logger = Logger.getLogger(PassThroughComponent.class.getName());
    private ComponentCategory category;
    private ProcessingComponentVisibility visibility;
    private boolean active;
    private String owner;

    public static PassThroughComponent passThroughComponent() {
        final PassThroughComponent component = new PassThroughComponent();
        component.setId(COMPONENT_ID);
        final SourceDescriptor source = new SourceDescriptor(COMPONENT_ID + "-input");
        source.setName("input");
        source.setCardinality(1);
        source.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addSource(source);
        final TargetDescriptor target1 = new TargetDescriptor(COMPONENT_ID + "-output");
        target1.setName("output");
        target1.setCardinality(1);
        target1.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addTarget(target1);
        component.setLabel("Pass-through");
        component.setDescription("Pass-through component that forwards the input to the output");
        component.setAuthors("CS GROUP");
        component.setCopyright("(C)2025");
        component.setVersion("1.0");
        component.setNodeAffinity(NodeAffinity.Any);
        component.setVisibility(ProcessingComponentVisibility.SYSTEM);
        return component;
    }

    public PassThroughComponent() {
        this.category = ComponentCategory.PASSTHROUGH;
    }
    /**
     * Checks if the component is active (i.e. not deleted).
     */
    public boolean isActive() {
        return active;
    }
    /**
     * Sets the active status of the component.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public ComponentCategory getCategory() {
        return category;
    }

    public void setCategory(ComponentCategory category) {
        if (category != ComponentCategory.PASSTHROUGH) {
            throw new IllegalArgumentException("category");
        }
        this.category = category;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
    /**
     * Returns the visibility of the component.
     */
    public ProcessingComponentVisibility getVisibility() {
        return visibility;
    }
    /**
     * Sets the visibility of the component.
     */
    public void setVisibility(ProcessingComponentVisibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public PassThroughComponent clone() throws CloneNotSupportedException {
        PassThroughComponent newDescriptor = (PassThroughComponent) super.clone();
        newDescriptor.active = this.active;
        newDescriptor.visibility = this.visibility;
        newDescriptor.category = this.category;
        return newDescriptor;
    }
}
