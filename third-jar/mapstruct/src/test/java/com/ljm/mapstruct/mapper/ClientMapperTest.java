package com.ljm.mapstruct.mapper;

import com.ljm.mapstruct.dto.ClientDto;
import com.ljm.mapstruct.entity.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.bind.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ClientMapperTest {

    @Autowired
    private ClientMapper clientMapper;

    @Autowired
    private ClientAbstractMapper clientAbstractMapper;

    @Test
    void clientMapperTest(){

        Client client = new Client();
        client.setName("tom");
        client.setId(1L);
        client.setDateOfBirth(LocalDate.now());
        List<Client> clientList = new ArrayList<>();
        clientList.add(client);

        List<ClientDto> clientDtoList = clientMapper.listToDtoList(clientList);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
        assertEquals(1L, clientDtoList.get(0).getId());
        assertEquals("tom", clientDtoList.get(0).getName());
        assertEquals(LocalDate.now().format(df), clientDtoList.get(0).getDateOfBirth());
    }

    @Test
    void clientAbstractMapperTest() throws ValidationException {

        Client client = new Client();
        client.setName("tom");
        client.setId(1L);
        client.setDateOfBirth(null);

        ClientDto clientDto = clientAbstractMapper.toDto(client);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
        assertEquals(1L, clientDto.getId());
        assertEquals("TOM", clientDto.getName());
        assertEquals(LocalDate.now().format(df), clientDto.getDateOfBirth());
    }

    @Test
    void clientAbstractMapperTestWithException(){

        Client client = new Client();
        client.setName("tom");
        client.setId(-1L);
        client.setDateOfBirth(null);

        assertThrows(ValidationException.class, ()->{
            ClientDto clientDto = clientAbstractMapper.toDto(client);
        });
    }

    @Test
    void clientMapperTestWithNullName(){

        Client client = new Client();
        client.setName(null);
        client.setId(-1L);
        client.setDateOfBirth(null);
        ClientDto clientDto = ClientMapper.INSTANCE.toDto(client);
        assertEquals("default", clientDto.getName());
    }


    @Test
    void clientAbstractMapperUpdatedTest() throws ValidationException {

        Client client = new Client();
        client.setName("tom");
        client.setId(1L);
        client.setDateOfBirth(null);

        ClientDto clientDto = new ClientDto();
        clientAbstractMapper.updateDto(client, clientDto);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MMM/yyyy");
        assertEquals(1L, clientDto.getId());
        assertEquals("TOM", clientDto.getName());
        assertEquals(LocalDate.now().format(df), clientDto.getDateOfBirth());
    }
}