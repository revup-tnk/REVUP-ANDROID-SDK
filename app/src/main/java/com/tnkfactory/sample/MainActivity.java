package com.tnkfactory.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tnkfactory.revup.RevupError;
import com.tnkfactory.revup.RevupSdk;
import com.tnkfactory.revup.interstitial.InterstitialAd;
import com.tnkfactory.revup.interstitial.InterstitialAdListener;
import com.tnkfactory.revup.listener.RevupInitializeListener;
import com.tnkfactory.revup.reward.RewardItem;
import com.tnkfactory.revup.reward.RewardedVideoAd;
import com.tnkfactory.revup.reward.RewardedVideoAdListener;
import com.tnkfactory.revup.rewardedinterstitial.RewardedInterstitialAd;
import com.tnkfactory.revup.rewardedinterstitial.RewardedInterstitialAdShowListener;

/**
 * readme
 *   - admob rewarded video network
 *     - need to add meta-data (com.google.android.gms.ads.APPLICATION_ID) to AndroidManifest.xml
 *
 */
public class MainActivity extends Activity implements RewardedVideoAdListener, InterstitialAdListener, RewardedInterstitialAdShowListener {

    private static final String TAG = MainActivity.class.getName();

    private String SAMPLE_MEDIA_ID = "";
    private String SAMPLE_MEDIA_SECRET = "";
    private String SAMPLE_REWARDED_VIDEO_UNIT = "";
    private String SAMPLE_INTERSTITIAL_UNIT = "";
    private String[] SAMPLE_REWARDED_INTERSTITIAL_UNIT = {};

    private static RewardedVideoAd mRewardedVideoAd;
    private static InterstitialAd mInterstitialAd;
    private static RewardedInterstitialAd mRewardedInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize revup sdk
        RevupSdk.initialize(this, SAMPLE_MEDIA_ID, SAMPLE_MEDIA_SECRET, new RevupInitializeListener() {
            @Override
            public void onInitialized(boolean isSuccess) {
                if (isSuccess) {
                    // get rewardVideo singleton instance
                    mRewardedVideoAd = RevupSdk.getRewardedVideoAdInstance(MainActivity.this);

                    // get interstitial singleton instance
                    mInterstitialAd = RevupSdk.getInterstitialAdInstance(MainActivity.this);

                    mRewardedInterstitialAd = RevupSdk.getRewardedInterstitialAdInstance(MainActivity.this);

                    // set listener
                    mRewardedVideoAd.setRewardedVideoAdListener(MainActivity.this);
                    mInterstitialAd.setInterstitialAdListener(MainActivity.this);
                    mRewardedInterstitialAd.setRewardedInterstitialAdListener(MainActivity.this);
                } else {
                    // Init 실패 에 대한 처리 Code
                }
            }
        });

        // set userId (user unique id)
        RevupSdk.setUserId("testUserId");

        ((Button) findViewById(R.id.btnLoadVideo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // load rewarded video
                mRewardedVideoAd.load(SAMPLE_REWARDED_VIDEO_UNIT);
            }
        });

        ((Button) findViewById(R.id.btnLoadInterstitial)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // load interstitial
                mInterstitialAd.load(SAMPLE_INTERSTITIAL_UNIT);
            }
        });

        ((Button) findViewById(R.id.btnPreloadRewardedInterstitial)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // load rewarded interstitial
                mRewardedInterstitialAd.preloadUnit(SAMPLE_REWARDED_INTERSTITIAL_UNIT);
            }
        });

        ((Button) findViewById(R.id.btnPreloadAllRewardedInterstitial)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // load rewarded interstitial
                mRewardedInterstitialAd.preloadAll();
            }
        });

    }

    // region implementation RewardedVideoAdListener
    @Override
    public void onRewardedVideoAdLoaded(String unitId) {
        Log.d(TAG, "onRewardedVideoAdLoaded");
        mRewardedVideoAd.show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(String unitId, RevupError revupError) {
        Log.e(TAG, "onRewardedVideoAdFailedToLoad : " + revupError);
    }

    @Override
    public void onRewardedVideoAdOpened(String unitId) {
        Log.d(TAG, "onRewardedVideoAdOpened");
    }

    @Override
    public void onRewardedVideoAdClosed(String unitId) {
        Log.d(TAG, "onRewardedVideoAdClosed");
        mRewardedVideoAd.load(SAMPLE_REWARDED_VIDEO_UNIT);
    }

    @Override
    public void onRewarded(String unitId, RewardItem rewardItem) {
        Log.d(TAG, "onRewarded");
    }

    @Override
    public void onRewardedVideoAdFailedToShow(String unitId, RevupError revupError) {
        Log.e(TAG, "onRewardedVideoAdFailedToLoad : " + revupError);
    }
    // endregion

    // region implementation InterstitialAdListener
    @Override
    public void onInterstitialAdLoaded() {
        Log.d(TAG, "onInterstitialAdLoaded");
        mInterstitialAd.show();
    }

    @Override
    public void onInterstitialAdFailedToLoad(RevupError revupError) {
        Log.e(TAG, "onInterstitialAdFailedToLoad : " + revupError);
    }

    @Override
    public void onInterstitialAdOpened(String unitId) {
        Log.d(TAG, "onInterstitialAdOpened");
    }

    @Override
    public void onInterstitialAdClosed(String unitId) {
        Log.d(TAG, "onInterstitialAdClosed");
        mInterstitialAd.load(SAMPLE_INTERSTITIAL_UNIT);
    }

    @Override
    public void onInterstitialAdFailedToShow(String unitId, RevupError revupError) {
        Log.e(TAG, "onInterstitialAdFailedToShow : " + revupError);
    }
    // endregion

    // region implementation RewardedInterstitialAdListener
    @Override
    public void onRewardedInterstitialAdLoaded(String unitId) {
        Log.d(TAG, "onRewardedInterstitialAdLoaded");
    }

    @Override
    public void onRewardedInterstitialAdFailedToLoad(String unitId, RevupError revupError) {
        Log.e(TAG, "onRewardedInterstitialAdFailedToLoad : " + revupError);
    }

    @Override
    public void onRewardedInterstitialAdSkipped(String unitId) {
        Log.d(TAG, "onRewardedInterstitialAdSkipped");
    }

    @Override
    public void onRewardedInterstitialAdOpened(String unitId) {
        Log.d(TAG, "onRewardedInterstitialAdOpened");
    }

    @Override
    public void onRewardedInterstitialAdClosed(String unitId) {
        Log.d(TAG, "onRewardedInterstitialAdClosed");
    }

    @Override
    public void onRewardedInterstitialAdRewarded(String unitId, RewardItem rewardItem) {
        Log.d(TAG, "onRewardedInterstitialAdRewarded");
    }

    @Override
    public void onRewardedInterstitialAdFailedToShow(String unitId, RevupError revupError) {
        Log.d(TAG, "onRewardedInterstitialAdFailedToShow : " + revupError);
    }
    // endregion
}
