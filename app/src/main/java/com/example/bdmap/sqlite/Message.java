package com.example.bdmap.sqlite;

public class Message {
    private int Id;
    private String Message;
    private String City;
    private String Time;
    public int getId()
    {
        return Id;
    }
    public void setId(int id)
    {
        this.Id=id;
    }
    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        this.Message = message;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        this.City = city;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        this.Time = time;
    }

    @Override
    public String toString(){
        String result = "";
        result += "信息：" + this.Message + "，";
        //result += "城市：" + this.City + "，";
        result += "时间：" + this.Time;
        return result;
    }
}
