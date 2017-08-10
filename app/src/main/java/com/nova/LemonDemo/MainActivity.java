package com.nova.LemonDemo;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.linksu.video_manager_library.listener.OnVideoPlayerListener;
import com.linksu.video_manager_library.ui.LVideoView;
import com.nova.LemonDemo.adapter.VideoAdapter;
import com.nova.LemonDemo.adapter.VideoFeedHolder;
import com.nova.LemonDemo.bean.Constant;
import com.nova.LemonDemo.bean.LemonVideoBean;
import com.nova.LemonDemo.bean.TabFragMainBeanItemBean;
import com.nova.LemonDemo.control.ScrollSpeedLinearLayoutManger;
import com.nova.LemonDemo.net.LemonVideoService;
import com.nova.LemonDemo.utils.StateBarUtils;
import com.nova.LemonDemo.utils.VisibilePercentsUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements
        VideoFeedHolder.OnHolderVideoFeedListener, OnVideoPlayerListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.rl_video)
    RecyclerView rlVideo;
    @BindView(R.id.iv_close_video_feed)
    ImageView ivCloseVideoFeed;
    @BindView(R.id.tv_video_carry)
    TextView tvVideoCarry;
    @BindView(R.id.rl_video_feed)
    RelativeLayout rlVideoFeed;
    @BindView(R.id.full_screen)
    FrameLayout fullScreen;
    @BindView(R.id.srl_lemon)
    SwipeRefreshLayout srlLemon;
    // 是否处于滚动状态
    private boolean mScrollState = false;
    // item 的位置
    private int lastItemPosition = 0;
    private int firstItemPosition = 0;
    private int visibleItemCount = 0;

    private int position = 0; // 最大显示百分比的屏幕内的子view的位置
    private int itemPosition = 0;// item 的位置
    private int playerPosition = 0;// 正在播放item 的位置
    private boolean isPause = false;// 是否暂停
    private boolean isThrumePause = false;// 是否手动暂停播放
    // 加载数据相关
    private List<TabFragMainBeanItemBean> itemBeens = new ArrayList<>();
    // 布局相关
    private ScrollSpeedLinearLayoutManger layoutManager;
    private VideoAdapter adapter;
    // 播放器
    private LVideoView lVideoView;

    private long currentPosition = 0;
    private long mDuration = 0;

    private boolean orientation = false;// 默认为竖屏的情况
    TabFragMainBeanItemBean itemBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 不锁屏
        StateBarUtils.setTranslucentColor(this);// 沉浸式状态栏
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initSlidingMenu();
        initArgs();
        initView();
        freshLayout();
        // adapter.setNewData(itemBeens);
    }

    /**
     * 加载布局之前的操作
     */
    public void initArgs() {
        adapter = new VideoAdapter(this);
        for (int i = 0; i < 10; i++) {
            itemBean = new TabFragMainBeanItemBean();
            itemBean.title = "看我的厉害:" + i;
            itemBean.video_url = "http://rmrbtest-image.peopleapp.com/upload/video/201707/1499914158feea8c512f348b4a.mp4";
            itemBean.id = "" + i;
            itemBeens.add(itemBean);
        }
    }

    /**
     * 初始化侧滑菜单
     */
    public void initSlidingMenu() {
        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.RIGHT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        // menu.setShadowDrawable(R.drawable.shadow);

        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        /**
         * SLIDING_WINDOW will include the Title/ActionBar in the content
         * section of the SlidingMenu, while SLIDING_CONTENT does not.
         */
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        // 为侧滑菜单设置布局
        menu.setMenu(R.layout.right_sliding_menu);
    }

    /**
     * 初始化view
     */
    public void initView() {
        lVideoView = new LVideoView(this);// 初始化播放器
        layoutManager = new ScrollSpeedLinearLayoutManger(this);
        rlVideo.setLayoutManager(layoutManager);
        adapter.setRecyclerView(rlVideo);
        rlVideo.setAdapter(adapter);
        getHttp();
        // adapter.setList(itemBeens);
        initListener();
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        ivCloseVideoFeed.setOnClickListener(this);
        tvVideoCarry.setOnClickListener(this);
        tvVideoCarry.setEnabled(false);
        lVideoView.setOnVideoPlayerListener(this);
        rlVideo.addOnScrollListener(new VideoFeedScrollListener());
        srlLemon.setOnRefreshListener(this);
    }

    /**
     * 使用(SwipeRefreshLayout + RecyclerView)方式实现简单的下拉刷新
     */
    private void freshLayout() {
        // 使用(SwipeRefreshLayout + RecyclerView)方式实现简单的下拉刷新
        srlLemon.setColorSchemeColors(Color.RED, Color.BLUE, Color.GREEN,
                Color.YELLOW);
        srlLemon.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        srlLemon.setRefreshing(false);// 取消进度框
                        Toast.makeText(getApplication(), "刷新成功",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRefresh() {
        /*
         * Observable .timer(2, TimeUnit.SECONDS,
         * AndroidSchedulers.mainThread()) .map(new Func1<Long, Object>() {
         * 
         * @Override public Object call(Long aLong) { fetchingNewData();
         * swipeRefreshLayout.setRefreshing(false);
         * adapter.notifyDataSetChanged(); Toast.makeText(MainActivity.this,
         * "Refresh Finished!", Toast.LENGTH_SHORT).show(); return null; }
         * }).subscribe();
         */
        Observable.timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .map(new Function<Long, Object>() {
                    @Override
                    public Object apply(@NonNull Long aLong) throws Exception {
                        fetchingNewData();
                        srlLemon.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Refresh Finished!",
                                Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }).subscribe();
    }

    // 下拉刷新加载数据
    private void fetchingNewData() {
    }

    /**
     * 列表的滚动监听
     */
    private class VideoFeedScrollListener
            extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView,
                int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:// 停止滑动
                mScrollState = false;
                // 滑动停止和松开手指时，调用此方法 进行播放
                autoPlayVideo(recyclerView);
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:// 用户用手指滚动
                mScrollState = true;
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:// 自动滚动
                mScrollState = true;
                break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager layoutManager = recyclerView
                    .getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                // 获取最后一个可见view的位置
                lastItemPosition = linearManager.findLastVisibleItemPosition();
                // 获取第一个可见view的位置
                firstItemPosition = linearManager
                        .findFirstVisibleItemPosition();
                // 获取可见view的总数
                visibleItemCount = linearManager.getChildCount();
                if (mScrollState) { // 滚动
                    srcollVisible(recyclerView, firstItemPosition,
                            lastItemPosition, visibleItemCount);
                } else { // 停止 第一次进入时调用此方法，进行播放第一个
                    ((LinearLayoutManager) rlVideo.getLayoutManager())
                            .scrollToPositionWithOffset(itemPosition, 20);
                    autoPlayVideo(rlVideo);
                }
            }
        }
    }

    /**
     * 滚动时 判断哪个view 显示百分比最大，哪个最大 视图亮起
     *
     * @param recyclerView
     * @param firstItemPosition
     * @param lastItemPosition
     * @param visibleItemCount
     *            屏幕显示的item数量
     */
    private void srcollVisible(RecyclerView recyclerView, int firstItemPosition,
            int lastItemPosition, int visibleItemCount) {
        for (int i = 0; i < visibleItemCount; i++) {
            if (recyclerView != null) {
                View childAt = recyclerView.getChildAt(i)
                        .findViewById(R.id.visiabile);
                recyclerView.getChildAt(i).findViewById(R.id.video_masked)
                        .setVisibility(View.VISIBLE);
                int visibilityPercents = VisibilePercentsUtils.getInstance()
                        .getVisibilityPercents(childAt);
                if (visibilityPercents == 100) {
                    position = i;
                }
            }
        }
        itemPosition = (firstItemPosition + position);
        recyclerView.getChildAt(position).findViewById(R.id.video_masked)
                .setVisibility(View.GONE);
        if (playerPosition == itemPosition) {// 说明还是之前的 item 并没有滑动到下一个
            // noting to do
        } else { // 说明亮起的已经不是当前的item了，是下一个或者之前的那个，我们停止变暗的item的播放
            missVideoTips();
            stopPlayer(playerPosition);
            playerPosition = itemPosition;
        }
    }

    /**
     * 停止滚动手指抬起时 动态添加播放器，开始播放视频，并获取之前的播放进度
     *
     * @param recyclerView
     */
    private void autoPlayVideo(final RecyclerView recyclerView) {
        if (!lVideoView.isPlayer()) {
            VideoFeedHolder childViewHolder = (VideoFeedHolder) recyclerView
                    .findViewHolderForAdapterPosition(itemPosition);
            if (childViewHolder != null) {
                // 注册监听以及隐藏蒙层
                childViewHolder.registerVideoPlayerListener(this);
                childViewHolder.goneMasked();
                childViewHolder.playerWifi();
                // int netType = NetChangeManager.getInstance().getNetType();
                // if (netType == 1 || Constants.VIDEO_FEED_WIFI) { //
                // WiFi的情况下，或者允许不是WiFi情况下继续播放
                // 动态添加播放器
                View itemView = childViewHolder.itemView;
                FrameLayout frameLayout = (FrameLayout) itemView
                        .findViewById(R.id.ll_video);
                frameLayout.removeAllViews();
                ViewGroup last = (ViewGroup) lVideoView.getParent();// 找到videoitemview的父类，然后remove
                if (last != null && last.getChildCount() > 0) {
                    last.removeAllViews();
                }
                frameLayout.addView(lVideoView);
                // 获取播放进度
                TabFragMainBeanItemBean itemBean = itemBeens.get(itemPosition);
                long videoProgress = itemBean.videoProgress;
                long duration = itemBean.mDuration;
                if (videoProgress != 0 && videoProgress != duration) { // 跳转到之前的进度，继续播放
                    lVideoView.startLive(itemBean.video_url);
                    lVideoView.setSeekTo(videoProgress);
                } else {// 从头播放
                    lVideoView.startLive(itemBean.video_url);
                }
                // }
            }
        }
    }

    /**
     * 滑动停止播放视频
     *
     * @param position
     */
    private void stopPlayer(int position) {
        VideoFeedHolder childViewHolder = (VideoFeedHolder) rlVideo
                .findViewHolderForAdapterPosition(position);
        if (childViewHolder != null) {
            if (lVideoView.isPlayer()) { // 如果正在播放，则停止并记录播放进度，否则不调用这个方法
                lVideoView.stopVideoPlay();
                TabFragMainBeanItemBean itemBean = itemBeens.get(position);
                itemBean.videoProgress = currentPosition;
                itemBean.mDuration = mDuration;
                itemBeens.set(position, itemBean);
            }
            childViewHolder.visMasked();// 显示蒙层
            View itemView = childViewHolder.itemView;
            FrameLayout frameLayout = (FrameLayout) itemView
                    .findViewById(R.id.ll_video);
            frameLayout.removeAllViews();
            childViewHolder.unRegisterVideoPlayerListener();// 注意我们需要解除上一个item的监听，不然会注册多个监听
        }
    }

    /**
     * 添加播放器
     *
     * @param position
     */
    private void addPlayer(int position) {
        VideoFeedHolder childViewHolder = (VideoFeedHolder) rlVideo
                .findViewHolderForAdapterPosition(position);
        if (childViewHolder != null) {
            View itemView = childViewHolder.itemView;
            FrameLayout frameLayout = (FrameLayout) itemView
                    .findViewById(R.id.ll_video);
            frameLayout.removeAllViews();
            ViewGroup last = (ViewGroup) lVideoView.getParent();// 找到videoitemview的父类，然后remove
            if (last != null && last.getChildCount() > 0) {
                last.removeAllViews();
            }
            frameLayout.addView(lVideoView);
        }
    }

    /**
     * 横竖屏切换
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        lVideoView.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            orientation = false;
            fullScreen.setVisibility(View.GONE);
            fullScreen.removeAllViews();
            ivCloseVideoFeed.setVisibility(View.VISIBLE);
            addPlayer(itemPosition);
            int mShowFlags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            fullScreen.setSystemUiVisibility(mShowFlags);
        } else { // 横屏
            orientation = true;
            ivCloseVideoFeed.setVisibility(View.GONE);
            ViewGroup viewGroup = (ViewGroup) lVideoView.getParent();
            if (viewGroup == null)
                return;
            viewGroup.removeAllViews();
            fullScreen.addView(lVideoView);
            fullScreen.setVisibility(View.VISIBLE);
            int mHideFlags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            fullScreen.setSystemUiVisibility(mHideFlags);
        }
    }

    @Override
    public void onVideoPrepared() {
    }

    @Override
    public void onVideoCompletion() {
        if (itemPosition != lastItemPosition) { // 若播放的不是最后一个，播放完成自动播放下一个
            VideoFeedHolder childViewHolder = (VideoFeedHolder) rlVideo
                    .findViewHolderForAdapterPosition(itemPosition);
            if (childViewHolder != null) {
                // 播放完成将之前的播放进度清空
                TabFragMainBeanItemBean itemBean = itemBeens.get(itemPosition);
                itemBean.videoProgress = 0;
                itemBean.mDuration = 0;
                itemBeens.set(itemPosition, itemBean);
                // 移除播放器
                childViewHolder.visMasked();
                View itemView = childViewHolder.itemView;
                FrameLayout frameLayout = (FrameLayout) itemView
                        .findViewById(R.id.ll_video);
                frameLayout.removeAllViews();
                childViewHolder.unRegisterVideoPlayerListener();// 注意我们需要解除上一个item的监听，不然会注册多个监听
            }
            itemPosition = itemPosition + 1;
            playerPosition = itemPosition;
            rlVideo.smoothScrollToPosition(itemPosition);
            //
            // ((LinearLayoutManager) rlVideo.getLayoutManager())
            // .scrollToPositionWithOffset(playerPosition, 20);
            // autoPlayVideo(rlVideo);
        }
    }

    @Override
    public void onVideoError(int i, String error) {
    }

    @Override
    public void onBufferingUpdate() {

    }

    @Override
    public void onVideoStopped() { // 停止视频播放时，记录视频的播放位置

    }

    @Override
    public void onVideoPause() { // 暂停视频播放

    }

    @Override
    public void onVideoThumbPause() { // 手动暂停视频播放
        isThrumePause = true;
    }

    @Override
    public void onVideoThumbStart() { // 手动开始视频播放
        isThrumePause = false;
    }

    @Override
    public void onVideoPlayingPro(long currentPosition, long mDuration,
            int mPlayStatus) {// 播放进度
        this.currentPosition = currentPosition;
        this.mDuration = mDuration;
        if (itemPosition != lastItemPosition) { // 若播放的不是最后一个，弹出播放下一个的提示
            float percent = (float) ((double) currentPosition
                    / (double) mDuration);
            DecimalFormat fnum = new DecimalFormat("##0.0");
            float c_percent = 0;
            c_percent = Float.parseFloat(fnum.format(percent));
            if (0.8 <= c_percent) {
                videoTips();
            } else {
                missVideoTips();
            }
        }
    }

    /***
     * 显示提示
     */
    public void videoTips() {
        if (itemPosition != lastItemPosition) {// 若播放的不是最后一个
            tvVideoCarry.setEnabled(true);
            tvVideoCarry.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏提示
     */
    public void missVideoTips() {
        tvVideoCarry.setEnabled(false);
        tvVideoCarry.setVisibility(View.GONE);
    }

    @Override
    public void videoWifiStart() {
        // 无WiFi的情况下继续播放
        autoPlayVideo(rlVideo);
    }

    @Override
    public void videoShare() {

    }

    @Override
    public void nightMode(boolean isNight) {
    }

    @Override
    public void onClick(View v) {// 点击事件
        switch (v.getId()) {
        case R.id.tv_video_carry:
            // itemPosition = itemPosition + 1;
            // rl_video.smoothScrollToPosition(itemPosition);
            // rl_video.smoothScrollBy(0, rl_video.getHeight());
            // 手动点击下一个,暂停之前的
            stopPlayer(playerPosition);
            // 开始播放下一个
            itemPosition = itemPosition + 1;
            playerPosition = itemPosition;
            rlVideo.smoothScrollToPosition(itemPosition);
            // ((LinearLayoutManager) rlVideo.getLayoutManager())
            // .scrollToPositionWithOffset(itemPosition, 20);
            // autoPlayVideo(rlVideo);
            break;
        case R.id.iv_close_video_feed:
            finish();
            break;
        }
    }

    /**
     * Activity 不在前台时 暂停播放
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (!isThrumePause) {// 若不是手动暂停，Activity进入后台自动暂停
            lVideoView.onPause();
        }
    }

    /**
     * Activity 重新进入前台 播放逻辑
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("linksu", "onResume(VideoFeedDetailAct.java:558) isThrumePause"
                + isThrumePause);
        if (!isThrumePause) { // 不是手动暂停且从后台进入前台
            lVideoView.currentPlayer();
        } else { // 进入后台之前是暂停的状态,再次进入还是暂停的状态
            lVideoView.startThumb();
        }
    }

    /**
     * Activity 退出时 停止播放
     */
    @Override
    public void finish() {
        super.finish();
        lVideoView.stopVideoPlay();
    }

    /**
     * Activity 销毁时 销毁播放器
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        lVideoView.removeAllViews();
    }

    // 进行网络请求
    private void getHttp() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.LEMON_VIDEO)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        LemonVideoService myService = retrofit.create(LemonVideoService.class);

        myService.getLemonVideo(1).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LemonVideoBean>() {

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull LemonVideoBean lemonVideoBean) {
                        adapter.setNewData(lemonVideoBean.getSublist());
                        srlLemon.setRefreshing(false); // 让SwipeRefreshLayout关闭刷新

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }
}
