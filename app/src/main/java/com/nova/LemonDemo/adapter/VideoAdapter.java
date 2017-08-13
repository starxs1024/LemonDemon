package com.nova.LemonDemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nova.LemonDemo.R;
import com.nova.LemonDemo.bean.LemonVideoBean;
import com.nova.LemonDemo.listener.ItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paraselene on 2017/8/9. Email ：15616165649@163.com
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoFeedHolder> {
    private List mlist;
    private Context context;
    private RecyclerView recyclerView;
    private ItemClickListener itemClickListener;

    public VideoAdapter(Context context) {
        this.context = context;
    }
    public VideoAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public VideoFeedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(context).inflate(R.layout.item,
                parent, false);
        return new VideoFeedHolder(inflater, context);
    }

    public void setNewData(List<LemonVideoBean.SublistBean> data) {
        mlist = data == null ? new ArrayList() : data;
        this.mNotify();
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void mNotify() {
        if (recyclerView != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(VideoFeedHolder holder, final int position) {
        holder.update(position, mlist);
        View itemView = holder.itemView;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mlist == null ? 0 : mlist.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * additional data;
     *
     * @param newData
     */
    public void addData(List newData) {
        mlist.addAll(newData);
//        notifyItemRangeInserted(mlist.size() - newData.size() + getHeaderLayoutCount(), newData.size());
        this.mNotify();
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * compatible getLoadMoreViewCount and getEmptyViewCount may change
     *
     * @param size Need compatible data size
     */
    private void compatibilityDataSizeChanged(int size) {
        final int dataSize = mlist == null ? 0 : mlist.size();
        if (dataSize == size) {
            notifyDataSetChanged();
        }
    }

}
