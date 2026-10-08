package com.strider.strider_media.model.response;

import lombok.Builder;

@Builder
public record PresignResponse(
        String s3Key,
        String presignedUrl,
        String cloudFrontUrl
) {
}
