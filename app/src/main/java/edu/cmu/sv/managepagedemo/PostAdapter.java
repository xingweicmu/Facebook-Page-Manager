package edu.cmu.sv.managepagedemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingwei on 11/25/15.
 */
public class PostAdapter extends ArrayAdapter{
    List list = new ArrayList();

    public PostAdapter(Context context, int resource) {
        super(context, resource);
    }

    static class DataHandler{
        TextView content;
        TextView views;
    }

    public void add(Object object) {
        super.add(object);
        list.add(object);
    }

    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        row=convertView;
        DataHandler handler;
        if(convertView==null)
        {
            LayoutInflater inflater=(LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row=inflater.inflate(R.layout.post_row,parent,false);
            handler=new DataHandler();
//            handler.poster=(ImageView)row.findViewById(R.id.move_poster);
            handler.content=(TextView)row.findViewById(R.id.move_title);
            handler.views=(TextView)row.findViewById(R.id.move_rating);
            row.setTag(handler);
        }
        else {

            handler=(DataHandler)row.getTag();
        }
        PostDataProvider dataProvider;

        dataProvider=(PostDataProvider)this.getItem(position);
//        handler.poster.setImageResource(dataProvider.getMove_poster_resource());
        handler.content.setText(dataProvider.getContent());
        handler.views.setText(dataProvider.getViews()+"");
        return row;
    }

}
