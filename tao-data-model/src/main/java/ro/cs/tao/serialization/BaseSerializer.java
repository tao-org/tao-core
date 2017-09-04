package ro.cs.tao.serialization;

import ro.cs.tao.component.constraints.CRSConstraint;
import ro.cs.tao.component.constraints.Constraint;
import ro.cs.tao.component.constraints.DimensionConstraint;
import ro.cs.tao.component.constraints.FormatConstraint;
import ro.cs.tao.component.constraints.GeometryConstraint;
import ro.cs.tao.component.constraints.SensorConstraint;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.validation.NotEmptyValidator;
import ro.cs.tao.component.validation.NotNullValidator;
import ro.cs.tao.component.validation.TypeValidator;
import ro.cs.tao.component.validation.Validator;
import ro.cs.tao.component.validation.ValueSetValidator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.Map;

/**
 * @author  kraftek
 * @date    2017-02-03
 */
abstract class BaseSerializer<T> implements Serializer<T, String> {
    private static Class[] classes;

    static {
        classes = new Class[] {
                Template.class, BasicTemplate.class,
                Constraint.class, CRSConstraint.class, DimensionConstraint.class, FormatConstraint.class, GeometryConstraint.class,
                SensorConstraint.class,
                Validator.class, NotEmptyValidator.class, NotNullValidator.class, TypeValidator.class, ValueSetValidator.class
        };
    }

    Class<T> tClass;
    JAXBContext context;

    BaseSerializer(Class<T> tClass) throws SerializationException {
        this.tClass = tClass;
        try {
            final Class[] classesCopy = new Class[classes.length + 1];
            System.arraycopy(classes, 0, classesCopy, 0, classes.length);
            classesCopy[classes.length] = tClass;
            this.context = JAXBContext.newInstance(classesCopy);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    BaseSerializer(Class<T> tClass, Map<String, Object> properties) throws SerializationException {
        this.tClass = tClass;
        try {
            final Class[] classesCopy = new Class[classes.length + 1];
            System.arraycopy(classes, 0, classesCopy, 0, classes.length);
            classesCopy[classes.length] = tClass;
            if (properties != null) {
                this.context = JAXBContext.newInstance(classesCopy, properties);
            } else {
                this.context = JAXBContext.newInstance(classesCopy);
            }
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }
}
