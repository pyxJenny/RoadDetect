package com.example.admin.mmaptest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;

/**
 * Created by admin on 2017/1/9.
 */
public class WelcomActivity extends Activity {
    private CheckBox check1, check2;
    private Button ok;
    boolean deletFile = false;
    String filePathRms = "/storage/emulated/0/Rms.txt";
    String filePathAdd = "/storage/emulated/0/Address.txt";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        check1 = (CheckBox) findViewById(R.id.check1);
        check2 = (CheckBox) findViewById(R.id.check2);
        ok = (Button) findViewById(R.id.ok);

        hint();
        fileExists(filePathRms,filePathAdd);


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(WelcomActivity.this,MainActivity.class);
                intent.putExtra("shishi",check1.isChecked());
                intent.putExtra("bendi", check2.isChecked());
                startActivity(intent);
            }
        });
    }

    public void hint(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(WelcomActivity.this);
        builder.setTitle("请确保手机GPS开启且处于联网状态");
        builder.setPositiveButton("好的",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                Toast toast = Toast.makeText(WelcomActivity.this,"谢谢配合",Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        builder.create().show();
    }

    public void fileExists(String filepathrms, String filepathadd){
        final AlertDialog.Builder builder = new AlertDialog.Builder(WelcomActivity.this);
        try {
            File f1 = new File(filepathrms);
            File f2 = new File(filepathadd);
            if (f1.exists() || f2.exists()) {
                builder.setTitle("提示:检测到上次测试文件，是否放弃？");
                builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deletFile = true;
                        deletFile(filePathRms,filePathAdd);
                    }
                });
                builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deletFile = false;
                        Toast toast = Toast.makeText(WelcomActivity.this, "已取消", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                builder.create().show();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        }
    public void deletFile(String filepathrms, String filepathadd){
        try {
            File f1 = new File(filepathrms);
            File f2 = new File(filepathadd);
            f1.getAbsoluteFile().delete();
            f2.getAbsoluteFile().delete();
            Toast toast = Toast.makeText(WelcomActivity.this, "已删除", Toast.LENGTH_SHORT);
            toast.show();

    }
        catch (Exception e){
        e.printStackTrace();}
    }
    }

