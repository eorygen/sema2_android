package com.orygenapps.sema.activity;

import com.orygenapps.sema.data.AnswerSetProxy;
import com.orygenapps.sema.data.ProgramVersionProxy;
import com.orygenapps.sema.data.SyncDataProxy;

import java.util.ArrayList;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by starehe on 22/03/15.
 */

public interface SEMAService {

    @FormUrlEncoded
    @POST("/api/1/token/")
    Response login(@Field("username") String username, @Field("password") String password);

    @POST("/api/1/sync/")
    Response sync(@Body SyncDataProxy syncDataProxy); // 304 not modified if no change

    @POST("/api/1/answers/")
    Response postAnswerSet(@Body AnswerSetProxy answerSetProxy);

    @FormUrlEncoded
    @POST("/api/1/participants/reset_password/")
    Response resetPassword(@Field("email") String email);

}