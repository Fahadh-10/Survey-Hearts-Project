package com.example.surveyheartproject.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surveyheartproject.Dao.TodoDao
import com.example.surveyheartproject.adapter.MainListADTR
import com.example.surveyheartproject.databinding.ActivityMainBinding
import com.example.surveyheartproject.manager.DataManager
import com.example.surveyheartproject.manager.DialogManager.ViewUpdateTodoDialog
import com.example.surveyheartproject.model.TodoItem
import io.realm.Realm
import io.realm.RealmConfiguration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mainListADTR: MainListADTR
    private lateinit var viewUpdateTodoDialog: ViewUpdateTodoDialog
    private var limit = 10
    private var skip = 0
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build())
        setAdapter()
        setUpListeners()
        fetchTodosServiceCall()
    }

    override fun onResume() {
        super.onResume()
        fetchTodosServiceCall()
    }

    private fun setAdapter() {
        binding.homeRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mainListADTR = MainListADTR()
        binding.homeRV.adapter = mainListADTR
        mainListADTR.setOnClickListeners(object : MainListADTR.OnItemClickListeners {
            override fun onUpdateItemClick(position: Int, todoItem: TodoItem) {
                viewUpdateTodoDialog = ViewUpdateTodoDialog(todoItem)
                viewUpdateTodoDialog.setOnUpdateListener { updatedTodoItem ->
                    mainListADTR.todoLists[position] = updatedTodoItem
                    mainListADTR.notifyItemChanged(position)
                    TodoDao.saveOrUpdateTodo(updatedTodoItem)
                }
                viewUpdateTodoDialog.show(supportFragmentManager, viewUpdateTodoDialog.tag)
            }

            override fun onDeleteItemClick(position: Int, todoItem: TodoItem) {
                showDeleteAlert(todoItem, position)
            }
        })
    }

    /**
     * This function is used to set up views for UI elements
     */
    private fun setUpListeners() {
        binding.addIV.setOnClickListener {
            viewUpdateTodoDialog = ViewUpdateTodoDialog()
            viewUpdateTodoDialog.show(supportFragmentManager, viewUpdateTodoDialog.tag)
        }

        binding.homeRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    fetchTodosServiceCall()
                }
            }
        })
    }

    /**
     * This service call is used to fetch the todos items based on the pagination.
     * If there is no internet we will show the products which is in DB
     */
    private fun fetchTodosServiceCall() {
        isLoading = true
        if (isNetworkAvailable()) {
            binding.progressBar.visibility = if (skip == 0) VISIBLE else GONE
            DataManager.getTodos(limit, skip, object : DataManager.APICallback<List<TodoItem>> {
                override fun onSuccess(response: List<TodoItem>) {
                    isLoading = false
                    if (skip == 0) {
                        binding.progressBar.visibility = GONE
                        mainListADTR.todoLists.clear()
                    }
                    mainListADTR.todoLists.addAll(response)
                    mainListADTR.notifyDataSetChanged()
                    skip += limit
                    mainListADTR.isFullyLoaded = response.isEmpty()
                }

                override fun onFailure(message: String) {
                    if (skip == 0) {
                        binding.progressBar.visibility = GONE
                    }
                    isLoading = false
                    getTodoListFromDB()
                }
            })
        } else {
            getTodoListFromDB()
        }
    }

    /**
     * This function is used to fetch the todos items from database
     */
    private fun getTodoListFromDB(){
        if (skip == 0) {
            binding.progressBar.visibility = GONE
        }
        val localTodos = TodoDao.fetchUser()
        mainListADTR.todoLists = localTodos.distinctBy { it.id } as ArrayList<TodoItem>
        mainListADTR.notifyDataSetChanged()
    }


    //Note : I haven't used this service call, because when we add it locally, we can't able to delete it in the server
    private fun deleteTodoItem(todoItem: TodoItem) {
        DataManager.deleteTodo(todoItem.id, object : DataManager.APICallback<Unit> {
            override fun onSuccess(response: Unit) {
                Toast.makeText(this@MainActivity, "Todo item deleted successfully", Toast.LENGTH_SHORT).show()
                fetchTodosServiceCall()
            }

            override fun onFailure(message: String) {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun Context.isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    private fun showDeleteAlert(todoItem: TodoItem, position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        builder.setMessage("Are you sure you want to delete?")
        builder.setPositiveButton("Yes") { _, _ ->
            TodoDao.deleteTodo(todoItem.id)
            mainListADTR.todoLists.removeAt(position)
            mainListADTR.notifyItemRemoved(position)
        }
        builder.setNegativeButton("No", null)
        builder.setCancelable(false)
        builder.show()
    }
}

