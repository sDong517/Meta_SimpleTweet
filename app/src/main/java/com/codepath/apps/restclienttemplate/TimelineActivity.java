package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.ScrollableLayout;

public class TimelineActivity extends AppCompatActivity implements ComposeFragment.TweetListener {
    public static final String TAG="TIMELINEACTIVITY";
    public static final int REQUEST_CODE_COMPOSE=33;
    TwitterClient client;
    TweetDao tweetDao;
    ActivityTimelineBinding binding;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    MenuItem progressBar;
    LinearLayoutManager layoutManager;

    ComposeFragment composeDialogFragment;
    SwipeRefreshLayout swipeRefreshLayout;
    EndlessRecyclerViewScrollListener scrollListener;
    Button btnLogOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        client=TwitterApp.getRestClient(this);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();

        rvTweets = binding.rvTweets;
        btnLogOut = binding.btnLogOut;
        swipeRefreshLayout = binding.swipeContainer;

        tweets = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG,"Getting more data");
                loadMoreData();
            }
        };
        TweetsAdapter.clickReply cR=new TweetsAdapter.clickReply() {
            @Override
            public void onClickReplyReaction(Tweet tweet) {
                showComposeDialog("@"+tweet.user.screenName);
            }
        };

        adapter=new TweetsAdapter(this,tweets,cR);

        rvTweets.setAdapter(adapter);
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        //Scroll listener for infinite pagination
        rvTweets.addOnScrollListener(scrollListener);


        //Get saved tweets in case we are offline
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"Showing data from database");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });

        populateHomeTimeline();

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.clearAccessToken();
                finish();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Log.i(TAG,"Fetching new data");
                populateHomeTimeline();

            }
        });

    }

    private void loadMoreData() {
        if (progressBar!=null)
            progressBar.setVisible(true);
        client.getNextPageOfTweets(tweets.get(tweets.size() - 1).id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    adapter.addAll(Tweet.fromJsonArray(json.jsonArray));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (progressBar!=null)
                    progressBar.setVisible(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                if (progressBar!=null)
                    progressBar.setVisible(false);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Override this method we use own menu and not the default
        getMenuInflater().inflate(R.menu.menu_main, menu);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_twitter_icon);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        progressBar=menu.findItem(R.id.miActionProgress);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Also override this method and choose to launch the composeActivity when the right menu item is pressed
        if(item.getItemId()==R.id.compose)
        {
            Toast.makeText(this,"Composer",Toast.LENGTH_LONG).show();
            showComposeDialog("");
            return true;
        }

        if (item.getItemId() == R.id.LogOut){
            client.clearAccessToken();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void showComposeDialog(String userName) {
        FragmentManager fm = getSupportFragmentManager();
        composeDialogFragment = ComposeFragment.newInstance(userName);
        composeDialogFragment.show(fm, "activity_compose");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_COMPOSE & resultCode==RESULT_OK)
        {
            Tweet tweet= Parcels.unwrap(data.getParcelableExtra("tweet")) ;
            tweets.add(0,tweet);
            adapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
        }
    }

    private void populateHomeTimeline() {
        if (progressBar!=null)
        {
            progressBar.setVisible(true);
        }

        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"Tweets retrieved as json");
                try {
                    List<Tweet> tweetsFromNetwork=Tweet.fromJsonArray(json.jsonArray);
                    adapter.clear();
                    adapter.addAll(tweetsFromNetwork);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG,"Saving data into database");
                            //Insert users first otherwise the Tweet's foreign keys will have no idea where to go
                            List<User> usersFromNetwork= User.fromJsonTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));
                            //Then insert tweets
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (progressBar!=null)
                {
                    progressBar.setVisible(false);
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG,"Could not retrieve tweets"+response);
            }
        });
    }

    @Override
    public void onTweetButton(Tweet tweet) {
        tweets.add(0,tweet);
        adapter.notifyItemInserted(0);
        rvTweets.scrollToPosition(0);
    }
}