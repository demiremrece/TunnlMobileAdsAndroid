package com.tunnl.mobileadsdk;

import android.app.Activity;

public class TunnlAds {

    private static AdStream adStream;
    private static AdsListener adListener;

    public static void Init(Activity mContext, String bundleId, AdsListener adsListener){
        adListener = adsListener;
        adStream = new AdStream();
        adStream.setDevListener(adsListener);
        adStream.init(mContext, bundleId);
    }

    public static void RequestInterstitialAd(){
        if (adStream != null){
            adStream.showAd(true,null,null,null);
        }
        else if( adListener != null) {
            adListener.onAPINotReady("API is not initialized.");
        }
    }

    public static void RequestBannerAd(String adSize, String adAlignment, String adPosition){
        if (adStream != null){
            adStream.showAd(false, adSize, adAlignment, adPosition);
        }
        else if(adListener != null) {
            adListener.onAPINotReady("API is not initialized.");
        }
    }
}
