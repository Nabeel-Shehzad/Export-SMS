package com.apptreo.export.sms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private int id;
    private String address;
    private String type;
    private String body;
    private String date;

    public Message(int id, String address, String type, String body, String date) {
        this.id = id;
        this.address = address;
        this.type = type;
        this.body = body;
        DateFormat obj = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
        Date res = new Date(date);
        this.date = obj.format(res);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "id= "+getId()+", address= "+getAddress()+", type= "+getType()+", body= "+getBody()+", date= "+getDate();
    }
}
