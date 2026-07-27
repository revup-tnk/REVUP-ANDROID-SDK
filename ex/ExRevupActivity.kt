package com.nps.adiscope.sampleapp.activity.ex

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.nps.adiscope.sampleapp.R
import com.nps.adiscope.sampleapp.activity.MainActivity
import com.tnkfactory.revup.walnut.vad.VAdLogger
import com.tnkfactory.revup.walnut.vad.activity.VAdAdiscopeAdListener
import com.tnkfactory.revup.walnut.vad.activity.VAdAdiscopeDelegate
import com.tnkfactory.revup.walnut.vad.model.AdLoadState
import com.tnkfactory.revup.walnut.vad.model.AdType

class ExRevupActivity : AppCompatActivity() {
    val viewModel: ExRevupViewModel by viewModels()

    val revupDelegate by lazy {
        VAdAdiscopeDelegate(this, this, viewModel, listener)
    }

    /**
     광고 이벤트 콜백을 받을 수 있는 리스너
     - 필요한 함수만 오버라이딩하여 이벤트에 따라 별도 처리할 수 있음
    */
    val listener = object : VAdAdiscopeAdListener {}

    /**
    유저 식별값
    - 유저를 식별할 수 있는 값을 Revup 이니셜라이즈 시 설정해줌
    - 원활한 리워드 지급을 위해 필수적으로 유저 고유 식별값을 설정해줘야함
     */
    val userId: String by lazy { "test_user" }

    /**
    어린이 여부
    - 만 13세(또는 거주 국가에서 적용되는 적정 연령) 미만 어린이가 접근 가능한 매체인 경우 childYN 별도 설정이 필요함
    - 해당 계정의 유저 나이가 만 13세 미만이면 "YES", 이상이면 "NO"
    - 13세 미만의 앱에서 유저 나이 미설정(null) 혹은 "YES"로 설정할 경우 제한된 네트워크 물량만 노출될 수 있음
     */
    val childYN: String by lazy { "NO" }

    private var btnShow: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ex_revup)
        btnShow = findViewById<TextView>(R.id.tv_revup_show).apply {
            setOnClickListener {
                viewModel.showInterstitial()
            }
        }

        VAdLogger.isLogEnabled = true

        revupDelegate.setOnLoadStateChanged { adType, _, state ->
            if (adType == AdType.INTERSTITIAL) {
                btnShow?.text = when (state) {
                    AdLoadState.LOADING -> "..."
                    else -> "광고 보기"
                }
            }
        }

        revupDelegate.observeViewModel()

        // Revup 제공 기본 로딩 다이얼로그 설정
        revupDelegate.setLoadingDialog()

        // 이미 적용된 로딩 다이얼로그가 있을 경우 해당 다이얼로그로 로딩 상태에 따라 다이얼로그를 보여줌
        // revupDelegate.setLoadingDialog(CustomLoadingDialog())

        // Revup 제공 기본 다이얼로그 가시성 및 UI 커스터마이징
        setRevupDefaultDialogConfig()

        // Revup 이니셜라이즈 후 런타임 중 계정 변경 발생 시 (ex. A 계정 로그아웃 -> B 계정 로그인 등)
        // 아래 함수로 userId, childYN 재설정이 필요함
        // RevupSdk.setUserId(userId)
        // RevupSdk.getOptionSetterInstance(activity).setChildYN(childYN)
        viewModel.initAdiscopeSdk(this, userId, childYN) { isSuccess ->
            if (isSuccess) {
                VAdLogger.d(TAG, "광고 SDK 초기화 성공")
                // 광고 사용 예제
                loadAdExample()
            } else {
                VAdLogger.d(TAG, "광고 SDK 초기화 실패")
                revupDelegate.showDialog(
                    message = "현재 광고를 이용할 수 없습니다.\n잠시후 다시 시도하세요.",
                    iconRes = com.tnkfactory.revup.sdk.R.drawable.nps_ic_error,
                    positiveClick = {
                        startActivity(
                            Intent(
                                this@ExRevupActivity,
                                MainActivity::class.java
                            )
                        )
                    }
                )
            }
        }
    }

    /**
     * 광고 로드 및 표시 사용 예제
     */
    private fun loadAdExample() {
        // === 방법 1: 미리 로드만 해두고 나중에 표시 (권장) ===
        viewModel.loadRV(RV_UNIT_ID)
        viewModel.loadInterstitial(INTER_UNIT_ID)

        // ... 나중에 원하는 시점에 (예: 버튼 클릭, 게임 클리어 등)
        // viewModel.showRV()  // unitId 없이 호출 - 마지막 로드된 광고 표시
        // viewModel.showInterstitial()

        // === 방법 2: 즉시 로드하고 표시 (기존 방식) ===
        // viewModel.showRV(RV_UNIT_ID)  // 로드되어 있으면 바로 표시, 없으면 로드 후 자동 표시
        // viewModel.showInterstitial(INTER_UNIT_ID)

        // === 방법 3: 다른 unitId로 즉시 표시 ===
        // viewModel.showRV("RV1")  // 다른 광고 유닛 사용
    }

    private fun setRevupDefaultDialogConfig() {
        viewModel.applyDialogConfig {
            setVisible(true)
            setButtonColor(BUTTON_COLOR.toColorInt())
            setTitleColor(getColor(R.color.nps_label_normal))
            setMessageColor(getColor(R.color.nps_label_normal))
            setAutoShow()
            setAutoRewardOnInterClosed(rewardType = "팝콘", amount = 100)
            setOnClick {
                startActivity(
                    Intent(
                        this@ExRevupActivity,
                        MainActivity::class.java
                    )
                )
            }
        }
    }

    companion object {
        const val TAG = "ExRevupActivity"

        // Revup으로부터 전달받은 리워드 광고 유닛명으로 교체
        const val RV_UNIT_ID = "RV1"
        // Revup으로부터 전달받은 인터스티셜 광고 유닛명으로 교체
        const val INTER_UNIT_ID = "IT1"

        const val BUTTON_COLOR = "#3894ff"
    }
}
