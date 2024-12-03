package com.caicai.kEventBus_demo

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.caicai.kEventBus_demo.fragment.ContactFragment
import com.caicai.kEventBus_demo.fragment.MenuFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // 获取 Fragment 容器的引用
        val menuContainer = findViewById<FrameLayout>(R.id.fragment_menu_container)
        val listContainer = findViewById<FrameLayout>(R.id.fragment_list_container)

        // 添加 Fragment 到容器中
        addFragment(menuContainer, MenuFragment())
        addFragment(listContainer, ContactFragment())
    }

    private fun addFragment(frameLayout: FrameLayout, fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(frameLayout.id, fragment)
        fragmentTransaction.commit()
    }
}