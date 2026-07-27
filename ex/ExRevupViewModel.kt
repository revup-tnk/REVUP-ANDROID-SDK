package com.nps.adiscope.sampleapp.activity.ex

import com.tnkfactory.revup.walnut.vad.viewmodel.VAdAdiscopeViewModelImpl

/**
 * Revup 광고 SDK를 사용하기 위한 샘플 ViewModel
 *
 * VAdAdiscopeViewModelImpl을 직접 상속받아 사용합니다.
 * - ViewModel lifecycle이 자동으로 관리됩니다
 * - viewModelScope를 사용할 수 있습니다
 * - 필요한 경우 메서드를 오버라이드하여 커스터마이징 가능합니다
 */
class ExRevupViewModel : VAdAdiscopeViewModelImpl()
