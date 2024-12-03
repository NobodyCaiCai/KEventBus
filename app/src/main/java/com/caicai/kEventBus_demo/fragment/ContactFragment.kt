package com.caicai.kEventBus_demo.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.caicai.KEventBus.EventBus
import com.caicai.KEventBus.Subscriber
import com.caicai.KEventBus.ThreadMode
import com.caicai.kEventBus_demo.R
import com.caicai.kEventBus_demo.bean.User

class ContactFragment : BaseFragment() {

    companion object {
        private val TAG = ContactFragment::class.java.simpleName
    }

    private var mListAdapt: ListAdapt? = null
    private var mRecycleView: RecyclerView? = null
    private val mContacts = ArrayList<User>(100)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.list_fragment, container, false)
        mRecycleView = rootView.findViewById(R.id.recyclerView)
        mockDates()
        initRecycleView()
        EventBus.getDefault().register(this)
        return rootView
    }

    private fun mockDates() {
        for (i in 0..5) {
            mContacts.add(User("user -$i"))
        }
    }

    private fun initRecycleView() {
        mListAdapt = ListAdapt(mContacts)
        mListAdapt?.setOnItemClickListener {
            Toast.makeText(context, "${mContacts[it]}", Toast.LENGTH_LONG).show()
            EventBus.getDefault().post(mContacts[it], MenuFragment.CLICK_TAG)
        }
        mRecycleView?.layoutManager = LinearLayoutManager(context)
        mRecycleView?.adapter = mListAdapt
    }

    @Subscriber
    private fun onReceiveInt(num: Int) {
        Toast.makeText(context, "onReceiveInt mun: $num", Toast.LENGTH_SHORT).show()
    }

    @Subscriber
    private fun onReceiveIntArray(array: IntArray) {
        Toast.makeText(context, "onReceiveIntArray: ${array.toList()}", Toast.LENGTH_SHORT).show()
    }

    @Subscriber
    private fun onReceiveBoolean(bo: Boolean) {
        Toast.makeText(context, "onReceiveBoolean: $bo", Toast.LENGTH_SHORT).show()
    }

    @Subscriber
    @SuppressLint("NotifyDataSetChanged")
    private fun addPerson(user: User) {
        Toast.makeText(context,"addPerson", Toast.LENGTH_SHORT).show()
        mContacts.add(user)
        mListAdapt?.notifyDataSetChanged()
    }

    @Subscriber(tag = MenuFragment.REMOVE_TAG)
    @SuppressLint("NotifyDataSetChanged")
    private fun removePerson(user: User) {
        Toast.makeText(context,"removePerson", Toast.LENGTH_SHORT).show()
        mContacts.remove(user)
        mListAdapt?.notifyDataSetChanged()
    }

    @Subscriber(tag = MenuFragment.ASYNC_TAG, threadMode = ThreadMode.ASYNC)
    private fun onReceiveAsyncTag(user: User) {
        try {
            val name = Thread.currentThread().name
            Log.i(TAG, "onReceiveAsyncTag: $name")
            mRecycleView?.post {
                Toast.makeText(context, "curThreadName: $name", Toast.LENGTH_LONG).show()
            }
            Thread.sleep(5 * 1000)
        } catch (e: Exception) {
            Log.i(TAG, "error message: $e")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    class ListAdapt(private val userList: ArrayList<User>) :
        RecyclerView.Adapter<ItemViewHolder>() {
        private var onItemClickListener: ((Int) -> Unit)? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
            return ItemViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userList.size
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val user = userList[position]
            holder.item.text = user.name
            holder.item.setOnClickListener {
                onItemClickListener?.invoke(position)
            }
            Log.d(TAG, "onBindViewHolder: position = $position")
        }

        fun setOnItemClickListener(listener: (Int) -> Unit) {
            onItemClickListener = listener
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val item: TextView = view.findViewById(R.id.item_text)
    }
}