package com.example.bdmap.activity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bdmap.R;
import com.example.bdmap.sqlite.ImportDB;
import com.example.bdmap.sqlite.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private ListView message_list;
    private List<Message>message_items;
    private ImportDB importDB;
    private Button add_message;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_admin);
        message_list=findViewById(R.id.message_list);
        add_message=findViewById(R.id.add_message);
        init_data();
        init_view();
        message_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String[] items = { "编辑","删除"};
                AlertDialog.Builder listDialog = new AlertDialog.Builder(AdminActivity.this);
                listDialog.setTitle("请选择");
                listDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals("编辑")) {
                            final String city=message_items.get(position).getCity();
                            final String message=message_items.get(position).getMessage();
                            AlertDialog.Builder customizeDialog = new AlertDialog.Builder(AdminActivity.this);
                            final View dialogView = LayoutInflater.from(AdminActivity.this).inflate(R.layout.edit_item,null);
                            customizeDialog.setTitle("编辑");
                            final EditText edit_text1 =  dialogView.findViewById(R.id.edit_text1);
                            final EditText edit_text2 = dialogView.findViewById(R.id.edit_text2);
                            TextView textView_1=dialogView.findViewById(R.id.textview_1);
                            TextView textView_2=dialogView.findViewById(R.id.textview_2);
                            textView_1.setText("城市：");
                            edit_text1.setText(city);
                            textView_2.setText("信息：");
                            edit_text2.setText(message);
                            customizeDialog.setView(dialogView);
                            customizeDialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String path=importDB.initDB();
                                            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//读写方式打开
                                            String new_city=edit_text1.getText().toString();
                                            String new_message=edit_text2.getText().toString();
                                            if(!new_city.isEmpty()&&!new_message.isEmpty()) {
                                                String sql = "update message set city = '" + new_city + "',message='" + new_message + "' where id = " + message_items.get(position).getId();
                                                db.execSQL(sql);
                                                db.close();
                                                init_data();
                                                init_view();
                                                Toast.makeText(AdminActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                                            }
                                            else {
                                                Toast.makeText(AdminActivity.this, "城市和信息不能为空", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                            customizeDialog.show();
                        }
                        else if(items[which].equals("删除")){
                            String path=importDB.initDB();
                            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//读写方式打开
                            String sql="delete from message where id="+message_items.get(position).getId();
                            db.execSQL(sql);
                            db.close();
                            init_data();
                            init_view();
                            Toast.makeText(AdminActivity.this,"删除成功",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                listDialog.show();
            }
        });
        add_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder customizeDialog = new AlertDialog.Builder(AdminActivity.this);
                final View dialogView = LayoutInflater.from(AdminActivity.this).inflate(R.layout.edit_item,null);
                customizeDialog.setTitle("请输入");
                final EditText edit_text1 =  dialogView.findViewById(R.id.edit_text1);
                final EditText edit_text2 = dialogView.findViewById(R.id.edit_text2);
                TextView textView_1=dialogView.findViewById(R.id.textview_1);
                TextView textView_2=dialogView.findViewById(R.id.textview_2);
                textView_1.setText("城市：");
                textView_2.setText("信息：");
                customizeDialog.setView(dialogView);
                customizeDialog.setPositiveButton("发布", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String city = edit_text1.getText().toString();
                        String message = edit_text2.getText().toString();
                        if (!city.isEmpty() && !message.isEmpty()) {
                            String path = importDB.initDB();
                            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);//读写方式打开
                            Calendar calendar = Calendar.getInstance();
                            String time=calendar.get(Calendar.MONTH)+1+"月"+calendar.get(Calendar.DAY_OF_MONTH)+"日"+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
                            String sql ="insert into message (message,city,time)values('"+message+"','"+city+"','"+time+"')";
                            db.execSQL(sql);
                            db.close();
                            init_data();
                            init_view();
                            Toast.makeText(AdminActivity.this, "发布成功", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(AdminActivity.this, "发布失败，城市和信息不能为空", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                customizeDialog.show();
            }
        });
    }
    private void init_data()
    {
        message_items=new ArrayList<>();
        importDB=new ImportDB(this);
        String path=importDB.initDB();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);//只读方式打开
        Cursor cursor = db.rawQuery("select * from message", null);
        cursor.moveToFirst();
        do
        {
            Message message = new Message();
            message.setId(cursor.getInt(0));
            message.setMessage(cursor.getString(1));
            message.setCity(cursor.getString(2));
            message_items.add(message);

        }while (cursor.moveToNext());
        db.close();
    }
    private void init_view()
    {
        List<HashMap<String, String>> list = new ArrayList<>();
        for (int i=0; i< message_items.size(); i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("city", message_items.get(i).getCity());
            map.put("message", message_items.get(i).getMessage());
            list.add(map);
        }
        SimpleAdapter simplead = new SimpleAdapter(this, list,
                R.layout.admin_item, new String[] { "city", "message" },
                new int[] {R.id.admin_city,R.id.admin_message});

        message_list.setAdapter(simplead);
    }
}
