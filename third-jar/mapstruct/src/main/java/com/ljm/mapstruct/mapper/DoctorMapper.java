package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.DoctorDto;
import com.ljm.mapstruct.entity.Doctor;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper
public interface DoctorMapper {

    DoctorMapper INSTANCE = Mappers.getMapper(DoctorMapper.class);

    DoctorDto toDto(Doctor doctor);


    @Mapping(target = "name", source = "name", qualifiedByName = "getDefaultValue")
    DoctorDto toDtoWithContext(Doctor doctor, @Context String context);

    @Named("getDefaultValue")
    default String getDefaultValue(String name, @Context String context){
        return name + context;
    }
}
