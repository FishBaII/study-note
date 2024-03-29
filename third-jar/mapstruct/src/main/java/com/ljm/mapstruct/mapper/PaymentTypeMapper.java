package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.payment.PaymentType;
import com.ljm.mapstruct.payment.PaymentTypeView;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaymentTypeMapper {

    PaymentTypeMapper INSTANCE = Mappers.getMapper(PaymentTypeMapper.class);

//    @ValueMappings({
//            @ValueMapping(source = "CARD_VISA", target = "CARD"),
//            @ValueMapping(source = "CARD_MASTER", target = "CARD"),
//            @ValueMapping(source = "CARD_CREDIT", target = "CARD")
//    })

    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = "CARD")
    PaymentTypeView paymentTypeToPaymentTypeView(PaymentType paymentType);
}
