package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeFragment extends DialogFragment implements TextView.OnEditorActionListener {

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
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    public interface TweetListener{
        void onTweetButton(Tweet tweet);
    }

    public ComposeFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ComposeFragment newInstance(String username)
    {
        ComposeFragment frag = new ComposeFragment();
        Bundle args = new Bundle();
        args.putString("username",username);
        frag.setArguments(args);
        return frag;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding=ActivityComposeBinding.inflate(getLayoutInflater());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnTweet=binding.btnTweet;
        etmlTweet=binding.etmlCompose;
        tvCharacterCount=binding.tvCharacterCounter;
        client=TwitterApp.getRestClient(getContext());
        color=btnTweet.getCurrentTextColor();
        tvCharacterCount.setText(String.valueOf(MAX_TWEET_LENGTH));
        etmlTweet.setText(getArguments().getString("username"));

        tvCharacterCount.setText(String.valueOf(MAX_TWEET_LENGTH-etmlTweet.getText().toString().length()));
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=etmlTweet.getText().toString();
                if(message.length()>MAX_TWEET_LENGTH)
                {
                    Toast.makeText(getActivity(),"Tweet Too Long",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(message.length()<=MIN_TWEET_LENGTH)
                {
                    Toast.makeText(getActivity(),"Tweet is Empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                client.publishTweet(message, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Tweet tweet= null;
                        try {
                            tweet = Tweet.fromJson(json.jsonObject);
                            TweetListener listener = (TweetListener) getActivity();
                            listener.onTweetButton(tweet);
                            dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                //Make it red if it's beyound the max length
                if (s.length()>MAX_TWEET_LENGTH)
                    btnTweet.setTextColor(Color.parseColor("#FF0000"));
                    //set it back to the original color if it is below the max.
                else
                    btnTweet.setTextColor(color);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etmlTweet.requestFocus();
    }

    @Override
    public void onStart() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        getDialog().getWindow().setLayout((6 * width)/7, (3 * height)/5);
        super.onStart();

    }
}
