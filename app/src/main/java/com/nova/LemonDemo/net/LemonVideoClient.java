package com.nova.LemonDemo.net;

import java.util.concurrent.TimeUnit;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Paraselene on 2017/7/28.
 * Email ：15616165649@163.com
 */

public class LemonVideoClient {
    private Retrofit retrofit;

    private static final int DEFAULT_TIMEOUT = 5;


    // 私有构造方法
    private LemonVideoClient(){

    }


    /**
     * 创建相应的服务接口
     */
    public <T> T create(Class<T> service, String baseUrl) {
        // 手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(service);
    }

    //在访问RetrofitClient时创建单例
    private static class SingletonHolder{
        private static final LemonVideoClient INSTANCE = new LemonVideoClient();
    }
    //获取单例
    public static LemonVideoClient getInstance(){
        return SingletonHolder.INSTANCE;
    }

}
