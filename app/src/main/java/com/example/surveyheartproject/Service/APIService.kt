package com.example.surveyheartproject.Service

import com.example.surveyheartproject.model.TodoItem
import com.example.surveyheartproject.model.TodoItemRequest
import com.example.surveyheartproject.model.TodoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @GET("todos")
    @Headers("Content-Type: application/json")
    fun getTodos(): Call<TodoResponse>

    @GET("todos")
    fun getTodos(@Query("limit") limit: Int, @Query("skip") skip: Int): Call<TodoResponse>

    @POST("todos/add")
    @Headers("Content-Type: application/json")
    fun addTodo(@Body todoItemRequest: TodoItemRequest): Call<TodoItem>

    @PUT("todos/{id}")
    @Headers("Content-Type: application/json")
    fun updateTodoStatus(@Path("id") id: Int, @Body requestBody: Map<String, Boolean>): Call<TodoItem>

    @DELETE("todos/{id}")
    @Headers("Content-Type: application/json")
    fun deleteTodo(@Path("id") id: Int): Call<Unit>


}