package com.example.surveyheartproject.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import java.io.Serializable


open class TodoResponse(
    @SerializedName("todos") var todos: RealmList<TodoItem> = RealmList()
) : RealmObject(), Serializable

open class TodoItem(
    @SerializedName("id") var id: Int = 0,
    @SerializedName("todo") var todo: String = "",
    @SerializedName("completed") var completed: Boolean = false,
    @SerializedName("userId") var userId: Int = 0
) : RealmObject(), Serializable

data class TodoItemRequest(
    @SerializedName("todo") var todo: String,
    @SerializedName("completed") var completed: Boolean,
    @SerializedName("userId") var userId: Int
)

