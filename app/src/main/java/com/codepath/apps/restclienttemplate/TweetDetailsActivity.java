package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailsBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;

import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity {
    public static final String TAG = "TweetDetailsActivity";
    private Tweet tweet;
    private ActivityTweetDetailsBinding binding;
    private TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTweetDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText(tweet.user.screenName);
        binding.tvBody.setText(tweet.body);
        binding.tvDate.setText(tweet.createdAt);

        Glide.with(this).load(tweet.tweet_URL).into(binding.ivTweet);
        Glide.with(this).load(tweet.user.publicImageUrl).into(binding.ivProfileImage);

        client=TwitterApp.getRestClient(this);

        if(tweet.liked)
            setLikeColor();
        else
            setNotLikeColor();

        if(tweet.retweeted)
            setRetweetColor();
        else
            setNotRetweetColor();

        binding.ibFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tweet.liked)
                {
                    client.favoriteTweet(String.valueOf(tweet.id) , new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Toast.makeText(TweetDetailsActivity.this,"Liked!",Toast.LENGTH_SHORT).show();
                            tweet.liked=true;
                            setLikeColor();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG,"Could not fetch tweet "+throwable.toString());
                            Toast.makeText(TweetDetailsActivity.this,"Could not like tweet",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else{
                    client.unFavoriteTweet(String.valueOf(tweet.id), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Toast.makeText(TweetDetailsActivity.this,"Liked!",Toast.LENGTH_SHORT).show();
                            tweet.liked = false;
                            setNotLikeColor();
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG,"Could not unlike tweet "+throwable.toString());
                            Toast.makeText(TweetDetailsActivity.this,"Could not unlike tweet",Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });

        binding.ibRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tweet.retweeted)
                {
                    client.reTweet(String.valueOf(tweet.id), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Toast.makeText(TweetDetailsActivity.this,"Retweeted!",Toast.LENGTH_SHORT).show();
                            tweet.retweeted=true;
                            setRetweetColor();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Toast.makeText(TweetDetailsActivity.this,"Could not be retweeted",Toast.LENGTH_SHORT).show();
                            Log.e(TAG,"Could not retweet "+throwable.toString());
                        }
                    });
                }
                else
                {
                    client.unreTweet(String.valueOf(tweet.id), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Toast.makeText(TweetDetailsActivity.this,"Unretweeted!",Toast.LENGTH_SHORT).show();
                            tweet.retweeted=false;
                            setNotRetweetColor();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Toast.makeText(TweetDetailsActivity.this,"Could not be unretweeted",Toast.LENGTH_SHORT).show();
                            Log.e(TAG,"Could not unretweet "+throwable.toString());
                        }
                    });
                }

            }
        });

    }
    public void setLikeColor()
    {
        binding.ibFavorite.setColorFilter(getResources().getColor(R.color.twitter_yellow));
    }
    public void setNotLikeColor()
    {
        binding.ibFavorite.setColorFilter(getResources().getColor(R.color.medium_gray));
    }

    public void setRetweetColor()
    {
        binding.ibRetweet.setColorFilter(getResources().getColor(R.color.inline_action_retweet));
    }

    public void setNotRetweetColor()
    {
        binding.ibRetweet.setColorFilter(getResources().getColor(R.color.medium_gray));
    }
}