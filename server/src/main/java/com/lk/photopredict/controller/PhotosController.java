package com.lk.photopredict.controller;

import com.lk.photopredict.repository.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/photos")
public class PhotosController {
    private static final Logger logger = LoggerFactory.getLogger(PhotosController.class);

    private final DataStore photosStorage;

    @Autowired
    public PhotosController(@Qualifier("image") DataStore photosStorage) {
        this.photosStorage = photosStorage;
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<ByteArrayResource> getPhoto(@PathVariable String photoId) {
        logger.info("Retrieving photo with ID: {}", photoId);
        byte[] photoData = photosStorage.getBytes(photoId);
        if (photoData == null) {
            logger.warn("Photo not found with ID: {}", photoId);
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(photoData);
        logger.info("Successfully retrieved photo with ID: {}", photoId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photoId + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(photoData.length)
                .body(resource);
    }
}
