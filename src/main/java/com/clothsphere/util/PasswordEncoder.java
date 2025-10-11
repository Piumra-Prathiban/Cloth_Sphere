package com.clothsphere.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordEncoder {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return encoder;
    }

    public static String encryptPassword(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    public static boolean matches(String plainPassword, String encryptedPassword) {
        return encoder.matches(plainPassword, encryptedPassword);
    }
}