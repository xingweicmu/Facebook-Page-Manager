package edu.cmu.sv.managepagedemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by xingwei on 11/23/15.
 */
public class PostActivity extends Activity {

    private Switch publishSwitch;
    private Button postButton;
    private EditText postEditText;
    private boolean publishFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_page);

        // Set up switch
        publishSwitch = (Switch) findViewById(R.id.publishSwitch);

        //set the switch to ON
        publishSwitch.setChecked(true);
        //attach a listener to check for changes in state
        publishSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
//                    switchStatus.setText("Switch is currently ON");
                    publishFlag = true;
                    Toast.makeText(getApplicationContext(), "Publish switch is set ON.", Toast.LENGTH_LONG).show();
                } else {
//                    switchStatus.setText("Switch is currently OFF");
                    publishFlag = false;
                    Toast.makeText(getApplicationContext(), "Publish switch is set OFF.", Toast.LENGTH_LONG).show();
                }

            }
        });

        //check the current state before we display the screen
//        if(publishSwitch.isChecked()){
//            Toast.makeText(getApplicationContext(), "Publish switch is currently ON.", Toast.LENGTH_LONG).show();
//        }
//        else {
//            Toast.makeText(getApplicationContext(), "Publish switch is currently ON.", Toast.LENGTH_LONG).show();
//        }

        postEditText = (EditText) findViewById(R.id.postEditText);
        postButton = (Button) findViewById(R.id.postButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), postEditText.getText()+ " "+publishFlag, Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(PostActivity.this, MainActivity.class);
//                startActivity(intent);
                if(publishFlag){
                    createRegularPost();
//                    retrieveAllPosts();
                }
                else{
//                    unpublishPost();
                }

            }
        });
    }

    private void createRegularPost() {
        new PostTask(this).execute("");
    }
    class PostTask extends AsyncTask<String, Void, String> {

        private Exception exception;
        //        private String response;
        private Context context;

        public PostTask(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... urls) {
            String response = "";
            try {
                URL url = new URL("https://graph.facebook.com/901893839866098/feed");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
//                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // FOR POST
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("message", "published post")
                        .appendQueryParameter("access_token", "CAAVOngp3Ys0BALNZCZCZBHpWvl4utVHM0ywTi6LQeBzPGpMq17rM4LkArniQMqOc6npLZAyNPEM7HFP8lE3skYAakswBORPEcMHb22yXvZBWqQYZBUGEZA8NSEwQ0dzB0YqvZCnSqoUsHd2ZCHecZA8ZCijJp3OSoDCH3fLHihCQtbMzZCP7Ps7bIZBl2");
                String query = builder.build().getEncodedQuery();

                // FOR GET
//                Uri.Builder builder = new Uri.Builder()
//                        .appendQueryParameter("fields", "id,message,from,to")
//                        .appendQueryParameter("access_token", "CAAB8PDqe1GoBAHffQeSzahQng2S8kZBxpB6kBpGS2FiYCtvbxcOLUcgzKR8ZAkAclUKZBh9dDp1MK9jtDpOli6Sv633EaSIkHbuREVVxZAv5iXarLJk0hZCImwRl7itk1eB3ej2a1ol5F1nZBAizZAlSZA74u2cZAqkZCZAjo3ZBUKZCLiriiRpkjdb4rtNkx3Fy4lKIZD");
//                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();
                Log.d("facebook##", responseCode + "");
//                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                    Log.d("facebook##", response);
//                }
//                else {
//                    response=null;
//                }

            }catch(Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String response) {
            // TODO: check this.exception
            // TODO: do something with the feed
            if(response != null) {
                Toast.makeText(getApplicationContext(), "Post success!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this.context, MainActivity.class));
            }
        }
    }

    private void retrieveAllPosts() {
        new GetTask().execute("https://graph.facebook.com/901893839866098/feed?fields=id,message,from,to&access_token=CAAVOngp3Ys0BACoqRehqry0EaCDFbWGdJbPxpKf77FdHDZBJl9nvrYYMOWFAKSj1ldhOSw8tpqVfpTTJ41y21HTg94NTL0J6TNYHbTtZBc7Y1Da3AsLYekuABBRrWwtdHclNZAFM9OjDLADbNzaJ16TELyx1xZCAgdOSQxGH5sPpA7f0dzYV");
    }

    class GetTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                String url = urls[0];

                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                int responseCode = con.getResponseCode();
                Log.d("facebook##", "Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                Log.d("facebook##", response.toString());
            }catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }



        protected void onPostExecute(String feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }

}
