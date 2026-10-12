# Release Note

REVUP Android SDK 라이브 배포 이력입니다.

---

## 1.0.9 — 2026.10.02

### ⚠️ minSdkVersion 24 로 상향 — 적용 전 확인해 주세요

`minSdkVersion` 이 23 에서 **24** 로 올라갑니다. **앱의 `minSdkVersion` 이 23 이하면
이 버전을 적용할 수 없습니다.** 23 을 유지해야 한다면 `1.0.8` 을 사용해 주세요.

Meta(FAN) 비더 어댑터와 `play-services-ads 25.5.0` 이 minSdk 24 를 요구하는 것이 직접
원인이며, SDK 전 모듈과 어댑터 6종을 24 로 통일했습니다.

### R8 full mode(AGP 9) 릴리즈 빌드가 시작 직후 죽던 문제 해결

Android Gradle Plugin 9.0 부터 keep 규칙 해석이 엄격해져(`-keep class A` 가 기본 생성자를
더 이상 자동으로 유지하지 않음), 일부 비더가 함께 가져오던 `androidx.work 2.8.1` 의 규칙으로는
`WorkDatabase` 생성자가 제거되어 앱이 `androidx.startup` 단계에서 종료되었습니다.

```
java.lang.RuntimeException: Unable to get provider androidx.startup.InitializationProvider:
  Failed to create an instance of class androidx.work.impl.WorkDatabase
```

`max` 어댑터가 `androidx.work 2.12.0` 을 명시적으로 의존하도록 바꿔 해결했습니다.
**매체에서 ProGuard 규칙을 추가하실 필요는 없습니다.**

### SDK 가 넣던 ProGuard keep 규칙 축소

Play Console 의 'DEX 코드 최적화' 점수 개선을 위해, SDK 가 넣던 과도한 keep 규칙을 좁혔습니다.

- `vungle` 어댑터가 `com.google.android.gms.**` 전체를 keep 하던 규칙을 제거했습니다.
  앱에 `play-services-ads` 만 있어도 5,000 개 이상이 난독화 대상에서 빠지고 있었습니다
- `tnkpub` 어댑터의 `com.tnkfactory.**` keep 을 필요한 패키지로 좁혔습니다
- 코어의 `com.google.android.gms.ads.**` keep 을 ADID 조회에 필요한 `AdvertisingIdClient`
  로 좁혔습니다
- 샘플 앱 기준 난독화되지 않는 클래스가 37,983 개 → 30,428 개로 줄었습니다

> 이 규칙들을 앱에서 무시하도록 처리해 두셨다면, 이 버전 적용 후 원래대로 되돌려 주세요.

### AppLovin MAX 및 네트워크 SDK 최신화

AppLovin MAX SDK 를 `13.6.2` → `13.6.4` 로 올리고 비더 어댑터 18종을 최신화했습니다.
메이저 버전이 바뀐 네트워크는 PubMatic 4 → 5, Bigo Ads 5 → 6, Pangle 7 → 8 입니다.

독립 어댑터가 쓰는 네트워크 SDK 도 MAX 비더 어댑터와 같은 버전으로 맞췄습니다.
같은 네트워크 SDK 가 서로 다른 버전으로 들어오지 않게 하기 위함입니다.

| 어댑터 | 네트워크 SDK |
|--------|--------------|
| admob | `play-services-ads` 25.2.0 → **25.5.0** |
| chartboost | `chartboost-sdk` 9.11.0 → **9.14.1** |
| pangle | `pag-sdk` 7.9.1.3 → **8.3.0.4** |
| vungle | `vungle-ads` 7.7.3 → **7.7.8** |
| max | `applovin-sdk` 13.6.2 → **13.6.4** |

네트워크별 전체 버전은 [README 의 Network Version](../README.md#network-version) 표를
참고해 주세요.

---

## 1.0.8 — 2026.10.01

- `revup_shim` 이 BOM 관리 대상이 되었습니다. 의존성에 버전을 적지 않아도 됩니다
- Kotlin 으로 작성된 호출부도 수정 없이 컴파일됩니다
  (자세한 내용은 [Adiscope Migration](./adiscope_migration.md) 참고)

---

## 1.0.7 이전

`1.0.7` 이전 이력은 별도로 관리되지 않습니다. 필요하시면 담당자에게 문의해 주세요.
