package com.dolph.blog.helpers;

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
}
