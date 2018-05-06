package com.tunnl.mobileadsdk;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Created by demiremrece on 30.11.2017.
 */

public class TunnlHttp  {

    private static TunnlHttp instance;
    private RequestQueue requestQueue;
    private Context context;

    private TunnlHttp(Context context){
        this.context = context;
        this.requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if(this.requestQueue == null){
            this.requestQueue = Volley.newRequestQueue(this.context);
        }
        return this.requestQueue;
    }

    public static synchronized  TunnlHttp getInstance (Context context){
        if(instance == null){
            instance = new TunnlHttp(context);
        }return instance;
    }

    public<T> void addToRequestQueue(Request<T> request) {
        requestQueue.add(request);
    }

}
