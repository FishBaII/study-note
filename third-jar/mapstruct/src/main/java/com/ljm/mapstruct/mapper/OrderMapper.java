package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.OrderDto;
import com.ljm.mapstruct.entity.Order;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(
        componentModel = "spring"
)
public interface OrderMapper
{

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);


    // set default value if empty
    @Mapping(target = "id", source = "id", defaultValue = "-1L")
    // set constant value
    @Mapping(target = "version", constant = "3.0.0")
    // use custom method
    @Mapping(target = "amount", source = "amount", qualifiedByName = "halfUp")
    // set format for number
    @Mapping(target = "price", source = "price", numberFormat = "$#.00")
    // set format for date or dateTime
    @Mapping(source = "orderTime", target = "orderTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    // set target with different name
    @Mapping(source = "currency", target = "cur")
    OrderDto toDto(Order order);

    @InheritInverseConfiguration
    //@Mapping(target = "id", source = "id", defaultValue = "-1L")
    //@Mapping(target = "remark", constant = "default remark")
    Order toModel(OrderDto orderDto);


    @Named("halfUp")
    static BigDecimal halfUp(BigDecimal price){
        if(price == null || price.compareTo(BigDecimal.ZERO) < 0){
            return BigDecimal.ZERO;
        }
        return price.setScale(0, RoundingMode.HALF_UP);
    }
}
