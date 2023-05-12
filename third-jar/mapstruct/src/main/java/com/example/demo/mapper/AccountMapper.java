package com.example.demo.mapper;

import com.example.demo.dto.AccountDto;
import com.example.demo.entity.Account;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(
        //change collection strategy:use 'addClient' method in AccountDto
        //collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        uses = {ClientMapper.class}
)
public interface AccountMapper {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(source = "account.clientList", target = "clientDtoList")
    AccountDto toDto(Account account);

    @Mapping(source = "accountDto.clientDtoList", target = "clientList")
    void updateModel(AccountDto accountDto, @MappingTarget Account account);
}
