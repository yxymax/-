package com.jsh.erp.test.api;

import com.jsh.erp.test.config.TestConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Day1 第 1 课：登录接口自动化
 *
 * 前置：后端已启动；数据库 checkcode_flag=0（验证码关闭）
 */
class LoginApiTest {

    @BeforeAll
    static void setUp() {
        AuthClient.initRestAssured();
    }

    @Test
    @DisplayName("正确账号密码应登录成功并返回 token")
    void shouldLoginSuccessfully() {
        Map<String, Object> body = new HashMap<>();
        body.put("loginName", TestConfig.loginName());
        body.put("password", DigestUtils.md5Hex(TestConfig.loginPassword()));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/user/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // 业务码（不是 HTTP 状态码）
        assertEquals(200, response.jsonPath().getInt("code"));
        assertEquals("user can login", response.jsonPath().getString("data.msgTip"));
        assertNotNull(response.jsonPath().getString("data.token"));
        assertEquals(TestConfig.loginName(), response.jsonPath().getString("data.user.loginName"));
    }

    @Test
    @DisplayName("错误密码应登录失败")
    void shouldFailWithWrongPassword() {
        Map<String, Object> body = new HashMap<>();
        body.put("loginName", TestConfig.loginName());
        body.put("password", DigestUtils.md5Hex("wrong-password"));

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/user/login")
                .then()
                .extract()
                .response();

        // 业务异常可能返回非 200 的 code，或抛错被包装；此处只断言“不能拿到合法 token”
        Integer code = response.jsonPath().get("code");
        String token = response.jsonPath().getString("data.token");
        assertTrue(code == null || code != 200 || token == null,
                "错误密码不应返回可用 token，实际响应: " + response.asString());
    }

    @Test
    @DisplayName("AuthClient 封装登录后可用 Token 访问需登录接口")
    void shouldAccessProtectedApiWithToken() {
        String token = AuthClient.loginAndGetToken();

        Response response = given()
                .header("X-Access-Token", token)
                .when()
                .get("/user/getUserSession")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertEquals(200, response.jsonPath().getInt("code"));
        assertEquals(TestConfig.loginName(),
                response.jsonPath().getString("data.user.loginName"));
    }
}
