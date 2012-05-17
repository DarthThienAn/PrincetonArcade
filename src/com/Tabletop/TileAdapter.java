package com.Tabletop;

import com.link.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


public class TileAdapter extends BaseAdapter {
    private Context mContext;
    int unselect = R.drawable.megusta;
    private Integer[] mThumbIds;

    public TileAdapter(Context c, Integer[] pics) {
        mContext = c;
        mThumbIds = pics;
    }
    
    public TileAdapter(Context c) {
        mContext = c;
        Integer [] a = {
        		unselect, unselect, unselect, unselect,
                unselect, unselect, unselect, unselect,
                unselect, unselect, unselect, unselect,
                unselect, unselect, unselect, unselect
        };
        mThumbIds = a;
    }

    public int getCount() {
        return mThumbIds.length;
    }
    

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        GameTile imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new GameTile(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (GameTile) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    /*private Integer[] mThumbIds = {
    		unselect, unselect, unselect, unselect,
            unselect, unselect, unselect, unselect,
            unselect, unselect, unselect, unselect,
            unselect, unselect, unselect, unselect
    };*/
}
