# Release Note

REVUP Android SDK 라이브 배포 이력입니다.

---

## 1.0.11 — 2026.11.18

- **앱의 R8 최적화 효과 개선 — SDK 가 넣던 ProGuard keep 규칙 대폭 축소**
  - SDK 가 자기 패키지 전체와 리소스 ID 클래스(R)까지 통째로 keep 하던 규칙을, 실행 중 이름으로 찾는
    대상만 남기도록 줄였습니다
  - 저희 샘플 앱 기준 R8 Configuration Analyzer 점수(shrink/optimize/obfuscate)가 약 37% → 42% 로 올랐습니다
  - TNK RWD SDK 를 함께 쓰는 앱은 RWD 8.09.44 이상과 함께 쓰면 TNK SDK 코드도 축소·난독화됩니다
  - 예전 가이드의 `-keep class com.tnkfactory.** { *; }` 가 앱 규칙에 있다면 지워 주세요.
    **매체에서 ProGuard 규칙을 추가하실 필요는 없습니다**
  - 복권 화면 웹뷰 스킴 후커 클래스의 keep 규칙도 SDK 가 포함합니다. 앱에 추가해 둔 규칙은 그대로 두어도 됩니다
- **SDK 팝업과 함께 리워드 전면(RI) 광고를 띄우면 앱이 종료되던 문제 해결**
  - `showWithPopup` 으로 리워드 전면 광고를 띄우면 팝업 레이아웃을 찾지 못해 앱이 종료되었습니다
  - 1.0.4 ~ 1.0.10 에서 발생합니다. 해당 기능을 쓰는 앱은 업데이트를 권장합니다
- **복권(LuckyEvent) 화면을 연 뒤 앱의 동영상·전면 광고 콜백이 오지 않던 문제 해결**
  - 복권 화면을 한 번 열고 닫으면, 이후 앱에서 직접 띄운 보상형 동영상·전면 광고의 로드·보상·닫힘 콜백이
    앱으로 전달되지 않았습니다 (로딩이 끝나지 않거나 보상 처리가 누락될 수 있었습니다)
- **복권(LuckyEvent) 화면 제어 API 추가** — [이벤트 가이드](./event_guide.md#이벤트-화면-종료)
  - `RevupLuckyEvent.closeLuckyEvent()` — 열린 복권 화면을 닫습니다. 위에서 광고가 재생 중이면 함께 닫히며 보상은 지급되지 않습니다
  - `RevupLuckyEvent.isLuckyEventShowing()` — 복권 화면이 열려 있는지 확인합니다
  - `RevupLuckyEvent.setStateListener(...)` — 화면 열림·닫힘을 받습니다. 닫힌 경로와 관계없이 화면당 한 번 호출됩니다
  - 복권 화면이 열린 상태에서 다시 열면(딥링크 등) 기존 화면을 닫고 새 화면을 띄웁니다
  - 복권 화면이 바뀌는 사이 광고가 화면 뒤에 남아 다시 볼 수 없던 경우, 남아 있던 광고 화면을 앞으로 가져옵니다
- **adiscope 호환 모듈(`revup_shim`) 개선** — [마이그레이션 가이드](./adiscope_migration.md)
  - 기존 adiscope Kotlin 코드가 nullable 값(`Activity?`, `String?` 등)을 넘기면 컴파일되지 않던 문제를 고쳤습니다
  - null 이 넘어와도 앱이 종료되지 않고 실패 콜백(`INVALID_REQUEST`)을 돌려줍니다
  - `getOfferwallAdInstance()` 가 예외를 던져 앱이 종료되던 문제를 고쳤습니다. 오퍼월은 지원하지 않으므로 `show` 는 실패 콜백을 돌려줍니다
- 네트워크 SDK 버전은 1.0.10 과 같습니다(TNK PUB 만 7.25.11 → **7.25.19**). 어댑터는 ProGuard 규칙 변경으로 반복번호를 `.11` 로 올렸습니다.
  **어댑터 `.11` 은 SDK 1.0.11 과 함께 써야 하므로 BOM 으로 받아 주세요**

| 어댑터 | 네트워크 SDK | 어댑터 버전 |
|--------|--------------|-------------|
| admob | `play-services-ads` 25.5.0 | 25.5.0.9 → **25.5.0.11** |
| chartboost | `chartboost-sdk` 9.14.1 | 9.14.1.9 → **9.14.1.11** |
| pangle | `pag-sdk` 8.3.0.4 | 8.3.0.4.9 → **8.3.0.4.11** |
| vungle | `vungle-ads` 7.7.8 | 7.7.8.9 → **7.7.8.11** |
| max | `applovin-sdk` 13.6.4 | 13.6.4.9 → **13.6.4.11** |
| tnkpub | 7.25.11 → **7.25.19** | 7.25.11.9 → **7.25.19.11** |

```groovy
implementation platform("com.tnkfactory.revup:revup-bom:1.0.11")
```

---

## 1.0.10 — 2026.11.04

- 32비트(armeabi-v7a) 기기에서 보상형 동영상 시청 후 앱이 종료되던 문제 해결
  - AppLovin MAX 등 클라이언트에서 보상을 판정하는 네트워크로 시청을 마쳤을 때 일부 비율로
    발생했으며, 이 경우 해당 시청 건의 보상이 지급되지 않았습니다
  - 64비트 기기에는 영향이 없었습니다
  - 32비트 기기 사용자가 있는 앱은 업데이트를 권장합니다
- 어댑터 버전은 1.0.9 와 같습니다. BOM 버전만 `1.0.10` 으로 올려 주세요

---

## 1.0.9 — 2026.10.02

- ⚠️ minSdkVersion 24 로 상향
- R8 full mode(AGP 9) 릴리즈 빌드가 시작 직후 죽던 문제 해결
- Play Console 의 'DEX 코드 최적화' 점수 개선을 위해 SDK 가 넣던 ProGuard keep 규칙 축소
- AppLovin MAX 및 네트워크 SDK 최신화

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
