package com.hwrdprkns.githubsso.library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GithubLoginActivity extends Activity {
    static final String TAG = GithubLoginActivity.class.getSimpleName();

    static String CALLBACK_URL = "http://localhost";
    static final String OAUTH_URL = "https://github.com/login/oauth/authorize";
    static final String OAUTH_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";

    public static final String KEY_RESULT = TAG + ":Result";

    static OkHttpClient client = new OkHttpClient();

    WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String accessCodeFragment = "code=";

                if (url.contains(CALLBACK_URL) && url.contains(accessCodeFragment)) {
                    // the GET request contains an authorization code
                    String code = url.substring(
                            (url.indexOf(accessCodeFragment) + accessCodeFragment.length()));
                    retrieveAccessToken(code);
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        webView.loadUrl(OAUTH_URL + "?client_id=" + getTokens().first);
        setContentView(webView);
    }

    void retrieveAccessToken(String accessCode) {
        String query = "client_id=" + getTokens().first + "&client_secret=" +
                getTokens().second + "&code=" + accessCode;
        String url = OAUTH_ACCESS_TOKEN_URL + "?" + query;
        Call call = client.newCall(
                new Request.Builder()
                        .url(url)
                        .post(null)
                        .addHeader("Accept", "application/json")
                        .build());
        call.enqueue(new Callback() {
            @Override public void onFailure(Request request, IOException e) {
            }

            @Override public void onResponse(Response response) throws IOException {
                JSONObject object = new JSONObject();
                try {
                    object = new JSONObject(response.body().string());
                } catch (JSONException ignored) {
                }
                response.body().close();
                onAuthCompleted(object.optString("access_token", ""));
            }
        });
    }

    Pair<String, String> getTokens() {
        ActivityInfo info;
        try {
            info = getPackageManager().getActivityInfo(this.getComponentName(),
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not find requested meta data!");
            throw new RuntimeException("Provide client_id and/or " +
                    "client_secret as activity metadata!");
        }
        final String clientId =
                info.metaData.getString("com.github.sso.library.client_id");
        final String clientSecret =
                info.metaData.getString("com.github.sso.library.client_secret");

        return new Pair<>(clientId, clientSecret);
    }

    void onAuthCompleted(String code) {
        int resultCode = TextUtils.isEmpty(code) ? RESULT_CANCELED : RESULT_OK;

        Bundle bundle = new Bundle();
        bundle.putString(KEY_RESULT, code);

        Intent resultIntent = new Intent();
        resultIntent.putExtras(bundle);
        setResult(resultCode, resultIntent);
        finish();
    }
}
