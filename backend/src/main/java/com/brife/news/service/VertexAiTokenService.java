package com.brife.news.service;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class VertexAiTokenService {

    private GoogleCredentials credentials;

    private synchronized GoogleCredentials getCredentials() throws IOException {
        if (credentials == null) {
            credentials = GoogleCredentials
                    .getApplicationDefault()
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            log.info("[VertexAI] Google Credentials 초기화 완료");
        }
        return credentials;
    }

    public String getAccessToken() throws IOException {
        GoogleCredentials creds = getCredentials();
        creds.refreshIfExpired();
        return creds.getAccessToken().getTokenValue();
    }
}
