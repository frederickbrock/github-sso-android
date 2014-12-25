package com.hwrdprkns.githubsso;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.hwrdprkns.githubsso.library.GithubLoginActivity;
import com.hwrdprkns.githubsso.sample.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    static final String GITHUB_TOKEN_PREF_KEY = "github_prefs";
    static final int GITHUB_TOKEN_REQUEST_KEY = 408;

    @InjectView(R.id.textView) TextView tokenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String githubToken = prefs.getString(GITHUB_TOKEN_PREF_KEY, null);
        if (githubToken == null) {
            startGithubLoginActivity();
        } else {
            tokenText.setText(githubToken);
        }
    }

    @OnClick(R.id.login_with_github)
    public void startGithubLoginActivity() {
        Intent intent = new Intent(this, GithubLoginActivity.class);
        startActivityForResult(intent, GITHUB_TOKEN_REQUEST_KEY);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GITHUB_TOKEN_REQUEST_KEY && resultCode == RESULT_OK && data != null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString(GITHUB_TOKEN_PREF_KEY,
                    data.getStringExtra(GithubLoginActivity.KEY_RESULT)).apply();
            tokenText.setText(data.getStringExtra(GithubLoginActivity.KEY_RESULT));
        }
    }
}
