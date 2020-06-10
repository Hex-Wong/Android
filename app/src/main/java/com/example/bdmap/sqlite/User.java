package com.example.bdmap.sqlite;

public class User {
    private int Id;
    private String Name;
    private String Password;
    public int getId(){return Id;}
    public void setId(int id){this.Id=id;}
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        this.Password = password;
    }

    @Override
    public String toString(){
        String result = "";
        result +="ID："+this.Id;
        result += "用户名：" + this.Name + "，";
        result += "密码：" + this.Password;
        return result;
    }

}
