package com.example.demo.mapper;

import com.example.demo.dto.ClientDto;
import com.example.demo.entity.Client;
import com.example.demo.valid.Validator;
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
