package com.gx.me.demo4;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gx.me.demo4.model.Const;
import com.gx.me.demo4.model.Novel;
import com.gx.me.demo4.utils.Tool;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ListViewActivity extends BaseActivity {
    private ListView lvNovel = null;
    private MyLsAdapter myLsAdapter = null; //ListView的Adapter
    private List<Novel> novelList = null; //存放小说列表
    private Context context = null;//当前类context对象
    private AsyncHttpClient m_HttpClient;//获取网络json数据
    private View viewPageXml; //viewpage.xml文件的对象
    //private ViewPager viewPager = null;
    private MyViewPager viewPager = null;
    private MyVpAdapter myVpAdapter = null; //ViewPage的Adapter
    private List<View> viewPageList = null; //存放轮播
    private LinearLayout indicator = null;//轮播中的点

    @Override
    public String getTag() {
        return "ListViewActivity";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        initData();
        loadViewPageData();
        loadListViewData();
        lvNovel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Novel novel = (Novel) parent.getAdapter().getItem(position);
                Intent it = new Intent(context, RecordlistsActivity.class);
                it.putExtra("novel_id", novel.id);
                it.putExtra("novel_poster", novel.poster);
                it.putExtra("novel_url", novel.url);
                startActivity(it);
            }
        });
        Intent it = new Intent(ListViewActivity.this, PlaybackService.class);
        startService(it);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        novelList = new LinkedList<Novel>();
        m_HttpClient = new AsyncHttpClient();
        lvNovel = (ListView) findViewById(R.id.list_view);
        context = ListViewActivity.this;
        myLsAdapter = new MyLsAdapter(novelList, context);
        lvNovel.setAdapter(myLsAdapter);
        viewPageList = new LinkedList<View>();
    }

    /**
     * 加载轮播数据
     */
    public void loadViewPageData() {
        viewPageXml = getLayoutInflater().inflate(R.layout.viewpage, lvNovel, false);//轮播xml文件
        viewPager = (MyViewPager) viewPageXml.findViewById(R.id.viewpage);//轮播图片
        indicator = (LinearLayout) viewPageXml.findViewById(R.id.indicator);//轮播点
        myVpAdapter = new MyVpAdapter(viewPageList);
        viewPager.setAdapter(myVpAdapter);
        //把viewpage(轮播)添加到listview(小说列表)的头部
        lvNovel.addHeaderView(viewPageXml);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    updateIndicator();
                }
            }
        });
    }

    /**
     * 加载小说列表数据
     */
    private void loadListViewData() {
        m_HttpClient.get(Const.NOVELAPI, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    JSONArray jsonArray = jsonObject.getJSONArray("novels");
                    //List<Novel> novels = new LinkedList<Novel>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Novel novel = Novel.json2novel(jsonArray.getJSONObject(i));
                        if (null != novel) {
                            novelList.add(novel);
                            //novels.add(novel);
                        }
                    }
                    //Novel.updateNovelsToDb(ListViewActivity.this, novels);
                    // 选取前5个novel作为 viewpager来显示
                    initPages();
                    // 数据加载 完毕， 通知列表去更新
                    myLsAdapter.notifyDataSetChanged();//小说列表
                    myVpAdapter.notifyDataSetChanged();//轮播
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ListViewActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
                }
                //查询本地数据绑定到集合
                //novelList = Novel.loadNovelsFromDb(ListViewActivity.this);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(ListViewActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
                //没网络加载本地数据（前提是已经加载过一次本地数据）
                // 选取前5个novel作为 viewpager来显示
                //initPages();

                // 数据加载 完毕， 通知列表去更新
                //myLsAdapter.notifyDataSetChanged();
                //myVpAdapter.notifyDataSetChanged();
            }
        });

    }

    /**
     * 初始化轮播图片数据
     */
    public void initPages() {
        viewPageList.clear();
        for (int i = 0; i < 5; i++) {
            Novel novel = novelList.get(i);
            ImageView imageView = (ImageView) getLayoutInflater().inflate(R.layout.viewpage_item, null);
            //获取图片并绑定到ImageView
            Tool.getImageCache(this).get(novel.poster, imageView);
            viewPageList.add(imageView);
        }

        //更新小圆点
        indicator.removeAllViews();
        for (int i = 0; i < 5; i++) {
            float width = Tool.dip2px(context, 8.0f);
            float height = Tool.dip2px(context, 8.0f);
            float margin_right = Tool.dip2px(context, 5.0f);
            View view = new View(context);
            if (i == 0) {
                view.setBackgroundResource(R.mipmap.indicator_unselected);
            } else {
                view.setBackgroundResource(R.mipmap.indicator_selected);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) width, (int) height, 1.0f);
            params.rightMargin = (int) margin_right;
            view.setLayoutParams(params);
            indicator.addView(view);
        }

    }

    /**
     * 轮播图片滑动后下面小点改变状态
     */
    public void updateIndicator() {
        int cuttentItem = viewPager.getCurrentItem();//当前项
        for (int i = 0; i < indicator.getChildCount(); i++) {//循环所有LinearLayout下子项
            View child = indicator.getChildAt(i);//获取子标签
            child.setBackgroundResource(R.mipmap.indicator_unselected); //全改为未选中
        }
        //把当前的改为选中
        indicator.getChildAt(cuttentItem % 5).setBackgroundResource(R.mipmap.indicator_selected);
    }


}
