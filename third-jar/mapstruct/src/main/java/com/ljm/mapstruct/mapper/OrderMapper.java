package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.OrderDto;
import com.ljm.mapstruct.entity.Order;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(
        componentModel = "spring"
)
public interface OrderMapper
{

    @Mapping(target = "remark", constant = "default remark")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "halfUp")
    @Mapping(target = "price", source = "price", numberFormat = "$#.00")
    @Mapping(target = "id", source = "id", defaultValue = "-1L")
    @Mapping(source = "orderTime", target = "orderTime", dateFormat = "yyy-MM-dd HH:mm:ss")
    OrderDto toDto(Order order);

    @InheritInverseConfiguration
    //@Mapping(target = "id", source = "id", defaultValue = "-1L")
    //@Mapping(target = "remark", constant = "default remark")
    Order toModel(OrderDto orderDto);


    @Named("halfUp")
    static BigDecimal halfUp(BigDecimal price){
        if(price == null){
            return BigDecimal.ZERO;
        }
        return price.setScale(0, RoundingMode.HALF_UP);
    }
}
