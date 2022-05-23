package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final int MAX_TWEET_LENGTH=280;
    public static final int MIN_TWEET_LENGTH=0;
    public static final String TAG="COMPOSEACTIVITY";
    Button btnTweet;
    EditText etmlTweet;
    TextView tvCharacterCount;
    TwitterClient client;
    ActivityComposeBinding binding;
    int color;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityComposeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnTweet=binding.btnTweet;
        etmlTweet=binding.etmlCompose;
        tvCharacterCount=binding.tvCharacterCounter;
        client=TwitterApp.getRestClient(this);
        color=btnTweet.getCurrentTextColor();
        tvCharacterCount.setText(String.valueOf(MAX_TWEET_LENGTH));
        if(getIntent().hasExtra("tweet_username"))
        {
            etmlTweet.setText("@"+getIntent().getStringExtra("tweet_username")+" ");
            tvCharacterCount.setText(String.valueOf(MAX_TWEET_LENGTH-getIntent().getStringExtra("tweet_username").length()));
        }



        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=etmlTweet.getText().toString();
                if(message.length()>MAX_TWEET_LENGTH)
                {
                    Toast.makeText(ComposeActivity.this,"Tweet Too Long",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(message.length()<=MIN_TWEET_LENGTH)
                {
                    Toast.makeText(ComposeActivity.this,"Tweet is Empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                client.publishTweet(message, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Tweet tweet= null;
                        try {
                            tweet = Tweet.fromJson(json.jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.i(TAG,"Published Tweet says" + tweet);
                        Intent intent=new Intent();
                        intent.putExtra("tweet", Parcels.wrap(tweet));
                        setResult(RESULT_OK,intent);
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                    }
                });
            }
        });

        etmlTweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharacterCount.setText(String.valueOf(MAX_TWEET_LENGTH-s.length()));
                if (s.length()>MAX_TWEET_LENGTH)
                    btnTweet.setTextColor(Color.parseColor("#FF0000"));
                else
                    btnTweet.setTextColor(color);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}