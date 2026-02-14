package com.htc.enter.service;

import java.util.List;

import com.htc.enter.model.Epic;
import com.htc.enter.dto.EpicDTO;

public interface EpicService {
	Epic save(Epic epic);
	Epic findById(Long id);
	    List<Epic> findAll();
	    void deleteById(Long id);

    Epic createFromDTO(EpicDTO dto);
    Epic updateFromDTO(Long id, EpicDTO dto);
}