package zhiyuan3g.com.pulltoloadrefresh.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 卫明阳 on 2016/7/27.
 */
public class DateAdapter extends BaseAdapter {
    private List<String> list;
    private Context context;

    public DateAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(context);
        textView.setText(list.get(position));
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(18);
        return textView;
    }
}
