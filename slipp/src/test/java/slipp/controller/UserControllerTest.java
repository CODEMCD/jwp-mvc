package slipp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import support.test.CustomWebTestClient;
import support.test.TestServerRunner;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerTest {
    private static final String USER_ID1 = "comac";
    private static final String USER_ID2 = "comac2";
    private static final String PASSWORD = "pw1234";
    private static final String NAME = "코맥";
    private static final String EMAIL = "park@naver.com";

    private CustomWebTestClient client;

    @BeforeEach
    void setUp() {
        TestServerRunner.run();
        client = CustomWebTestClient.of(8080);
    }

    @Test
    void 회원가입_성공() {
        signUp(USER_ID1, PASSWORD, NAME, EMAIL);
    }

    @Test
    void 로그인_성공() {
        signUp(USER_ID2, PASSWORD, NAME, EMAIL);

        login(USER_ID2, PASSWORD);
    }

    @Test
    void 유저_리스트_조회() {
        signUp(USER_ID1, PASSWORD, NAME, EMAIL);
        signUp(USER_ID2, PASSWORD, NAME, EMAIL);

        String cookie = getCookie(USER_ID2, PASSWORD);

        client.build()
                .get()
                .uri("/users")
                .header("Cookie", cookie)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(Objects.requireNonNull(response.getResponseBody()));
                    assertTrue(responseBody.contains(USER_ID1));
                    assertTrue(responseBody.contains(USER_ID2));
                })
        ;
    }

    @Test
    void 유저_프로파일_조회() {
        signUp(USER_ID1, PASSWORD, NAME, EMAIL);

        String cookie = getCookie(USER_ID1, PASSWORD);

        client.build()
                .get()
                .uri("/users/profile?userId=" + USER_ID1)
                .header("Cookie", cookie)
                .exchange()
                .expectStatus()
                .isOk()
        ;
    }

    @Test
    void 유저_수정_페이지() {
        signUp(USER_ID1, PASSWORD, NAME, EMAIL);

        String cookie = getCookie(USER_ID1, PASSWORD);

        client.build()
                .get()
                .uri("/users/updateForm?userId=" + USER_ID1)
                .header("Cookie", cookie)
                .exchange()
                .expectStatus()
                .isOk()
        ;
    }

    @Test
    void 유저_수정_성공() {
        signUp(USER_ID1, PASSWORD, NAME, EMAIL);

        String cookie = getCookie(USER_ID1, PASSWORD);

        String updatedUserId = "cocomac";
        String updatedPassword = "pw123456";
        String updatedName = "코코맥";
        String updatedEmail = "cocomac@naver.com";

        client.build()
                .put()
                .uri("users/update?userId=" + USER_ID1)
                .header("Cookie", cookie)
                .body(BodyInserters
                        .fromFormData("userId", updatedUserId)
                        .with("password", updatedPassword)
                        .with("name", updatedName)
                        .with("email", updatedEmail))
                .exchange()
                .expectStatus()
                .isFound()
        ;
    }

    private String getCookie(String userId, String password) {
        return login(userId, password)
                .returnResult(String.class)
                .getResponseHeaders()
                .getFirst("Set-Cookie")
                ;
    }

    private WebTestClient.ResponseSpec login(String userId, String password) {
        return client.build()
                .post()
                .uri("/users/login")
                .body(BodyInserters
                        .fromFormData("userId", userId)
                        .with("password", password))
                .exchange()
                .expectStatus()
                .isFound()
                ;
    }

    private WebTestClient.ResponseSpec signUp(String userId, String password, String name, String email) {
        return client.build()
                .post()
                .uri("/users/create")
                .body(BodyInserters
                        .fromFormData("userId", userId)
                        .with("password", password)
                        .with("name", name)
                        .with("email", email))
                .exchange()
                .expectStatus()
                .isFound()
                ;
    }

}
