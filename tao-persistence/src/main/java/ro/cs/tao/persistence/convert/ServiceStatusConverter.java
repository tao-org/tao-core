package ro.cs.tao.persistence.convert;

import ro.cs.tao.topology.ServiceStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for TemplateType enum stored values
 *
 * @author oana
 */
public class ServiceStatusConverter implements AttributeConverter<ServiceStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceStatus attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public ServiceStatus convertToEntityAttribute(Integer dbData) {
        return dbData != null ? ServiceStatus.valueOf(ServiceStatus.getEnumConstantNameByValue(dbData)) : null;
    }
}
