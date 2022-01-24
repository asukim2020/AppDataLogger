package kr.co.greentech.dataloggerapp.rxjava;

import android.util.Log;

import io.realm.internal.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpHelper {
    private static OkHttpClient client = new OkHttpClient();

    public static String get(String url) throws java.io.IOException {
        Request request = new Request.Builder()
        .url(url)
        .build();

        try {
            Response res = client.newCall(request).execute();
            return res.body().string();
        } catch (java.io.IOException e) {
            System.out.println(e.getMessage());
            throw  e;
        }
    }
}
