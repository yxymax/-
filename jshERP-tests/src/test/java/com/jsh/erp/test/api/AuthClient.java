package com.jsh.erp.test.api;

import com.jsh.erp.test.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 封装登录，供后续接口用例复用 Token。
 */
public final class AuthClient {

    private AuthClient() {
    }

    public static void initRestAssured() {
        RestAssured.baseURI = TestConfig.apiBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /**
     * 登录成功后返回 X-Access-Token。
     * 前端传的是 MD5(明文密码)，这里保持一致。
     */
    public static String loginAndGetToken() {
        return loginAndGetToken(TestConfig.loginName(), TestConfig.loginPassword());
    }

    public static String loginAndGetToken(String loginName, String passwordPlain) {
        initRestAssured();

        Map<String, Object> body = new HashMap<>();
        body.put("loginName", loginName);
        body.put("password", DigestUtils.md5Hex(passwordPlain));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/user/login")
                .then()
                .extract()
                .response();

        assertEquals(200, response.jsonPath().getInt("code"),
                "登录失败: " + response.asString());
        String token = response.jsonPath().getString("data.token");
        assertNotNull(token, "响应里没有 token");
        return token;
    }
}
