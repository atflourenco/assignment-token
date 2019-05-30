package com.token.model;

import java.io.Serializable;

public class QRCodeData implements Serializable {
    private String key;
    private String label;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "QRCodeData{" +
                "key='" + key + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
