package com.caicai.kEventBus_demo.fragment

import android.widget.Toast
import androidx.fragment.app.Fragment

import com.caicai.kEventBus_demo.bean.User
import com.caicai.KEventBus.Subscriber

open class BaseFragment : Fragment() {

    companion object {
        const val SUPER_TAG = "super_tag"
    }

    @Subscriber(tag = SUPER_TAG)
    private fun privateMethodInSuper(user: User) {
        Toast.makeText(context, "sticky message", Toast.LENGTH_SHORT).show()
    }
}