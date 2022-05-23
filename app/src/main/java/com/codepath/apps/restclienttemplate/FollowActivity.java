package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ActivityFollowBinding;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.ScrollableLayout;

public class FollowActivity extends AppCompatActivity {
    public  static final String TAG = "FollowActivity";
    User user;
    ActivityFollowBinding binding;
    ScrollableLayout scrollableLayout;
    TwitterClient client;
    List<User> userList;
    FollowAdapter adapter;
    RecyclerView rvFollow;
    MenuItem progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFollowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        rvFollow = binding.rvFollow;
        scrollableLayout = findViewById(R.id.scrollable_layout);
        client = TwitterApp.getRestClient(this);
        userList = new ArrayList<>();
        adapter = new FollowAdapter(this, userList);

        rvFollow.setAdapter(adapter);
        rvFollow.setLayoutManager(new LinearLayoutManager(this));

        Glide.with(this).load(user.publicImageUrl).into(binding.ivPF);
        getSupportActionBar().setTitle(user.name);

        binding.Switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userList.clear();
                if (binding.Switch.getText().toString() == "Currently Following")
                {
                    binding.Switch.setText("Current Follower(s)");
                    loadFollower();
                }
                else
                {
                    binding.Switch.setText("Current Following(s)");
                    loadFollowing();
                }
            }
        });

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                // Obtain a View that is a scroll container (RecyclerView, ListView, ScrollView, WebView, etc)
                // and call its `canScrollVertically(int) method.
                // Please note, that if `ViewPager is used, currently displayed View must be obtained
                // because `ViewPager` doesn't delegate `canScrollVertically` method calls to it's children
                final View view = rvFollow;
                return view.canScrollVertically(direction);
            }
        });
        binding.Switch.setText("Currently Following");
        loadFollowing();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.compose).setVisible(false);
        progressBar = menu.findItem(R.id.miActionProgress);
        return super.onPrepareOptionsMenu(menu);
    }

    public void loadFollowing()
    {
        if (progressBar != null)
            progressBar.setVisible(true);
        client.getFollowing(String.valueOf(user.id), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // get user data and figure out how to parse it
                    JSONArray jsonArray = jsonObject.getJSONArray("users");
                    for (int i = 0; i<jsonArray.length(); i++) {
                        JSONObject user = jsonArray.getJSONObject(i);
                        userList.add(User.fromJson(user));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (progressBar!=null)
                    progressBar.setVisible(false);
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
            }
        });
    }

    public void loadFollower()
    {
        if (progressBar!=null)
            progressBar.setVisible(true);
        client.getFollowers(String.valueOf(user.id), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // get user data and figure out how to parse it
                    JSONArray jsonArray = jsonObject.getJSONArray("users");
                    for (int i = 0; i<jsonArray.length(); i++) {
                        JSONObject user = jsonArray.getJSONObject(i);
                        userList.add(User.fromJson(user));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (progressBar!=null)
                    progressBar.setVisible(false);
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
            }
        });
    }

}