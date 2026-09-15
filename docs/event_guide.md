## 서드파티 이벤트 제공


### 이벤트 등록

매체 영업팀을 통해 이벤트 아이디와 키값을 발급 받아 사용 합니다.

`eventId`: 이벤트를 식별하는 고유 키값입니다. 예) 10002501 : 룰렛, 10002502 : 복권, 10002503 : 출석

`pubId`: 이벤트매체 식별용 키값입니다.


### 이벤트 모듈 참조 추가
```groovy
implementation "com.tnkfactory.revup:revupLuckyEvent:1.0.7"
```


### Method
- 사용자 아이디를 설정합니다.
- `void TnkEventBuilder.setUserName(String: userName)`

- 어린이 여부를 설정합니다. ("YES" or "NO")
- `void TnkEventBuilder.setChildYn(childYn: String)`

- 이벤트 아이디와 앱아이디를 설정합니다.
- `void TnkEventBuilder.setEventIdTnkAppId(eventId: String, pubId: String)`

- 이벤트 화면을 표시합니다. (필수)
- `void TnkEventBuilder.show(activity: Activity)`


#### sample
```kotlin
TnkEventActivity.TnkEventBuilder()
    .setUserName("tnk_test")
    .setChildYn("YES") // or "NO"
    .setEventIdTnkAppId("25120101","00000000-0000-0000-0000-000000000000")
    .show(this@MainActivity)
```

```kotlin
val scheme = "revup0000://thirdpartyevent?event_id=25120101&pub_id=00000000-0000-0000-0000-000000000000&md_user_nm=tnk_test&child_yn=YES"
    Intent().apply {
        action = Intent.ACTION_VIEW
        data = android.net.Uri.parse(scheme)
    }.also {
        startActivity(it)
    }
```

scheme 으로 진입할 때 `event_id`(또는 `app_id`), `pub_id`(또는 `pid`), `md_user_nm` 은 필수입니다.
하나라도 없으면 **안내 토스트가 표시된 뒤 화면이 닫히며**(`1.0.5` 이상), 누락된 항목은 logcat 에 출력됩니다.

```
E/RevupEvent: scheme parameter missing: md_user_nm
```

> SDK 초기화는 이벤트 화면이 진입 시점에 수행합니다. 앱의 패키지명이 등록되어 있지 않으면
> 초기화가 실패하므로, 패키지명 변경이나 테스트용 패키지 사용 시 사전에 등록 요청 부탁드립니다.


---

## 이벤트 화면 테마

이벤트 화면을 라이트 / 다크 / 시스템 설정 중에서 고를 수 있습니다. (`1.0.6` 이상)

<span style="color:red">`1.0.7` 에서 기본값이 바뀌었습니다.</span> 아래 표를 확인해 주세요.

**적용 범위는 네이티브 영역입니다.** 화면 배경, 상태바·내비게이션바 아이콘 대비,
SDK 가 표시하는 안내 다이얼로그가 대상입니다.
이벤트 웹 페이지 자체는 웹에서 정한 대로 표시됩니다.

### LuckyEventTheme

| 값 | 동작 |
| --- | --- |
| `LuckyEventTheme.LIGHT` | 단말 설정과 무관하게 항상 라이트 |
| `LuckyEventTheme.DARK` | 단말 설정과 무관하게 항상 다크 |
| `LuckyEventTheme.SYSTEM` | 단말의 다크모드 설정을 따름 |
| `LuckyEventTheme.UNSPECIFIED` | SDK 가 테마에 관여하지 않음 (`1.0.7` 이상 · **기본값**) |

> **`1.0.7` 부터 기본값이 `UNSPECIFIED` 입니다.**
> 지정하지 않으면 SDK 는 화면 배경도, 시스템 바 스타일도, AppCompat 의 night mode 도
> 건드리지 않습니다. 매체 앱이 정해 둔 테마가 그대로 유지됩니다.
>
> 이벤트 화면이 단말 다크모드를 따르길 원하시면 `setEventTheme(LuckyEventTheme.SYSTEM)` 을
> **명시해 주세요.** `SYSTEM` 도 night mode 에 값을 쓰는 동작이라 기본값이 될 수 없습니다.

> ⚠️ **`1.0.6` 을 쓰고 계셨다면 확인이 필요합니다.**
> `1.0.6` 은 기본값이 `SYSTEM` 이라, 테마를 설정하지 않아도 SDK 가 night mode 에 값을
> 썼습니다. 그 설정이 **이벤트 화면을 벗어난 뒤에도 앱에 남아** 매체 앱 테마가 바뀌는
> 문제가 있었습니다. `1.0.7` 로 올리시면 해결됩니다.
> 다크모드 동작을 유지하시려면 위처럼 `SYSTEM` 을 명시해 주세요.

