package com.example.surveyheartproject.manager

import com.example.surveyheartproject.Service.APIService
import com.example.surveyheartproject.Dao.TodoDao
import com.example.surveyheartproject.helper.BASE_URL
import com.example.surveyheartproject.model.TodoItem
import com.example.surveyheartproject.model.TodoItemRequest
import com.example.surveyheartproject.model.TodoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DataManager {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: APIService = retrofit.create(APIService::class.java)

    /**
     * Fetches a list of todos items from the API based on the specified limit and skip parameters.
     * @param callback The callback to be invoked upon successful or failed API call.
     */
    fun getTodos(limit: Int, skip: Int, callback: APICallback<List<TodoItem>>) {
        val call = apiService.getTodos(limit, skip)
        call.enqueue(object : Callback<TodoResponse> {
            override fun onResponse(call: Call<TodoResponse>, response: Response<TodoResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback.onSuccess(it.todos)
                    } ?: callback.onFailure("Response body is null")
                } else {
                    callback.onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<TodoResponse>, t: Throwable) {
                callback.onFailure(t.message ?: "Unknown error")
            }
        })
    }

    /**
     * Adds a new todos item to the API and saves it to the local database.
     * @param callback The callback to be invoked upon successful or failed API call.
     */
    fun addTodo(todoItemRequest: TodoItemRequest, callback: APICallback<TodoItem>) {
        val call = apiService.addTodo(todoItemRequest)
        call.enqueue(object : Callback<TodoItem> {
            override fun onResponse(call: Call<TodoItem>, response: Response<TodoItem>) {
                if (response.isSuccessful) {
                    response.body()?.let { todoItem ->
                        val uniqueId = generateUniqueId()
                        val newTodoItem = TodoItem(
                            id = uniqueId,
                            todo = todoItem.todo,
                            completed = todoItem.completed,
                            userId = todoItem.userId
                        )
                            TodoDao.saveOrUpdateTodo(newTodoItem)
                            callback.onSuccess(newTodoItem)
                    } ?: callback.onFailure("Response body is null")
                } else {
                    callback.onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<TodoItem>, t: Throwable) {
                callback.onFailure(t.message ?: "Unknown error")
            }
        })
    }

    /**
     * Generates a unique ID for a new todo item.
     * @return The unique ID generated.
     */
    fun generateUniqueId(): Int {
        val highestId = TodoDao.getHighestId()
        return highestId + 1
    }

    /**
     * Updates the todo item on the API and local database.
     * @param callback The callback to be invoked upon successful or failed API call.
     */
    fun updateTodoStatus(
        id: Int,
        todo: String,
        completed: Boolean,
        callback: APICallback<TodoItem>
    ) {
        val requestBody = mapOf("completed" to completed)
        val call = apiService.updateTodoStatus(id, requestBody)
        call.enqueue(object : Callback<TodoItem> {
            override fun onResponse(call: Call<TodoItem>, response: Response<TodoItem>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val filter = TodoDao.fetchUser().filter { it.id == id }
                        TodoDao.saveOrUpdateTodo(it)
                        callback.onSuccess(it)
                    } ?: callback.onFailure("Response body is null")
                } else {
                    callback.onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<TodoItem>, t: Throwable) {
                callback.onFailure(t.message ?: "Unknown error")
            }
        })
    }

    /**
     * Deletes a todo item from the API and local database.
     *
     */
    fun deleteTodo(id: Int, callback: APICallback<Unit>) {
        val call = apiService.deleteTodo(id)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    TodoDao.deleteTodo(id)
                    callback.onSuccess(Unit)
                } else {
                    callback.onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onFailure(t.message ?: "Unknown error")
            }
        })
    }

    /**
     * Callback interface to handle API call responses.
     */
    interface APICallback<T> {
        fun onSuccess(response: T)
        fun onFailure(message: String)
    }
}
