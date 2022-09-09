package com.apptreo.export.sms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonWriter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private int numPermissionGranted = 0;
    private final static int SMS_EXPORT = 0;
    private final static String[] REQUIRED_PERMISSIONS = {
            "android.permission.READ_SMS",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    private boolean isExternalStorageWritable(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
    private File getExportFile(String name){
        if(!this.isExternalStorageWritable()){
            return null;
        }
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; ++i){
            if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                this.numPermissionGranted++;
            }else{
                this.showMessage(permissions[i] + " permission must be granted to export messages");
            }
        }
        if(this.numPermissionGranted == REQUIRED_PERMISSIONS.length){
            this.export(requestCode);
        }
    }
    public void dumToJson(Cursor cursor, OutputStreamWriter outputStreamWriter)throws IOException {
        JsonWriter writer = new JsonWriter(outputStreamWriter);
        writer.beginArray();
        while(cursor.moveToNext()){
            writer.beginObject();
            int numColumns = cursor.getColumnCount();
            for (int i = 0; i < numColumns; ++i){
                String name = cursor.getColumnName(i);
                String value = cursor.getString(i);
                writer.name(name).value(value);
            }
            writer.endObject();
        }
        writer.endArray();
        writer.close();
    }


    public void export(int exportType){

    }
}