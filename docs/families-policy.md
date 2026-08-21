# Revup Google Play 가족 정책 준수하기
- Play의 정책 업데이트에 따라 구글 가족정책 준수 대상 앱인 경우 **2022년 4월 전까지 2.0.9 이상으로 필수 업데이트**해야합니다.
- Revup SDK 2.0.9부터 어린이로 간주될 수 있는 대상의 GAID(Google Advertising ID)를  Zero-out함으로써 퍼블리셔(매체)가 Play 가족 정책을 준수하면서 광고 수익화를 하는데 도움을 주고 있습니다.
- 정책 준수를 위해서는 퍼블리셔가 아래 `적용 순서 2단계, 3단계`를 직접 작업해야하며, 이는 Revup SDK에 의해 자동으로 연동되지 않습니다. 구글 가족정책에 대한 책임은 퍼블리셔에 있습니다.

<br/>

## 적용 순서

### 1단계 : 어드민 설정
- Play Console에 등록된 앱 타켓팅 정보를 담당자에게 전달해야 합니다.
    - 타겟팅 정보는 앱 최초 등록 시 설정됩니다.
    - 최초 등록 후 앱 타겟팅 정보가 변경된 경우 Revup 담당자에게 변경 요청을 해야합니다. [Revup_support](mailto:Revup_support@neowiz.com) 혹은 담당자에게 설정 여부를 확인해주세요.

<br/>

### 2단계 : ["중립적인 연령 심사"](https://support.google.com/googleplay/android-developer/answer/9867159?visit_id=637810911509208708-3309869707&rd=1#neutral-agescreen) 구현하기
- 아동 여부 판단 및 사용자가 연령을 속이지 않도록 생년월일을 자유롭게 입력 요청하는 시스템 사용을 권고합니다. 만약 생년월일을 타겟 연령으로 미리 설정하거나 특정 연령이 되어야 앱에 액세스 가능하다는 유도성 표시는 Google Play 정책에 위반되니 유의 바랍니다.   
  ![birthday](https://github.com/adiscope/Adiscope-Unity-UPM/assets/60415962/4c5f21db-600a-46f3-bf15-c853b923fa31)
- 입력받은 생년월일을 토대로, 한국은 "만 14세 미만", 외 국가는 [거주 국가에서 적용되는 적정 연령](https://support.google.com/accounts/answer/1350409) 기준으로 **유저 단위로 어린이 여부(childYN)를 판단**해야 합니다. 거주 국가별 적정 연령을 구현하기 어려운 경우 만 13세 미만을 기준으로 삼아도 무방합니다.
- 연령 분류 조치 구현에 따른 childYN(어린이 여부)은 다음과 같습니다.
    - 어린에 해당하거나 정보가 없는 경우 : `"YES"` or `Null`
    - 어린이가 아닌 경우(어른인 경우) : `"NO"`
- 유저의 실수로, 어린이임에도 어른으로 설정하여 childYN을 "NO"로 전달하는 경우 Revup은 관여하지 않습니다.

<br/>

### 3단계 : SDK 초기화 시 혹은 광고 유닛 호출 전 유저 정보 설정하기
- SDK 초기화 단계에 유저 정보 설정(`setUserIdChild` 호출)을 권고합니다. (앱 Flow에 적합한 시점으로 골라서 선택하시면 됩니다)
- 다만, 불가피하게 위 시점에 전달이 어려운 경우 광고 유닛(RV, interstitial, RI) 로드 호출 전에 전으로 대체할 수 있으나 광고 수익에 영향이 갈 수 있습니다. (RV 및 Interstitial의 광고 효율 및 커버리지에 영향)

<br/>

## 구현
#### RevupUserType
```kotlin
enum class RevupUserType(val value: Int, val childYN: String) {
    None(0, ""), Adult(1, "NO"), Child(2, "YES");
}
```
- userType은 각각 세가지로 구분하고 있습니다.
  - `None`: 알 수 없음 (유저 나이를 판별할 수 없을 때)
  - `Adult`: 만 13세 이상 유저
  - `Child`: 만 13세 미만의 아동 유저

<br/>

setUserIdChild 호출 시점에 따라 구현 방법은 아래와 같습니다.

<br/>

### SDK 초기화 시
- SDK 초기화 전 유저 정보를 set한 후 초기화를 호출해야합니다.
```java
// none: RevupUserType.None (0)
// adult: RevupUserType.Adult (1)
// child: RevupUserType.Child (2)
RevupUserType userType = RevupUserType.None;
String userId = ""; // set unique user id to identify the user in reward information
RevupSdk.setUserIdChild(userId, userType);
RevupSdk.initialize(this, mediaId, mediaSecret, callbackTag, new RevupInitializeListener() {
    @Override 
    public void onInitialized(boolean isSuccess) {
        if (isSuccess) {
            // Initialized
        } else {
            // Failed to initialize
        }
    });
}
```
<br/>

### 광고 유닛 호출 전
- 광고 유닛(Rewarded Video, Interstitial) 호출 전 유저 정보를 set합니다.
```java
// none: RevupUserType.None (0)
// adult: RevupUserType.Adult (1)
// child: RevupUserType.Child (2)
RevupUserType userType = RevupUserType.None;
String userId = ""; // set unique user id to identify the user in reward information
RevupSdk.setUserIdChild(userId, userType);
```
<br/>

## 동작
- 퍼블리셔에서 Revup으로 전달한 childYN에 따른 Revup 광고 동작은 아래 표와 같습니다.
- 정책에 따라 어린이로 간주되는 대상은 광고 활동에 제약이 있으며, 이로 인해 광고 효율 및 커버리지에 제한적인 영향이 있을 수 있습니다.

|      childYN      | RV(Rewarded Video), Interstitial |
|:-----------------:|:--------------------------------:|
| `"YES"` or `Null` |    구글 인증 네트워크로만 표시, GAID 미전달     |
|      `"NO"`       |       모든 네트워크 표시, GAID 전달        |

<br/>

## Google Play 정책 - Families Policy (구글 가족 정책)
### 정책 준수 대상
- Google Play Console의 타겟층에 만 13세 미만 연령대가 포함된다고 선언한 앱   
  ![googleconsole](https://github.com/adiscope/Adiscope-Unity-UPM/assets/60415962/87f995b1-bbc4-47cf-859d-08b5e3addb3b)   

<br/>

### 주요 요구 사항
#### 내용
- 2022.04
    - 어린이 또는 연령을 알 수 없는 사용자의 Android 광고 ID(AAID), SIM 일련번호, 빌드 일련번호, BSSID, MAC, SSID, IMEI 및/또는 IMSI를 전송해서는 안 됩니다.
- 2019.05
    - 어린이와 그 이상의 연령대 사용자를 모두 타겟팅하는 경우 연령 분류 조치를 구현해야합니다.
    - 어린이에게 표시되는 광고는 [가족을 위한 광고 프로그램(Google Play 인증 광고 SDK)](https://support.google.com/googleplay/android-developer/answer/9283445) 중 하나에서만 표시되도록 해야합니다.

<br/>

#### 관련 링크
- [어린이와 가족을 위한 앱 및 게임 개발하기](https://developer.android.com/google-play/guides/families)
- [가족을 위한 앱 프로그램](https://play.google.com/about/families/)
- [가족 정책 센터](https://play.google.com/about/families/children-and-families/#!?zippy_activeEl=families-policy%23families-policy)
