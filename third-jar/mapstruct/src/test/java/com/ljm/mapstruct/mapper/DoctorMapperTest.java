package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.AccountDto;
import com.ljm.mapstruct.dto.ClientDto;
import com.ljm.mapstruct.dto.DoctorDto;
import com.ljm.mapstruct.entity.Account;
import com.ljm.mapstruct.entity.Client;
import com.ljm.mapstruct.entity.Doctor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
class DoctorMapperTest {

    @Autowired
    private DoctorMapper doctorMapper;

    @Test
    void toDto() {

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("tom");
        doctor.setSpecialty("psychology");

        DoctorDto doctorDto = DoctorMapper.INSTANCE.toDto(doctor);
        assertEquals("tom", doctorDto.getName());
        assertEquals("psychology", doctorDto.getSpecialization());

    }


    @Test
    void toAccountDto() {

        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("P-00000001");
        Client client = new Client();
        client.setId(2L);
        client.setName("apple");
        client.setDateOfBirth(LocalDate.now());
        List<Client> clientList = new ArrayList<>();
        clientList.add(client);
        account.setClientList(clientList);

        AccountDto accountDto = AccountMapper.INSTANCE.toDto(account);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
        assertEquals(1L, accountDto.getId());
        assertEquals("P-00000001", accountDto.getAccountNumber());
        assertEquals("apple", accountDto.getClientDtoList().get(0).getName());
        assertEquals(LocalDate.now().format(df), accountDto.getClientDtoList().get(0).getDateOfBirth());


    }

    @Test
    void updatedAccountDto() {

        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("P-00000001");
        Client client = new Client();
        client.setId(2L);
        client.setName("apple");
        List<Client> clientList = new ArrayList<>();
        clientList.add(client);
        account.setClientList(clientList);


        AccountDto accountDto = new AccountDto();
        accountDto.setId(3L);
        accountDto.setAccountNumber("P-00000002");
        ClientDto clientDto = new ClientDto();
        clientDto.setId(12L);
        clientDto.setName("cat");
        List<ClientDto> clientDtoList = new ArrayList<>();
        clientDtoList.add(clientDto);
        accountDto.setClientDtoList(clientDtoList);

        AccountMapper.INSTANCE.updateModel(accountDto, account);
        assertEquals(3L, accountDto.getId());
        assertEquals("P-00000002", accountDto.getAccountNumber());
        assertEquals("cat", accountDto.getClientDtoList().get(0).getName());


    }

    @Test
    void clientMapperTest(){

        Client client = new Client();
        client.setName("tom");
        client.setId(1L);
        client.setDateOfBirth(LocalDate.now());
        List<Client> clientList = new ArrayList<>();
        clientList.add(client);

        List<ClientDto> clientDtoList = ClientMapper.INSTANCE.map(clientList);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
        assertEquals(1L, clientDtoList.get(0).getId());
        assertEquals("tom", clientDtoList.get(0).getName());
        assertEquals(LocalDate.now().format(df), clientDtoList.get(0).getDateOfBirth());
    }
}