package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.model.Client;
import com.htc.enter.service.ClientDocumentService;

@Service
public class ClientDocumentServiceImpl extends BaseDocumentService implements ClientDocumentService {

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePasswordProtectedClientsDocument(List<Client> clients, String password) 
            throws IOException {
        return generatePasswordProtectedDocument(
                "Client Directory",
                "No clients found.",
                clients,
                password,
                this::addClientContent);
    }

    private void addClientContent(XWPFDocument doc, Client client) {
        XWPFParagraph p = createSectionHeader(doc, "Client: " + client.getName() + " (ID: " + client.getClient_id() + ")");
        
        XWPFRun r2 = p.createRun();
        addField(r2, "Email", client.getEmail());
        addField(r2, "Phone", client.getPhn_no() != 0 ? client.getPhn_no() : null);
        addField(r2, "Address", client.getAddress());
        addSectionSpacing(r2);
    }
}