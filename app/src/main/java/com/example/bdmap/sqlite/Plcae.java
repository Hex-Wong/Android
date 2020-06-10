package com.example.bdmap.sqlite;

public class Plcae {
    private String pname;
    private String latitude;
    private String longitude;
    private int Id;

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getPname() {
        return pname;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }
}
