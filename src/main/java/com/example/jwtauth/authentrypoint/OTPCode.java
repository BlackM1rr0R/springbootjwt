package com.example.jwtauth.authentrypoint;

import org.springframework.stereotype.Service;

@Service
public class OTPCode {
    public String generateOTPCode() {
        int otp = (int) (Math.random() * 9000) + 1000;
        return String.valueOf(otp);
    }
}
