package com.iptv.player.ui.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.iptv.player.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TvMainActivity : FragmentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_main)
        
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tv_main_container, TvMainFragment())
                .commitNow()
        }
    }
}