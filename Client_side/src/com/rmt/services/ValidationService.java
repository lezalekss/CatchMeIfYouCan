package com.rmt.services;

public class ValidationService {

    private static ValidationService service = null;

    private ValidationService(){

    }

    public static ValidationService getValidationServiceInstance(){
        if(service == null){
            service = new ValidationService();
        }
        return service;
    }



    public boolean validatePasswordDigits(String password) {
        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean validatePasswordUpperAndLowerCases(String password) {
        boolean containsUpperCase = false;
        boolean containsLowerCase = false;

        for (int i = 0; i < password.length(); i++) {
            if (Character.isUpperCase(password.codePointAt(i))) {
                containsUpperCase = true;
                if (containsLowerCase) {
                    break;
                } else {
                    containsLowerCase = this.containsLowerCase(password.substring(i + 1));
                    break;
                }
            } else if (Character.isLowerCase(password.codePointAt(i))) {
                containsLowerCase = true;
                if (containsUpperCase) {
                    break;
                } else {
                    containsUpperCase = this.containsUpperCase(password.substring(i + 1));
                    break;
                }
            }
        }
        return containsUpperCase && containsLowerCase;
    }

    private boolean containsLowerCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLowerCase(string.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }
    private boolean containsUpperCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isUpperCase(string.codePointAt(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean passwordTooShort(String password){
        return password.length() < 8;
    }

    public boolean passwordTooLong(String password){
        return password.length() > 20;
    }
}
