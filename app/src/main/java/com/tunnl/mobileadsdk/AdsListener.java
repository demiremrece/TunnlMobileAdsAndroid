package com.tunnl.mobileadsdk;

/**
 * Created by Emre Demir on 20.09.2016.
 */

public abstract class AdsListener {

    public void onAdClosed() {
    }
    public void onAdStarted() {
    }
    public void onAdRecieved(AdEvent data) {
    }
    public void onAdFailed(String msg) {
    }
    public void onAPIReady(){
    }
    public void onAPINotReady(String error){
    }

}
