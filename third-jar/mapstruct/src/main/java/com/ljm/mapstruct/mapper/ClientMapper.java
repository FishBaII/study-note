package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.ClientDto;
import com.ljm.mapstruct.entity.Client;
import com.ljm.mapstruct.util.SystemUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper(
        componentModel = "spring",
        imports = {SystemUtil.class}
)
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);
    @Mapping(source = "dateOfBirth", target = "dateOfBirth", dateFormat = "dd/MMM/yyyy")
    @Mapping(source = "name", target = "name", defaultExpression = "java(SystemUtil.getName())")
    ClientDto toDto(Client Client);


    //use method 'toDto' if exist
    List<ClientDto> listToDtoList(List<Client> clientList);


    Set<ClientDto> setConvert(Set<Client> clientSet);

    Map<String, ClientDto> mapConvert(Map<String, Client> clientMap);

}
