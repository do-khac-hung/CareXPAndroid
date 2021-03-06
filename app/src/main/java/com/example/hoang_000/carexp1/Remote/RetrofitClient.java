package com.example.hoang_000.carexp1.Remote;

/**
 * Created by hoang_000 on 24/03/2018.
 */

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Phụ trách lấy dữ liệu các đối tượng như xưởng, trạm xăng.. từ Google
 */
public class RetrofitClient {

    private static Retrofit retrofit=null;

    /**
     * lấy dữ liệu các đối tượng như xưởng, trạm xăng.. từ Google
     * @param baseUrl   URL chính  https://goo.gl/maps/
     * @return     Cấu trúc chứa thông tin ve 1 xưởng, trạm xăng...
     */
    public static  Retrofit getClient(String baseUrl){
        if (retrofit==null)
        {
            retrofit=new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}