### Method

- 이후에 열리는 모든 이벤트 화면의 테마를 설정합니다.
- `void RevupLuckyEvent.setEventTheme(theme: LuckyEventTheme)`

- 현재 설정된 테마를 반환합니다.
- `LuckyEventTheme RevupLuckyEvent.getEventTheme()`

- 이 빌더로 여는 화면에만 테마를 적용합니다. (전역 설정보다 우선)
- `TnkEventBuilder TnkEventBuilder.setTheme(theme: LuckyEventTheme)`

#### sample

앱 전체에 적용하는 경우 `Application` 에서 한 번 설정합니다.

```kotlin
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RevupLuckyEvent.setEventTheme(LuckyEventTheme.SYSTEM)
    }
}
```

```java
RevupLuckyEvent.setEventTheme(LuckyEventTheme.LIGHT);
```

화면마다 다르게 열어야 한다면 빌더에 지정합니다.

```kotlin
TnkEventActivity.TnkEventBuilder()
    .setUserName("tnk_test")
    .setEventIdTnkAppId("25120101", "00000000-0000-0000-0000-000000000000")
    .setTheme(LuckyEventTheme.DARK)
    .show(this@MainActivity)
```

> **화면을 열기 전에 호출해야 합니다.**
> 이미 떠 있는 이벤트 화면에는 반영되지 않습니다.

> **scheme 으로 진입하는 경우에도 전역 설정이 적용됩니다.**
> 다만 앱 프로세스가 새로 뜬 직후에는 아직 설정 전일 수 있으므로,
> scheme 진입을 사용하는 매체는 `Application` 에서 설정하는 것을 권장합니다.

> `SYSTEM` 으로 설정한 상태에서 사용자가 단말의 다크모드를 전환해도
> 화면이 다시 로드되지 않고 색상만 바뀝니다. 진행 중이던 이벤트가 유지됩니다.


---

## 이벤트 화면 종료

푸시 알림을 눌러 다른 화면으로 이동해야 하는 경우처럼, 매체가 이벤트 화면을 닫아야 할 때 사용합니다.

**이벤트 화면과 그 위에 떠 있는 광고 화면이 함께 닫힙니다.** 광고가 전면으로 재생 중이어도 정리되므로,
매체는 목적 화면으로 이동시키기만 하면 됩니다.

### Method

- 이벤트 화면 종료를 요청합니다. (브로드캐스트 방식)
- `void TnkEventActivity.sendFinishBroadcast(context: Context)`

- 이벤트 화면 종료를 요청합니다. (직접 호출 방식)
- `boolean TnkEventActivity.finishIfActive()`
- 반환값: 닫은 화면이 있으면 `true`. 없으면 요청이 보관되며 `false` 를 반환합니다.

- 종료 요청 브로드캐스트의 액션 문자열을 반환합니다. (`{패키지명}.TNK_EVENT_FINISH`)
- `String TnkEventActivity.getFinishAction(context: Context)`

#### sample

```kotlin
// 알림 진입 화면 등에서 호출
TnkEventActivity.sendFinishBroadcast(context)
```

인텐트를 직접 발송해도 동일하게 동작합니다.

```kotlin
context.sendBroadcast(
    Intent(TnkEventActivity.getFinishAction(context)).apply {
        setPackage(context.packageName)
    }
)
```

> **요청 시점에 화면이 없어도 됩니다.**
> 앱 프로세스가 종료된 상태에서 알림으로 진입하는 경우처럼, 요청 시점에 이벤트 화면이 아직 없을 수 있습니다.
> 이때 요청은 유실되지 않고 보관되었다가, 해당 화면이 다시 나타나는 시점에 처리됩니다. (`1.0.4` 이상)

> **알림에서 화면 이동은 액티비티로 처리해야 합니다.**
> Android 12 부터는 알림에서 실행된 리시버·서비스가 액티비티를 띄울 수 없습니다.
> 알림의 `PendingIntent` 는 액티비티를 가리키게 하고, 그 액티비티에서 위 종료 API 를 호출한 뒤
> 목적 화면으로 이동시키는 구성을 권장합니다.


---

## 광고 시작 / 종료 콜백

이벤트 화면 안에서 노출되는 광고(리워드 영상, 전면광고)의 시작·종료 시점을 전달받습니다. (`1.0.4` 이상)

