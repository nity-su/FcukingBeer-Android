package xyz.pongsakorn.fcukingbeer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Pongsakorn on 11/25/2015.
 */
public class Server {

    AsyncHttpClient client;

    public Server() {
        client = new AsyncHttpClient();
        client.setTimeout(60000);
    }

    public void sendPhoto(File photo, final Listener listener) {
        listener.onStart();
        try {
            RequestParams params = new RequestParams();
            params.put("image", photo);

            client.post("http://pongsakorn.xyz:12345", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    try {
                        Gson gson = new Gson();

                        Type listType = new TypeToken<List<RectModel>>(){}.getType();
                        List<RectModel> rects = gson.fromJson(response.getJSONArray("data").toString(), listType);
                        listener.onSuccess(rects);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFail(0, e.toString());
                    };
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    if (errorResponse==null)
                        listener.onFail(statusCode, "");
                    else
                        listener.onFail(statusCode, errorResponse.toString());
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public interface Listener {
        public void onStart();
        public void onSuccess(List<RectModel> res);
        public void onFail(int status, String err);
    }

}
