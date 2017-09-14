package com.gx.me.demo4;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by TO on 2017/9/11.
 */

public class MyVpAdapter extends PagerAdapter {
    List<View> pageView = null;

    public MyVpAdapter() {
    }

    public MyVpAdapter(List<View> pageView) {
        this.pageView = pageView;
    }

    @Override
    public int getCount() {
        return pageView.size() * 1000;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(pageView.get(position % 5));
        return pageView.get(position % 5);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }


}
