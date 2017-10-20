/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package th.co.krungthaiaxa.api.elife.line.v2.client;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import th.co.krungthaiaxa.api.elife.line.v2.client.model.LineMessage;
import th.co.krungthaiaxa.api.elife.line.v2.client.model.LineMultiCastMessage;
import th.co.krungthaiaxa.api.elife.line.v2.model.LineAccessToken;

import java.util.Map;

/**
 * <p>LINE v2 API interface</p>
 */
public interface LineAPI {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("v2/oauth/accessToken")
    Call<LineAccessToken> accessToken(
            @Field("grant_type") String grant_type,
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret);

    @Headers("Content-Type: application/json")
    @POST("v2/bot/message/push")
    Call<Map> pushMessage(
            @Header("Authorization") String accessToken,
            @Body LineMessage lineMessage);

    @Headers("Content-Type: application/json")
    @POST("v2/bot/message/multicast")
    Call<Map> pushMulticastMessage(
            @Header("Authorization") String accessToken,
            @Body LineMultiCastMessage multicastMessage);

    @Headers("Content-Type: text/plain")
    @GET("v2/bot/dedisco/migration/userId")
    Call<ResponseBody> getLineUserIdFromMid(
            @Header("Authorization") String accessToken,
            @Query("mid") String mid);

}

