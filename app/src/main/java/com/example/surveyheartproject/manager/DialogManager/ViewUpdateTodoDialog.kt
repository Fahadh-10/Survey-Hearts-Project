package com.example.surveyheartproject.manager.DialogManager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.surveyheartproject.Dao.TodoDao
import com.example.surveyheartproject.R
import com.example.surveyheartproject.activity.MainActivity
import com.example.surveyheartproject.databinding.ViewUpdateTodoDialogBinding
import com.example.surveyheartproject.manager.DataManager
import com.example.surveyheartproject.model.TodoItem
import com.example.surveyheartproject.model.TodoItemRequest

class ViewUpdateTodoDialog(private var todoItem: TodoItem? = null) : DialogFragment() {

    private lateinit var binding: ViewUpdateTodoDialogBinding
    private lateinit var mContext: Context
    private var updateListener: ((TodoItem) -> Unit)? = null

    fun setOnUpdateListener(listener: (TodoItem) -> Unit) {
        this.updateListener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            binding = ViewUpdateTodoDialogBinding.inflate(requireActivity().layoutInflater)
            builder.setView(binding.root)
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setOnKeyListener { _, keyCode, _ ->
                return@setOnKeyListener keyCode == KeyEvent.KEYCODE_BACK
            }
            binding.cancelACB.setOnClickListener {
                dismiss()
            }
            binding.addUpdateACB.setOnClickListener {
                if (binding.nameET.text.isNullOrEmpty()) {
                    Toast.makeText(mContext,
                        getString(R.string.name_should_not_be_empty), Toast.LENGTH_SHORT).show()
                } else {
                    if (todoItem == null) {
                        addNewTodoServiceCall()
                    } else {
                        updateTodo()
                    }
                }
            }
            setupViews()
            return dialog
        } ?: throw IllegalStateException("Activity not be null")
    }

    /**
     * This function is used to set up views for UI elements
     */
    private fun setupViews() {
        if (todoItem != null) {
            binding.titleTV.text = getString(R.string.update)
            binding.addUpdateACB.text = getString(R.string.update)
            binding.nameET.setText(todoItem?.todo)
        } else {
            binding.titleTV.text = getString(R.string.add)
            binding.addUpdateACB.text = getString(R.string.add)
        }
    }

    /**
     * This service call is used to add a product in todo item
     */
    private fun addNewTodoServiceCall() {
        val todoItemRequest = TodoItemRequest(
            todo = binding.nameET.text.toString(),
            completed = false,
            userId = 5
        )

        DataManager.addTodo(todoItemRequest, object : DataManager.APICallback<TodoItem> {
            override fun onSuccess(response: TodoItem) {
                (activity as MainActivity).mainListADTR.todoLists.add(0, response)
                (activity as MainActivity).mainListADTR.notifyItemInserted(0)
                TodoDao.saveOrUpdateTodo(response)
                dismiss()
            }

            override fun onFailure(message: String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTodoServiceCall() {
        todoItem?.let {
            DataManager.updateTodoStatus(it.id, binding.nameET.text.toString(),false, object : DataManager.APICallback<TodoItem> {
                override fun onSuccess(response: TodoItem) {
                    TodoDao.saveOrUpdateTodo(response)
                    dismiss()
                }

                override fun onFailure(message: String) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * This function is used to update a saved product in todo item
     */
    private fun updateTodo() {
        todoItem?.let {
            it.todo = binding.nameET.text.toString()
            TodoDao.saveOrUpdateTodo(it)
            updateListener?.invoke(it)
            dismiss()
        }
    }


}
