package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.DoctorDto;
import com.ljm.mapstruct.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DoctorMapper {

    DoctorMapper INSTANCE = Mappers.getMapper(DoctorMapper.class);

    DoctorDto toDto(Doctor doctor);

}
