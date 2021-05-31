package qoi.myhealth.View.Activity.Setting

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.my_toolbar.*
import qoi.myhealth.Ble.BleDelegate
import qoi.myhealth.Ble.BleManager
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.Fragment.AppSettingFragment
import qoi.myhealth.View.Activity.Setting.Fragment.DeviceSettingFragment
import qoi.myhealth.View.Activity.Setting.Fragment.HomeFragment

// デバイスアクティビティーの基底クラス
abstract class BaseActivity: AppCompatActivity(), BleDelegate {
    // 発生場所の特定用変数
    protected val TAG = this::class.java.simpleName

    // デバイス接続マネージャー
    protected val bleManager: BleManager = BleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()?.setCustomView(R.layout.my_toolbar)
        toolbtn.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()

        // 接続・切断完了時に呼び出し先の設定
        bleManager.bleDelegate = this
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            // 戻るボタンが押されたら
            android.R.id.home -> {
                // 現在のActivityを終了する
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun createDeveiceSetting(){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.clActivityMain, DeviceSettingFragment.createInstance())
            commit()
        }
    }

    fun createHome(){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.clActivityMain, HomeFragment.createInstance())
            commit()
        }
    }

    fun createAppSetinng(){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.clActivityMain, AppSettingFragment.createInstance())
            commit()
        }
    }

    // メニューボタンメソッド
    fun selectNavi(){
        bottom_navigation.setOnNavigationItemSelectedListener{
            when(it.itemId){
                // HOMEボタン押下
                R.id.home->{
                    createHome()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.device->{
                    createDeveiceSetting()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.user->{
                    createAppSetinng()
                    return@setOnNavigationItemSelectedListener true
                }
                else->{
                    println(it)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }

}