package com.nova.LemonDemo;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.nova.LemonDemo.bean.LemonVideoBean;
import com.nova.LemonDemo.control.NetChangeManager;
import com.nova.LemonDemo.control.ScrollSpeedLinearLayoutManger;
import com.nova.LemonDemo.dao.LemonNetData;
import com.nova.LemonDemo.listener.ItemClickListener;
import com.nova.LemonDemo.net.LemonVideoService;
import com.nova.LemonDemo.receiver.NetworkConnectChangedReceiver;
import com.nova.LemonDemo.utils.StateBarUtils;
import com.nova.LemonDemo.utils.VisibilePercentsUtils;
import com.nova.LemonDemo.utils.WindowsUtils;

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
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        NetworkConnectChangedReceiver.ConnectChangedListener {

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
    private List<LemonVideoBean> itemBeens = new ArrayList<>();
    private List<LemonVideoBean.SublistBean> lemonBeens;
    LemonNetData lemonNetData;
    // 相关工具
    WindowsUtils windowsUtils;
    // 布局相关
    private ScrollSpeedLinearLayoutManger layoutManager;
    private VideoAdapter adapter;
    // 播放器
    private LVideoView lVideoView;

    private long currentPosition = 0;
    private long mDuration = 0;

    private boolean orientation = false;// 默认为竖屏的情况
    private boolean isPrepared;

    LemonVideoBean itemBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 不锁屏
        StateBarUtils.setTranslucentColor(this);// 沉浸式状态栏
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initSlidingMenu();
        getHttp();
        initArgs();
        initView();
        freshLayout();

        // adapter.setNewData(itemBeens);
    }

    /**
     * 加载布局之前的操作
     */
    public void initArgs() {
        adapter = new VideoAdapter(this, new ItemClickListener() {
            @Override
            public void onItemClick(View v, Object object) {
                // 手动点击下一个,暂停之前的 并显示蒙层
                stopPlayer(playerPosition);
                missVideoTips();
                // 缓慢平滑的滚动到下一个
                itemPosition = (int) object;
                playerPosition = itemPosition;
                rlVideo.smoothScrollToPosition(itemPosition);
            }
        });
        windowsUtils = new WindowsUtils();
        for (int i = 0; i < 10; i++) {
            itemBean = new LemonVideoBean();
            // itemBean.title = "看我的厉害:" + i;
            // itemBean.video_url =
            // "http://rmrbtest-image.peopleapp.com/upload/video/201707/1499914158feea8c512f348b4a.mp4";
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
        windowsUtils.setTransparentToStateBar(this);
        layoutManager = new ScrollSpeedLinearLayoutManger(this);
        rlVideo.setLayoutManager(layoutManager);
        adapter.setRecyclerView(rlVideo);
        rlVideo.setAdapter(adapter);
        // adapter.setList(itemBeens);
        lemonNetData = new LemonNetData();
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
        // Observable.timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
        // .map(new Function<Long, Object>() {
        // @Override
        // public Object apply(@NonNull Long aLong) throws Exception {
        // fetchingNewData();
        // srlLemon.setRefreshing(false);
        // adapter.notifyDataSetChanged();
        // Toast.makeText(MainActivity.this, "Refresh Finished!",
        // Toast.LENGTH_SHORT).show();
        // return null;
        // }
        // }).subscribe();
    }

    /**
     * 下拉刷新加载数据
     */
    private void fetchingNewData() {
        Log.d("Da", "+++++++++++++++++++++++=");
    }

    @Override
    public void wifiNetwork(boolean flag) {
        autoPlayVideo(rlVideo);

    }

    @Override
    public void dataNetwork(boolean flag) {
        Log.e("linksu", "onReceive(dataNetwork:273) 切换到数据流量");
        if (!Constant.VIDEO_FEED_WIFI) {
            dataNetwork(itemPosition);
            autoPlayVideo(rlVideo);
        }
    }

    @Override
    public void notNetWork() {

    }

    /**
     * 数据流量时显示的逻辑
     *
     * @param position
     */
    private void dataNetwork(int position) {
        VideoFeedHolder childViewHolder = (VideoFeedHolder) rlVideo
                .findViewHolderForAdapterPosition(position);
        if (childViewHolder != null) {
            View itemView = childViewHolder.itemView;
            FrameLayout frameLayout = (FrameLayout) itemView
                    .findViewById(R.id.ll_video);
            frameLayout.removeAllViews();
            lVideoView.stopVideoPlay();
            LemonVideoBean itemBean = itemBeens.get(position);
            itemBean.videoProgress = currentPosition;
            itemBean.mDuration = mDuration;
            itemBeens.set(position, itemBean);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * 列表的滚动监听
     */

    private class VideoFeedScrollListener
            extends RecyclerView.OnScrollListener {
        LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView,
                int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:// 停止滑动
                mScrollState = false;
                // 滑动停止和松开手指时，调用此方法 进行播放
                autoPlayVideo(recyclerView);

//                // 滑动状态停止并且剩余少于两个item时，自动加载下一页
//                if (newState == RecyclerView.SCROLL_STATE_IDLE
//                        && lastItemPosition + 2 >= linearManager
//                                .getItemCount()) {
//                    // new
//                    // GetData().execute("http://gank.io/api/data/福利/10/"+(++page));
//                    lemonNetData.getNetData(2,adapter);
//
//                }
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
                if (!NetChangeManager.getInstance().hasNet()) { // 无网络的情况
                    Toast.makeText(this, "无法连接到网络,请稍后再试", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    int netType = NetChangeManager.getInstance().getNetType();
                    if (netType == 1 || Constant.VIDEO_FEED_WIFI) { // WiFi的情况下，或者允许不是WiFi情况下继续播放
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
                        LemonVideoBean itemBean = itemBeens.get(itemPosition);
                        long videoProgress = itemBean.videoProgress;
                        long duration = itemBean.mDuration;
                        if (videoProgress != 0 && videoProgress != duration) { // 跳转到之前的进度，继续播放
                            lVideoView.startLive(lemonBeens.get(itemPosition)
                                    .getV_videolink());
                            lVideoView.setSeekTo(videoProgress);
                        } else {// 从头播放
                            lVideoView.startLive(lemonBeens.get(itemPosition)
                                    .getV_videolink());
                        }
                    }
                }
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
                LemonVideoBean itemBean = itemBeens.get(position);
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
        isPrepared = true;
    }

    @Override
    public void onVideoCompletion() {
        if (itemPosition != lastItemPosition) { // 若播放的不是最后一个，播放完成自动播放下一个
            isPrepared = false;
            missVideoTips();
            VideoFeedHolder childViewHolder = (VideoFeedHolder) rlVideo
                    .findViewHolderForAdapterPosition(itemPosition);
            if (childViewHolder != null) {
                // 播放完成将之前的播放进度清空
                LemonVideoBean itemBean = itemBeens.get(itemPosition);
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
            int currentSeconds = (int) (currentPosition / 1000);
            int totalSeconds = (int) (mDuration / 1000);
            if (isPrepared) {
                if ((totalSeconds - currentSeconds) <= 5) { // 播放时间小于等于5s 时提示
                    videoTips();
                } else {
                    missVideoTips();
                }
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
            missVideoTips();
            // 手动点击下一个,暂停之前的
            stopPlayer(playerPosition);
            // 开始播放下一个
            itemPosition = itemPosition + 1;
            playerPosition = itemPosition;
            rlVideo.smoothScrollToPosition(itemPosition);
            break;
        case R.id.iv_close_video_feed:
            finish();
            break;
        }
    }

    /**
     * 返回键的处理
     *
     * @param keyCode
     * @param event
     * @return
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 横屏播放的情况
            if (getResources()
                    .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                return true;
            }
        }
        return false;
    }

    private NetworkConnectChangedReceiver connectChangedReceiver;

    /**
     * Activity 不在前台时 暂停播放
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectChangedReceiver);
        lVideoView.onThumePause();
    }

    /**
     * Activity 重新进入前台 播放逻辑
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (connectChangedReceiver == null) {
            connectChangedReceiver = new NetworkConnectChangedReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectChangedReceiver, filter);
        connectChangedReceiver.setConnectChangeListener(this);
        lVideoView.startThumb();
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
                        lemonBeens = lemonVideoBean.getSublist();
                        adapter.setNewData(lemonBeens);
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

    // /**
    // * 项目启动时 LemonThread 获取数据线程
    // */
    // class LemonThread implements Runnable {
    //
    // @Override
    // public void run() {
    // getHttp();
    // }
    // }
}
