package com.dolph.blog.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class OtpGenerator {
    public static String newOtp() {
        int otpLength = 5;

        int minDigitValue = (int) Math.pow(10, otpLength - 1);
        int maxDigitValue = (int) Math.pow(10, otpLength) - 1;

        Random random = new Random();

        int otpValue = random.nextInt(maxDigitValue - minDigitValue + 1) + minDigitValue;

        String otp = String.format("%05d", otpValue);

        return otp;
    }

    public static  final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static boolean isOtpValid(String otpExpiry){
        LocalDateTime expiryTime = LocalDateTime.parse(otpExpiry, formatter);
        LocalDateTime currentTime = LocalDateTime.now();

        return !currentTime.isAfter(expiryTime);
    }
}
