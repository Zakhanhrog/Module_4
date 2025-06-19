package com.example.memorymuseum.dto;

import com.example.memorymuseum.model.MemoryFile;
import com.example.memorymuseum.model.MemoryStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class MemoryDto {
    private Long id;

    @NotEmpty(message = "{memory.title.notempty}")
    private String title;

    private String description;

    @NotNull(message = "{memory.date.notnull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate memoryDate;

    private String location;
    private Integer emotionTypeId;
    private MemoryStatus status;

    private List<MemoryFile> existingFiles = new ArrayList<>();
    private List<Long> filesToDeleteIds = new ArrayList<>();
}