package com.htc.enter.serviceimpl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.ClientDTO;
import com.htc.enter.model.Client;
import com.htc.enter.repository.ClientRepository;
import com.htc.enter.service.ClientService;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repo;

    public ClientServiceImpl(ClientRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "clients", allEntries = true)
    public Client save(Client client) {
        return repo.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "clients", key = "#id")
    public Client findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "clients")
    public List<Client> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "clients", allEntries = true)
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "clients", allEntries = true)
    public Client createFromDTO(ClientDTO dto) {
        Client c = new Client();
        if (dto.getClientId() != null) c.setClient_id(dto.getClientId());
        c.setName(dto.getName());
        c.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) c.setPhn_no(dto.getPhoneNumber());
        c.setAddress(dto.getAddress());
        return repo.save(c);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "clients", allEntries = true)
    public Client updateFromDTO(Long id, ClientDTO dto) {
        Client existing = findById(id);
        if (existing == null) throw new IllegalArgumentException("Client not found with id: " + id);
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) existing.setPhn_no(dto.getPhoneNumber());
        if (dto.getAddress() != null) existing.setAddress(dto.getAddress());
        return repo.save(existing);
    }
}