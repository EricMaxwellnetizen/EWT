package com.htc.enter.service;

import java.io.IOException;

import com.htc.enter.dto.DocumentPayload;

public interface UserAppService {
    DocumentPayload buildUsersDocument() throws IOException;
}
