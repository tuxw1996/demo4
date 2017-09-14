package com.gx.me.demo4;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
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


public class RecordlistsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordlists);
        m_lsRecord = (ListView) findViewById(R.id.listview);
        m_lsRecord.setAdapter(m_lsAdapter);
        m_lsRecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Record record = (Record) parent.getAdapter().getItem(position);
                boolean playlocal = false;
                long dlid = Tool.getdlId(RecordlistsActivity.this, record.getDownloadKey());
                if (dlid > 0) {
                    Cursor cursor = Tool.queryDownload(RecordlistsActivity.this, dlid);

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

                Intent it = new Intent(RecordlistsActivity.this, PlaybackActivity.class);
                it.putExtra("novel_poster", getIntent().getStringExtra("novel_poster"));
                startActivity(it);

            }
        });
        m_poster = getIntent().getStringExtra("poster");
        m_url     = getIntent().getStringExtra("url");

        loadData();
    }

    public void loadData () {
        String api = Const.RECORDAPI+"?novel_id="+getIntent().getLongExtra("novel_id", -1);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(api, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    JSONArray jsonArray = jsonObject.getJSONArray("records");
                    for (int i=0; i<jsonArray.length(); ++i) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Record record = Record.json2record(json);
                        if (record != null) {
                            m_Records.add(record);
                        }
                    }
                    m_lsAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(RecordlistsActivity.this, "加载失败！", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(RecordlistsActivity.this, "加载失败！", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onResume () {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(m_Receiver, intentFilter);
    }

    @Override
    protected void onPause () {
        super.onPause();
        unregisterReceiver(m_Receiver);
    }

    BaseAdapter m_lsAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return  m_Records.size();
        }

        @Override
        public Object getItem(int position) {
            return m_Records.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.rec_ls_item, parent, false);
                holder = new Holder();
                holder.mTx = (TextView) convertView.findViewById(R.id.name);
                holder.mBtn = (Button) convertView.findViewById(R.id.download_btn);
                holder.mBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Record record = (Record) v.getTag();
                        String novel_url = getIntent().getStringExtra("novel_url");
                        String url = Tool.getRecordUrl(novel_url, record.getUrl(), 1800);

                        Tool.postDownloadTask(RecordlistsActivity.this, url, record.getDownloadfilename(), record.getDownloadKey());
                        ((TextView) v).setText("下载中");
                        v.setClickable(false);
                    }
                });
                convertView.setTag(holder);
            }else {
                holder = (Holder) convertView.getTag();
            }

            Record record = (Record) getItem(position);

            holder.mTx.setText(record.getName());

            holder.mBtn.setTag(record);

            long downloadid = Tool.getdlId(RecordlistsActivity.this, record.getDownloadKey());

            if (downloadid > 0) {
                int status = Tool.getStatus(Tool.queryDownload(RecordlistsActivity.this, downloadid), true);
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    holder.mBtn.setVisibility(View.GONE);
                    holder.mBtn.setClickable(true);
                }else if (status == DownloadManager.STATUS_FAILED){
                    holder.mBtn.setVisibility(View.VISIBLE);
                    holder.mBtn.setText("下载失败");
                    holder.mBtn.setClickable(true);
                }else {
                    holder.mBtn.setVisibility(View.VISIBLE);
                    holder.mBtn.setText("下载中");
                    holder.mBtn.setClickable(false);
                }
            }else {
                holder.mBtn.setVisibility(View.VISIBLE);
                holder.mBtn.setText("下载");
                holder.mBtn.setClickable(true);
            }
            // 更新downloadbutton

            return convertView;
        }

        class Holder {
            TextView mTx;
            Button mBtn;
        }
    };


    BroadcastReceiver m_Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            m_lsAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public String getTag() {
        return "Recordlist Actvity";
    }

    List<Record> m_Records = new LinkedList<>();
    ListView m_lsRecord;
    String m_poster;
    String m_url;
}
