package com.example.bdmap.activity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdmap.R;
import com.example.bdmap.sqlite.ImportDB;
import com.example.bdmap.sqlite.Message;
import com.example.bdmap.sqlite.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class SuperAdminActivity extends AppCompatActivity {
    private ListView user_liset;
    private List<User> user_items;
    private ImportDB importDB;
    private Button add_user;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_superadmin);
        user_liset=findViewById(R.id.user_list);
        add_user=findViewById(R.id.add_user);
        init_data();
        init_view();
        user_liset.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String[] items = { "编辑","删除"};
                AlertDialog.Builder listDialog = new AlertDialog.Builder(SuperAdminActivity.this);
                listDialog.setTitle("请选择");
                listDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals("编辑")) {
                            final String name=user_items.get(position).getName();
                            final String password=user_items.get(position).getPassword();
                            AlertDialog.Builder customizeDialog = new AlertDialog.Builder(SuperAdminActivity.this);
                            final View dialogView = LayoutInflater.from(SuperAdminActivity.this).inflate(R.layout.edit_item,null);
                            customizeDialog.setTitle("编辑");
                            final EditText edit_text1 =  dialogView.findViewById(R.id.edit_text1);
                            final EditText edit_text2 = dialogView.findViewById(R.id.edit_text2);
                            TextView textView_1=dialogView.findViewById(R.id.textview_1);
                            TextView textView_2=dialogView.findViewById(R.id.textview_2);
                            textView_1.setText("用户名：");
                            edit_text1.setText(name);
                            textView_2.setText("密  码：");
                            edit_text2.setText(password);
                            customizeDialog.setView(dialogView);
                            customizeDialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String path=importDB.initDB();
                                    SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//读写方式打开
                                    String new_name=edit_text1.getText().toString();
                                    String new_password=edit_text2.getText().toString();
                                    if(!new_name.isEmpty()&&!new_password.isEmpty()) {
                                        String sql = "update user set name = '" + new_name + "',password='" + new_password + "' where id = " + user_items.get(position).getId();
                                        db.execSQL(sql);
                                        db.close();
                                        init_data();
                                        init_view();
                                        Toast.makeText(SuperAdminActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(SuperAdminActivity.this, "用户名和密码不能为空", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            customizeDialog.show();
                        }
                        else if(items[which].equals("删除")){
                            String path=importDB.initDB();
                            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//读写方式打开
                            String sql="delete from user where id="+user_items.get(position).getId();
                            db.execSQL(sql);
                            db.close();
                            init_data();
                            init_view();
                            Toast.makeText(SuperAdminActivity.this,"删除成功",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                listDialog.show();
            }
        });
        add_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder customizeDialog = new AlertDialog.Builder(SuperAdminActivity.this);
                final View dialogView = LayoutInflater.from(SuperAdminActivity.this).inflate(R.layout.edit_item,null);
                customizeDialog.setTitle("请输入");
                final EditText edit_text1 =  dialogView.findViewById(R.id.edit_text1);
                final EditText edit_text2 = dialogView.findViewById(R.id.edit_text2);
                TextView textView_1=dialogView.findViewById(R.id.textview_1);
                TextView textView_2=dialogView.findViewById(R.id.textview_2);
                textView_1.setText("用户名：");
                textView_2.setText("密  码：");
                customizeDialog.setView(dialogView);
                customizeDialog.setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = edit_text1.getText().toString();
                        String password = edit_text2.getText().toString();
                        if (!name.isEmpty() && !password.isEmpty()) {
                            boolean exist =find_name(name);
                            if (exist){
                                Toast.makeText(SuperAdminActivity.this, "添加失败，用户名已存在", Toast.LENGTH_LONG).show();
                            }
                            else {

                                String path = importDB.initDB();
                                SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//读写方式打开
                                String sql = "insert into user (name,password,type)values('" + name + "','" + password + "','管理员')";
                                db.execSQL(sql);
                                db.close();
                                init_data();
                                init_view();
                                Toast.makeText(SuperAdminActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(SuperAdminActivity.this, "添加失败，用户名和密码不能为空", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                customizeDialog.show();
            }
        });

    }
    private void init_data()
    {
        user_items=new ArrayList<>();
        importDB=new ImportDB(this);
        String path=importDB.initDB();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);//只读方式打开
        Cursor cursor = db.rawQuery("select * from user", null);
        cursor.moveToFirst();
        do
        {
            if(!cursor.getString(3).equals("高级管理员")) {
                User user = new User();
                user.setId(cursor.getInt(0));
                user.setName(cursor.getString(1));
                user.setPassword(cursor.getString(2));
                user_items.add(user);
            }
        }while (cursor.moveToNext());
        db.close();
    }
    private void init_view()
    {
        List<HashMap<String, String>> list = new ArrayList<>();
        for (int i=0; i< user_items.size(); i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("name", user_items.get(i).getName());
            map.put("password", user_items.get(i).getPassword());
            list.add(map);
        }
        SimpleAdapter simplead = new SimpleAdapter(this, list,
                R.layout.admin_item, new String[] { "name", "password" },
                new int[] {R.id.admin_city,R.id.admin_message});

        user_liset.setAdapter(simplead);
    }
    private boolean find_name(String name)
    {
        for (int i=0; i< user_items.size(); i++)
        {
            if(user_items.get(i).getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
}
