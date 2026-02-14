package com.htc.enter.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.htc.enter.model.Epic;

public interface EpicDocumentService {
    ByteArrayOutputStream generatePasswordProtectedManagerEpicsDocument(Long managerId, List<Epic> epics, String password) throws IOException;
}