### Method

- 리스너를 등록합니다. `null` 을 전달하면 해제됩니다.
- `void RevupLuckyEvent.setEventListener(listener: RevupLuckyEventListener?)`

### RevupLuckyEventListener

| 콜백 | 시점 |
| --- | --- |
| `onLuckyEventAdStarted(info: LuckyEventAdInfo)` | 광고가 화면에 노출되기 시작했을 때 |
| `onLuckyEventAdFinished(info: LuckyEventAdInfo)` | 광고가 닫혔을 때 |

광고 하나당 시작과 종료가 **1:1 로 호출**되며, 콜백은 항상 메인 스레드에서 실행됩니다.
광고 노출에 실패해 화면이 뜨지 않은 경우에는 두 콜백 모두 호출되지 않습니다.

### LuckyEventAdInfo

| 필드 | 설명 |
| --- | --- |
| `adType` | `LuckyEventAdType.REWARDED_VIDEO` 또는 `LuckyEventAdType.INTERSTITIAL` |
| `unitId` | 광고 unit id (알 수 없는 경우 빈 문자열) |
| `rewarded` | 리워드 지급 콜백을 받았는지 여부. **종료 콜백에서만 유효**합니다 |
| `errorMessage` | 정상 종료가 아닌 사유. 정상 종료면 `null` |

`errorMessage` 값

| 값 | 의미 |
| --- | --- |
| `failed_to_show` | 광고 노출 도중 실패 (네트워크 사유가 있으면 해당 메시지) |
| `insufficient_activation_time` | 전면광고 유효 시청 시간(5초) 미달 |
| `activity_destroyed` | 광고 종료 콜백 없이 이벤트 화면이 종료됨 |

#### sample

```kotlin
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        RevupLuckyEvent.setEventListener(object : RevupLuckyEventListener {
            override fun onLuckyEventAdStarted(info: LuckyEventAdInfo) {
                Log.d(TAG, "ad started: ${info.adType} / ${info.unitId}")
            }

            override fun onLuckyEventAdFinished(info: LuckyEventAdInfo) {
                Log.d(TAG, "ad finished: rewarded=${info.rewarded}, error=${info.errorMessage}")
            }
        })
    }
}
```

```java
RevupLuckyEvent.setEventListener(new RevupLuckyEventListener() {
    @Override
    public void onLuckyEventAdStarted(LuckyEventAdInfo info) {
        Log.d(TAG, "ad started: " + info.getAdType() + " / " + info.getUnitId());
    }

    @Override
    public void onLuckyEventAdFinished(LuckyEventAdInfo info) {
        Log.d(TAG, "ad finished: rewarded=" + info.getRewarded()
                + ", error=" + info.getErrorMessage());
    }
});
```

> **리스너는 Application 에서 등록하세요.**
> 리스너는 프로세스 전역에 보관되므로, `Activity` 에 구현해 등록하면 화면이 종료되어도 참조가 남습니다.
> `Activity` 에서 등록해야 한다면 `onDestroy()` 에서 `setEventListener(null)` 로 해제해야 합니다.

> 이 콜백은 **이벤트 화면 안에서 노출되는 광고**만 대상으로 합니다.
> 매체가 별도 지면에서 직접 호출하는 리워드 영상·전면광고는 각 광고의 리스너를 사용하세요.


---

## 웹뷰 스킴 URL 전달받기

이벤트 웹 페이지가 호출한 `tnkscheme://` URL 을 매체 앱이 전달받아 직접 처리할 수 있습니다.
이벤트 참여·적립 시점을 분석 도구로 보내는 등, 웹 페이지의 동작을 앱에서 이어받을 때 사용합니다.

