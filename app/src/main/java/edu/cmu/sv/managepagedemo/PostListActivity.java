package edu.cmu.sv.managepagedemo;

import android.app.Activity;
import android.content.Intent;
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

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_list);
        listView=(ListView)findViewById(R.id.postList);
//        move_rating=getResources().getStringArray(R.array.move_rating);
//        move_title=getResources().getStringArray(R.array .move_title);

        ArrayList<PostDataProvider> posts= getIntent().getParcelableArrayListExtra("posts");
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
                Toast.makeText(getBaseContext(), position + "is selected", Toast.LENGTH_SHORT).show();
//                Intent myIntent = new Intent(view.getContext(), second_class.class);
//                startActivity(myIntent);
            }
        });

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
