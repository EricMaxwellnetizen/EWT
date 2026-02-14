package com.htc.enter.service;

import java.util.List;

import com.htc.enter.model.Admin;
import com.htc.enter.model.Client;
import com.htc.enter.dto.ClientDTO;

public interface ClientService {
	Client save(Client client);
	Client findById(Long id);
	    List<Client> findAll();
	    void deleteById(Long id);

    Client createFromDTO(ClientDTO dto);
    Client updateFromDTO(Long id, ClientDTO dto);
}