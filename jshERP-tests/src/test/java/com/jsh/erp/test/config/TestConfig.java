package com.jsh.erp.test.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取测试配置。改账号/地址只改 config.properties。
 */
public final class TestConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("找不到 config.properties");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private TestConfig() {
    }

    public static String apiBaseUrl() {
        return PROPS.getProperty("api.baseUrl");
    }

    public static String webBaseUrl() {
        return PROPS.getProperty("web.baseUrl");
    }

    public static String loginName() {
        return PROPS.getProperty("login.name");
    }

    public static String loginPassword() {
        return PROPS.getProperty("login.password");
    }
}
