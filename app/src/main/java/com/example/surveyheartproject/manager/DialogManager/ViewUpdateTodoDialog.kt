package com.example.surveyheartproject.manager.DialogManager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.surveyheartproject.Result
import com.example.surveyheartproject.databinding.ViewUpdateTodoDialogBinding
import com.example.surveyheartproject.model.TodoItem
import com.example.surveyheartproject.model.TodoItemRequest
import com.example.surveyheartproject.viewmodel.MainViewModel

class ViewUpdateTodoDialog(private var todoItem: TodoItem? = null) : DialogFragment() {

    private lateinit var binding: ViewUpdateTodoDialogBinding
    private lateinit var mainViewModel: MainViewModel

    private var updateListener: (() -> Unit)? = null

    fun setOnUpdateListener(listener: () -> Unit) {
        this.updateListener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = ViewUpdateTodoDialogBinding.inflate(layoutInflater)
        builder.setView(binding.root)

        binding.viewModel = mainViewModel
        binding.todoItem = todoItem
        binding.lifecycleOwner = this

        binding.cancelACB.setOnClickListener { dismiss() }
        binding.addUpdateACB.setOnClickListener {
            if (binding.nameET.text.isNullOrEmpty()) {
                Toast.makeText(context, "Name should not be empty", Toast.LENGTH_SHORT).show()
            } else {
                if (todoItem == null) {
                    addNewTodo()
                } else {
                    updateTodo()
                }
            }
        }

        return builder.create()
    }

    private fun addNewTodo() {
        val todoItemRequest = TodoItemRequest(
            todo = binding.nameET.text.toString(),
            completed = false,
            userId = 5
        )
        mainViewModel.addTodo(todoItemRequest)
        dismiss()
    }

    private fun updateTodo() {
        todoItem?.let {
            it.todo = binding.nameET.text.toString()
            mainViewModel.updateTodoStatus(it.id, it.todo, it.completed)
        }
        dismiss()
    }
}
