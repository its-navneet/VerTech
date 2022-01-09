package com.example.android.vertech

import android.graphics.drawable.ClipDrawable.VERTICAL
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.vertech.R
import com.example.android.vertech.db.UserEntity
import kotlinx.android.synthetic.main.activity_todo.*

class todo : AppCompatActivity(),RecyclerViewAdapter.RowClickListener {
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    lateinit var viewModel: TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)
        supportActionBar?.hide()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@todo )
            recyclerViewAdapter = RecyclerViewAdapter(this@todo)
            adapter = recyclerViewAdapter
//            val divider = DividerItemDecoration(applicationContext, VERTICAL)
//            addItemDecoration(divider)
        }
        viewModel = ViewModelProviders.of(this).get(TodoViewModel::class.java)
        viewModel.getAllUsersObservers().observe(this, Observer {
            recyclerViewAdapter.setListData(ArrayList(it))
            recyclerViewAdapter.notifyDataSetChanged()
        })
        addButton.setOnClickListener {
            val name  =projectName.text.toString()
            val email  = projectDescription.text.toString()
            val phone = projectMembers.text.toString()
            if(addButton.text.equals("Add")) {
                val user = UserEntity(0, name, email, phone)
                viewModel.insertUserInfo(user)
            } else {
                val user = UserEntity(projectName.getTag(projectDescription.id).toString().toInt(), name, email, phone)
                viewModel.updateUserInfo(user)
                addButton.setText("Add")
            }
            projectName.setText("")
            projectDescription.setText("")
        }
    }

    override fun onDeleteUserClickListener(user: UserEntity) {
        viewModel.deleteUserInfo(user)
    }

    override fun onItemClickListener(user: UserEntity) {
        projectName.setText(user.name)
        projectDescription.setText(user.email)
        projectMembers.setText(user.phone)
        projectName.setTag(projectName.id, user.id)
        addButton.text = "Update"
    }
}