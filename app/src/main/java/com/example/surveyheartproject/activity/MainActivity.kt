package com.example.surveyheartproject.activity

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surveyheartproject.Dao.TodoDao
import com.example.surveyheartproject.R
import com.example.surveyheartproject.Service.APIService
import com.example.surveyheartproject.manager.TodoRepository
import com.example.surveyheartproject.ViewModelFactory
import com.example.surveyheartproject.adapter.MainListADTR
import com.example.surveyheartproject.databinding.ActivityMainBinding
import com.example.surveyheartproject.manager.DialogManager.ViewUpdateTodoDialog
import com.example.surveyheartproject.model.TodoItem
import com.example.surveyheartproject.viewmodel.MainViewModel
import com.example.surveyheartproject.Result

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainListADTR: MainListADTR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val apiService = APIService.create()
        val todoDao = TodoDao

        val factory = ViewModelFactory(TodoRepository(apiService, todoDao))
        mainViewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        setupAdapter()
        setupListeners()
        observeViewModel()

        if (todoDao.fetchUser().size >  0){
            mainViewModel.fetchTodosFromDb()
        } else {
            mainViewModel.fetchTodos()
        }
    }

    private fun setupAdapter() {
        binding.homeRV.layoutManager = LinearLayoutManager(this)
        mainListADTR = MainListADTR()
        binding.homeRV.adapter = mainListADTR
        mainListADTR.setOnClickListeners(object : MainListADTR.OnItemClickListeners {
            override fun onUpdateItemClick(position: Int, todoItem: TodoItem) {
                showUpdateDialog(todoItem)
            }

            override fun onDeleteItemClick(position: Int, todoItem: TodoItem) {
                deleteTodoItemAlertDialog(todoItem.id)
            }
        })
    }

    private fun deleteTodoItemAlertDialog(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        builder.setMessage("Are you sure you want to Delete?")
        builder.setPositiveButton("Yes") { _, _ ->
            mainViewModel.deleteTodo(id)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun setupListeners() {
        binding.addIV.setOnClickListener {
            showUpdateDialog(null)
        }

        binding.homeRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && !mainViewModel.isLoading) {
                    mainViewModel.fetchTodos()
                }
            }
        })
    }

    private fun observeViewModel() {
        mainViewModel.todos.observe(this, Observer {
            mainListADTR.updateList(it)
        })

        mainViewModel.error.observe(this, Observer {
            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
        })

        mainViewModel.addTodoResult.observe(this, Observer {
            if (it.status == Result.Status.SUCCESS) {
                mainListADTR.addItem(it.data!!)
            } else {
                Toast.makeText(this@MainActivity, it.message ?: "Error adding todo", Toast.LENGTH_SHORT).show()
            }
        })

        mainViewModel.updateTodoResult.observe(this, Observer {
            if (it.status == Result.Status.SUCCESS) {
                mainListADTR.updateItem(it.data!!)
            } else {
                Toast.makeText(this@MainActivity, it.message ?: "Error updating todo", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUpdateDialog(todoItem: TodoItem?) {
        val dialog = ViewUpdateTodoDialog(todoItem)
        dialog.setOnUpdateListener {
            mainViewModel.fetchTodosFromDb()
        }
        dialog.show(supportFragmentManager, "UpdateTodoDialog")
    }
}


