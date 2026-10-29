# MAX Ad Review

AppLovin MAX 로 노출된 광고 소재를 수집해 검토·차단할 수 있게 해 주는 AppLovin 의 Gradle 플러그인입니다.
`max` 어댑터를 사용하는 매체에 한해 적용하며, **선택 사항**입니다. 적용하지 않아도 레브업 광고 노출·보상에는 영향이 없습니다.

**AppLovin 원문 가이드**
- [MAX Android Integration — Enable Ad Review](https://support.applovin.com/en/max/android/overview/integration)
- [Ad Review FAQ](https://support.applovin.com/en/max/ad-review/ad-review-faq)

이 문서는 원문을 레브업 연동 기준으로 정리한 것입니다. 플러그인 자체의 동작과 옵션은 AppLovin 원문이 기준입니다.

<br/>

## 1. Ad Review 키 받기

**레브업 측에 Ad Review 키를 요청합니다.** AppLovin 대시보드에서 직접 발급받은 키는 사용하지 않습니다.

레브업을 통한 MAX 광고는 레브업의 AppLovin 계정으로 송출됩니다. 소재 데이터가 노출과 연결되려면 같은 계정의 Ad Review 키가 필요합니다.

<br/>

## 2. 플러그인 저장소 추가

레브업 저장소(`https://repository.tnkad.net/repository/public/`)가 AppLovin 저장소(`https://artifacts.applovin.com/android`)를 미러링하고 있습니다.
레브업 연동에 이미 쓰고 있는 이 저장소 하나로 플러그인을 받을 수 있습니다. 외부 접속이 제한된 빌드 환경이라면 레브업 저장소를 사용하세요.

아래 두 방식 중 하나를 사용합니다.

**A) `buildscript` 방식 (AppLovin 원문 방식)**

**build.gradle(root)**
```groovy
buildscript {
    repositories {
        maven { url "https://repository.tnkad.net/repository/public/" }
        // 또는 AppLovin 저장소를 직접 사용
        // maven { url "https://artifacts.applovin.com/android" }
    }
    dependencies {
        classpath "com.applovin.quality:AppLovinQualityServiceGradlePlugin:+"
    }
}
```

**B) `plugins` 방식 (Gradle 7 이상)**

**settings.gradle**
```groovy
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url "https://repository.tnkad.net/repository/public/" }
    }
}
```

**build.gradle(root)**
```groovy
plugins {
    id 'com.applovin.quality' version '+' apply false
}
```

> 버전 `+` 는 빌드할 때마다 최신 플러그인을 받습니다. 빌드를 재현 가능하게 유지하려면 버전을 고정하세요(문서 작성 시점 최신: `5.12.12`).

<br/>

## 3. 앱 모듈에 플러그인 적용

전달받은 Ad Review 키를 `apiKey` 에 넣습니다. 위 2번에서 어느 방식을 썼든 동일합니다.

**build.gradle(app) — Groovy**
```groovy
apply plugin: 'applovin-quality-service'

applovin {
    apiKey "전달받은 Ad Review 키"
}
```

**build.gradle.kts(app) — Kotlin DSL**
```kotlin
plugins {
    id("applovin-quality-service")
}

applovin {
    apiKey = "전달받은 Ad Review 키"
}
```

<br/>

## 4. debug 빌드에서 끄기 (권장)

Ad Review 는 빌드 시간을 늘립니다. AppLovin 은 debug 빌드에서는 꺼 두는 것을 권장합니다. `applovin` 블록에 추가합니다.

```groovy
applovin {
    apiKey "전달받은 Ad Review 키"
    disableIf buildType: 'debug'
    // product flavor 단위: disableIf productFlavor: 'foo'
}
```

```kotlin
applovin {
    apiKey = "전달받은 Ad Review 키"
    disableIf(mapOf("buildType" to "debug"))
    // product flavor 단위: disableIf(mapOf("productFlavor" to "foo"))
}
```

<br/>

## 5. 빌드 환경 확인

- **네트워크**: 플러그인은 빌드 중에 `api2.safedk.com`(HTTPS, 443)에 접속합니다(Ad Review 키 확인, SDK 정보 업로드).
  이 접속이 막혀 있으면 Ad Review 를 이용할 수 없습니다. CI 등 외부 접속이 제한된 환경에서는 이 주소를 허용해야 합니다.
  플러그인 저장소 미러링으로는 대신할 수 없습니다.
  - 4번에서 끈 빌드 타입을 빌드할 때도 접속을 시도합니다.
- **Gradle configuration cache**: 플러그인이 지원하지 않습니다. Ad Review 를 켠 빌드(release 등)는 configuration cache 를 끄고 빌드해야 합니다.

<br/>

## 6. 알려진 문제

### Jetpack Compose + Kotlin 2.3 이상에서 release 빌드 실패

Kotlin 2.3 의 Compose 컴파일러는 Compose 매핑을 R8 의 `mapping.txt` 에 덧붙이는 `merge<Variant>ComposeMapping` 작업을 추가합니다.
Ad Review 플러그인의 `safedkInstrumentation<Variant>` 작업은 이 파일을 읽지만 작업 의존성을 선언하지 않아, 아래 오류로 빌드가 실패할 수 있습니다.

```
Task ':app:safedkInstrumentation<Variant>' uses this output of task ':app:merge<Variant>ComposeMapping'
without declaring an explicit or implicit dependency.
```

AppLovin 플러그인에서 수정되기 전까지는 앱 모듈의 build.gradle 에 아래 코드를 추가해 의존성을 직접 연결합니다.
모든 variant 에 적용되며, Compose 를 쓰지 않는 앱에서는 아무 동작도 하지 않습니다.

**build.gradle(app) — Groovy**
```groovy
// AppLovin Ad Review 플러그인이 Compose 매핑 작업 의존성을 선언하지 않는 문제 우회
tasks.matching { it.name.startsWith('safedkInstrumentation') }.configureEach { task ->
    def variant = task.name - 'safedkInstrumentation'
    task.dependsOn(tasks.matching { it.name == "merge${variant}ComposeMapping" })
}
```

**build.gradle.kts(app) — Kotlin DSL**
```kotlin
// AppLovin Ad Review 플러그인이 Compose 매핑 작업 의존성을 선언하지 않는 문제 우회
tasks.matching { it.name.startsWith("safedkInstrumentation") }.configureEach {
    val variant = name.removePrefix("safedkInstrumentation")
    dependsOn(tasks.matching { it.name == "merge${variant}ComposeMapping" })
}
```

관련 이슈: [AppLovin-MAX-Ad-Review-SDK-Android #17](https://github.com/AppLovin/AppLovin-MAX-Ad-Review-SDK-Android/issues/17)

<br/>

## 7. 적용 확인

- Ad Review 를 켠 빌드(release 등)의 빌드 로그에서 `safedkInstrumentation<Variant>` 작업이 오류 없이 실행되는지 확인합니다.
- 소재 수집 여부는 레브업 담당자에게 확인을 요청해 주세요.

<br/>

## 제거

2번과 3번에서 추가한 코드를 지우면 됩니다.
