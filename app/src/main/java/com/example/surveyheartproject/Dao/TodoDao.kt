package com.example.surveyheartproject.Dao

import com.example.surveyheartproject.model.TodoItem
import io.realm.Realm
import java.io.IOException

object TodoDao {

    /**
     * Saves or updates a single todos item in the Realm database.
     * @param todo The todo item to be saved or updated.
     */
    fun saveOrUpdateTodo(todo: TodoItem) {
        val realm = Realm.getDefaultInstance()
        try {
            realm.beginTransaction()
            realm.insertOrUpdate(todo)
            realm.commitTransaction()
        } catch (e: IOException) {
            realm.cancelTransaction()
        }
        realm.close()
    }

    /**
     * Fetches the list of todos items stored in the Realm database.
     * @return An ArrayList containing the fetched todos items.
     */
    fun fetchUser(): ArrayList<TodoItem>{
        val toDoList: ArrayList<TodoItem> = ArrayList<TodoItem>()
        val realm = Realm.getDefaultInstance()
        realm.use {
            val results = realm.where(TodoItem::class.java).findAll()
            if (results != null) {
                for (result in realm.copyFromRealm(results)){
                    toDoList.add(result)
                }
            }
        }
        realm.close()
        return toDoList
    }

    /**
     * Delete a todos item with the specified ID from the Realm database.
     * @param id The ID of the todos item will be deleted.
     */
    fun deleteTodo(id: Int) {
        val realm = Realm.getDefaultInstance()
        realm.use {
            try {
                realm.beginTransaction()
                val result = realm.where(TodoItem::class.java).equalTo("id", id).findFirst()
                result?.deleteFromRealm()
                realm.commitTransaction()
            } catch (e: IOException) {
                realm.cancelTransaction()
            }
        }
    }

    /**
     * Retrieves the highest ID among the todos items stored in the Realm database.
     */
    fun getHighestId(): Int {
        val realm = Realm.getDefaultInstance()
        val maxId = realm.where(TodoItem::class.java).max("id")
        realm.close()
        return maxId?.toInt() ?: 0
    }
}