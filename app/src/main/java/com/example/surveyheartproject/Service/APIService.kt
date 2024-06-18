package com.example.surveyheartproject.Service

import com.example.surveyheartproject.helper.BASE_URL
import com.example.surveyheartproject.model.TodoItem
import com.example.surveyheartproject.model.TodoItemRequest
import com.example.surveyheartproject.model.TodoResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    suspend fun getTodos(@Query("limit") limit: Int, @Query("skip") skip: Int): TodoResponse

    @POST("todos/add")
    suspend fun addTodo(@Body todoItemRequest: TodoItemRequest): TodoItem

    @PUT("todos/{id}")
    suspend fun updateTodoStatus(@Path("id") id: Int, @Body requestBody: Map<String, Boolean>): TodoItem

    @DELETE("todos/{id}")
    suspend fun deleteTodo(@Path("id") id: Int): Unit

    companion object {
        fun create(): APIService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL) // Replace with your base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(APIService::class.java)
        }
    }
}
