package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.model.Story;
import com.htc.enter.model.User;
import com.htc.enter.repository.StoryRepository;
import com.htc.enter.service.StoryAppService;
import com.htc.enter.service.StoryDocumentService;
import com.htc.enter.service.UserAuthService;
import com.htc.enter.service.UserService;
import com.htc.enter.util.DocumentPasswordUtil;

@Service
public class StoryAppServiceImpl implements StoryAppService {

    private final StoryRepository storyRepository;
    private final StoryDocumentService documentService;
    private final UserAuthService authService;
    private final UserService userService;

    public StoryAppServiceImpl(StoryRepository storyRepository,
                               StoryDocumentService documentService,
                               UserAuthService authService,
                               UserService userService) {
        this.storyRepository = storyRepository;
        this.documentService = documentService;
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentPayload buildManagerStoriesDocument(Long managerId) throws IOException {
        // Find all stories where the manager (from project) matches
        List<Story> stories = storyRepository.findAll().stream()
                .filter(story -> story.getManager() != null && story.getManager().getId().equals(managerId))
                .collect(Collectors.toList());
        
        User manager = userService.findById(managerId).orElse(null);
        User current = authService.getCurrentUser();
        String password = DocumentPasswordUtil.resolvePassword(manager, current);

        ByteArrayOutputStream os = documentService.generatePasswordProtectedManagerStoriesDocument(
                managerId, stories, password);
        String fileName = "manager_" + managerId + "_stories.docx";
        return DocumentPayload.ofDocx(os.toByteArray(), fileName);
    }
}