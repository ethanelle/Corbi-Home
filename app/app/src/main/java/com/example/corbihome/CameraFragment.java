package com.example.corbihome;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;

public class CameraFragment extends Fragment {
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, viewGroup, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updatePhotos();
    }

    /**
     * Photos comes in JSON format:
     * {"photos":["url", "url", "url",...,"url"]}
     */
    public void updatePhotos() {
        final TextView photosMessage = getView().findViewById(R.id.camera_message);
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        final String url = "http://ec2-18-206-127-80.compute-1.amazonaws.com:1997/photos/";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // parse photos URLs to render
                try {
                    JSONObject response_json = new JSONObject(response);
                    JSONArray image_urls = response_json.getJSONArray("photos");

                    int len = 12;
                    if (image_urls.length() < 12)
                        len = image_urls.length();

                    for (int i = 0; i < len; i++) {
                        String image_url = (String) image_urls.get(i);
                        String image_id = "image" + (i + 1);

                        Log.i("Value: ", image_id);

                        new CustomImageView((ImageView) getView().findViewById(getResId(image_id, R.id.class))).execute(image_url);
                    }

                } catch (JSONException e) {
                    Log.e("CameraFragment:updatePhotos", "Could not parse the incoming JSON string");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CameraFragment", error.toString());
                if (photosMessage != null) {
                    photosMessage.setText(getResources().getString(R.string.camera_message_HTTP_error));
                    if (photosMessage.getVisibility() == View.INVISIBLE) {
                        photosMessage.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        queue.add(stringRequest);

    }

    public static Drawable LoadImageFromWeb(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, "Image");
        } catch (Exception e) {
            Log.e("CustomImageView:LoadImageFromWeb", e.toString());
            return null;
        }
    }

    public int getResId(String name, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(name);
            return idField.getInt(idField);
        } catch (Exception e) {
            Log.e("CameraFragment:getResId", e.toString());
            return -1;
        }
    }
}
