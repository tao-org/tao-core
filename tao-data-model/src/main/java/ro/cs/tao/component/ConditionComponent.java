package ro.cs.tao.component;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import ro.cs.tao.component.enums.ComponentCategory;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.component.template.engine.JavascriptTemplateEngine;
import ro.cs.tao.component.template.engine.TemplateEngine;
import ro.cs.tao.component.template.engine.VelocityTemplateEngine;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.naming.NameExpressionParser;
import ro.cs.tao.eodata.naming.NamingRule;
import ro.cs.tao.eodata.naming.ParseException;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A decision component is a descriptor for routing the input, based on a condition, to the specific output.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "conditionComponent")
public class ConditionComponent extends TaoComponent {
    public static final String ONE_CONDITION_COMPONENT_ID = "one-condition-component";
    public static final String TWO_CONDITIONS_COMPONENT_ID = "two-conditions-component";
    private static final Logger logger = Logger.getLogger(ConditionComponent.class.getName());
    private ComponentCategory category;
    private Template template;
    private TemplateType templateType;
    private TemplateEngine templateEngine;
    private ProcessingComponentVisibility visibility;
    private Set<ParameterDescriptor> parameters;
    private boolean active;
    private String owner;

    /**
     * Creates a condition component with one condition (if - else)
     */
    public static ConditionComponent oneConditionComponent() {
        final ConditionComponent component = new ConditionComponent();
        component.setId(ONE_CONDITION_COMPONENT_ID);
        final SourceDescriptor source = new SourceDescriptor(ONE_CONDITION_COMPONENT_ID + "-input");
        source.setName("input");
        source.setCardinality(1);
        source.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addSource(source);
        final TargetDescriptor target1 = new TargetDescriptor(ONE_CONDITION_COMPONENT_ID + "-output-1");
        target1.setName("output1");
        target1.setCardinality(1);
        target1.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addTarget(target1);
        final TargetDescriptor target2 = new TargetDescriptor(ONE_CONDITION_COMPONENT_ID + "-output-2");
        target2.setName("output2");
        target2.setCardinality(1);
        target2.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addTarget(target2);
        component.addParameter(new ParameterDescriptor(ONE_CONDITION_COMPONENT_ID + "-condition",
                                                       "condition", ParameterType.REGULAR, JavaType.STRING, null, "Condition", "Condition", "n/a",
                                                       null, null, null, true));
        component.setTemplateContents("#if ($condition)\noutput1\n#else\noutput2\n#end");
        component.setLabel("Single condition");
        component.setDescription("Condition component that acts like an if-else");
        component.setAuthors("CS GROUP");
        component.setCopyright("(C)2025");
        component.setVersion("1.0");
        component.setNodeAffinity(NodeAffinity.Any);
        component.setVisibility(ProcessingComponentVisibility.SYSTEM);
        return component;
    }

    /**
     * Creates a condition component with two conditions (if - else if - else)
     */
    public static ConditionComponent twoConditionsComponent() {
        final ConditionComponent component = new ConditionComponent();
        component.setId(TWO_CONDITIONS_COMPONENT_ID);
        final SourceDescriptor source = new SourceDescriptor(TWO_CONDITIONS_COMPONENT_ID + "-input");
        source.setName("input");
        source.setCardinality(1);
        source.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addSource(source);
        final TargetDescriptor target1 = new TargetDescriptor(TWO_CONDITIONS_COMPONENT_ID + "-output-1");
        target1.setName("output1");
        target1.setCardinality(1);
        target1.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addTarget(target1);
        final TargetDescriptor target2 = new TargetDescriptor(TWO_CONDITIONS_COMPONENT_ID + "-output-2");
        target2.setName("output2");
        target2.setCardinality(1);
        target2.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addTarget(target2);
        final TargetDescriptor target3 = new TargetDescriptor(TWO_CONDITIONS_COMPONENT_ID + "-output-3");
        target3.setName("output3");
        target3.setCardinality(1);
        target3.setDataDescriptor(new DataDescriptor(DataFormat.ANY));
        component.addTarget(target3);
        component.addParameter(new ParameterDescriptor(TWO_CONDITIONS_COMPONENT_ID + "-condition-1",
                                                       "condition1", ParameterType.REGULAR, JavaType.STRING, null, "First Condition", "First Condition", "n/a",
                                                       null, null, null, true));
        component.addParameter(new ParameterDescriptor(TWO_CONDITIONS_COMPONENT_ID + "-condition-2",
                                                       "condition2", ParameterType.REGULAR, JavaType.STRING, null, "Second Condition", "Second Condition", "n/a",
                                                       null, null, null, true));
        component.setTemplateContents("#if ($condition1)\noutput1\n#elseif ($condition2)\noutput2\n#else\noutput3\n#end");
        component.setLabel("Double condition");
        component.setDescription("Condition component that acts like an if-elseif-else");
        component.setVersion("1.0");
        component.setAuthors("CS GROUP");
        component.setCopyright("(C)2025");
        component.setNodeAffinity(NodeAffinity.Any);
        component.setVisibility(ProcessingComponentVisibility.SYSTEM);
        return component;
    }

