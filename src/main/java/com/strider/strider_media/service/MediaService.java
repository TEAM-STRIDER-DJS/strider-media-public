package com.strider.strider_media.service;

import com.strider.strider_media.model.response.PresignResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class MediaService {

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    private final S3Presigner presigner;
    private final S3Client s3Client;
    private final String bucket;
    private final long presignExpirationSeconds;

    public MediaService(S3Presigner presigner,
                        S3Client s3Client,
                        @Value("${aws.s3.bucket}") String bucket,
                        @Value("${aws.s3.presign-expiration-seconds:300}") long presignExpirationSeconds) {
        this.presigner = presigner;
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.presignExpirationSeconds = presignExpirationSeconds;
    }

    /**
     * Pre-signed PUT URL 생성 (폴더 + 접두사 혼합 전략)
     */
    public PresignResponse generatePresignedPutUrl(String filename, String contentType, String type) {
        String s3Key;
        switch (type) {
            case "thumbnail":
                s3Key = "thumbnail/thumb_" + UUID.randomUUID() + "__" + filename;
                break;
            case "preview":
                s3Key = "preview/preview_" + UUID.randomUUID() + "__" + filename.replaceAll("\\.[^/.]+$", ".jpg");
                break;
            default: // original
                s3Key = "original/" + UUID.randomUUID() + "__" + filename;
                break;
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofSeconds(presignExpirationSeconds))
                .build();

        try {
            // 1. Presigned URL 생성
            PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
            String presignedUrl = presigned.url().toString();

            // 2. CloudFront URL 생성
            String cloudFrontUrl = this.generateCloudFrontUrl(s3Key);

            return PresignResponse.builder()
                    .s3Key(s3Key)
                    .presignedUrl(presignedUrl)
                    .cloudFrontUrl(cloudFrontUrl).build();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL", e);
            throw e;
        }
    }

    /**
     * S3 객체 삭제
     * @param key S3 객체 키
     */
    public void deleteMedia(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("Deleted S3 object: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete S3 object: {}", key, e);
            throw new RuntimeException("S3 deletion failed");
        }
    }

    /**
     * S3 Key를 받아 CloudFront URL을 생성
     */
    public String generateCloudFrontUrl(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) return null;
        return "https://" + cloudFrontDomain + "/" + s3Key;
    }

}
