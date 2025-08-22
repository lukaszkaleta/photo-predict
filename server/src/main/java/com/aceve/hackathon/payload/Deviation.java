package com.aceve.hackathon.payload;

import java.util.List;


/**
 * Data which will be analyzed.
 *
 * @param id
 * @param timestamp
 * @param images as urls to image files
 * @param recordings as urls to image files.
 * @param comment
 */
public record Deviation(
        String id,
        String timestamp,
        List<String> images,
        List<String> recordings,
        String comment) {
}
