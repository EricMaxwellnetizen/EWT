package com.htc.enter.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.htc.enter.model.Story;

public interface StoryDocumentService {
    ByteArrayOutputStream generatePasswordProtectedManagerStoriesDocument(Long managerId, List<Story> stories, String password) throws IOException;
}
