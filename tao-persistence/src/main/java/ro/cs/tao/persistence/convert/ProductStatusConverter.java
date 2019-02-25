package ro.cs.tao.persistence.convert;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.eodata.enums.ProductStatus;

import javax.persistence.AttributeConverter;

public class ProductStatusConverter implements AttributeConverter<ProductStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProductStatus productStatus) {
        return productStatus != null ? productStatus.value() : null;
    }

    @Override
    public ProductStatus convertToEntityAttribute(Integer integer) {
        return integer != null ? EnumUtils.getEnumConstantByValue(ProductStatus.class, integer) : null;
    }
}
