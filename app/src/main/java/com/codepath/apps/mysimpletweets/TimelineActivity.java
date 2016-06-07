package com.codepath.apps.mysimpletweets;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter aTweets;
    private ListView lvTweets;
    ComposeDialog composeDialog = ComposeDialog.newInstance("Compose Tweet");

    private static int pageScollCount = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        lvTweets = (ListView) findViewById(R.id.lvTweets);
        tweets = new ArrayList<>();
        aTweets = new TweetsArrayAdapter(this, tweets);
        client = TwitterApplication.getTwitterClient();

        lvTweets.setAdapter(aTweets);

        // Attach the listener to the AdapterView onCreate
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(View view, int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                if(page > pageScollCount) {

                    //get another 10 pages.
                    pageScollCount = pageScollCount+10;
                    populateTimeline(pageScollCount);

                }
                // or customLoadMoreDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }

            @Override
            public int getFooterViewType() {
                return 0;
            }
        });

        populateTimeline(10);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
//        Log.d("DEBUG",menu.toString());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle when settings is selected

        switch (item.getItemId()){
            case R.id.miCompose:
                showComposeDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        composeDialog.show(fm, "compose");

    }


    private void tweetNow() {

    }

    private void populateTimeline(int page){
        if(client == null)
            client = TwitterApplication.getTwitterClient();

        client.getHomeTimeline(page, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG:",response.toString());

                ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);
                aTweets.clear();
                aTweets.addAll(tweets);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

        });
    }

    public void onTweetNow(View view) {
        EditText etTweet = composeDialog.getEtTweet();

        String tweetText = etTweet.getText().toString();
        client.postTweet(tweetText, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("DEBUG:",response.toString());

                ArrayList<Tweet> tweets = Tweet.fromJSONArray(response);
                aTweets.clear();
                aTweets.addAll(tweets);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

        });
        composeDialog.dismiss();
        populateTimeline(1);
    }

    public void onCancel(View view) {
        // dismiss the dialog.
        composeDialog.dismiss();
    }
}