## 서드파티 이벤트 제공


### 이벤트 등록

매체 영업팀을 통해 이벤트 아이디와 키값을 발급 받아 사용 합니다.

`eventId`: 이벤트를 식별하는 고유 키값입니다. 예) 10002501 : 룰렛, 10002502 : 복권, 10002503 : 출석

`pubId`: 이벤트매체 식별용 키값입니다.


### 이벤트 모듈 참조 추가
```groovy
implementation "com.tnkfactory.revup:revupLuckyEvent:1.0.4"
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
