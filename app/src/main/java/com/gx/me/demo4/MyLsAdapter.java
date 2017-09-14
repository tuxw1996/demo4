package com.gx.me.demo4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gx.me.demo4.model.Novel;
import com.gx.me.demo4.utils.Tool;

import java.util.List;

/**
 * Created by TO on 2017/9/11.
 */

public class MyLsAdapter extends BaseAdapter {
    private List<Novel> list;
    private Context context;

    public MyLsAdapter() {
    }

    public MyLsAdapter(List<Novel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override

    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            viewHolder.novel_name = (TextView) convertView.findViewById(R.id.txt_name);
            viewHolder.author = (TextView) convertView.findViewById(R.id.txt_author);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //Novel novel = (Novel) getItem(position);
        // 使用ImageCache 做异步图像加载.
        Tool.getImageCache(context).get(list.get(position).nj_avatar, viewHolder.avatar);
        viewHolder.novel_name.setText(list.get(position).novel_name);
        viewHolder.author.setText(list.get(position).author);
        return convertView;
    }

    class ViewHolder {
        ImageView avatar;
        TextView novel_name;
        TextView author;
    }
}
