package com.tunnl.mobileadsdk;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by demiremrece on 30.11.2017.
 */

public interface TunnlHttpCallback {
    void onSuccess(JSONObject data);
    void onError(VolleyError error);
}
