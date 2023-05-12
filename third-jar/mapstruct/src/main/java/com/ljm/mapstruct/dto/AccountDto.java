package com.ljm.mapstruct.dto;

import java.util.ArrayList;
import java.util.List;

public class AccountDto {

    private Long id;

    private String accountNumber;

    private List<ClientDto> clientDtoList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public List<ClientDto> getClientDtoList() {
        return clientDtoList;
    }

    public void setClientDtoList(List<ClientDto> clientDtoList) {
        this.clientDtoList = clientDtoList;
    }

    public void addClient(ClientDto clientDto) {
        if (clientDtoList == null) {
            clientDtoList = new ArrayList<>();
        }

        clientDtoList.add(clientDto);
    }
}
