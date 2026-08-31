# Adiscope → Revup 마이그레이션 (revup_shim)

기존 Adiscope SDK 로 연동된 앱을 **호출부를 고치지 않고** Revup 으로 교체하기 위한 호환 모듈입니다.

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

## 연동

**build.gradle(app)**

```groovy
dependencies {
    Dependency revupBom = platform("com.tnkfactory.revup:revup-bom:1.0.6")
    implementation revupBom

    implementation "com.tnkfactory.revup:revupCore"
    implementation "com.tnkfactory.revup:revupAndroid"

    // [optional] adiscope 호환 계층
    // BOM 관리 대상이 아니므로 버전을 직접 적어야 하며, BOM 버전과 같은 값을 사용합니다.
    implementation "com.tnkfactory.revup:revup_shim:1.0.6"
}
```

<span style="color:red">`revup_shim` 은 BOM 이 버전을 고정하지 않습니다.</span>
버전을 생략하면 해석에 실패하고, BOM 과 다른 값을 적으면 코어 모듈 버전이 어긋납니다.
**BOM 을 올릴 때 이 줄도 같이 올려 주세요.**

`revup_shim` 은 `revupCore` · `revupAndroid` · `revupLuckyEvent` 를 함께 가져옵니다.

> SDK 를 사내 라이브러리 모듈로 한 번 감싸서 배포하는 구조라면, 그 모듈에서는
> `implementation` 이 아니라 `api` 로 선언해야 앱 쪽에서 옛 클래스가 보입니다.

<br/>

## 제공하는 API

옛 클래스 이름과 시그니처를 그대로 유지하고, 내부에서 Revup 구현으로 위임합니다.
**앱 코드를 고칠 필요가 없습니다.**

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

## 제공하지 않는 것 — 오퍼월

<span style="color:red">Revup 에는 오퍼월 API 가 없습니다.</span>

```java
AdiscopeSdk.getOfferwallAdInstance(activity);   // UnsupportedOperationException
```

`OfferwallAd` 인터페이스는 **컴파일이 깨지지 않도록 형태만 유지**되어 있고,
인스턴스를 얻는 시점에 예외를 던집니다. 즉 빌드는 통과하고 실행 중에 실패합니다.

오퍼월을 사용 중이었다면 교체 전에 담당자와 협의해 주세요.

<br/>

## ProGuard

별도 설정이 필요 없습니다. 옛 패키지명이 난독화되면 기존 바이너리가 링크되지 않으므로,
모듈이 아래 규칙을 스스로 포함해 배포합니다.

```proguard
-keep class com.adiscope.** { *; }
-keep class com.nps.adiscope.** { *; }
```

<br/>

## 걷어내기

호환 계층은 마이그레이션 기간용입니다. 제공하는 클래스는 모두 `@Deprecated` 이므로
IDE 경고를 따라 호출부를 Revup API 로 옮길 수 있습니다.

1. 앱 코드의 `com.nps.adiscope.*` 참조를 Revup API 로 교체
2. 서드파티 SDK 가 옛 클래스를 참조하지 않는지 확인
3. `revup_shim` 의존성 제거

3번을 먼저 하면 앞서 설명한 `NoClassDefFoundError` 가 재현됩니다. **순서를 지켜 주세요.**

<br/>

## 참고

* [API Documentation](./api_documentation.md)
* [Error Information](./error_info.md)
* [Third Party Event](./event_guide.md)
