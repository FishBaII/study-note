package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.ClientDto;
import com.ljm.mapstruct.entity.Client;
import com.ljm.mapstruct.valid.Validator;
import org.mapstruct.*;

import javax.xml.bind.ValidationException;
import java.time.LocalDate;

@Mapper(componentModel = "spring",uses = {Validator.class})
public abstract class ClientAbstractMapper {

    @BeforeMapping
    protected void validate(Client client) {
        if(client.getDateOfBirth() == null){
            client.setDateOfBirth(LocalDate.now());
        }
    }

    @AfterMapping
    protected void updateResult(@MappingTarget ClientDto clientDto) {
        clientDto.setName(clientDto.getName().toUpperCase());
    }

    @Mapping(source = "dateOfBirth", target = "dateOfBirth", dateFormat = "dd/MMM/yyyy")
    public abstract ClientDto toDto(Client Client) throws ValidationException;


//    @InheritInverseConfiguration
//    public abstract Client toModel(ClientDto clientDto) throws ValidationException;

    @InheritConfiguration
    public abstract void updateDto(Client Client, @MappingTarget ClientDto clientDto) throws ValidationException;
}