    public ConditionComponent() {
        this.templateType = TemplateType.VELOCITY;
        this.templateEngine = new VelocityTemplateEngine();
        this.category = ComponentCategory.CONDITION;
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
        if (category != ComponentCategory.CONDITION) {
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
     * Returns the template of this component.
     */
    @JsonIgnore
    public Template getTemplate() {
        return this.template;
    }

    /**
     * Sets the template of this component.
     * If the template is not of the type supported by this component, an exception is thrown.
     */
    @JsonIgnore
    public void setTemplate(Template template) throws TemplateException {
        if (template != null) {
            if (!getTemplateType().equals(template.getTemplateType())) {
                throw new TemplateException("Incompatible template type");
            }
            this.template = template;
            this.template.associateWith(getTemplateEngine());
        }
    }
    /**
     * Returns the template engine associated with the component.
     */
    @JsonIgnore
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        if (templateEngine == null || templateEngine.getTemplateType() != TemplateType.VELOCITY) {
            throw new IllegalArgumentException("templateEngine");
        }
        this.templateEngine = templateEngine;
    }
    /**
     * Returns the template type supported by this component.
     */
    @XmlTransient
    public TemplateType getTemplateType() {
        return templateType;
    }
    /**
     * Sets the template type supported by this component.
     */
    public void setTemplateType(TemplateType templateType) {
        if (templateType == null || templateType != TemplateType.VELOCITY) {
            throw new IllegalArgumentException("templateType");
        }
        this.templateType = templateType;
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
    /**
     * Returns the parameter descriptors of this component.
     */
    @XmlElementWrapper(name = "parameters")
    public Set<ParameterDescriptor> getParameterDescriptors() {
        if (this.parameters == null) {
            this.parameters = new LinkedHashSet<>();
        }
        return this.parameters;
    }

    /**
     * Sets the parameter descriptors of this component.
     */
    public void setParameterDescriptors(Set<ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ParameterDescriptor parameterDescriptor) {
        final Set<ParameterDescriptor> descriptors = getParameterDescriptors();
        descriptors.add(parameterDescriptor);
    }
    /**
     * Returns the template contents of the component.
     */
    @JsonGetter(value = "templatecontents")
    public String getTemplateContents(){
        return this.template != null ? this.template.getContents() : null;
    }
    /**
     * Sets the template contents of the component.
     */
    @JsonSetter(value = "templatecontents")
    public void setTemplateContents(String contents) {
        this.template = new BasicTemplate();
        this.template.setTemplateType(getTemplateType());
        this.template.setContents(contents, false);
        this.template.associateWith(getTemplateEngine());
    }

    /**
     * Processes a template using the given naming rules and variable values.
     *
     * The method applies transformations to a template based on the provided naming rules and substitutions
     * derived from the variable values. Conditions within the template are resolved using a parser and evaluated
     * using a JavaScript engine, with the final output potentially transformed further for specific targets.
     *
     * @param rules a list of {@code NamingRule} objects that define patterns or rules for naming tokens in the template
     * @param values a list of {@code Variable} objects representing key-value pairs used to substitute placeholders
     *               and resolve conditions within the template
     * @return the processed template as a {@code String}, or {@code null} if transformation cannot be applied
     * @throws ParseException if an error occurs during the parsing of condition expressions within the template
     */
    public String processTemplate(List<NamingRule> rules, List<Variable> values) throws ParseException {
        String transformedTemplate;
        if (!rules.isEmpty() && rules.size() == 1 && values != null && !values.isEmpty()) {
            NameExpressionParser parser = new NameExpressionParser(rules.get(0));
            Variable input = values.stream().filter(v -> v.getKey().equals(this.sources.get(0).getName())).findFirst().orElseThrow();
            List<Variable> conditions = values.stream().filter(v -> v.getKey().startsWith("condition")).collect(Collectors.toList());
            String templateContents = getTemplateContents();
            // Velocity doesn't evaluate expressions, it considers them as objects.
            // We need to use an actual evaluator, like the Javascript one.
            final JavascriptTemplateEngine engine = new JavascriptTemplateEngine();
            for (Variable condition : conditions) {
                String value = parser.resolve(condition.getValue(), null);
                String evalValue = String.valueOf(engine.evaluate(value));
                templateContents = templateContents.replace("$" + condition.getKey(), evalValue);
                logger.fine(String.format("Condition %s for input %s resolved to %s",
                                          condition.getKey(),
                                          input.getValue(),
                                          evalValue));
            }
            this.template.setContents(templateContents, false);
            transformedTemplate = templateEngine.transform(this.template,
                                                           values.stream().collect(Collectors.toMap(Variable::getKey, Variable::getValue)));
            if (transformedTemplate == null || "null".equals(transformedTemplate)) {
                Logger.getLogger(ProcessingComponent.class.getName())
                      .warning(String.format("Component %s produced an empty invocation template", this.id));
            } else {
                transformedTemplate = transformedTemplate.replace("\n", "").replace("$", "");
                for (TargetDescriptor targetDescriptor : this.targets) {
                    if (targetDescriptor.getName().equals(transformedTemplate)) {
                        targetDescriptor.setDataDescriptor(this.sources.get(0).getDataDescriptor());
                    }
                }
            }
        } else {
            transformedTemplate = null;
        }
        return transformedTemplate;
    }

    @Override
    public ConditionComponent clone() throws CloneNotSupportedException {
        ConditionComponent newDescriptor = (ConditionComponent) super.clone();
        newDescriptor.templateType = this.templateType;
        if (this.template != null) {
            newDescriptor.template = this.template.copy();
        }
        newDescriptor.active = this.active;
        newDescriptor.visibility = this.visibility;
        newDescriptor.category = this.category;
        return newDescriptor;
    }
}