> iOS 의 `luckyEventWebViewNavigated(_:vc:)` 에 대응하는 기능입니다.
> 다만 **동작 방식이 다르므로** 아래 [iOS 와의 차이](#ios-와의-차이) 를 확인해 주세요.

### 동작

```
이벤트 웹 페이지 (location.href = "tnkscheme://...")
  → SDK 가 후커 클래스의 adEventWebViewNavigated(activity, url) 호출
      ├─ true  반환 → 매체가 처리한 것으로 보고 SDK 는 아무것도 하지 않음
      └─ false 반환 → SDK 기본 처리 진행
```

후커는 **SDK 기본 처리보다 먼저, 메인 스레드에서** 호출됩니다.

### 1. 후커 클래스 작성

```kotlin
class LuckyEventSchemeHooker {
    fun adEventWebViewNavigated(ac: FragmentActivity, url: String): Boolean {
        if (!url.startsWith("tnkscheme:")) return false

        // SDK 기본 처리 항목은 SDK 에 맡깁니다
        if (url.contains("history_back") || url.contains("close_view") || url.contains("open_new_window")) {
            return false
        }

        // 매체 앱에서 처리
        val uri = Uri.parse(url)
        Log.d(TAG, "lucky event scheme: host=${uri.host}, url=$url")
        return true
    }
}
```

```java
public class LuckyEventSchemeHooker {
    public boolean adEventWebViewNavigated(FragmentActivity ac, String url) {
        if (!url.startsWith("tnkscheme:")) return false;

        // SDK 기본 처리 항목은 SDK 에 맡깁니다
        if (url.contains("history_back") || url.contains("close_view") || url.contains("open_new_window")) {
            return false;
        }

        // 매체 앱에서 처리
        Uri uri = Uri.parse(url);
        Log.d(TAG, "lucky event scheme: host=" + uri.getHost() + ", url=" + url);
        return true;
    }
}
```

| 항목 | 규칙 |
| --- | --- |
| 메서드 이름 | 정확히 `adEventWebViewNavigated` |
| 파라미터 | `(FragmentActivity, String)` — 순서와 타입 모두 일치 |
| 반환형 | `Boolean` (Java 는 `boolean`) |
| 생성자 | 인자 없는 생성자 |

<span style="color:red">SDK 는 이 메서드를 이름으로 찾아 호출합니다.</span>
이름이나 시그니처가 조금이라도 다르면 **오류 없이 호출되지 않습니다.**

> 호출될 때마다 **새 인스턴스를 생성**합니다.
> 호출 사이에 유지할 상태가 있다면 `companion object`(Java 는 `static`)에 보관하세요.

### 2. 후커 클래스 등록

#### Method

- 스킴 URL 을 전달받을 후커 클래스를 등록합니다.
- `TnkEventBuilder TnkEventBuilder.setHookerClass(ac: Activity, iClass: Class<*>)`

#### sample

```kotlin
TnkEventActivity.TnkEventBuilder()
    .setUserName("tnk_test")
    .setEventIdTnkAppId("25120101", "00000000-0000-0000-0000-000000000000")
    .setHookerClass(this@MainActivity, LuckyEventSchemeHooker::class.java)
    .show(this@MainActivity)
```

> **등록은 앱에 저장되어 유지됩니다.**
> 한 번 등록하면 앱을 다시 켜도, `setHookerClass` 없이 여는 화면이나 scheme 으로 진입한 화면에도 적용됩니다.
> 해제하는 API 는 없으므로, 처리하지 않을 URL 은 `false` 를 반환해 주세요.

> **scheme 진입만 사용하는 매체**는 앱 첫 화면 등에서 빌더로 `setHookerClass` 만 호출해 두세요. (`show` 는 호출하지 않아도 됩니다)
> 앱 설치 후 한 번도 등록하지 않은 상태에서는 URL 이 전달되지 않습니다.

### 3. release 빌드 ProGuard 규칙 추가

```
-keep class com.your.package.LuckyEventSchemeHooker {
    public <init>();
    public boolean adEventWebViewNavigated(androidx.fragment.app.FragmentActivity, java.lang.String);
}
```

<span style="color:red">이 규칙이 없으면 release 빌드에서만 동작하지 않습니다.</span>
앱 코드에서 직접 호출하지 않는 메서드라 R8 이 제거하거나 이름을 바꿉니다.
디버그 빌드에서 확인했더라도 **난독화된 release 빌드로 반드시 다시 확인**해 주세요.

### SDK 기본 처리 항목

후커가 `false` 를 반환하면 SDK 가 아래와 같이 처리합니다.

| URL | SDK 동작 |
| --- | --- |
| `tnkscheme://history_back` | 이전 페이지로 이동. 이전 페이지가 없으면 화면 닫기 |
| `tnkscheme://close_view` | 이벤트 화면 닫기 |
| `tnkscheme://open_new_window?url=...` | `url` 을 외부 브라우저·앱으로 열기 |
| `market:` · `intent:` | 스토어 또는 해당 앱 실행 |

<span style="color:red">위 항목에 `true` 를 반환하면 뒤로가기·닫기·외부 링크가 동작하지 않습니다.</span>

### iOS 와의 차이

| 항목 | Android | iOS |
| --- | --- | --- |
| 전달 범위 | 웹뷰가 이동하는 URL 대부분 (`http`·`https` 포함) | SDK 기본 처리 항목을 제외한 `tnkscheme://` URL 만 |
| 처리 여부 | 반환값으로 매체가 결정 | 전달만 받음 |
| 등록 | `setHookerClass` (앱에 저장되어 유지) | `REVUPDelegate` 구현 |

**1번 샘플처럼 걸러내면 두 플랫폼이 같은 URL 을 받습니다.**

<br/>

## 광고 재생 차단 (`1.0.7` 이상)

앱 상태에 따라 **복권 화면 안의 광고 재생을 매체가 막을 수 있습니다.**
음악·통화처럼 소리가 겹치면 안 되는 상황을 위한 기능입니다.

설정하지 않으면 지금까지와 동일하게 항상 재생됩니다.

### 동작

```
카드 클릭 → 이벤트 페이지가 SDK 에 물어봄 → 매체 판단
                                              ├─ 재생 가능 → 광고 재생
                                              └─ 재생 불가 → 페이지가 진행을 멈추고 안내 표시
```

**차단 시 광고를 요청하지 않고 화면도 넘어가지 않습니다.** 카드는 그대로 남습니다.

### 방법 1 — Application 이 구현

```java
public class MyApplication extends Application implements ICanPlayRv {
    @Override
    public LuckyEventCanPlayAdInfo isLuckyEventCanPlayAd() {
        boolean playing = myPlayer.isPlaying();
        // 두 번째 인자는 재생 불가 사유 문구입니다. 비워 두면 페이지 기본 문구가 쓰입니다
        return new LuckyEventCanPlayAdInfo(!playing, "음악 재생 중에는 광고를 볼 수 없어요");
    }
}
```

### 방법 2 — 람다로 등록

광고를 별도 모듈로 분리해 `Application` 에서 SDK 타입을 볼 수 없는 경우에 씁니다.

```kotlin
// Application.onCreate() 에서 등록하세요
RevupLuckyEvent.setCanPlayRv {
    LuckyEventCanPlayAdInfo(!myPlayer.isPlaying, "음악 재생 중에는 광고를 볼 수 없어요")
}
```

<span style="color:red">등록은 반드시 `Application.onCreate()` 에서 하세요.</span>
복권 화면은 스킴으로도 열립니다. `Activity` 에서 등록하면, 프로세스가 죽었다 살아나는
경로에서 그 `Activity` 가 만들어지지 않아 등록이 비어 있을 수 있습니다.

### 호출 규약

| 항목 | 내용 |
| --- | --- |
| 시점 | 광고를 띄우기 직전 (미리 로드할 때는 묻지 않습니다) |
| 스레드 | 항상 메인 스레드 |
| 반환 | **즉시 반환해야 합니다** |

<span style="color:red">네트워크 호출이나 락 대기를 하면 ANR 로 이어집니다.</span>
로컬 상태 조회만 해주세요. 예외를 던지면 차단으로 처리합니다.

판정은 **재생 시작 시점 1회**입니다. 광고가 시작된 뒤 음악을 켜도 중간에 끊지 않습니다.
재생 중인 광고를 끊으면 리워드 지급 판정이 꼬이기 때문입니다.

### 안내 문구

문구를 화면에 표시하는 것은 **이벤트 웹 페이지**입니다. SDK 는 매체가 정한 문구를 전달만 합니다.

| 순위 | 값 |
| --- | --- |
| 1 | 판단할 때마다 실어 보낸 `LuckyEventCanPlayAdInfo` 의 문구 |
| 2 | `setCannotPlayRvMessage()` 로 설정해 둔 문구 |
| 3 | 둘 다 없으면 **페이지 기본 문구** |

```java
// 앱 단위로 한 번만 설정하면 됩니다. 앱을 다시 켜도 유지됩니다
RevupLuckyEvent.setCannotPlayRvMessage(context, "음악 재생 중에는 광고를 볼 수 없어요");

// 설정하지 않았으면 빈 문자열을 반환합니다 (null 아님)
RevupLuckyEvent.getCannotPlayRvMessage(context);
```

### Method

- 재생 가능 여부를 판단할 콜백을 등록합니다. `null` 을 넣으면 해제됩니다.
- `void RevupLuckyEvent.setCanPlayRv(ICanPlayRv callback)`

- 재생 불가 안내 문구를 설정합니다. `null` 이나 공백이면 설정이 지워집니다.
- `void RevupLuckyEvent.setCannotPlayRvMessage(Context context, String message)`

- 설정된 문구를 반환합니다. 미설정이면 `""` 입니다.
- `String RevupLuckyEvent.getCannotPlayRvMessage(Context context)`
