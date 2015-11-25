package edu.cmu.sv.managepagedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by xingwei on 11/24/15.
 */
public class PostListActivity extends Activity{
    ListView listView;
//    int[]move_poster_resource={R.drawable.movi,R.drawable.move5,R.drawable.move6,
//            R.drawable.movi11,R.drawable.movi2,R.drawable.movi3,R.drawable.movi7,R.drawable.movi4,R.drawable.movi9,R.drawable.movi4,};
    String[]move_title = new String[]{"This is test 1", "This is test 2"};
    int[]move_rating = new int[]{2, 3};
    PostAdapter adapter;
    ArrayList<PostDataProvider> posts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_list);
        listView=(ListView)findViewById(R.id.postList);
//        move_rating=getResources().getStringArray(R.array.move_rating);
//        move_title=getResources().getStringArray(R.array .move_title);

        posts= getIntent().getParcelableArrayListExtra("posts");
        Log.d("facebook##", posts.size()+"");
        int i=0;
        adapter=new PostAdapter(getApplicationContext(),R.layout.post_row);
        listView.setAdapter(adapter);
//        for (String title:move_title) {
//            PostDataProvider dataProvider=new PostDataProvider(title,move_rating[i]);
        for(PostDataProvider dataProvider: posts) {
            adapter.add(dataProvider);
            i++;

        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?>parent, View view, int position, long id){
                Toast.makeText(getBaseContext(), position + " is selected", Toast.LENGTH_SHORT).show();
                PostDataProvider current = posts.get(position);
                String currentId = current.getId();
//                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("read_insights"));
                new GetTask().execute("https://graph.facebook.com/901893839866098_907621532626662/insights/post_impressions_unique/lifetime?access_token=CAAVOngp3Ys0BACoqRehqry0EaCDFbWGdJbPxpKf77FdHDZBJl9nvrYYMOWFAKSj1ldhOSw8tpqVfpTTJ41y21HTg94NTL0J6TNYHbTtZBc7Y1Da3AsLYekuABBRrWwtdHclNZAFM9OjDLADbNzaJ16TELyx1xZCAgdOSQxGH5sPpA7f0dzYV");
//                Intent myIntent = new Intent(view.getContext(), second_class.class);
//                startActivity(myIntent);
            }
        });

    }

    class GetTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            String result = "";
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
                result = response.toString();
                Log.d("facebook##", response.toString());
            }catch(Exception e) {
                e.printStackTrace();
            }
            return result;
        }



        protected void onPostExecute(String response) {
            // Parse the response
            try {
                JSONObject jsonObject = new JSONObject(response);
////                JSONArray array = null;
//
                if(jsonObject != null) {
                    JSONArray array = jsonObject.getJSONArray("data");
//                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(0);
                        String views = item.getJSONArray("values").getJSONObject(0).getString("value");
//                        String message = item.getString("message");
//                        String createTime = item.getString("created_time");
//                        String id = item.getString("id");
                        Log.d("facebook##", "views: " + views);
//                        posts.add(new PostDataProvider(message, createTime, id));
//                    }
                }
                // show detail dialog
                new AlertDialog.Builder(PostListActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Post Detail")
                        .setMessage("Message: This is a test\nViews: 1\nCreated Date:2016")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
//                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
