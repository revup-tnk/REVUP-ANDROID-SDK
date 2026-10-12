# Release Note

REVUP Android SDK 라이브 배포 이력입니다.

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
