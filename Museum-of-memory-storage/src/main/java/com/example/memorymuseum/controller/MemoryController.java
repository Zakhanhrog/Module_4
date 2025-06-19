package com.example.memorymuseum.controller;

import com.example.memorymuseum.dto.MemoryDto;
import com.example.memorymuseum.model.Memory;
import com.example.memorymuseum.model.MemoryFile;
import com.example.memorymuseum.model.MemoryStatus;
import com.example.memorymuseum.model.User;
import com.example.memorymuseum.service.EmotionTypeService;
import com.example.memorymuseum.service.FileStorageService;
import com.example.memorymuseum.service.MemoryService;
import com.example.memorymuseum.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/memories")
public class MemoryController {

    private final MemoryService memoryService;
    private final UserService userService;
    private final EmotionTypeService emotionTypeService;
    private final FileStorageService fileStorageService;
    private static final String MEMORY_FILES_SUBFOLDER = "memories";


    @Autowired
    public MemoryController(MemoryService memoryService, UserService userService,
                            EmotionTypeService emotionTypeService, FileStorageService fileStorageService) {
        this.memoryService = memoryService;
        this.userService = userService;
        this.emotionTypeService = emotionTypeService;
        this.fileStorageService = fileStorageService;
    }

    private User getCurrentUserOrThrow() {
        return userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String showCreateMemoryForm(Model model) {
        if (!model.containsAttribute("memoryDto")) {
            MemoryDto memoryDto = new MemoryDto();
            memoryDto.setStatus(MemoryStatus.PRIVATE); // Default status
            model.addAttribute("memoryDto", memoryDto);
        }
        model.addAttribute("emotionTypes", emotionTypeService.findAll());
        model.addAttribute("pageTitle", "memory.form.title.create");
        model.addAttribute("formAction", "/memories/create");
        return "memory/memory-form";
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String createMemory(@Valid @ModelAttribute("memoryDto") MemoryDto memoryDto,
                               BindingResult result,
                               @RequestParam("files") List<MultipartFile> files,
                               RedirectAttributes redirectAttributes, Model model) {
        User currentUser = getCurrentUserOrThrow();

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.memoryDto", result);
            redirectAttributes.addFlashAttribute("memoryDto", memoryDto);
            model.addAttribute("emotionTypes", emotionTypeService.findAll());
            model.addAttribute("pageTitle", "memory.form.title.create");
            model.addAttribute("formAction", "/memories/create");
            return "memory/memory-form";
        }

        memoryService.saveMemory(memoryDto, files, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Kỷ niệm đã được tạo thành công!");
        return "redirect:/memories/timeline";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String showEditMemoryForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUserOrThrow();
        Optional<Memory> memoryOpt = memoryService.findById(id);

        if (memoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Kỷ niệm không tồn tại.");
            return "redirect:/memories/timeline";
        }

        Memory memory = memoryOpt.get();
        if (!memory.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa kỷ niệm này.");
        }

        if (!model.containsAttribute("memoryDto")) {
            MemoryDto memoryDto = new MemoryDto();
            memoryDto.setId(memory.getId());
            memoryDto.setTitle(memory.getTitle());
            memoryDto.setDescription(memory.getDescription());
            memoryDto.setMemoryDate(memory.getMemoryDate());
            memoryDto.setLocation(memory.getLocation());
            memoryDto.setStatus(memory.getStatus());
            if (memory.getEmotionType() != null) {
                memoryDto.setEmotionTypeId(memory.getEmotionType().getId());
            }
            // Populate existing files
            memoryDto.setExistingFiles(memory.getFiles().stream()
                    .map(mf -> {
                        MemoryFile displayFile = new MemoryFile();
                        displayFile.setId(mf.getId());
                        displayFile.setOriginalFileName(mf.getOriginalFileName());
                        // Create a relative path for display if filePath is absolute
                        String displayPath = mf.getFilePath();
                        // Construct URL for display
                        displayPath = "/memories/files/" + mf.getMemory().getId() + "/" + Paths.get(mf.getFilePath()).getFileName().toString();
                        displayFile.setFilePath(displayPath);
                        return displayFile;
                    })
                    .collect(Collectors.toList()));
            model.addAttribute("memoryDto", memoryDto);
        }

        model.addAttribute("emotionTypes", emotionTypeService.findAll());
        model.addAttribute("pageTitle", "memory.form.title.edit");
        model.addAttribute("formAction", "/memories/update/" + id);
        return "memory/memory-form";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public String updateMemory(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("memoryDto") MemoryDto memoryDto,
                               BindingResult result,
                               @RequestParam("newFiles") List<MultipartFile> newFiles,
                               RedirectAttributes redirectAttributes, Model model) {
        User currentUser = getCurrentUserOrThrow();

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.memoryDto", result);
            redirectAttributes.addFlashAttribute("memoryDto", memoryDto); // Keep the user's input
            // Re-populate existing files for display if validation fails
            if(memoryDto.getId() != null) { // id should be there if it's an update
                memoryService.findById(memoryDto.getId()).ifPresent(memory ->
                        memoryDto.setExistingFiles(memory.getFiles().stream()
                                .map(mf -> {
                                    MemoryFile displayFile = new MemoryFile();
                                    displayFile.setId(mf.getId());
                                    displayFile.setOriginalFileName(mf.getOriginalFileName());
                                    String displayPath = "/memories/files/" + mf.getMemory().getId() + "/" + Paths.get(mf.getFilePath()).getFileName().toString();
                                    displayFile.setFilePath(displayPath);
                                    return displayFile;
                                }).collect(Collectors.toList()))
                );
            }
            model.addAttribute("emotionTypes", emotionTypeService.findAll());
            model.addAttribute("pageTitle", "memory.form.title.edit");
            model.addAttribute("formAction", "/memories/update/" + id);
            return "memory/memory-form";
        }

        memoryService.updateMemory(id, memoryDto, newFiles, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Kỷ niệm đã được cập nhật thành công!");
        return "redirect:/memories/timeline";
    }


    @GetMapping("/timeline")
    @PreAuthorize("isAuthenticated()")
    public String userTimeline(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        User currentUser = getCurrentUserOrThrow();
        Pageable pageable = PageRequest.of(page, size, Sort.by("memoryDate").descending().and(Sort.by("createdAt").descending()));
        Page<Memory> memoryPage = memoryService.findMemoriesByUser(currentUser, pageable);
        model.addAttribute("memoryPage", memoryPage);
        model.addAttribute("timelineTitle", "memory.timeline.my");
        return "memory/timeline";
    }

    @GetMapping("/public")
    public String publicTimeline(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("memoryDate").descending().and(Sort.by("createdAt").descending()));
        Page<Memory> memoryPage = memoryService.findPublicMemories(pageable);
        model.addAttribute("memoryPage", memoryPage);
        model.addAttribute("timelineTitle", "memory.timeline.public");
        return "memory/timeline";
    }

    @GetMapping("/{id}")
    public String viewMemory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Memory> memoryOpt = memoryService.findById(id);
        if (memoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Kỷ niệm không tìm thấy.");
            return "redirect:/home";
        }
        Memory memory = memoryOpt.get();
        Optional<User> currentUserOpt = userService.getCurrentUser();

        if (memory.getStatus() == MemoryStatus.PRIVATE) {
            if (currentUserOpt.isEmpty() || !memory.getUser().getId().equals(currentUserOpt.get().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xem kỷ niệm này.");
                return "redirect:/home";
            }
        }

        model.addAttribute("memory", memory);
        return "memory/memory-detail";
    }

    @GetMapping("/files/{memoryId}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable Long memoryId, @PathVariable String filename) {
        Optional<Memory> memoryOpt = memoryService.findById(memoryId);
        if(memoryOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        String subfolder = MEMORY_FILES_SUBFOLDER + "/" + memoryId;
        Resource file = fileStorageService.loadAsResource(filename, subfolder);

        if (file == null || !file.exists() || !file.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteMemory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUserOrThrow();
        try {
            memoryService.deleteMemory(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Kỷ niệm đã được xóa thành công.");
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa kỷ niệm này.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/memories/timeline";
    }
}