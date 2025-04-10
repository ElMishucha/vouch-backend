//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.model;

import lombok.Generated;

public class Source {
    String title;
    String url;
    String date;
    String body;
    String support;

    @Generated
    public String getTitle() {
        return this.title;
    }

    @Generated
    public String getUrl() {
        return this.url;
    }

    @Generated
    public String getDate() {
        return this.date;
    }

    @Generated
    public String getBody() {
        return this.body;
    }

    @Generated
    public String getSupport() {
        return this.support;
    }

    @Generated
    public Source(final String title, final String url, final String date, final String body, final String support) {
        this.title = title;
        this.url = url;
        this.date = date;
        this.body = body;
        this.support = support;
    }

    @Generated
    public Source() {
    }
}
