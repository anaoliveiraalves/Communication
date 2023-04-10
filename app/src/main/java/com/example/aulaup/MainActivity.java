package com.example.aulaup;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

import android.os.AsyncTask;
import com.jcraft.jsch.*;

public class MainActivity extends Activity {
    ApplicationExecutors exec;
    Timer _t;
    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MANAGE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (shouldAskPermissions()) {
            askPermissions();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                if (!Environment.isExternalStorageManager()){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }

        }
        exec = new ApplicationExecutors();

        _t = new Timer();
        doPeriodicTest();
    }
    protected void onDestroy() {
        super.onDestroy();
        exec.getBackground().execute(
                () -> {
                    _t.cancel();
                });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void saveFile(View button){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        if( mExternalStorageAvailable && mExternalStorageWriteable ){
            EditText editText = findViewById(R.id.editText1);
            String message = editText.getText().toString();
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "valores.txt");
            BufferedWriter bw;
            try {
                bw = new BufferedWriter(new FileWriter(file, true));
                bw.write(message);
                bw.newLine();
                bw.close();
                //bw.flush();
                editText.setText(" ");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("CURRENT_THREAD"+e.getMessage(), Long.toString(Thread.currentThread().getId()));
            }
            //Nao esquecer:  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        }

    }

    public void enviarMensagem (View view){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText1);
        String message = editText.getText().toString();
        intent.putExtra("Mensagem enviada:", message);
        startActivity(intent);
        Toast.makeText(getApplicationContext(),"Olá",Toast.LENGTH_SHORT).show();
    }

    public void newUploadFile(View button){
        exec.getNetworkThread().execute(
                () -> {
                    //In this block, we are now on a background thread
                    try {
                        JSch ssh = new JSch();
                        Session session = ssh.getSession("******", "****.****.***.****", 22);
                        // Remember that this is just for testing and we need a quick access, you can add an identity and known_hosts file to prevent
                        // Man In the Middle attacks
                        java.util.Properties config = new java.util.Properties();
                        config.put("StrictHostKeyChecking", "no");
                        session.setConfig(config);
                        session.setPassword("*******");

                        session.connect();
                        Channel channel = session.openChannel("sftp");
                        channel.connect();

                        ChannelSftp sftp = (ChannelSftp) channel;

                        sftp.cd("data");
                        // If you need to display the progress of the upload, read how to do it in the end of the article

                        // use the put method , if you are using android remember to remove "file://" and use only the relative path
                        sftp.put(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/valores.txt", "valores.txt");
                        channel.disconnect();
                        session.disconnect();
                    } catch (JSchException e) {
                        Log.d("NETWORK_THREAD"+e.getMessage(), Long.toString(Thread.currentThread().getId()));
                    } catch (SftpException e) {
                        Log.d("NETWORK_THREAD"+e.getMessage(), Long.toString(Thread.currentThread().getId()));
                    }


                }
        );

    }


    public void doPeriodicTest(){
        exec.getBackground().execute(
                () -> {
                    _t.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {


                            runOnUiThread(new Runnable() //run on ui thread
                            {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Clock:" + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }, 1000, 1000); //A recurrent task with a interval of 1000ms
                }
        );
    }
}
