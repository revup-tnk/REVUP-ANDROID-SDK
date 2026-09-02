# Revup-Android-Sample
[![GitHub package.json version](https://img.shields.io/badge/Android-1.0.5-blue)](../../releases)
[![GitHub package.json version](https://img.shields.io/badge/iOS-1.1.5-blue)](https://github.com/revup-tnk/REVUP-iOS-SDK)

## Requirements
- minSdkVersion 23
- compileSdkVersion 36
<details>
<summary>Network Adapter Requirements</summary>
<div markdown="1">  

| Adapter    | minSdk | bidding | in-house |
|------------|--------|---------|----------|
| admob      | 23     | -       | -        |
| chartboost | 21     | -       | -        |
| max        | 21     | O       | -        |
| pangle     | 23     | -       | -        |
| vungle     | 21     | -       | -        |
| tnkpub     | 23     | -       | O        |

#### Network Version
| Ad Network          | Android Version | MAX bidder |
|---------------------|-----------------|------------|
| AdMob               | 25.2.0          | O          |
| AppLovin            | 13.6.2          | O          |
| BidMachine          | 3.6.1           | O          |
| Bigo                | 5.8.2           | O          |
| Chartboost          | 9.11.0          | O          |
| DT Exchange         | 8.4.4           | O          |
| InMobi              | 11.2.0          | O          |
| Ironsource          | 9.4.0           | O          |
| Liftoff(Vungle)     | 7.7.3           | O          |
| Line                | 3000.0.1        | O          |
| Meta(Fan)           | 6.21.0          | O          |
| Mintegral(Mobvista) | 17.1.51         | O          |
| Moloco              | 4.8.0           | O          |
| Ogury               | 6.2.2           | O          |
| Pangle              | 7.9.1.3         | O          |
| Pubmatic            | 4.11.0          | O          |
| TNKPub              | 7.25.11         | -          |
| Unity Ads           | 4.16.6          | O          |
| Verve               | 3.8.1           | O          |

> ⚠️ 애드몹 SDK(`com.google.android.gms:play-services-ads`)를 프로젝트에 이미 포함하고 있는 경우 버전 호환에 유의   
> 기존에 gms SDK 사용중인 퍼블리셔는 admob, max 어댑터 사용 시 25버전으로 마이그레이션 필요 [(관련 문서)](https://developers.google.com/admob/android/migration?hl=en)
> - gms 25 버전: 레브업 `1.0.2` 이상 (현재 배포되는 모든 버전)

</div>
</details>


## Contents
* [**Integration Guide**](#integration-guide) 
  * [1. Import Revup Sdk](#1-import-revup-sdk)
  * [2. Initialize](#2-initialize-revup-sdk)
  * [3. Set User Info](#3-set-user-info)
* [**Ad Formats**](#ad-formats)
  * [Rewarded Ads](#rewarded-ads)
  * [Interstitial Ads](#interstitial-ads)
  * [Rewarded Interstitial Ads](#rewarded-interstitial-ads)
* [API Documentation](./docs/api_documentation.md)
* [Error Information](./docs/error_info.md)
* [Reward Callback](./docs/reward_callback_info.md)
* [Third Party Event](./docs/event_guide.md)
* [Adiscope Migration](./docs/adiscope_migration.md)
* [Release Note](https://github.com/tnkfactory/REVUP-ANDROID-SDK/wiki/release_note)

<br/>

## Integration Guide
### 1. Import Revup Sdk
#### 1-1. Add Revup module

운영에 필요한 각각의 네트워크 어댑터 의존성을 추가

**build.gradle(root)**
```groovy
repositories {
    google()
    mavenCentral()
  
    // [required] revup library
    maven {
        url "https://repository.tnkad.net:8443/repository/android/"
    }

    // [optional] revup network library
    // pangle 혹은 max 연동 시 추가
    maven { url "https://artifact.bytedance.com/repository/pangle" }
  
    // chartboost 혹은 max 연동 시 추가
    maven { url "https://cboost.jfrog.io/artifactory/chartboost-ads/" }

    // max 연동 시 아래 url 모두 추가
    maven { url "https://artifactory.bidmachine.io/bidmachine" }
    maven { url "https://maven.ogury.co" }
    maven { url "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea" }
    maven { url "https://android-sdk.is.com" }
    maven { url "https://repo.pubmatic.com/artifactory/public-repos" }
    maven { url "https://verve.jfrog.io/artifactory/verve-gradle-release" }

    // tnkpub 연동 시 추가
    maven { url "https://repository.tnkad.net:8443/repository/public/" }
}
```
<br/>  

**build.gradle(app)**  
앱 모듈의 build.gradle의 `manifestPlaceholders`에 레브업 설정값을 정의해야 하며, <span style="color:red">미정의 시 컴파일 에러가 발생함.</span>
레브업 측에 아래 값에 대해 문의 후 기입

* `adiscope_media_id`: 매체 아이디
* `adiscope_media_secret`: 매체 시크릿키


```groovy
android {
    defaultConfig {
        manifestPlaceholders = [
            adiscope_media_id    : "media id 기입필요",
            adiscope_media_secret: "media secret 기입필요"
        ]
    }
}
```

<br/>

bom 연동 방식을 권장.   
어댑터별 버전을 명시하지 않아도 코어 모듈 버전으로 매핑된 버전의 어댑터가 자동으로 연동됨
```groovy
dependencies {
    // bom으로 연동 시 어댑터별 버전을 명시하지 않아도 코어 모듈 버전으로 매핑된 버전의 어댑터가 자동으로 연동됨
    Dependency revupBom = platform("com.tnkfactory.revup:revup-bom:1.0.7")
    implementation revupBom
  
    // [required] revup core library
    implementation "com.tnkfactory.revup:revupCore"
    implementation "com.tnkfactory.revup:revupAndroid"

    // [optional] adiscope 호환 계층 - 기존 adiscope 연동을 교체하는 경우에만 추가
    // bom 관리 대상이 아니므로 버전을 직접 적으며, bom 과 같은 값을 사용
    // 자세한 내용은 docs/adiscope_migration.md 참고
    // implementation "com.tnkfactory.revup:revup_shim:1.0.7"

    // [optional] revup lucky event library
    implementation "com.tnkfactory.revup:revupLuckyEvent"
    
    // [optional] revup network adapter library
    // bidding, waterfall adapter
    implementation "com.tnkfactory.revup:adapter.admob"
    
    // bidding adapter
    implementation "com.tnkfactory.revup:adapter.max"

    // waterfall adapter
    implementation "com.tnkfactory.revup:adapter.chartboost"
    implementation "com.tnkfactory.revup:adapter.pangle"
    implementation "com.tnkfactory.revup:adapter.vungle"
  
    // direct sold adapter
    implementation "com.tnkfactory.revup:adapter.tnkpub"
}
```
<br/>

<details>
<summary> ⚠️ exoplayer 2.15.x 사용하는 경우 (SDK 의존성 충돌) </summary>
<div markdown="1">

- exoplayer 2.15.x 이하 사용 시 chartboost sdk에서 충돌 발생
  - chartboost에서 exoplayer(`com.google.android.exoplayer:exoplayer-core`) 2.18.7과 media3 exoplayer(`androidx.media3:media3-exoplayer`) 1.4.1를 모두 사용하고 있음
- exoplayer 2.18.x 이상 혹은 androidx media3 exoplayer 사용을 권고
  - revup의 adapter.max의 chartboost, moloco sdk에서 exoplayer 2.18.x 사용 중으로 기존 의존성을 exclude하면 크래시가 발생함

<br/>

</div>
</details>

<details>
<summary>(bom 미사용) 각 모듈 버전을 명시하여 연동할 경우</summary>
<div markdown="1">

```groovy
dependencies {
    // [required] revup core library
    implementation "com.tnkfactory.revup:revupCore:1.0.7"
    implementation "com.tnkfactory.revup:revupAndroid:1.0.7"

    // [optional] revup lucky event library
    implementation "com.tnkfactory.revup:revupLuckyEvent:1.0.7"
  
    // [optional] revup network adapter library
    // bidding, waterfall adapter
    implementation "com.tnkfactory.revup:adapter.admob:25.2.0.6"
    
    // bidding adapter
    implementation "com.tnkfactory.revup:adapter.max:13.6.2.7"

    // waterfall adapter
    implementation "com.tnkfactory.revup:adapter.chartboost:9.11.0.6"
    implementation "com.tnkfactory.revup:adapter.pangle:7.9.1.3.7"
    implementation "com.tnkfactory.revup:adapter.vungle:7.7.3.6"

    // direct sold adapter
    implementation "com.tnkfactory.revup:adapter.tnkpub:7.25.11.6"
}
```
<br/>

</div>
</details>

> 하위 버전 연동 시 해당 버전의 브랜치 `README`를 참고하여 연동해주세요. <br/><br/>
> revupCore 버전 기준으로 코어 버전과 매핑되는 어댑터 버전이 아닐 경우 <br/>
> 이니셜라이즈 시점에 아래와 같이 에러 레벨의 로그가 표시됩니다.

![adapter version checker log](https://github.com/user-attachments/assets/286e83f0-8b63-4e3f-bb09-ad86e15df83c)
<br/><br/>

#### 1-2. Add Revup, Network AppId metadata in `AndroidManifest.xml`

**AndroidManifest.xml**  
레브업으로부터 설정값을 전달받은 후 `adiscope_media_id`, `adiscope_media_secret` 메타데이터 추가  
admob 혹은 max 어댑터 연동 시 애드몹 appId 메타데이터를 추가해야 함 (값 미기입 시 앱 크래시 발생)
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools">
    <application>
        <!-- define mediaId, secretKey metadata and 
         use RevupSdk.initialize(activity, listener) function,
         sdk reads this information and initializes it  -->
        <meta-data android:name="adiscope_media_id" android:value="${adiscope_media_id}"/>
        <meta-data android:name="adiscope_media_secret" android:value="${adiscope_media_secret}"/>

        <!-- insert admob app id for revup admob/max network adapter -->
        <meta-data
              android:name="com.google.android.gms.ads.APPLICATION_ID"
              android:value="INPUT_YOUR_ADMOB_APP_ID"/>
    </application>
</manifest>
```

<br/>

### 2. Initialize Revup Sdk
이니셜라이즈 함수는 크게 아래 두 가지로 지원하며, 용도에 따라 선택해서 사용할 수 있음
* `RevupSdk.initialize(activity: Activity, mediaId: Int, mediaSecret: String, listener: RevupInitializeListener)`
* `RevupSdk.initialize(activity: Activity, listener: RevupInitializeListener)`
* [Other Initialize API](./docs/api_documentation.md#revupsdk)

<br/>

#### A) RevupSdk.initialize(activity, mediaId, mediaSecret, listener)
빌드 환경에 따라 매체 환경을 코드 내에서 분기처리하여 이니셜라이즈하는 경우

```kotlin
// initialize parameters
// mediaId: 관리자를 통해 발급
// mediaSecret: 관리자를 통해 발급
// callbackTag: 관리자를 통해 발급, 기본 ""
// childYN: 어린이 여부를 설정해주는 값 (Google GMA에 세팅)
RevupSdk.initialize(
  activity,
  INPUT_YOUR_MEDIA_ID,
  "INPUT_YOUR_MEDIA_SECRET_KEY"
) { isSuccess ->
    if (isSuccess) {
        Log.d(TAG, "RevupSdk initialized.")
        // (recommend) get ad instance and set ad event listener
        mRewardedVideoAd = RevupSdk.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.setRewardedVideoAdListener(this)

        mInterstitialAd = RevupSdk.getInterstitialAdInstance(this)
        mInterstitialAd.setInterstitialAdListener(this)

        mRewardedInterstitialAd = RevupSdk.getRewardedInterstitialAdInstance(this)
        mRewardedInterstitialAd.setRewardedInterstitialAdListener(this)
   } else {
        Log.d(TAG, "RevupSdk initialize failed.")
   }
}
```

<br/>

#### B) RevupSdk.initialize(activity, listener)
mediaId, mediaSecret 값을 고정으로 사용하는 경우

> 앱 모듈의 build.gradle의 `manifestPlaceholders`에 정의된 `adiscope_media_id`, `adiscope_media_secret` 값을
> 매니페스트에 각각 <span style="color:#3894ff">**adiscope_media_id**</span>, <span style="color:#3894ff">**adiscope_media_secret**</span> meta-data로 설정하고
> <span style="color:darkgrey">_RevupSdk.initialize(activity, listener)_</span> 함수를 사용하면 SDK가 해당 meta-data 값을 읽어와 이니셜라이즈를 수행한다.

```kotlin
RevupSdk.initialize(
    activity
) { isSuccess ->
    if (isSuccess) {
        Log.d(TAG, "RevupSdk initialized.")
        // (recommend) get ad instance and set ad event listener
        mRewardedVideoAd = RevupSdk.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.setRewardedVideoAdListener(this)

        mInterstitialAd = RevupSdk.getInterstitialAdInstance(this)
        mInterstitialAd.setInterstitialAdListener(this)

        mRewardedInterstitialAd = RevupSdk.getRewardedInterstitialAdInstance(this)
        mRewardedInterstitialAd.setRewardedInterstitialAdListener(this)
    } else {
        Log.d(TAG, "RevupSdk initialize failed.")
    }
}
```

<br/>

### 3. Set User Info
```kotlin
// none: RevupUserType.None (0)
// adult: RevupUserType.Adult (1)
// child: RevupUserType.Child (2)
val userType = RevupUserType.None
val userId = "" // set unique user id to identify the user in reward information
RevupSdk.setUserIdChild(userId, userType) // 되도록이면 initalize 호출 전 설정 권장
RevupSdk.initialize(this, mediaId, mediaSecret, callbackTag, new RevupInitializeListener() {
    @Override 
    public void onInitialized(boolean isSuccess) {
        if (isSuccess) {
            // Initialized
        } else {
            // Failed to initialize
        }
    })
```
* 참여/시청한 광고에 대한 보상 지급을 위한 사용자 아이디 및 사용자 타입 설정
  * `Rewarded Video`, `Interstitial`, `Rewarded Interstitial` 사용 시 필수 설정
* SDK 초기화 이전 또는 광고 로드 전에 호출되어야 함
* 다계정 사용이 가능한 서비스일 경우, 계정 변경 시 `setUserIdChild` 호출로 레브업에 변경 정보를 전달해주어야 함
  * 그렇지 않을 경우 변경된 계정 정보로 보상 지급이 되지 않음
* `userId`: 최대길이 64자
* `userType`
  * None: 알 수 없음 (유저 나이를 판별할 수 없을 때)
  * Adult: 만 13세 이상 유저
  * Child: 만 13세 미만의 아동 유저

<br/><br/>

## Ad Formats

### Rewarded Ads
#### Create Ad Instance
```kotlin
import com.tnkfactory.revup.reward.RewardedVideoAd
    var mRewardedVideoAd : RewardedVideoAd ?= null
if (RevupSdk.isInitialize()) {
    mRewardedVideoAd = RevupSdk.getRewardedVideoAdInstance(this)
}
```
<br/>

#### Set Event Callback
```kotlin
if (mRewardedVideoAd != null) {
    mRewardedVideoAd.setRewardedVideoAdListener(this)

    override fun onRewardedVideoAdLoaded(unitId: String) {
        // rewarded ad is ready
    }

    override fun onRewardedVideoAdFailedToLoad(unitId: String, revupError: RevupError) {
        // rewarded ad failed to load
    }

    override fun onRewardedVideoAdOpened(unitId: String) {
        // rewarded ad show completed
    }

    override fun onRewardedVideoAdClosed(unitId: String) {
        // rewarded ad closed
    }

    override fun onRewarded(unitId: String, rewardItem: RewardItem) {
        // user should receive the reward
        // RewardItem.getType: 보상 타입
        // RewardItem.getAmount: 보상 양
    }

    override fun onRewardedVideoAdFailedToShow(unitId: String, revupError: RevupError) {
        // rewarded ad failed to show
    }
}
```
<br/>

#### Load
```kotlin
if (mRewardedVideoAd != null) {
    String RV_UNIT_ID = ""
    mRewardedVideoAd.load(RV_UNIT_ID)
} else {
    // Reinitialize
}
```
* 레브업 이니셜라이즈 후 로드 호출 가능
* 특정 보상형 광고 유닛에 속한 광고 네트워크의 광고를 로드
* 광고 유닛명은 반드시 대문자로 전달해야 함
* RewardedVideoAd의 `Load`와 `Show`는 pair로 호출
  * Load를 한 후 Show를 하고, 광고를 Show한 후에는 다시 Load하여 다음 번 Show를 준비
* 광고가 Show되는 동안 다음 광고를 Load할 수 있지만 이는 사용하는 Mediation Ad Network에 따라 달라질 수 있으므로 항상 보장되는 동작은 아님
* 로드 성공 시 `onRewardedVideoAdLoaded`, 로드 실패 시 `onRewardedVideoAdFailedToLoad` 이벤트 콜백 호출
* 로드 성공 콜백에 따라 보상형 광고 송출(show) 가능
* **(Optional)** load의 시간이 필요해 로드 중에는 프로그래스바 노출 추천

<br/>

#### isLoaded
```kotlin
if (mRewardedVideoAd != null) {
    if (mRewardedVideoAd.isLoaded(RV_UNIT_ID)) {
        // show rewarded ad
    } else {
        // do something else
    }
} else {
    // Reinitialize
}
```
* 특정 보상형 광고 유닛의 광고 로드 여부 상태를 확인할 수 있음
* 광고 유닛명은 반드시 대문자로 전달해야 함

<br/>

#### Show
```kotlin
if (mRewardedVideoAd != null) {
    if (mRewardedVideoAd.show()) {
        // succeed
    }else{
        // this show request is duplicated
    }
} else {
    // Reinitialize
}
```
* 마지막으로 로드된 보상형 광고를 사용자에게 송출함
* show 성공 시 `onRewardedVideoAdOpened`, 실패 시 `onRewardedVideoAdFailedToShow` 이벤트 콜백 호출
* `onRewardedVideoAdOpened`가 호출되었다면 이후 `onRewardedVideoAdClosed`가 항상 호출
* RewardedVideoAd의 `Load`와 `Show`는 pair로 호출
  * Load를 한 후 Show를 하고, 광고를 Show한 후에는 다시 Load하여 다음 번 Show를 준비
* `onRewarded` 콜백이 호출되었을 경우 사용자에게 보상 지급이 가능함

<br/>

#### Callback Reward
```kotlin
override fun onRewarded(unitId: String, rewardItem: RewardItem) {
    // user should receive the reward
    // RewardItem.getType: 보상 타입
    // RewardItem.getAmount: 보상 양
}
```
* 보상이 주어져야 할 경우 `OnRewarded`가 호출되며 파라미터로 관련 정보가 전달 (`RewardItem`)
  * `RewardItem.type`: 보상 타입
  * `RewardItem.amount`: 보상의 양
* 이 보상 정보를 바탕으로 게임 내에서 보상을 지급
* `OnRewarded`는 보통 `onRewardedVideoAdOpened` 와 `onRewardedVideoAdClosed` 사이에 호출되는 경우가 많으나 광고 미디에이션 네트워크마다 동작이 다를 수 있음
* `OnRewarded`가 호출되지 않는 경우도 존재할 수 있음
  * 보상 콜백 설정을 Server-to-Server(S2S)로 하였다면, Video 시청 후에는 `OnRewarded`가 호출되지 않음
* Reward 정보는 어뷰징 방지를 위해서 S2S 방식으로 전달 받는 것을 권장
  * S2S 방식을 선택하더라도 보상이 전달 될 시에는 `OnRewarded`가 호출
  * 이때는 서버를 통해 전달받은 정보를 기준으로 처리하고, `OnRewarded`를 통해 전달받은 정보는 검증용으로 사용하거나 무시하도록 함


<br/>

### Interstitial Ads

#### Create Ad Instance
```kotlin
import com.tnkfactory.revup.interstitial.InterstitialAd
var mInterstitialAd : InterstitialAd? = null
if (RevupSdk.isInitialize()) {
    mInterstitialAd = RevupSdk.getInterstitialAdInstance(this)
}
```

#### Set Event Callback
```kotlin
if (mInterstitialAd != null) {
    mInterstitialAd.setInterstitialAdListener(this)

    override fun onInterstitialAdLoaded() {
        // interstitial ad is ready
    }

    override fun onInterstitialAdFailedToLoad(revupError: RevupError) {
        // interstitial ad failed to load
    }

    override fun onInterstitialAdOpened(unitId: String) {
        // interstitial ad show completed
    }

    override fun onInterstitialAdClosed(unitId: String) {
        // interstitial ad closed
    }

    override fun onInterstitialAdFailedToShow(unitId: String, revupError: RevupError) {
        // interstitial ad failed to show
    }
} else {

}
```

#### Load
```kotlin
if (mInterstitialAd != null) {
    val INTERSTITIAL_UNIT_ID = ""
    mInterstitialAd.load(INTERSTITIAL_UNIT_ID)
} else {
    // Reinitialize
}
```
* 레브업 이니셜라이즈 후 로드 호출 가능
* 특정 인터스티셜 유닛에 속한 광고 네트워크의 광고를 로드
* 광고 유닛명은 반드시 대문자로 전달해야 함
* Interstitial의 `Load`와 `Show`는 pair로 호출
  * Load를 한 후 Show를 하고, 광고를 Show한 후에는 다시 Load하여 다음 번 Show를 준비
* 광고가 Show되는 동안 다음 광고를 Load할 수 있지만 이는 사용하는 Mediation Ad Network에 따라 달라질 수 있으므로 항상 보장되는 동작은 아님
* 로드 성공 시 `onInterstitialAdLoaded`, 로드 실패 시 `onInterstitialAdFailedToLoad` 이벤트 콜백 호출
* 로드 성공 콜백에 따라 인터스티셜 광고 송출(show) 가능

#### isLoaded
```kotlin
if (mInterstitialAd != null) {
    if (mInterstitialAd.isLoaded(INTERSTITIAL_UNIT_ID)) {
        // show interstitial ad
    } else {
        // ad is not loaded
    }
} else {
  // Reinitialize
}
```
* 특정 인터스티셜 유닛의 광고 로드 여부 상태를 확인할 수 있음
* 광고 유닛명은 반드시 대문자로 전달해야 함

#### Show
```kotlin
if (mInterstitialAd != null) {
    if (mInterstitialAd.show()) {
        // succeed
    } else {
        // this show request is duplicated
    }
} else {
  // Reinitialize
}
```
* 마지막으로 로드된 인터스티셜 광고를 사용자에게 송출함
* show 성공 시 `onInterstitialAdOpened`, 실패 시 `onInterstitialAdFailedToShow` 이벤트 콜백 호출
* `onInterstitialAdOpened`가 호출되었다면 이후 `onInterstitialAdClosed`가 항상 호출
* Interstitial의 `Load`와 `Show`는 pair로 호출
  * Load를 한 후 Show를 하고, 광고를 Show한 후에는 다시 Load하여 다음 번 Show를 준비

<br/>

### Rewarded Interstitial Ads

#### Create Ad Instance
```kotlin
import com.tnkfactory.revup.rewardedinterstitial.RewardedInterstitialAd
var mRewardedInterstitialAd : RewardedInterstitialAd? = null
if (RevupSdk.isInitialize()) {
    mRewardedInterstitialAd = RevupSdk.getRewardedInterstitialAdInstance(this)  
}
```
<br/>

#### Set Event Callback
```kotlin
if (mRewardedInterstitialAd != null) {
    mRewardedInterstitialAd.setRewardedInterstitialAdListener(this)

    override fun onRewardedInterstitialAdLoaded(unitId: String) {
        // rewarded interstitial ad loaded
    }

    override fun onRewardedInterstitialAdFailedToLoad(unitId: String, revupError: RevupError) {
        // rewarded interstitial ad failed to load
    }

    override fun onRewardedInterstitialAdSkipped(unitId: String) {
        // user skipped rewarded interstitial ad
    }

    override fun onRewardedInterstitialAdOpened(unitId: String) {
        // rewarded interstitial ad show completed
    }

    override fun onRewardedInterstitialAdClosed(unitId: String) {
        // rewarded interstitial ad closed
    }

    override fun onRewardedInterstitialAdRewarded(unitId: String, rewardItem: RewardItem) {
        // user should receive the reward
        // RewardItem.getType: 보상 타입
        // RewardItem.getAmount: 보상 양
    }

    override fun onRewardedInterstitialAdFailedToShow(unitId: String, revupError: RevupError) {
        // rewarded interstitial ad failed to show
    }
}
```
<br/>

#### Load
```kotlin
if (mRewardedInterstitialAd != null) {
    mRewardedInterstitialAd.load("RI_UNIT1")
} else {
    // Reinitialize
}
```
* 파라미터에 지정한 전면형 보상 광고 유닛 1개를 로드
* 광고 유닛명은 반드시 대문자로 전달해야 함
* 로드 결과는 `onRewardedInterstitialAdLoaded` 또는 `onRewardedInterstitialAdFailedToLoad` 콜백으로 전달됨

<br/>

#### Preload
```kotlin
// preload rewarded interstitial ad which belongs to specific unit list
val unitList = arrayOf("RI_UNIT1", "RI_UNIT2", "RI_UNIT3")
if (mRewardedInterstitialAd != null) {
    mRewardedInterstitialAd.preloadUnit(unitList)
} else {
    // Reinitialize
}
```
* 파라미터에 지정한 전면형 보상 광고 유닛들에 대한 로드를 순차적으로 진행
* 광고 유닛명은 반드시 대문자로 전달해야 함
* 로드 결과는 `onRewardedInterstitialAdLoaded` 또는 `onRewardedInterstitialAdFailedToLoad` 콜백으로 전달됨

<br/>

#### Preload All
```kotlin
// preload all activated rewarded interstitial ad
if (mRewardedInterstitialAd != null) {
    mRewardedInterstitialAd.preloadAll()
} else {
    // Reinitialize
}
```
* 어드민 페이지에 등록된 활성화된 전면형 보상 광고 유닛들을 순차적으로 로드
* 로드 결과는 `onRewardedInterstitialAdLoaded` 또는 `onRewardedInterstitialAdFailedToLoad` 콜백으로 전달됨

<br/>

#### IsLoaded
```kotlin
if (mRewardedInterstitialAd != null) {
    boolean isLoaded = mRewardedInterstitialAd.isLoaded("RI_UNIT1")
}
```
* 해당 유닛이 로드되어 있는지 확인
* 광고 유닛명은 반드시 대문자로 전달해야 함

<br/>

#### Show
```kotlin
if (mRewardedInterstitialAd != null) {
    mRewardedInterstitialAd.show("RI_UNIT1")
} else {
    // Reinitialize
}
```
* 로드된 전면형 보상 광고의 유닛을 지정하여 사용자에게 **팝업 없이 바로** 보여줌
* 광고 유닛명은 반드시 대문자로 전달해야 함
* `onRewardedInterstitialAdRewarded`는 보통 `onRewardedInterstitialAdOpened` 와 `onRewardedInterstitialAdClosed` 사이에 호출되는 경우가 많으나 광고 미디에이션 네트워크마다 동작이 다를 수 있음
* show는 중복하여 호출할 수 없음
* show 완료/실패 후 해당 유닛이 **자동으로 리로드되지 않으므로** 필요 시 `load` 또는 `preloadUnit`을 직접 호출하여 다시 로드해야 함

<br/>

#### ShowWithPopup
```kotlin
if (mRewardedInterstitialAd != null) {
    mRewardedInterstitialAd.showWithPopup("RI_UNIT1")
} else {
    // Reinitialize
}
```
* 로드된 전면형 보상 광고의 유닛을 지정하여 **안내 팝업을 보여준 뒤** 해당 광고를 사용자에게 보여줌
  * 안내 팝업의 건너뛰기 버튼 클릭 시 팝업이 종료되며 `onRewardedInterstitialAdSkipped`가 호출됨
* 광고 유닛명은 반드시 대문자로 전달해야 함
* `onRewardedInterstitialAdRewarded`는 보통 `onRewardedInterstitialAdOpened` 와 `onRewardedInterstitialAdClosed` 사이에 호출되는 경우가 많으나 광고 미디에이션 네트워크마다 동작이 다를 수 있음
* show는 중복하여 호출할 수 없음
* show 완료/실패 후 해당 유닛이 **자동으로 리로드되지 않으므로** 필요 시 `load` 또는 `preloadUnit`을 직접 호출하여 다시 로드해야 함

<br/>

#### Callback Reward
```kotlin
override fun onRewardedInterstitialAdRewarded(unitId: String, rewardItem: RewardItem) {
    // user should receive the reward
    // RewardItem.getType: 보상 타입
    // RewardItem.getAmount: 보상 양
}
```
* 보상이 주어져야 할 경우 `onRewardedInterstitialAdRewarded`가 호출되며 파라미터로 관련 정보가 전달 (`RewardItem`)
  * `RewardItem.type`: 보상 타입
  * `RewardItem.amount`: 보상의 양
* 이 보상 정보를 바탕으로 게임 내에서 보상을 지급
* `onRewardedInterstitialAdRewarded`는 보통 `onRewardedInterstitialAdOpened` 와 `onRewardedInterstitialAdClosed` 사이에 호출되는 경우가 많으나 광고 미디에이션 네트워크마다 동작이 다를 수 있음
* `onRewardedInterstitialAdRewarded`가 호출되지 않는 경우도 존재할 수 있음
  * 보상 콜백 설정을 Server-to-Server(S2S)로 하였다면, Video 시청 후에는 `onRewardedInterstitialAdRewarded`가 호출되지 않음
* Reward 정보는 어뷰징 방지를 위해서 S2S 방식으로 전달 받는 것을 권장
  * S2S 방식을 선택하더라도 보상이 전달 될 시에는 `onRewardedInterstitialAdRewarded`가 호출
  * 이때는 서버를 통해 전달받은 정보를 기준으로 처리하고, `onRewardedInterstitialAdRewarded`를 통해 전달받은 정보는 검증용으로 사용하거나 무시하도록 함

<br/>