# Adiscope → Revup 마이그레이션 (revup_shim)

기존 Adiscope SDK 로 연동된 앱을 **호출부를 고치지 않고** Revup 으로 교체하기 위한 호환 모듈입니다.
(Kotlin 으로 작성된 호출부는 1.0.7 에서 일부 수정이 필요합니다 — [Kotlin 에서 호출하는 경우](#kotlin-에서-호출하는-경우) 참고)

이 문서 하나로 교체가 끝나도록 필요한 설정을 모두 담았습니다. 신규 연동이라면 대신
[README](../README.md) 를 보세요.

<br/>

## 필요한 경우

아래 중 하나라도 해당하면 `revup_shim` 이 필요합니다.

* 앱 코드가 `com.nps.adiscope.*` 를 직접 호출하고 있고, 이번 교체에서는 SDK 만 바꾸려는 경우
* 앱이 사용하는 **서드파티 SDK 가 옛 클래스로 컴파일된 바이너리**를 들고 있는 경우

두 번째가 특히 중요합니다. 서드파티 SDK 가 `com.adiscope.luckyevent.tnk.TnkEventActivity` 를
직접 참조하고 있으면, 이 모듈 없이 교체할 때 **런타임에 `NoClassDefFoundError`** 가 발생합니다.
컴파일은 통과하므로 빌드 단계에서는 드러나지 않습니다.

> **신규 연동이라면 필요하지 않습니다.** 옛 API 참조가 없으므로 추가하지 마세요.

<br/>

## 요구사항

* `minSdkVersion` 23
* `compileSdkVersion` 36

어댑터별 minSdk 와 네트워크 SDK 버전은 [README 의 Requirements](../README.md#requirements) 를 참고하세요.

> ⚠️ 애드몹 SDK(`com.google.android.gms:play-services-ads`)를 이미 포함하고 있다면 버전 호환에 유의하세요.
> admob · max 어댑터 사용 시 gms 25 버전으로 마이그레이션이 필요합니다.

<br/>

## 순서

아래 순서를 지켜 주세요. 특히 2번을 건너뛰면 빌드가 깨집니다.

| | 작업 | 건너뛰면 |
|---|---|---|
| 1 | [저장소 추가](#1-저장소-추가) | 의존성 해석 실패 |
| 2 | [기존 Adiscope 의존성 제거](#2-기존-adiscope-의존성-제거) | **Duplicate class 빌드 실패** |
| 3 | [Revup 의존성 추가](#3-revup-의존성-추가) | — |
| 4 | [매체 설정값 확인](#4-매체-설정값) | 초기화 실패 |

<br/>

## 1. 저장소 추가

Revup 아티팩트는 **Maven Central 에 없습니다.** 아래 저장소 하나만 추가하면 됩니다.
미러링과 그룹 설정이 되어 있어 네트워크 어댑터가 쓰는 SDK 도 이 경로로 함께 해석됩니다.

**settings.gradle**

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        // [required] revup library
        maven { url "https://repository.tnkad.net/repository/public/" }
    }
}
```

`settings.gradle` 에 `dependencyResolutionManagement` 가 없다면 최상위 `build.gradle` 의
`repositories` 에 동일하게 추가하세요.

`google()` 과 `mavenCentral()` 은 미러링 대상이 아니므로 그대로 두어야 합니다.

기존 Adiscope 저장소(`https://repository.adiscope.com/...`)와, 네트워크별로 추가해 두었던
저장소(bytedance · chartboost · bidmachine · ogury · mintegral · ironsource · pubmatic · verve 등)는
모두 제거할 수 있습니다.

<br/>

## 2. 기존 Adiscope 의존성 제거

<span style="color:red">**`com.nps.adiscope` 그룹 의존성을 모두 제거해야 합니다.**</span>

`revup_shim` 은 옛 클래스 이름(`com.nps.adiscope.AdiscopeSdk` 등)을 그대로 재현합니다.
기존 `adiscopeCore` 가 남아 있으면 같은 FQCN 이 두 모듈에서 나와 빌드가 깨집니다.

```
Duplicate class com.nps.adiscope.AdiscopeSdk found in modules
  adiscopeCore-5.4.3.aar and revup_shim-1.0.7.aar
```

**제거 대상** — BOM, 코어, 어댑터 전부입니다.

```groovy
dependencies {
    // ↓ 아래를 전부 삭제
    implementation platform("com.nps.adiscope:adiscope-bom:5.4.3")
    implementation "com.nps.adiscope:adiscopeCore"
    implementation "com.nps.adiscope:adiscopeAndroid"
    implementation "com.nps.adiscope:adiscopeWalnut"   // 대응 모듈 없음. 그냥 삭제
    implementation "com.nps.adiscope:adapter.admob"
    implementation "com.nps.adiscope:adapter.max"
    // ... 그밖의 com.nps.adiscope 의존성 전부
}
```

지워도 컴파일은 그대로 통과합니다. 옛 클래스는 `revup_shim` 이 대신 제공하기 때문입니다.
**호출부를 고치지 않는다는 것은 앱 코드 이야기이고, 의존성은 교체 대상입니다.**

<br/>

## 3. Revup 의존성 추가

**build.gradle(app)**

```groovy
dependencies {
    Dependency revupBom = platform("com.tnkfactory.revup:revup-bom:1.0.7")
    implementation revupBom

    // [required] revup core library
    implementation "com.tnkfactory.revup:revupCore"
    implementation "com.tnkfactory.revup:revupAndroid"

    // [optional] adiscope 호환 계층
    // BOM 관리 대상이 아니므로 버전을 직접 적어야 하며, BOM 버전과 같은 값을 사용합니다.
    implementation "com.tnkfactory.revup:revup_shim:1.0.7"

    // [optional] revup network adapter library
    // 쓰던 어댑터를 그대로 옮깁니다. 아티팩트명은 같고 group 만 다릅니다.
    implementation "com.tnkfactory.revup:adapter.admob"
    implementation "com.tnkfactory.revup:adapter.max"
    implementation "com.tnkfactory.revup:adapter.chartboost"
    implementation "com.tnkfactory.revup:adapter.pangle"
    implementation "com.tnkfactory.revup:adapter.vungle"
    implementation "com.tnkfactory.revup:adapter.tnkpub"
}
```

<span style="color:red">`revup_shim` 은 BOM 이 버전을 고정하지 않습니다.</span>
버전을 생략하면 해석에 실패하고, BOM 과 다른 값을 적으면 코어 모듈 버전이 어긋납니다.
**BOM 을 올릴 때 이 줄도 같이 올려 주세요.**

`revup_shim` 은 `revupCore` · `revupAndroid` · `revupLuckyEvent` 를 함께 가져옵니다.

> SDK 를 사내 라이브러리 모듈로 한 번 감싸서 배포하는 구조라면, 그 모듈에서는
> `implementation` 이 아니라 `api` 로 선언해야 앱 쪽에서 옛 클래스가 보입니다.

<br/>

### 어댑터 대응표

group 만 바뀌고 아티팩트명은 같습니다. 버전은 BOM 이 관리하므로 적지 않습니다.

| 기존 | 교체 후 | BOM 1.0.7 기준 버전 |
| --- | --- | --- |
| `com.nps.adiscope:adapter.admob` | `com.tnkfactory.revup:adapter.admob` | 25.2.0.6 |
| `com.nps.adiscope:adapter.max` | `com.tnkfactory.revup:adapter.max` | 13.6.2.7 |
| `com.nps.adiscope:adapter.chartboost` | `com.tnkfactory.revup:adapter.chartboost` | 9.11.0.6 |
| `com.nps.adiscope:adapter.pangle` | `com.tnkfactory.revup:adapter.pangle` | 7.9.1.3.7 |
| `com.nps.adiscope:adapter.vungle` | `com.tnkfactory.revup:adapter.vungle` | 7.7.3.6 |
| `com.nps.adiscope:adapter.tnkpub` | `com.tnkfactory.revup:adapter.tnkpub` | 7.25.11.6 |

어댑터를 빠뜨리면 초기화는 성공하지만 광고가 채워지지 않습니다. 빌드도 통과하고 에러도 없으므로
교체 전후의 어댑터 목록을 반드시 대조해 주세요.

어댑터 버전이 코어와 어긋나면 이니셜라이즈 시점에 에러 레벨 로그로 알려줍니다.

<br/>

### Kotlin DSL 로 쓰는 경우

```kotlin
dependencies {
    implementation(platform("com.tnkfactory.revup:revup-bom:1.0.7"))

    implementation("com.tnkfactory.revup:revupCore")
    implementation("com.tnkfactory.revup:revupAndroid")
    implementation("com.tnkfactory.revup:revup_shim:1.0.7")

    implementation("com.tnkfactory.revup:adapter.admob")
    // ... 그밖의 어댑터
}
```

<br/>

## 4. 매체 설정값

**meta-data 이름과 값을 그대로 두면 됩니다.** Revup 은 `revup_*` 를 먼저 찾고, 없으면
`adiscope_*` 로 폴백합니다. 기존 매니페스트와 `manifestPlaceholders` 를 고칠 필요가 없습니다.

| 1순위 | 폴백 | 용도 |
| --- | --- | --- |
| `revup_media_id` | `adiscope_media_id` | 매체 아이디 |
| `revup_media_secret` | `adiscope_media_secret` | 매체 시크릿키 |
| `revup_sub_domain` | `adiscope_sub_domain` | 서브 도메인 (옵션) |

`AdiscopeSdk.initialize(activity, listener)` 오버로드는 이 meta-data 를 읽어 초기화합니다.
mediaId · mediaSecret 을 인자로 넘기는 오버로드를 쓰고 있었다면 그쪽도 그대로 동작합니다.

**AndroidManifest.xml** — 기존 그대로

```xml
<application>
    <meta-data android:name="adiscope_media_id" android:value="${adiscope_media_id}"/>
    <meta-data android:name="adiscope_media_secret" android:value="${adiscope_media_secret}"/>

    <!-- admob · max 어댑터 연동 시 필수. 값 미기입 시 앱 크래시 발생 -->
    <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID"
               android:value="INPUT_YOUR_ADMOB_APP_ID"/>
</application>
```

`${...}` 치환을 쓰고 있다면 `manifestPlaceholders` 정의도 그대로 두세요. 정의가 빠지면
매니페스트 머지 단계에서 빌드가 깨집니다.

> <span style="color:red">**매체 아이디와 시크릿키 값 자체는 레브업 측에 확인해 주세요.**</span>
> 기존 Adiscope 매체 값을 그대로 쓸 수 있는지, 재발급이 필요한지는 매체마다 다릅니다.

<br/>

## 제공하는 API

옛 클래스 이름과 시그니처를 그대로 유지하고, 내부에서 Revup 구현으로 위임합니다.
**앱 코드를 고칠 필요가 없습니다.** (Kotlin 호출부는 [아래](#kotlin-에서-호출하는-경우) 참고)

| 옛 API | 위임 대상 |
| --- | --- |
| `com.nps.adiscope.AdiscopeSdk` | `com.tnkfactory.revup.RevupSdk` |
| `com.nps.adiscope.reward.RewardedVideoAd` | 리워드 동영상 |
| `com.nps.adiscope.interstitial.InterstitialAd` | 전면 광고 |
| `com.nps.adiscope.rewardedinterstitial.RewardedInterstitialAd` | 리워드 전면 광고 |
| `com.nps.adiscope.model.UnitStatus` / `AdiscopeUserType` | 유닛 상태 · 사용자 타입 |
| `com.nps.adiscope.reward.RewardItem` / `AdiscopeError` | 리워드 · 에러 모델 |
| `com.adiscope.luckyevent.tnk.TnkEventActivity` | `com.tnkfactory.revup.luckyevent.tnk.TnkEventActivity` |

`com.adiscope.luckyevent.tnk.TnkEventActivity` 는 **액티비티가 아닙니다.**
옛 호출부가 쓰던 정적 메서드만 제공하는 껍데기이고 화면은 Revup 이 담당하므로,
**AndroidManifest 에 선언하지 마세요.**

<br/>

### Kotlin 에서 호출하는 경우

옛 Adiscope SDK 는 Java 로 작성되어 있어, 이를 호출하던 Kotlin 코드는 Java 관례대로 쓰여 있습니다.
`revup_shim` 은 Kotlin 으로 작성되어 있어서 **1.0.7 에서는** 아래 호출이 컴파일되지 않습니다.
Java 호출부는 해당하지 않습니다.

| 기존 Kotlin 코드 | 1.0.7 에서 바꿀 코드 |
| --- | --- |
| `AdiscopeSdk.initialize(activity) { isSuccess -> ... }` | `AdiscopeSdk.initialize(activity, object : AdiscopeInitializeListener { override fun onInitialized(isSuccess: Boolean) { ... } })` |
| `getUnitStatus(unitId) { error, status -> ... }` | `getUnitStatus(unitId, object : IUnitStatus { override fun onResult(error: AdiscopeError?, unitStatus: UnitStatus?) { ... } })` |
| `error.code` · `error.description` · `error.xb3TraceId` | `error.getCode()` · `error.getDescription()` · `error.getXb3TraceId()` |
| `rewardItem.amount` · `rewardItem.type` | `rewardItem.getAmount()` · `rewardItem.getType()` |
| `unitStatus.isLive` · `unitStatus.isActive` | `unitStatus.isLive()` · `unitStatus.isActive()` |

메서드가 여러 개인 리스너(`RewardedVideoAdListener` 등)는 원래 Java 에서도 람다로 쓸 수 없었으므로
기존 `object :` 형태 그대로 두면 됩니다.

**1.0.8 부터는 위 표의 왼쪽(기존 코드)이 수정 없이 컴파일됩니다.** 1.0.7 에 맞춰 오른쪽처럼 고쳤다면,
1.0.8 로 올릴 때 getter 호출(`error.getCode()` · `rewardItem.getAmount()` · `unitStatus.isLive()`)은
`Unresolved reference` 로 컴파일되지 않으므로 왼쪽 문법으로 되돌려 주세요.
`object :` 로 바꾼 리스너는 그대로 두어도 됩니다.

<br/>

### 대응이 없는 것

| 옛 모듈 | 조치 |
| --- | --- |
| `com.nps.adiscope:adiscopeWalnut` | **의존성에서 삭제하세요.** Revup 에 대응 모듈이 없습니다 |

`adiscopeWalnut` 은 동영상 간편 연동 모듈입니다. 옛 가이드 예제를 그대로 복사하면서 의존성에만
남아 있고 실제로는 호출하지 않는 경우가 대부분이라, **지워도 앱 동작에 영향이 없습니다.**

호출부가 정말 있다면 `revup_shim` 이 제공하는 리워드 동영상 API 로 대체하면 됩니다.
그 밖의 이유로 이 모듈이 필요하다고 판단되면 담당자에게 문의해 주세요.

<br/>

## ProGuard

별도 설정이 필요 없습니다. 옛 패키지명이 난독화되면 기존 바이너리가 링크되지 않으므로,
모듈이 아래 규칙을 스스로 포함해 배포합니다.

```proguard
-keep class com.adiscope.** { *; }
-keep class com.nps.adiscope.** { *; }
```

기존 `proguard-rules.pro` 에 적어 둔 Adiscope keep 규칙은 남겨 두어도 무방합니다.

<br/>

## 교체 후 확인

* 의존성 트리에 `com.nps.adiscope` 가 남아 있지 않은지 (`./gradlew :app:dependencies`)
* Kotlin 호출부가 있다면 [Kotlin 에서 호출하는 경우](#kotlin-에서-호출하는-경우) 의 수정이 반영되었는지
* 교체 전후 어댑터 목록이 같은지
* 초기화 콜백이 `isSuccess = true` 로 끝까지 실행되는지
* 광고 포맷별 `load` → `isLoaded` → `show` 가 정상 동작하는지

<br/>

## 걷어내기

호환 계층은 마이그레이션 기간용입니다. 제공하는 클래스는 모두 `@Deprecated` 이므로
IDE 경고를 따라 호출부를 Revup API 로 옮길 수 있습니다.

1. 앱 코드의 `com.nps.adiscope.*` 참조를 Revup API 로 교체
2. 서드파티 SDK 가 옛 클래스를 참조하지 않는지 확인
3. `revup_shim` 의존성 제거

3번을 먼저 하면 앞서 설명한 `NoClassDefFoundError` 가 재현됩니다. **순서를 지켜 주세요.**

Revup API 사용법은 [README 의 Integration Guide](../README.md#integration-guide) 를 참고하세요.

<br/>

## 참고

* [SDK 연동 가이드 (README)](../README.md)
* [API Documentation](./api_documentation.md)
* [Error Information](./error_info.md)
* [Third Party Event](./event_guide.md)
