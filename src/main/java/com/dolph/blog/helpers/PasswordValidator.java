package com.dolph.blog.helpers;

public class PasswordValidator {
    public static boolean isValidPassword(String password){
        if(password.length() <6){
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for(char c: password.toCharArray()){
            if(Character.isLetter(c)){
                hasLetter = true;
            }else if(Character.isDigit(c)){
                hasDigit = true;
            }

            if(hasLetter && hasDigit){
                return true;
            }
        }

        return false;
    }
}
