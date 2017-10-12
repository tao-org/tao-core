package ro.cs.tao.persistence.convert;

import ro.cs.tao.topology.ServiceStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for TemplateType enum stored values
 *
 * @author oana
 *
 */
@Converter
public class ServiceStatusConverter implements AttributeConverter<ServiceStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceStatus attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public ServiceStatus convertToEntityAttribute(Integer dbData) {
        return ServiceStatus.valueOf(ServiceStatus.getEnumConstantNameByValue(dbData));
    }
}
