package com.alphafinity.alphafinity.model;

import org.apache.commons.lang3.StringUtils;

public class Valid {
    public final Boolean isValid;
    public final Boolean isNotValid;
    public final String error;

    public Valid(Boolean isValid, String error) {
        this.isValid = isValid;
        this.isNotValid = !isValid;
        this.error = error;
    }

    public Valid(Boolean isValid) {
        this.isValid = isValid;
        this.isNotValid = !isValid;
        this.error = StringUtils.EMPTY;
    }
}
