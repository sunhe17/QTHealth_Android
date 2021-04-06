package qoi.myhealth

import android.app.Application
import qoi.myhealth.Manager.ShareDataManager


class QOIHealthApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ShareDataManager.setUp(this)

    }
}