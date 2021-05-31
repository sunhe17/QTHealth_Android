package qoi.myhealth.Controller.Util

import android.app.Application
import qoi.myhealth.Manager.ShareDataManager


class QOIHealthApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ShareDataManager.setUp(this)

    }
}