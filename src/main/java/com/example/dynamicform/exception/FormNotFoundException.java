package com.example.dynamicform.exception;

public class FormNotFoundException extends RuntimeException {
    private final String formName;

    public FormNotFoundException(String formName) {
        super("Form not found: " + formName);
        this.formName = formName;
    }

    public String getFormName() {
        return formName;
    }
}
