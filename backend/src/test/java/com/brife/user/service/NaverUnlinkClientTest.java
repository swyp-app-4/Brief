package com.brife.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NaverUnlinkClientTest {

    @Test
    void createsBeanWithoutRestClientBuilderBean() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                    "naver.login.client-id", "client-id",
                    "naver.login.client-secret", "client-secret"
            )));
            context.register(NaverUnlinkClient.class);
            context.refresh();

            context.getBean(NaverUnlinkClient.class);
        }
    }

    @Test
    void revokesRefreshTokenAndItsLinkedAccessToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NaverUnlinkClient client = new NaverUnlinkClient(
                builder.baseUrl("https://nid.naver.com").build(), "client-id", "client-secret");
        MultiValueMap<String, String> expectedForm = new LinkedMultiValueMap<>();
        expectedForm.add("client_id", "client-id");
        expectedForm.add("client_secret", "client-secret");
        expectedForm.add("token", "refresh-token");
        expectedForm.add("token_type_hint", "refresh_token");
        server.expect(once(), requestTo("https://nid.naver.com/oauth2.0/revoke"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formData(expectedForm))
                .andRespond(withSuccess());

        client.revokeRefreshToken("refresh-token");

        server.verify();
    }
}
