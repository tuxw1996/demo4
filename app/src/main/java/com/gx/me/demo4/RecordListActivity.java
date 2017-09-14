package com.gx.me.demo4;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gx.me.demo4.model.Const;
import com.gx.me.demo4.model.Msg;
import com.gx.me.demo4.model.Record;
import com.gx.me.demo4.model.ServiceEvent;
import com.gx.me.demo4.utils.Tool;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class RecordListActivity extends AppCompatActivity {
    private String m_poster = null;
    private String m_url = null;
    private ListView lvRecord = null;
    private AsyncHttpClient httpClient = null;
    private List<Record> recordList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        m_poster = getIntent().getStringExtra("poster");//上一个Activity传过来数据
        m_url = getIntent().getStringExtra("url");//上一个Activity传过来数据
        initData();

        lvRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Record record = (Record) parent.getAdapter().getItem(position);
                boolean playlocal = false;
                long dlid = Tool.getdlId(RecordListActivity.this, record.getDownloadKey());
                if (dlid > 0) {
                    Cursor cursor = Tool.queryDownload(RecordListActivity.this, dlid);

                    int status = Tool.getStatus(cursor, false);

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        String file_uri = Tool.getDownloadFile(cursor, true);
                        EventBus.getDefault().post(new ServiceEvent(Msg.PLAY_LOCAL, file_uri));
                        playlocal = true;
                    }
                }

                if (playlocal == false) {
                    String novel_url = getIntent().getStringExtra("novel_url");
                    String url = Tool.getRecordUrl(novel_url, record.getUrl(), 1800);
                    EventBus.getDefault().post(new ServiceEvent(Msg.PLAY, url));
                }

                Intent it = new Intent(RecordListActivity.this, PlaybackActivity.class);
                //获取上一个Activity传递的参数并传到下一层
                it.putExtra("novel_poster", getIntent().getStringExtra("novel_poster"));
                startActivity(it);
            }
        });

        m_poster = getIntent().getStringExtra("poster");//头像
        m_url = getIntent().getStringExtra("url");

        loadData();
    }

    /**
     * 初始化数据
     */
    public void initData() {
        lvRecord = (ListView) findViewById(R.id.list_record);
        recordList = new LinkedList<Record>();
        httpClient = new AsyncHttpClient();
        lvRecord.setAdapter(baseAdapter);
    }

    /**
     * 加载数据
     */
    public void loadData() {
        String api = Const.RECORDAPI + "?novel_id=" + getIntent().getIntExtra("novel_id", -1);
        httpClient.get(api, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    JSONArray jsonArray = jsonObject.getJSONArray("records");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        Record record = Record.json2record(jo);
                        if (null != record) {
                            recordList.add(record);
                        }
                    }
                    baseAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(RecordListActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(RecordListActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
            }
        });
    }

    BaseAdapter baseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return recordList.size();
        }

        @Override
        public Object getItem(int position) {
            return recordList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (null == convertView) {
                convertView = getLayoutInflater().inflate(R.layout.record_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) convertView.findViewById(R.id.txt_recordName);
                viewHolder.button = (Button) convertView.findViewById(R.id.btn_recordDownload);
                //点击下载按钮
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Record record = (Record) v.getTag();
                        String novel_url = getIntent().getStringExtra("novel_url");
                        String url = Tool.getRecordUrl(novel_url, record.getUrl(), 1800);//拼接下载路径

                        Tool.postDownloadTask(RecordListActivity.this, url, record.getDownloadfilename(), record.getDownloadKey());
                        ((TextView) v).setText("下载中");
                        v.setClickable(false);//下载按钮不可点
                    }
                });
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            viewHolder.textView.setText(recordList.get(position).getName());
            viewHolder.button.setTag(recordList.get(position));//下载按钮存一个Record

            long downloadid = Tool.getdlId(RecordListActivity.this, recordList.get(position).getDownloadKey());
            if (downloadid > 0) {
                int status = Tool.getStatus(Tool.queryDownload(RecordListActivity.this, downloadid), true);
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    viewHolder.button.setVisibility(View.GONE);
                    viewHolder.button.setClickable(true);
                } else if (status == DownloadManager.STATUS_FAILED) {
                    viewHolder.button.setVisibility(View.VISIBLE);
                    viewHolder.button.setText("下载失败");
                    viewHolder.button.setClickable(true);
                } else {
                    viewHolder.button.setVisibility(View.VISIBLE);
                    viewHolder.button.setText("下载中");
                    viewHolder.button.setClickable(false);
                }
            } else {
                viewHolder.button.setVisibility(View.VISIBLE);
                viewHolder.button.setText("下载");
                viewHolder.button.setClickable(true);
            }
            // 更新downloadbutton

            return convertView;
        }

        class ViewHolder {
            TextView textView;
            Button button;
        }
    };

    //广播
    BroadcastReceiver m_Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            baseAdapter.notifyDataSetChanged();//更新数据
        }
    };


}
