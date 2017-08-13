package com.nova.LemonDemo.dao;

import com.nova.LemonDemo.adapter.VideoAdapter;
import com.nova.LemonDemo.bean.Constant;
import com.nova.LemonDemo.bean.LemonVideoBean;
import com.nova.LemonDemo.net.LemonVideoClient;
import com.nova.LemonDemo.net.LemonVideoService;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Paraselene on 2017/8/13. Email ：15616165649@163.com
 */

public class LemonNetData {
    public void getNetData(int mTotalCounter, final VideoAdapter adapter) {

        LemonVideoClient.getInstance()
                .create(LemonVideoService.class, Constant.LEMON_VIDEO)
                .getLemonVideo(mTotalCounter).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<LemonVideoBean>() {
                    @Override
                    public void accept(@NonNull LemonVideoBean lemonVideoBean)
                            throws Exception {
                        List<LemonVideoBean.SublistBean> data = lemonVideoBean
                                .getSublist();
                        adapter.addData(data);
                        // mTotalCounter +=1;
                        // mCurrentCounter = mTotalCounter;
                        // videoRecyclerAdapter
                        // .loadMoreComplete();
                    }
                });
    }
}
