package com.ezlol.ezchat;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

class Requests {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static Response get(String url, Map<String, String> headers) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS).build();
            Headers headersBuild = Headers.of(headers);
            Request request = new Request.Builder().url(url).headers(headersBuild).build();
            okhttp3.Response response = client.newCall(request).execute();

            ResponseBody responseBody = response.body();
            if(responseBody != null)
                return new Response(responseBody.string(), response.code(), response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response post(String url, String json, Map<String, String> headers){
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS).build();

            RequestBody body = RequestBody.create(json, JSON);
            Headers headersBuild = Headers.of(headers);
            Request request = new Request.Builder().url(url).headers(headersBuild).post(body).build();
            okhttp3.Response response = client.newCall(request).execute();

            ResponseBody responseBody = response.body();
            if(responseBody != null)
                return new Response(responseBody.string(), response.code(), response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response put(String url, String json, Map<String, String> headers) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS).build();

            RequestBody body = RequestBody.create(json, JSON);
            Headers headersBuild = Headers.of(headers);
            Request request = new Request.Builder().url(url).headers(headersBuild).put(body).build();
            okhttp3.Response response = client.newCall(request).execute();

            ResponseBody responseBody = response.body();
            if(responseBody != null)
                return new Response(responseBody.string(), response.code(), response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response post(String url, String json){
        return post(url, json, new HashMap<>());
    }

    public static Response put(String url, String json){
        return put(url, json, new HashMap<>());
    }

    public static Response get(String url){
        return get(url, new HashMap<>());
    }
}

class Response {
    private final String string;
    private final int statusCode;
    private final okhttp3.Response responseBody;

    public Response(String string, int statusCode, okhttp3.Response responseBody) {
        this.string = string;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public okhttp3.Response getResponseBody() {
        return responseBody;
    }

    public JSONObject json() throws JSONException {
        return new JSONObject(string);
    }

    public int getStatusCode() {
        return statusCode;
    }

    @NonNull
    @Override
    public String toString() {
        return string;
    }
}