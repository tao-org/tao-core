package ro.cs.tao.execution.util;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.persistence.DataSourceComponentGroupProvider;
import ro.cs.tao.datasource.persistence.DataSourceComponentProvider;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.persistence.*;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.services.bridge.spring.SpringContextBridgedServices;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.Tuple;

import java.util.Comparator;
import java.util.List;

/**
 * Utility class that provides methods for retrieving information about components, such as
 * memory requirements or input/output cardinality. This class relies on services from the
 * Spring application context to query and process component-related data.
 */
public class ComponentUtilities {

    private static final ProcessingComponentProvider processingComponentProvider;
    private static final ContainerProvider containerProvider;
    private static final DataSourceComponentProvider dataSourceComponentProvider;
    private static final DataSourceComponentGroupProvider dataSourceComponentGroupProvider;
    private static final ConditionComponentProvider conditionComponentProvider;
    private static final WPSComponentProvider wpsComponentProvider;
    private static final WMSComponentProvider wmsComponentProvider;
    private static final OGCComponentProvider ogcComponentProvider;
    private static final GroupComponentProvider groupComponentProvider;

    static {
        final SpringContextBridgedServices context = SpringContextBridge.services();
        processingComponentProvider = context.getService(ProcessingComponentProvider.class);
        containerProvider = context.getService(ContainerProvider.class);
        dataSourceComponentProvider = context.getService(DataSourceComponentProvider.class);
        dataSourceComponentGroupProvider = context.getService(DataSourceComponentGroupProvider.class);
        conditionComponentProvider = context.getService(ConditionComponentProvider.class);
        wpsComponentProvider = context.getService(WPSComponentProvider.class);
        wmsComponentProvider = context.getService(WMSComponentProvider.class);
        ogcComponentProvider = context.getService(OGCComponentProvider.class);
        groupComponentProvider = context.getService(GroupComponentProvider.class);
    }

    /**
     * Searches for a TaoComponent by its identifier within the persistence layers.
     * The method examines multiple component categories in a specific order
     * to locate the component associated with the provided identifier.
     *
     * @param id The unique identifier of the component to be retrieved. Must not be null or empty.
     * @return The TaoComponent with the given identifier if found within the persistence layers.
     *         Returns null if the identifier is null or empty.
     * @throws PersistenceException If no component with the specified identifier is found across
     *                              the persistence layers.
     */
    public static TaoComponent findComponent(String id) throws PersistenceException {
        if (StringUtilities.isNullOrEmpty(id)) {
            return null;
        }
        TaoComponent component = processingComponentProvider.get(id);
        if (component == null) {
            component = dataSourceComponentProvider.get(id);
            if (component == null) {
                component = conditionComponentProvider.get(id);
                if (component == null) {
                    component = wpsComponentProvider.get(id);
                    if (component == null) {
                        component = wmsComponentProvider.get(id);
                        if (component == null) {
                            component = ogcComponentProvider.get(id);
                            if (component == null) {
                                component = groupComponentProvider.get(id);
                                if (component == null) {
                                    component = dataSourceComponentGroupProvider.get(id);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (component == null) {
            throw new PersistenceException(String.format("No such component [%s]", id));
        }
        return component;
    }

    /**
     * Retrieves the memory requirements for a specified processing component.
     *
     * @param component The processing component for which memory requirements need to be determined.
     * @return An integer representing the memory requirements of the component in the associated application.
     *         Returns 0 if the component is not associated with any application or if memory requirements are not defined.
     */
    public static int getComponentMemoryRequirements(ProcessingComponent component) {
        Container container = containerProvider.get(component.getContainerId());
        Application app = container.getApplications().stream().filter(a -> a.getName().equals(component.getId())).findFirst().orElse(null);
        return app != null ? app.getMemoryRequirements() : 0;
    }

    /**
     * Returns the cardinality of inputs for a given component
     * @param component      The component
     */
    public static int getSourceCardinality(TaoComponent component) {
        int cardinality = -1;
        if (component != null) {
            List<SourceDescriptor> sources = component.getSources();
            if (sources != null && !sources.isEmpty()) {
                cardinality = sources.stream()
                        .max(Comparator.comparingInt(SourceDescriptor::getCardinality))
                        .get().getCardinality();
            }
        }
        return cardinality;
    }

    /**
     * Returns the cardinality of outputs for a given component
     * @param component      The component
     */
    public static int getTargetCardinality(TaoComponent component) {
        int cardinality = -1;
        if (component != null) {
            List<TargetDescriptor> targets = component.getTargets();
            if (targets != null && !targets.isEmpty()) {
                cardinality = targets.stream().mapToInt(TargetDescriptor::getCardinality).sum();
            }
        }
        return cardinality;
    }

    /**
     * Finds and returns the matching ports between a source component and a target component.
     * The method identifies a compatible pair consisting of a source port from the target component
     * and a target port from the source component based on defined compatibility conditions.
     *
     * @param source The source TaoComponent whose targets are evaluated for compatibility.
     * @param target The target TaoComponent whose sources are evaluated for compatibility.
     * @return A Tuple containing the matching TargetDescriptor from the source component and
     *         the matching SourceDescriptor from the target component. Returns null if no matching
     *         pair is found.
     */
    public static Tuple<TargetDescriptor, SourceDescriptor> findMatchingPorts(TaoComponent source, TaoComponent target) {
        Tuple<TargetDescriptor, SourceDescriptor> pair = null;
        final List<TargetDescriptor> sourceTargets = source.getTargets();
        TargetDescriptor candidateTarget = null;
        SourceDescriptor candidateSource = null;
        if (sourceTargets != null && !sourceTargets.isEmpty()) {
            final List<SourceDescriptor> targetSources = target.getSources();
            if (targetSources != null && !targetSources.isEmpty()) {
                for (TargetDescriptor sourceTarget : sourceTargets) {
                    for (SourceDescriptor targetSource : targetSources) {
                        if (targetSource.isCompatibleWith(sourceTarget)) {
                            if (candidateSource != null) {
                                if (candidateSource.getCardinality() < targetSource.getCardinality()) {
                                    candidateSource = targetSource;
                                    if (candidateTarget == null || candidateTarget.getCardinality() < sourceTarget.getCardinality()) {
                                        candidateTarget = sourceTarget;
                                    }
                                }
                            } else {
                                candidateSource = targetSource;
                                candidateTarget = sourceTarget;
                            }
                        }
                    }
                }
            }
        }
        if (candidateTarget != null && candidateTarget != null) {
            pair = new Tuple<>(candidateTarget, candidateSource);
        }
        return pair;
    }

    public static List<ProcessingComponent> getContainerComponents(String containerName) {
        final Container container = containerProvider.getByName(containerName);
        if (container == null) {
            return List.of();
        }
        return processingComponentProvider.listByContainer(container.getId());
    }
}
