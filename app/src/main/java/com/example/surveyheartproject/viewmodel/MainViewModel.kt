package com.example.surveyheartproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.surveyheartproject.manager.TodoRepository
import com.example.surveyheartproject.model.TodoItem
import com.example.surveyheartproject.model.TodoItemRequest
import kotlinx.coroutines.launch
import com.example.surveyheartproject.Result

class MainViewModel (private val repository: TodoRepository) : ViewModel() {

    private val _todos = MutableLiveData<List<TodoItem>>()
    val todos: LiveData<List<TodoItem>> get() = _todos

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    val addTodoResult = MutableLiveData<Result<TodoItem>>()
    val updateTodoResult = MutableLiveData<Result<TodoItem>>()

    var limit = 10
    var skip = 0
    var isLoading = false


    init {
        fetchTodosFromDb()
    }

    fun fetchTodos() {
        _loading.value = true
        isLoading = true
        viewModelScope.launch {
            val result = repository.getTodosFromApi(limit, skip)
            result.observeForever {
                _loading.value = false
                isLoading = false
                if (it.status == Result.Status.SUCCESS) {
                    val currentTodos = _todos.value ?: emptyList()
                    _todos.value = currentTodos + (it.data ?: emptyList())
                    skip += limit
                } else {
                    Log.e("Error", it.message.toString())
                }
            }
        }
    }

    fun fetchTodosFromDb() {
        _loading.value = true
        val todosFromDb = repository.getTodosFromDb()
        _todos.value = todosFromDb
        _loading.value = false
    }

    fun addTodo(todoItemRequest: TodoItemRequest) {
        val result = repository.addTodoToDb(todoItemRequest)
        result.observeForever {
            addTodoResult.postValue(it)
        }
    }

    fun updateTodoStatus(id: Int, todo: String, completed: Boolean) {
        val result = repository.updateTodoStatusInDb(id, todo, completed)
        result.observeForever {
            updateTodoResult.postValue(it)
        }
    }

    fun deleteTodo(id: Int) {
        viewModelScope.launch {
            val result = repository.deleteTodoFromDb(id)
            result.observeForever {
                repository.deleteTodoFromDb(id)
                fetchTodosFromDb()
            }
            fetchTodosFromDb()
        }
    }
}
