package com.tunnl.mobileadsdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class AdStream {

    private AdsListener devListener;
    private PublisherInterstitialAd mInterstitialAd;
    private Activity mContext;
    private String bundleId;
    private PublisherAdView publisherAdView;
    private FrameLayout rootview;
    private RelativeLayout relativeLayoutContainer;
    private ArrayList<TunnlData> tunnlDatas;
    private int currentRequestInd = -1;
    private RequestAdHandler requestAdHandler;

    void init(Activity mContext, String bundleId) {
        this.bundleId = bundleId;
        setmContext(mContext);
        setRootview((FrameLayout) mContext.findViewById(android.R.id.content));
        initBannerObject();

        if (devListener != null) devListener.onAPIReady();

    }

    void setDevListener(AdsListener adsListener){
        this.devListener = adsListener;
    }

    private void initBannerObject() {
        /* Ad request object for banners*/
        RelativeLayout rl = new RelativeLayout(this.mContext);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ));

        setRelativeLayoutContainer(rl);

        publisherAdView = new PublisherAdView(this.mContext);
        publisherAdView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ));

        setPublisherAdView(publisherAdView);

        // add adcontainer into rootview
        this.relativeLayoutContainer.addView(publisherAdView);

        this.rootview.addView(relativeLayoutContainer);
    }

    private void requestBanner(String size, String alignment, String position, Bundle customParams, String unitId) {

        if (this.relativeLayoutContainer != null) {

            if (publisherAdView != null) {

                this.relativeLayoutContainer.removeView(publisherAdView);
                publisherAdView = new PublisherAdView(this.mContext);
                publisherAdView.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                ));
                setPublisherAdView(publisherAdView);
                // add adcontainer into rootview
                this.relativeLayoutContainer.addView(publisherAdView);
            }

            PublisherAdRequest adRequest;
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            builder.addNetworkExtrasBundle(AdMobAdapter.class, customParams);

            adRequest = builder.build();
            handleBannerParams(size, alignment, position);

            publisherAdView.setAdUnitId(unitId);

            publisherAdView.loadAd(adRequest);
            publisherAdView.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    if (devListener != null)
                        devListener.onAdClosed();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                    String error = "";
                    switch (errorCode) {
                        case 0:
                            error += "Internal Error.\nSomething happened internally; for instance, an invalid response was received from the ad server.\nConstant Value: " + errorCode;
                            break;
                        case 1:
                            error += "Invalid request.\nThe ad request was invalid; for instance, the ad unit ID was incorrect.\nConstant Value: " + errorCode;
                            break;
                        case 2:
                            error += "Network error.\nThe ad request was unsuccessful due to network connectivity.\nConstant Value: " + errorCode;
                            break;
                        case 3:
                            error += "No fill.\nThe ad request was successful, but no ad was returned due to lack of ad inventory.\nConstant Value: " + errorCode;
                            break;
                    }

                    if (devListener != null) {
                        devListener.onAdFailed(error);
                    } else if (requestAdHandler != null)
                        requestAdHandler.Error(error);

                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    if (devListener != null)
                        devListener.onAdStarted();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();

                    AdEvent adEvent = new AdEvent();
                    adEvent.isInterstitial = false;
                    adEvent.dimensions = publisherAdView.getAdSize();
                    if (devListener != null)
                        devListener.onAdRecieved(adEvent);

                    requestAdHandler.Succes();
                }
            });
        }
    }

    private void handleBannerParams(String size, String alignment, String position) {

        switch (size) {
            case "320x50":
                publisherAdView.setAdSizes(AdSize.BANNER);

                break;
            case "320x100":
                publisherAdView.setAdSizes(AdSize.LARGE_BANNER);

                break;
            case "300x250":
                publisherAdView.setAdSizes(AdSize.MEDIUM_RECTANGLE);

                break;
            case "468x60":
                publisherAdView.setAdSizes(AdSize.FULL_BANNER);

                break;
            case "728x90":
                publisherAdView.setAdSizes(AdSize.LEADERBOARD);

                break;
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.relativeLayoutContainer.getLayoutParams();

        switch (position) {
            case "top":
                params.gravity = Gravity.TOP;

                break;
            case "middle":
                params.gravity = Gravity.CENTER_VERTICAL;

                break;
            case "bottom":
                params.gravity = Gravity.BOTTOM;

                break;
        }

        switch (alignment) {
            case "center":
                params.gravity = params.gravity | Gravity.CENTER;

                break;
            case "left":
                params.gravity = params.gravity | Gravity.START;

                break;
            case "right":
                params.gravity = params.gravity | Gravity.END;

                break;
        }
        this.relativeLayoutContainer.setLayoutParams(params);
    }

    private void requestInterstitial(Bundle customParams, String unitId) {
        PublisherAdRequest adRequest;
        mInterstitialAd = new PublisherInterstitialAd(mContext);

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addNetworkExtrasBundle(AdMobAdapter.class, customParams);
        adRequest = builder.build();

        mInterstitialAd.setAdUnitId(unitId);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();

                if (devListener != null) devListener.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                String error = "";
                switch (errorCode) {
                    case 0:
                        error += "Internal Error.\nSomething happened internally; for instance, an invalid response was received from the ad server.\nConstant Value: " + errorCode;
                        break;
                    case 1:
                        error += "Invalid request.\nThe ad request was invalid; for instance, the ad unit ID was incorrect.\nConstant Value: " + errorCode;
                        break;
                    case 2:
                        error += "Network error.\nThe ad request was unsuccessful due to network connectivity.\nConstant Value: " + errorCode;
                        break;
                    case 3:
                        error += "No fill.\nThe ad request was successful, but no ad was returned due to lack of ad inventory.\nConstant Value: " + errorCode;
                        break;
                }

                if (devListener != null) devListener.onAdFailed(error);
                else if (requestAdHandler != null) requestAdHandler.Error(error);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (devListener != null) devListener.onAdStarted();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                showInterstitialAd();

                AdEvent adEvent = new AdEvent();
                adEvent.isInterstitial = true;
                adEvent.dimensions = "Interstitial";

                if (devListener != null)
                    devListener.onAdRecieved(adEvent);

                requestAdHandler.Succes();
            }
        });
        mInterstitialAd.loadAd(adRequest);
    }

    private void showInterstitialAd() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    void showAd(final boolean isInterstitial, final String adSize, final String alignment, final String position) {

        currentRequestInd = -1;
        String msize = "interstitial";
        if (!isInterstitial) msize = adSize;
        String url = "http://pub.tunnl.com/oppm?bundleid=and." + bundleId + "&msize=" + msize;

        // getting unit id from tunnl for ad request
        TunnlHttpRequest.sendHttpRequest(this.mContext, url, Request.Method.GET, null, new TunnlHttpCallback() {
            @Override
            public void onSuccess(JSONObject data) {
                try {
                    JSONArray items = data.getJSONArray("Items");
                    tunnlDatas = new ArrayList<>();
                    TunnlData tunnlData;

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject object = (JSONObject) items.get(i);
                        tunnlData = new TunnlData();
                        tunnlData.setAdu(object.getString("Adu"));
                        tunnlData.setErr(object.getString("Err"));
                        tunnlData.setImp(object.getString("Imp"));

                        JSONArray custom_params = object.getJSONArray("CustomParams");
                        Bundle customParams = new Bundle();
                        for (int j = 0; j < custom_params.length(); j++) {
                            JSONObject obj = (JSONObject) custom_params.get(j);
                            customParams.putString(obj.getString("Key"), obj.getString("Value"));
                        }
                        tunnlData.setCustomParams(customParams);
                        tunnlDatas.add(tunnlData);
                    }

                    if (items.length() > 0)
                        requestHandler(isInterstitial, adSize, alignment, position);
                    else {
                        if (devListener != null)
                            devListener.onAdFailed("Something went wrong fetching advertisement settings. Please contact with support team.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    TunnlUtils.log("Something went wrong parsing json tunnl data.\nData:\n" + data.toString());
                }
            }

            @Override
            public void onError(VolleyError error) {
                TunnlUtils.log("Something went wrong fetching unit id from tunnl.");
            }
        });

    }

    private void requestHandler(final boolean isInterstitial, final String adSize, final String alignment, final String position) {

        currentRequestInd++;
        final TunnlData data = tunnlDatas.get(currentRequestInd);
        if (isInterstitial) {
            requestInterstitial(data.getCustomParams(), data.getAdu());
        } else {
            requestBanner(adSize, alignment, position, data.getCustomParams(), data.getAdu());
        }

        requestAdHandler = new RequestAdHandler() {
            @Override
            public void Succes() {
                TunnlHttpRequest.sendStringRequest(mContext, tunnlDatas.get(currentRequestInd).getImp().replace("https", "http"), Request.Method.GET, null, new TunnlHttpCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                        TunnlUtils.log("Imp");
                    }

                    @Override
                    public void onError(VolleyError error) {
                        TunnlUtils.log("Imp error");
                    }
                });
            }

            @Override
            public void Error(String err) {

                String url = tunnlDatas.get(currentRequestInd).getErr().replace("https", "http");
                TunnlHttpRequest.sendStringRequest(mContext, url, Request.Method.GET, null, new TunnlHttpCallback() {
                    @Override
                    public void onSuccess(JSONObject data) {
                    }

                    @Override
                    public void onError(VolleyError error) {
                    }
                });

                currentRequestInd++;
                if (tunnlDatas != null && tunnlDatas.size() > 0 && currentRequestInd < tunnlDatas.size()) {
                    TunnlData data = tunnlDatas.get(currentRequestInd);

                    if (isInterstitial) {
                        requestInterstitial(data.getCustomParams(), data.getAdu());
                    } else {
                        requestBanner(adSize, alignment, position, data.getCustomParams(), data.getAdu());
                    }
                } else {
                    currentRequestInd = -1;
                    tunnlDatas = null;

                    if (devListener != null)
                        devListener.onAdFailed(err);

                }

            }
        };
    }


    /**
     * SETTERs
     **/
    private void setRelativeLayoutContainer(RelativeLayout relativeLayoutContainer) {
        this.relativeLayoutContainer = relativeLayoutContainer;
    }

    private void setPublisherAdView(PublisherAdView publisherAdView) {
        this.publisherAdView = publisherAdView;
    }

    private void setmContext(Activity mContext) {
        this.mContext = mContext;
    }

    private void setRootview(FrameLayout rootview) {
        this.rootview = rootview;
    }
}
