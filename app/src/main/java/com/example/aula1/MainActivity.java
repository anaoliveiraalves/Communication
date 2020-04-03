package com.example.aula1;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.util.Log;

import android.os.AsyncTask;
import com.jcraft.jsch.*;



public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                bw.flush();
                editText.setText(" ");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //Nao esquecer:  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        }

    }


    private static class LongOperation extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                JSch ssh = new JSch();
                Session session = ssh.getSession("username", "sftpserver", 22);
                // Remember that this is just for testing and we need a quick access, you can add an identity and known_hosts file to prevent
                // Man In the Middle attacks
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword("password");

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
                System.out.println(e.getMessage());
                e.printStackTrace();
            } catch (SftpException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return "Terminado";
        }



        @Override
        protected void onPostExecute(String result) {
            Log.d("PostExecuted",result);
        }



    }

    public void uploadFile(View button) {
        new LongOperation().execute();
    }


}
