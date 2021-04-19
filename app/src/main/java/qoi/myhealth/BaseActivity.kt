package qoi.myhealth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.Ble.C18.C18Delegate
import qoi.myhealth.Ble.ServiceUUID

// デバイスアクティビティーの基底クラス
abstract class BaseActivity: AppCompatActivity(), BleDelegate {
    // 発生場所の特定用変数
    protected val TAG = this::class.java.simpleName

    // デバイス接続マネージャー
    protected val bleManager: BleManager = BleManager

    override fun onStart() {
        super.onStart()

        // 接続・切断完了時に呼び出し先の設定
        bleManager.bleDelegate = this

    }
}