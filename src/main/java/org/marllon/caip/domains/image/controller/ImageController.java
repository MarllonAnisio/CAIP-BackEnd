package org.marllon.caip.domains.image.controller;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.domains.image.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('STUDENT', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileStorageService.upload(file, "caip/reports");
        return ResponseEntity.ok(Map.of("url", imageUrl));
    }
}
