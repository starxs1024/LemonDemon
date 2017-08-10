package com.nova.LemonDemo.net;


import com.nova.LemonDemo.bean.LemonVideoBean;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Paraselene on 2017/7/28. Email ：15616165649@163.com
 */

public interface LemonVideoService {
    /**
     * @param page
     *            查询的页数
     * @param pagesize
     *            一页数据显示的条数
     * @return 查询结束返回的被观察者
     */
    // http://47.91.104.211:8080/Lemon/LemonVideo?pageNo=2
    @GET("LemonVideo?")
    Observable<LemonVideoBean> getLemonVideo(@Query("pageNo") int page);

}
