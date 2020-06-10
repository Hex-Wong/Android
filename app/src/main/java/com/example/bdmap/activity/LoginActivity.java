package com.example.bdmap.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bdmap.R;
import com.example.bdmap.sqlite.ImportDB;

public class LoginActivity extends AppCompatActivity {
    private EditText name;
    private EditText password;
    private Button login;
    private ImportDB importDB;
    private String user_name;
    private String user_password;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        name=findViewById(R.id.name);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_name=name.getText().toString();
                user_password=password.getText().toString();
                int t=find(user_name,user_password);
                if (t==0) {
                    Toast.makeText(LoginActivity.this.getApplicationContext(),
                            "账号密码错误", Toast.LENGTH_SHORT).show();
                }
                else if(t==1) {
                    Toast.makeText(LoginActivity.this.getApplicationContext(),
                            "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,
                            AdminActivity.class);
                    startActivity(intent);
                }
                else if (t==2) {
                    Toast.makeText(LoginActivity.this.getApplicationContext(),
                            "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,
                            SuperAdminActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
    private int find(String U,String P)
    {
        importDB=new ImportDB(this);
        String path=importDB.initDB();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);//只读方式打开
        Cursor cursor = db.rawQuery("select * from user", null);
        cursor.moveToFirst();
        do
        {
            if (cursor.getString(1).equals(U) && cursor.getString(2).equals(P))//密码匹配成功
            {
                if (cursor.getString(3).equals("管理员")){ return 1; }
                else if(cursor.getString(3).equals("高级管理员")) { return 2; }
            }
        }while (cursor.moveToNext());

        return 0;
    }
}
