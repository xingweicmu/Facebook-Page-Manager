package edu.cmu.sv.managepagedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by xingwei on 11/24/15.
 */
public class PostListActivity extends Activity{
    ListView listView;
    PostAdapter adapter;
    PostDataProvider selectedPost = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_list);
        listView=(ListView)findViewById(R.id.postList);

        final ArrayList<PostDataProvider> posts= getIntent().getParcelableArrayListExtra("posts");
        Log.d("facebook##", posts.size()+"");
        int i=0;
        adapter=new PostAdapter(getApplicationContext(),R.layout.post_row);
        listView.setAdapter(adapter);

        for(PostDataProvider dataProvider: posts) {
            adapter.add(dataProvider);
            i++;

        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), position + " is selected", Toast.LENGTH_SHORT).show();
                selectedPost = posts.get(position);
                String currentId = selectedPost.getId();

                // Read access code from sharedpreference
                SharedPreferences prefs = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                String pageAccessTotken = prefs.getString("access_token", null);
                Log.d("facebook##", "access_token: "+pageAccessTotken);

                new GetTask().execute("https://graph.facebook.com/"+currentId+"/insights/post_impressions_unique/lifetime?access_token="+pageAccessTotken);
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

                if(jsonObject != null) {
                    JSONArray array = jsonObject.getJSONArray("data");
                    JSONObject item = array.getJSONObject(0);
                    String views = item.getJSONArray("values").getJSONObject(0).getString("value");
                    selectedPost.setViews(Integer.parseInt(views));
                    Log.d("facebook##", "views: " + views);
                }
                // show detail dialog
                new AlertDialog.Builder(PostListActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Post Detail")
                        .setMessage(selectedPost.toString())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
