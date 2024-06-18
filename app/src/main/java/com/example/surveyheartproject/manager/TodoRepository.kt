package com.example.surveyheartproject.manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.surveyheartproject.Dao.TodoDao
import com.example.surveyheartproject.Result
import com.example.surveyheartproject.Service.APIService
import com.example.surveyheartproject.model.TodoItem
import com.example.surveyheartproject.model.TodoItemRequest

class TodoRepository(private val apiService: APIService, private val dao: TodoDao) {

    fun getTodosFromApi(limit: Int, skip: Int): LiveData<Result<List<TodoItem>>> = liveData {
        emit(Result.loading())
        try {
            val response = apiService.getTodos(limit, skip)
            dao.saveOrUpdateTodos(response.todos)
            emit(Result.success(response.todos))
        } catch (exception: Exception) {
            emit(Result.error(exception.message ?: "Error fetching todos"))
        }
    }

    fun getTodosFromDb(): List<TodoItem> {
        return dao.fetchUser()
    }

    fun addTodoToDb(todoItemRequest: TodoItemRequest): LiveData<Result<TodoItem>> = liveData {
        emit(Result.loading())
        try {
            val todoItem = TodoItem(
                id = dao.getHighestId() + 1,
                todo = todoItemRequest.todo,
                completed = todoItemRequest.completed,
                userId = todoItemRequest.userId
            )
            dao.saveOrUpdateTodo(todoItem)
            emit(Result.success(todoItem))
        } catch (exception: Exception) {
            emit(Result.error(exception.message ?: "Error adding todo"))
        }
    }

    fun updateTodoStatusInDb(id: Int, todo: String, completed: Boolean): LiveData<Result<TodoItem>> = liveData {
        emit(Result.loading())
        try {
            val todoItem = dao.fetchUser().find { it.id == id }
            if (todoItem != null) {
                todoItem.todo = todo
                todoItem.completed = completed
                dao.saveOrUpdateTodo(todoItem)
                emit(Result.success(todoItem))
            } else {
                emit(Result.error("Todo not found"))
            }
        } catch (exception: Exception) {
            emit(Result.error(exception.message ?: "Error updating todo"))
        }
    }

    fun deleteTodoFromDb(id: Int): LiveData<Result<Unit>> = liveData {
        emit(Result.loading())
        try {
            dao.deleteTodo(id)
            emit(Result.success(Unit))
        } catch (exception: Exception) {
            emit(Result.error(exception.message ?: "Error deleting todo"))
        }
    }
}
