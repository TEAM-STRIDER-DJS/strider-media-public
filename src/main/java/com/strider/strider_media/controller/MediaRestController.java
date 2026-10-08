package com.strider.strider_media.controller;

import com.strider.strider_common_lib.response.StriderResponse;
import com.strider.strider_media.model.request.PresignRequest;
import com.strider.strider_media.model.request.SampleRequest;
import com.strider.strider_media.model.response.PresignResponse;
import com.strider.strider_media.model.response.SampleResponse;
import com.strider.strider_media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/media")
public class MediaRestController {
    private final MediaService mediaService;

    @PostMapping("/presign")
    public ResponseEntity<StriderResponse<PresignResponse>> presign(@Validated @RequestBody PresignRequest request) {
        PresignResponse response = mediaService.generatePresignedPutUrl(
                request.filename(),
                request.contentType(),
                request.type()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(StriderResponse.responseBuilder(response, PresignResponse.class));
    }

    @PostMapping("/delete")
    public void deleteMedia(@RequestParam("key") String encodedKey) {
        String key = new String(Base64.getUrlDecoder().decode(encodedKey), StandardCharsets.UTF_8);
        mediaService.deleteMedia(key);
    }



}
