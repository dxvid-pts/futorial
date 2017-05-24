package de.davepe.futorial.tabs.forum;

import java.io.Serializable;

public class Page implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;

    private int number;

    public Page(String url, int number) {
        this.url = url;
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}