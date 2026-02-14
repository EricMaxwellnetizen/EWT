package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.model.Epic;
import com.htc.enter.model.User;
import com.htc.enter.repository.EpicRepository;
import com.htc.enter.service.EpicAppService;
import com.htc.enter.service.EpicDocumentService;
import com.htc.enter.service.UserAuthService;
import com.htc.enter.service.UserService;
import com.htc.enter.util.DocumentPasswordUtil;

@Service
public class EpicAppServiceImpl implements EpicAppService {

    private final EpicRepository epicRepository;
    private final EpicDocumentService documentService;
    private final UserAuthService authService;
    private final UserService userService;

    public EpicAppServiceImpl(EpicRepository epicRepository,
                              EpicDocumentService documentService,
                              UserAuthService authService,
                              UserService userService) {
        this.epicRepository = epicRepository;
        this.documentService = documentService;
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentPayload buildManagerEpicsDocument(Long managerId) throws IOException {
        // Find all epics where the manager_id matches
        List<Epic> epics = epicRepository.findAll().stream()
                .filter(epic -> epic.getManager_id() != null && epic.getManager_id().getId().equals(managerId))
                .collect(Collectors.toList());
        
        User manager = userService.findById(managerId).orElse(null);
        User current = authService.getCurrentUser();
        String password = DocumentPasswordUtil.resolvePassword(manager, current);

        ByteArrayOutputStream os = documentService.generatePasswordProtectedManagerEpicsDocument(
                managerId, epics, password);
        String fileName = "manager_" + managerId + "_epics.docx";
        return DocumentPayload.ofDocx(os.toByteArray(), fileName);
    }
}