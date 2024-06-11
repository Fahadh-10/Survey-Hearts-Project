package com.example.surveyheartproject.adapter

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.surveyheartproject.R
import com.example.surveyheartproject.databinding.MainListBinding
import com.example.surveyheartproject.model.TodoItem

class MainListADTR() : RecyclerView.Adapter<MainListADTR.HomeListVH>() {

    var todoLists = ArrayList<TodoItem>()
    var isFullyLoaded = false
    lateinit var context: Context

    private var mOnItemClickListeners: OnItemClickListeners? = null

    interface OnItemClickListeners {
        fun onUpdateItemClick(position: Int, todoItem: TodoItem)
        fun onDeleteItemClick(position: Int, todoItem: TodoItem)
    }

    fun setOnClickListeners(onItemClick: OnItemClickListeners) {
        this.mOnItemClickListeners = onItemClick
    }

    class HomeListVH(mBinding: MainListBinding) : RecyclerView.ViewHolder(mBinding.root) {
        val binding: MainListBinding = mBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListVH {
        context = parent.context
        return HomeListVH(
            MainListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return todoLists.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: HomeListVH, position: Int) {
        val todoList = todoLists[position]
        holder.binding.progressBar.visibility = if (position == todoLists.lastIndex && !isFullyLoaded) VISIBLE else GONE
        holder.binding.titleTV.text = todoList.todo
        holder.binding.descriptionTV.text =
            context.getString(R.string.user_id).plus(todoList.userId.toString())

        holder.binding.editIV.setOnClickListener {
            mOnItemClickListeners?.onUpdateItemClick(position, todoList)
        }

        holder.binding.deleteIV.setOnClickListener {
            mOnItemClickListeners?.onDeleteItemClick(position, todoList)
        }
    }

}