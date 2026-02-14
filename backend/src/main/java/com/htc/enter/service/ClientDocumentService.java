package com.htc.enter.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.htc.enter.model.Client;

public interface ClientDocumentService {
    ByteArrayOutputStream generatePasswordProtectedClientsDocument(List<Client> clients, String password) throws IOException;
}
