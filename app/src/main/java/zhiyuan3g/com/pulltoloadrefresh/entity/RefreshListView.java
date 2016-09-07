package zhiyuan3g.com.pulltoloadrefresh.entity;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import zhiyuan3g.com.pulltoloadrefresh.R;

/**
 * Created by 卫明阳 on 2016/7/27.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {
    private View headerView;//头布局
    private int headerViewHeight;//头布局实际高度
    private int downY;//按下时Y的坐标
    private int firstVisibleItemPosition;//第一条数据item

    private final int DOWN_PULL_REFRESH = 0;//下拉刷新状态
    private final int RELEASE_REFRESH = 1;//松开刷新状态
    private final int REFRESHING = 2;//正在刷新中
    private int currentState = DOWN_PULL_REFRESH;//头布局状态,默认为下拉刷新

    private Animation upAnimation;//向上旋转的动画
    private Animation downAnimation;//向下旋转的动画

    private ImageView list_head_image;//头布局箭头图标
    private ProgressBar list_head_progressbar;//头布局的进度条
    private TextView head_txt_refresh;//头布局的刷新文字
    private TextView head_txt_currentTime;//头布局的最近更新时间

    private OnRefreshListener mOnRefreshListener;//刷新和加载监听
    private boolean isScorllToBottom;//是否滑动到底部
    private View footerView;//脚布局对象
    private int footerViewHeight;//脚布局的高度
    private boolean isLoadingMore = false;//是否正在加载更多

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFooterView();
        initHeaderView();
        this.setOnScrollListener(this);
    }

    //初始化脚布局
    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.listview_footer, null);
        footerView.measure(0, 0);//测量脚布局的高度
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        Log.i("hjkl", footerViewHeight + "");
        this.addFooterView(footerView);
    }

    //初始化头布局
    private void initHeaderView() {
        headerView = View.inflate(getContext(), R.layout.list_head, null);
        list_head_image = (ImageView)
                headerView.findViewById(R.id.list_head_image);
        list_head_progressbar = (ProgressBar)
                headerView.findViewById(R.id.list_head_progressbar);
        head_txt_refresh = (TextView)
                headerView.findViewById(R.id.head_txt_refresh);
        head_txt_currentTime = (TextView)
                headerView.findViewById(R.id.head_txt_currentTime);

        head_txt_currentTime.setText("最后刷新时间：" + getLastUpdateTime());

        headerView.measure(0, 0);
        headerViewHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        this.addHeaderView(headerView);
        initAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();//按下时y的绝对位置

                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) ev.getY();//移动时y的绝对位置
                int diff = (moveY - downY) / 2;//手指移动的距离
                Log.i("TAG","ACTION_DOWN:"+downY+"");
                Log.i("TAG","ACTION_MOVE:"+moveY+"");
                Log.i("TAG",">>>>>>>>>>:"+diff+"");
                int paddingTop = -headerViewHeight + diff;//头布局此时还隐藏的高度
                //当firstVisibleItemPosition=0为第一条才刷新数据，
                //paddingTop移动的距离大于0,说明此时已经头部已经全部出现了
                if (firstVisibleItemPosition == 0 && paddingTop > -headerViewHeight) {
                    if (paddingTop > 0 && currentState == DOWN_PULL_REFRESH) {
                        Log.i("TAG", "松开刷新");
                        currentState = RELEASE_REFRESH;//松开刷新状态
                        refreshHeaderView();
                    } else if (paddingTop < 0 && currentState == RELEASE_REFRESH) {
                        Log.i("TAG", "下拉刷新");
                        currentState = DOWN_PULL_REFRESH;//下拉刷新状态
                        refreshHeaderView();
                    }
                    headerView.setPadding(0, paddingTop, 0, 0);//此时头布局隐藏的距离
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentState == RELEASE_REFRESH) {
                    Log.i("ghjkl", currentState + "");
                    headerView.setPadding(0, 0, 0, 0);
                    currentState = REFRESHING;//正在刷新状态
                    refreshHeaderView();
                    //如果此时用户刷新监听mOnRefreshListener不为空，
                    // 说明用户正在下拉刷新进行时
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onDownPullRefresh();
                    }
                } else if (currentState == DOWN_PULL_REFRESH) {
                    Log.i("HHHH", "hhh" + currentState);
                    headerView.setPadding(0, -headerViewHeight, 0, 0);
                }
                break;
        }
        return super.onTouchEvent(ev);
    }


    //根据currentState状态刷新头布局
    private void refreshHeaderView() {
        switch (currentState) {
            case DOWN_PULL_REFRESH://下拉刷新
                head_txt_refresh.setText("下拉刷新");
                list_head_image.startAnimation(downAnimation);
                break;
            case RELEASE_REFRESH://松开刷新
                head_txt_refresh.setText("松开刷新");
                list_head_image.startAnimation(upAnimation);
                break;
            case REFRESHING://刷新进行时
                list_head_image.clearAnimation();
                list_head_image.setVisibility(GONE);
                list_head_progressbar.setVisibility(VISIBLE);
                head_txt_refresh.setText("正在刷新中...");
                break;
        }
    }


    //刷新完毕，该隐藏头布局了
    public void hideHeaderView() {
        headerView.setPadding(0, -headerViewHeight, 0, 0);
        list_head_image.setVisibility(VISIBLE);
        list_head_progressbar.setVisibility(GONE);
        head_txt_refresh.setText("下拉刷新");
        head_txt_currentTime.setText("最后刷新时间：" + getLastUpdateTime());
        currentState = DOWN_PULL_REFRESH;
    }

    //获取当前系统最新时间
    public String getLastUpdateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(System.currentTimeMillis());
    }

    //初始化动画
    private void initAnimation() {
        upAnimation = new RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(true);

        downAnimation = new RotateAnimation(-180f, -360f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING) {
            if (isScorllToBottom && !isLoadingMore) {
                isLoadingMore = true;
                footerView.setPadding(0, 0, 0, 0);
                this.setSelection(this.getCount());

                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLodingMore();
                }
            }
        }
    }

    /**
     * @param view
     * @param firstVisibleItem //当前屏幕显示在顶部item的position
     * @param visibleItemCount //当前屏幕显示了多少条目的总数
     * @param totalItemCount   //listView的条目的总数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        firstVisibleItemPosition = firstVisibleItem;
        if (getLastVisiblePosition() == (totalItemCount - 1)) {
            isScorllToBottom = true;
        } else {
            isScorllToBottom = false;
        }
    }

    //隐藏脚布局
    public void hideFooterView() {
        footerView.setPadding(0, -footerViewHeight, 0, 0);
        isLoadingMore = false;
    }

    //用户监听设置
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    public interface OnRefreshListener {
        //下拉刷新
        void onDownPullRefresh();

        //上拉加载更多
        void onLodingMore();
    }
}
