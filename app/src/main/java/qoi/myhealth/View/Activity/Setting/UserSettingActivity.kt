package qoi.myhealth.View.Activity.Setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_user_setting.*
import kotlinx.android.synthetic.main.my_toolbar.*
import qoi.myhealth.R
import qoi.myhealth.View.Activity.Setting.Fragment.UserSettingFragment

class UserSettingActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting)

        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()?.setCustomView(R.layout.my_toolbar)
        toolbtn.visibility = View.GONE
        backimg.visibility = View.VISIBLE
        head_title.text = "ユーザー情報 設定"

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.UserSettingActivity, UserSettingFragment.getInstance())
            commit()
        }
    }

    override fun onStart() {
        super.onStart()

        backimg.setOnClickListener {
            val intent = Intent(this,SetAccessKeyActivity::class.java)
            startActivity(intent)
        }

        nextimg.setOnClickListener{
            UserSettingFragment.getInstance().saveAppInfoDat()
            val intent = Intent(this, qoi.myhealth.View.Activity.Setting.DeviceScanActivity::class.java)
            startActivity(intent)
        }
    }
}