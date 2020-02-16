package com.example.corbihome;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class MotionFragment extends Fragment {
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_motion, viewGroup, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateMotionList();
    }

    public void updateMotionList() {
        final TextView motion_view = getView().findViewById(R.id.motion_list);
        motion_view.setMovementMethod(new ScrollingMovementMethod());
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        String url = "http://ec2-18-206-127-80.compute-1.amazonaws.com:1997/motion_list";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(motion_view != null)
                    motion_view.setText(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MotionFragment", error.toString());
                if(motion_view != null)
                    motion_view.setText(getResources().getString(R.string.motion_list_HTTP_error));
            }
        });
        queue.add(stringRequest);
    }
}
