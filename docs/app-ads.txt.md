# app-ads.txt

## app-ads.txt 등록 방법
- 스토어에 등록되어 있는 개발자 웹사이트의 루트 디렉터리에 app-ads.txt 파일을 업로드 (`www.`는 제외)
    - `https://<hostname>/app-ads.txt`
    - ex) `example.com/app-ads.txt`
- https, http 모두 가능하나 https로 시작하는 웹사이트에 등록을 권장함
    - 일부 네트웍사가 http로 시작하는 웹사이트 통신을 허용하지 않아, app-ads.txt에 접근하지 못하고 광고 load를 실패한 케이스가 존재했음
- 네트웍사에서 24시간 내로 app-ads.txt 파일을 크롤링하고 인증함

<br></br>

## app-ads.txt 란
- 광고를 요청한 앱이 등록된 앱임을 광고주에게 증명하여 인앱 광고 사기를 방지해주는 파일
- IAB Tech Lab에서 지정한 형식을 기반으로 네트웍사가 승인된 앱에 광고 인벤토리를 판매하도록 함
- 즉, 모든 네트웍사가 공통으로 개발자 웹사이트에 있는 app-ads.txt를 크롤링하여 승인받은 광고 물량을 제공해줌
- app-ads.txt 사용 시 광고 사기를 차단해주며 eCPM 개선에 도움이 됨

<br>

<aside>
⚠️ 스토어에 등록되어 있는(출시된) 앱만 적용이 가능하며,
스토어 정보에 개발자 웹사이트가 포함되어 있어야 함
</aside>

<br></br>

## 개발자 웹사이트 등록 혹은 확인 방법
- **AOS**
    - Play Console의 앱 정보 > 스토어 등록정보 > 연락처 세부정보 > 개발자 웹사이트 URL
    - [Play Console | 개발자 페이지 생성 또는 업데이트 문서](https://support.google.com/googleplay/android-developer/answer/9873827?hl=ko)
- **iOS**
    - 스토어 리스트의 마케팅 URL 필드

<br></br>

## 관련 레퍼런스
#### [IAB Tech Lab | ads.txt - Authorized Digital Sellers](https://iabtechlab.com/ads-txt/)
#### [Google AdMob 고객센터 | app-ads.txt 정보](https://support.google.com/admob/answer/9787936?hl=ko&ref_topic=7384409&sjid=15160708219461390133-AP#)
