package com.htc.enter.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.htc.enter.model.User;

public interface UserDocumentService {
    ByteArrayOutputStream generatePasswordProtectedUsersDocument(List<User> users, String password) throws IOException;
}
