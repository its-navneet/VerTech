package com.example.android.vertech.views

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.vertech.R
import com.example.android.vertech.adapters.RoomDatabaseAdapter
import com.example.android.vertech.db.UserEntity
import com.example.android.vertech.viewModels.TodoViewModel
import kotlinx.android.synthetic.main.activity_todo.*

class todo : AppCompatActivity(), RoomDatabaseAdapter.RowClickListener {
    lateinit var roomDatabaseAdapter: RoomDatabaseAdapter
    lateinit var viewModel: TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)
        supportActionBar?.hide()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@todo)
            roomDatabaseAdapter = RoomDatabaseAdapter(this@todo)
            adapter = roomDatabaseAdapter
//            val divider = DividerItemDecoration(applicationContext, VERTICAL)
//            addItemDecoration(divider)
        }
        viewModel = ViewModelProviders.of(this).get(TodoViewModel::class.java)
        viewModel.getAllUsersObservers().observe(this, Observer {
            roomDatabaseAdapter.setListData(ArrayList(it))
            roomDatabaseAdapter.notifyDataSetChanged()
        })
        addButton.setOnClickListener {
            val name = projectName.text.toString()
            val email = projectDescription.text.toString()
            val phone = projectMembers.text.toString()
            if (addButton.text.equals("Add")) {
                val user = UserEntity(0, name, email, phone)
                viewModel.insertUserInfo(user)
            } else {
                val user = UserEntity(
                    projectName.getTag(projectName.id).toString().toInt(),
                    name,
                    email,
                    phone
                )
                viewModel.updateUserInfo(user)
                projectName.text = null
                projectDescription.text = null
                projectMembers.text = null
                addButton.text = "Add"
            }
            projectName.setText("")
            projectDescription.setText("")
            projectMembers.setText("")
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

    override fun onShareClickListener(user: UserEntity) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, user.name + "\n" + user.email + "\n" + user.phone)
        startActivity(intent)
    }

}