## 서드파티 이벤트 제공


### 이벤트 등록

매체 영업팀을 통해 이벤트 아이디와 키값을 발급 받아 사용 합니다.

`eventId`: 이벤트를 식별하는 고유 키값입니다. 예) 10002501 : 룰렛, 10002502 : 복권, 10002503 : 출석

`pubId`: 이벤트매체 식별용 키값입니다.


### 이벤트 모듈 참조 추가
```groovy
implementation "com.tnkfactory.revup:revupLuckyEvent:5.1.6"
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
