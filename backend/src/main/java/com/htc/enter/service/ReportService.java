package com.htc.enter.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.htc.enter.model.Epic;
import com.htc.enter.model.Project;
import com.htc.enter.model.Story;

public interface ReportService {
    byte[] exportProjectsExcel(List<Project> projects) throws Exception;
    byte[] exportProjectsWord(List<Project> projects, String password) throws Exception;
    byte[] exportProjectsPdf(List<Project> projects, String password) throws Exception;

    byte[] exportEpicsExcel(List<Epic> epics) throws Exception;
    byte[] exportEpicsWord(List<Epic> epics, String password) throws Exception;
    byte[] exportEpicsPdf(List<Epic> epics, String password) throws Exception;

    byte[] exportStoriesExcel(List<Story> stories) throws Exception;
    byte[] exportStoriesWord(List<Story> stories, String password) throws Exception;
    byte[] exportStoriesPdf(List<Story> stories, String password) throws Exception;
}
