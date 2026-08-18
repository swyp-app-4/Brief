package com.brife.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class NaverUnlinkClient {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public NaverUnlinkClient(@Value("${naver.login.client-id}") String clientId,
                             @Value("${naver.login.client-secret}") String clientSecret) {
        this(RestClient.builder().baseUrl("https://nid.naver.com").build(), clientId, clientSecret);
    }

    NaverUnlinkClient(RestClient restClient, String clientId, String clientSecret) {
        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void revokeRefreshToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");

        restClient.post()
                .uri("/oauth2.0/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }
}
