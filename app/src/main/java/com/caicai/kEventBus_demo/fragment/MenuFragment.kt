package com.caicai.kEventBus_demo.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.caicai.KEventBus.EventBus
import com.caicai.KEventBus.Subscriber
import com.caicai.kEventBus_demo.R
import com.caicai.kEventBus_demo.StickyActivity
import com.caicai.kEventBus_demo.bean.StickyUser
import com.caicai.kEventBus_demo.bean.User

import kotlin.random.Random

class MenuFragment : BaseFragment() {

    companion object {
        const val CLICK_TAG = "click_tag"
        const val THREAD_TAG = "thread_tag"
        const val ASYNC_TAG = "async"
        const val REMOVE_TAG = "remove"
        val TAG: String = EventBus::class.java.simpleName
    }

    private var clickTv: TextView? = null

    private var timeTv: TextView? = null

    private var threads: Array<PostThread?> = arrayOfNulls(4)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.menu_fragment, container, false)

        clickTv = rootView.findViewById(R.id.click_tv)
        timeTv = rootView.findViewById(R.id.timer_tv)

        setViewClickListener(rootView, R.id.my_post_button) {
            EventBus.getDefault().post(User("Mr.CaiCai" + Random(100).nextInt()))
        }

        setViewClickListener(rootView, R.id.my_remove_button) {
            EventBus.getDefault().post(User("user -2"), REMOVE_TAG)
        }

        setViewClickListener(rootView, R.id.my_post_async_event_button) {
            EventBus.getDefault().post(User("async-user"), ASYNC_TAG)
        }

        setViewClickListener(rootView, R.id.my_post_list_btn) {
            postListData()
        }

        setViewClickListener(rootView, R.id.my_post_to_supper_btn) {
            EventBus.getDefault().post(User("Super"), SUPER_TAG)
        }

        setViewClickListener(rootView, R.id.my_post_to_thread_btn) {
            EventBus.getDefault().post("Im in Main Thread", THREAD_TAG)
        }

        setViewClickListener(rootView, R.id.post_primitive_btn) {
            EventBus.getDefault().post(12345)
            EventBus.getDefault().post(true)
            EventBus.getDefault().post(intArrayOf(1, 2))
        }

        setViewClickListener(rootView, R.id.post_sticky_tv) {
            EventBus.getDefault().postSticky(StickyUser("sticky 事件"))
            startActivity(Intent(activity, StickyActivity::class.java))
        }

        startThread()

        EventBus.getDefault().register(this)

        return rootView
    }

    private fun startThread() {
        for (i in 0..3) {
            threads[i] = PostThread(i)
            threads[i]?.start()
        }
    }

    private fun postListData() {
        val list = ArrayList<User>()
        for (i in 0..4) {
            list.add(User("user - $i"))
        }
        EventBus.getDefault().post(list)
    }

    @Subscriber
    private fun subscribeUseList(list: ArrayList<User>) {
        Toast.makeText(activity, list.toString(), Toast.LENGTH_LONG).show()
    }

    @Subscriber(tag = CLICK_TAG)
    private fun onReceiveClick(clickPerson: User) {
        clickTv?.text = clickPerson.name
    }

    @Subscriber
    private fun onReceiveString(msg: String) {
        timeTv?.text = msg
    }

//    @Subscriber
//    private fun onReceiveStringInPost(msg: String) {
////        Toast.makeText(activity, "onReceiveStringInPost$msg", Toast.LENGTH_LONG).show()
//    }

    private fun setViewClickListener(rootView: View, resId: Int, clickListener: () -> Unit) {
        rootView.findViewById<View>(resId).setOnClickListener { clickListener.invoke() }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    inner class PostThread(private val mIndex: Int) : Thread() {
        init {
            name = "Thread - $mIndex"
            EventBus.getDefault().register(this)
        }

        @Subscriber(tag = THREAD_TAG)
        private fun sayHello(msg: String) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
        }

        override fun run() {
            while (!interrupted()) {
                val msg = if ((Looper.getMainLooper() == Looper.myLooper())) {
                    "is Main Thread"
                } else {
                    " is Not Main"
                } + "\n" + name

                EventBus.getDefault().post(msg)

                try {
                    sleep((1000 * mIndex + 3000).toLong())
                } catch (e: Exception) {
                    Log.i(TAG, "exception: $e")
                }
            }
        }
    }
}