package com.strider.strider_media.model.request;

public record PresignRequest (
        String filename,
        String contentType,
        String type
) {
}
