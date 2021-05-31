package qoi.myhealth.View.Activity.Setting.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeFragment: Fragment() {

    private var group:ViewGroup? = null

    companion object {
        fun createInstance() : HomeFragment {
            val fragmentMain =
                HomeFragment()
            return fragmentMain
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        group = container
        return inflater.inflate(qoi.myhealth.R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}