package com.example.memorymuseum.dto;

import com.example.memorymuseum.model.MemoryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemorySearchDto {
    private String title;
    private String description;
    private Long userId;
    private String username;
    private String location;
    private MemoryStatus status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long emotionTypeId;
    private String tagName;
}
