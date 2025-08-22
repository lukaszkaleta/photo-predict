package com.aceve.hackathon.payload;

import java.util.List;

/**
 * Incoming deviation.
 *
 * @param images as array of decoded images
 * @param recordings as array of decoded recordings
 * @param comment comment from specialist
 */
public record DeviationRequest(List<String> images, List<String> recordings, String comment) {
}
