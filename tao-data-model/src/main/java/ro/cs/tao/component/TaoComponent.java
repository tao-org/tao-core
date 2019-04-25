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
package ro.cs.tao.component;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import ro.cs.tao.security.SessionContext;

import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.*;

/**
 * Base class for TAO components. Components can be:
 * - processing components
 * - data source components
 * - group components
 *
 * @author Cosmin Cara
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public abstract class TaoComponent extends StringIdentifiable {
    protected String label;
    protected String version;
    protected String description;
    protected String authors;
    protected String copyright;
    protected String nodeAffinity;

    /*protected int cardinality;
    protected int targetCardinality = 1;*/
    protected List<SourceDescriptor> sources;
    protected List<TargetDescriptor> targets;
    protected List<String> tags;

    protected Map<String, StringIdentifiable> descriptorIndex = new HashMap<>();

    private SessionContext sessionContext;

    @Override
    public String defaultId() { return "NewComponent"; }

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
    public List<SourceDescriptor> getSources() {
        return sources;
    }
    /**
     * Sets the inputs of this component
     * @param sources   An array of source (input) descriptors
     */
    public void setSources(List<SourceDescriptor> sources) {
        this.sources = sources;
        if (this.sources != null) {
            this.sources.forEach(s -> this.descriptorIndex.put(s.getId(), s));
        }
    }

    /**
     * Adds an input to this component
     */
    public void addSource(SourceDescriptor source) {
        if (this.sources == null) {
            this.sources = new ArrayList<>();
        }
        source.setParentId(this.id);
        this.sources.add(source);
        this.descriptorIndex.put(source.getId(), source);
    }
    /**
     * Removes an input of this component
     */
    public void removeSource(SourceDescriptor source) {
        source.setParentId(null);
        if (this.sources != null) {
            this.sources.remove(source);
            this.descriptorIndex.remove(source.getId());
        }
    }
    /**
     * Returns the outputs of this component
     */
    @XmlElementWrapper(name = "outputs")
    public List<TargetDescriptor> getTargets() {
        return targets;
    }
    /**
     * Sets the outputs of this component
     *
     * @param targets   An array of target (output) descriptors
     */
    public void setTargets(List<TargetDescriptor> targets) {
        this.targets = targets;
        if (this.targets != null) {
            this.targets.forEach(t -> this.descriptorIndex.put(t.getId(), t));
        }
    }
    /**
     * Adds an output to this component.
     *
     * @param target    The output descriptor to be added
     */
    public void addTarget(TargetDescriptor target) {
        if (this.targets == null) {
            this.targets = new ArrayList<>();
        }
        target.setParentId(this.id);
        this.targets.add(target);
        this.descriptorIndex.put(target.getId(), target);
    }
    /**
     * Removes an output of this component
     *
     * @param target    The output descriptor to be removed
     */
    public void removeTarget(TargetDescriptor target) {
        target.setParentId(null);
        if (this.targets != null) {
            this.targets.remove(target);
            this.descriptorIndex.remove(target.getId());
        }
    }
    @XmlElementWrapper(name = "tags")
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void addTags(String... values) {
        if (values == null || values.length == 0) {
            return;
        }
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        Collections.addAll(this.tags, values);
    }
    public void removeTag(String tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
        }
    }
    /**
     * Returns a copy (clone) of this component
     */
    @Override
    public TaoComponent clone() throws CloneNotSupportedException {
        TaoComponent clone = (TaoComponent) super.clone();
        if (this.sources != null) {
            List<SourceDescriptor> clonedSources = new ArrayList<>(this.sources.size());
            for (SourceDescriptor sourceDescriptor : this.sources) {
                SourceDescriptor dClone = sourceDescriptor.clone();
                dClone.setParentId(clone.getId());
                clonedSources.add(dClone);
            }
            clone.setSources(clonedSources);
        }
        if (this.targets != null) {
            List<TargetDescriptor> clonedTargets = new ArrayList<>(this.targets.size());
            for (TargetDescriptor targetDescriptor : this.targets) {
                TargetDescriptor dClone = targetDescriptor.clone();
                dClone.setParentId(clone.getId());
                clonedTargets.add(dClone);
            }
            clone.setTargets(clonedTargets);
        }
        return clone;
    }

    public <T extends StringIdentifiable> T findDescriptor(String id) { return (T) this.descriptorIndex.get(id); }

    public boolean hasDescriptor(String id) { return this.descriptorIndex.containsKey(id); }
}
