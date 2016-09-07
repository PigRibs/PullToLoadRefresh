package zhiyuan3g.com.pulltoloadrefresh.activity;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import zhiyuan3g.com.pulltoloadrefresh.R;
import zhiyuan3g.com.pulltoloadrefresh.adapter.DateAdapter;
import zhiyuan3g.com.pulltoloadrefresh.entity.RefreshListView;

public class MainActivity extends AppCompatActivity implements RefreshListView.OnRefreshListener {
    private RefreshListView listView;
    private List<String> list = new ArrayList<>();
    private List<String> tempList = new ArrayList<>();
    private DateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (RefreshListView) findViewById(R.id.listView);
        list = getDate();
        adapter = new DateAdapter(list, this);
        listView.setAdapter(adapter);
        listView.setOnRefreshListener(this);
    }

    private List<String> getDate() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            list.add("这是一条listView的数据" + (i + 1));
        }
        return list;
    }

    @Override
    public void onDownPullRefresh() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SystemClock.sleep(2000);
                int number = (int) (Math.random() * (8 + 1 - 5) + 5);

                for (int i = 0; i < number; i++) {
                    tempList.add("这是下拉刷新>>>>>>>更多出来的数据" + (i + 1));
                }
                tempList.addAll(list);
                list.clear();
                list.addAll(tempList);
                tempList.clear();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                adapter.notifyDataSetChanged();
                listView.hideHeaderView();
            }
        }.execute(new Void[]{});
    }

    @Override
    public void onLodingMore() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SystemClock.sleep(3000);
                int number = (int) (Math.random() * (10 + 1 - 5) + 5);
                for (int i = 0; i < number; i++) {
                    list.add("这是上拉加载>>>>>>更多出来的数据" + (i + 1));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                adapter.notifyDataSetChanged();
                listView.hideFooterView();
            }
        }.execute(new Void[]{});
    }
}
