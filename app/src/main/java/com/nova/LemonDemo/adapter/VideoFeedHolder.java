package com.nova.LemonDemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nova.LemonDemo.R;
import com.nova.LemonDemo.bean.LemonVideoBean;
import com.nova.LemonDemo.bean.TabFragMainBeanItemBean;

import java.util.List;

/**
 * Created by Paraselene on 2017/8/9. Email ：15616165649@163.com
 */

public class VideoFeedHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
    private View video_masked;
    private TextView item_tv, tv_video_time, tv_video_readCount;
    private List<LemonVideoBean.SublistBean> mlist;
    private ImageView img;
    private LinearLayout ll_not_wifi;
    private ImageView iv_video_feed_start;
    private TextView tv_video_shear, tv_video_more, tv_video_comment;
    private Context context;
    private FrameLayout ll_video;
    private LemonVideoBean.SublistBean itemBean;

    /**
     * 初始化view
     *
     * @param itemView
     * @param context
     */
    VideoFeedHolder(View itemView, Context context) {
        super(itemView);
        this.item_tv = (TextView) itemView.findViewById(R.id.item_tv);
        this.ll_video = (FrameLayout) itemView.findViewById(R.id.ll_video);
        this.img = (ImageView) itemView.findViewById(R.id.img);
        this.video_masked = itemView.findViewById(R.id.video_masked);
        this.tv_video_time = (TextView) itemView
                .findViewById(R.id.tv_video_time);
        this.tv_video_readCount = (TextView) itemView
                .findViewById(R.id.tv_video_readCount);
        this.iv_video_feed_start = (ImageView) itemView
                .findViewById(R.id.iv_video_feed_start);
        this.ll_not_wifi = (LinearLayout) itemView
                .findViewById(R.id.ll_not_wifi);
        this.tv_video_shear = (TextView) itemView
                .findViewById(R.id.tv_video_shear);
        this.tv_video_more = (TextView) itemView
                .findViewById(R.id.tv_video_more);
        this.tv_video_comment = (TextView) itemView
                .findViewById(R.id.tv_video_comment);
        this.context = context;
        bindListener();
    }

    /**
     * 绑定监听
     */
    private void bindListener() {
        tv_video_shear.setOnClickListener(this);
        tv_video_more.setOnClickListener(this);
        tv_video_comment.setOnClickListener(this);
        iv_video_feed_start.setOnClickListener(this);
    }

    /**
     * 初始化值
     *
     * @param position
     */
    public void update(int position, List<LemonVideoBean.SublistBean> mlist) {
        this.mlist = mlist;
        itemBean = mlist.get(position);
        item_tv.setText(itemBean.getV_title());
        // 设置缩略图
        String imaglinks = itemBean.getV_imagelinks();
        Glide.with(context).load(imaglinks).into(img);
    }

    /**
     * 判断是不是WiFi的情况
     */
    public void playerWifi() {
    }

    /**
     * 显示当前播放的 item 的蒙层和 图片
     */
    public void visMasked() {
        img.setVisibility(View.VISIBLE);
        video_masked.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏当前播放的 item 的蒙层和 图片
     */
    public void goneMasked() {
        img.setVisibility(View.GONE);
        video_masked.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.iv_video_feed_start:
            // Constants.VIDEO_FEED_WIFI = true;
            ll_not_wifi.setVisibility(View.GONE);
            iv_video_feed_start.setEnabled(false);
            if (listener != null) {
                listener.videoWifiStart();
            }
            break;
        case R.id.tv_video_shear:
            if (listener != null) {

            }
            break;
        case R.id.tv_video_comment:
            break;
        case R.id.tv_video_more:
            break;
        }
    }

    public interface OnHolderVideoFeedListener {
        void videoWifiStart();

        void videoShare();

        void nightMode(boolean isNight);
    }

    private VideoFeedHolder.OnHolderVideoFeedListener listener;

    /**
     * 注册监听
     *
     * @param listener
     */
    public void registerVideoPlayerListener(
            VideoFeedHolder.OnHolderVideoFeedListener listener) {
        this.listener = listener;
    }

    /**
     * 解除监听
     */
    public void unRegisterVideoPlayerListener() {
        if (listener != null) {
            listener = null;
        }
    }
}
