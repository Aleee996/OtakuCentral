package com.example.animeshowtime

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

abstract class RecyclerFragment(
    protected var searchString : Array<String>,
    protected val jsonList : Array<ArrayList<JSONObject>>,
    protected var nScrolls : Array<Int>,
    protected var hasNextPage : Array<Boolean>
) : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onDestroy() {
        super.onDestroy()
    }

    protected fun manageApi(
        noResultFoundTextView: TextView,
        searchedElement: String,
        i: Int,
        recyclerView: RecyclerView
    ) {
        val deferred: Deferred<JSONObject> = lifecycleScope.async {
            withContext(Dispatchers.IO) {
                httpReq(searchString[i], nScrolls[i])
            }
        }
        lifecycleScope.launch {
            //TODO gestire null string o no results
            try {
                val tempJsonList = ArrayList<JSONObject>()
                val tempNextPage = getResults(deferred, tempJsonList)

                //ignoring type errors for episodes
                try {
                    tempJsonList.removeIf {
                        it.getString("genres").contains("Hentai", true) ||
                                it.getString("type").equals("music", true) ||
                                it.getString("synopsis").equals("null") ||
                                it.getString("type").equals("null") ||
                                !it.getBoolean("approved")
                    }
                } catch (e: Exception) {
                    //Log.d("mytagJsonRemoveif", e.message ?: e.toString())
                }

                //if empty la ricerca non ha prodotto risultato
                if (jsonList[i].isEmpty() && tempJsonList.isEmpty())
                    noResultFoundTextView.text =
                        getString(R.string.textResultsNotFound, searchedElement)

                else if (searchedElement == "Episodes" && jsonList[i].size <= 1 && tempJsonList.isEmpty()) {
                    noResultFoundTextView.setTextColor(Color.parseColor("#DD2C00"))
                    noResultFoundTextView.text = getText(R.string.noEpisodesError)
                }

                //else update recycler
                else {
                    val startPosition = jsonList[i].size
                    jsonList[i].addAll(tempJsonList)
                    recyclerView.adapter?.notifyItemRangeInserted(startPosition, tempJsonList.size)
                    hasNextPage[i] = tempNextPage
                    nScrolls[i]++
                }
            } catch (e: Exception) {
                //Log.e("MyHTTPErr", e.message ?: "null e.msg $e")
                if (e is IOException)
                    noResultFoundTextView.text = getString(R.string.networkError)
                noResultFoundTextView.setTextColor(Color.parseColor("#DD2C00"))
            }
        }
    }


    protected fun httpReq(searchString: String, page: Int): JSONObject {
        try {

            //HTTP get request from URL when page==1 no need to pass to it to the request
            val url = if (page == 1)
                URL(searchString)
            else URL("$searchString&page=$page")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            //JSONize the data
            val inputStream = BufferedInputStream(connection.inputStream)
            val response = inputStream.bufferedReader().use { it.readText() }
            //Log.v("mytagHttpReq", response)
            return JSONObject(response)
        } catch (e: Exception) {
            //Log.e("MytagHTTPErr", e.message ?: "null e.msg $e")
            if (e is IOException)
                throw e
        }
        return JSONObject("")
    }

    protected suspend fun getResults(
        deferred: Deferred<JSONObject>,
        tempJsonList: ArrayList<JSONObject>
    ): Boolean {
        try {
            val response = deferred.await()
            val animeList = response.getJSONArray("data")
            for (i in 0 until animeList.length())
                tempJsonList.add(animeList.getJSONObject(i))
            return response.getJSONObject("pagination").getBoolean("has_next_page")
        } catch (e: Throwable) {
            //Log.e("myDeferredErr", e.message ?: "null e msg")
            if (e is IOException)
                throw e
        }
        return false
    }

    protected fun addOnRecyclerScrolled (
        noResultFoundTextView: TextView,
        searchedElement: String,
        i: Int,
        recyclerView: RecyclerView
    )
    {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val llm = if (this@RecyclerFragment is SearchFragment)
                    recyclerView.layoutManager as GridLayoutManager
                    else recyclerView.layoutManager as LinearLayoutManager
                val last = llm.findLastVisibleItemPosition()
                if (last >= jsonList[i].size - 4 && hasNextPage[i]) {
                    hasNextPage[i] = false
                    manageApi(
                        noResultFoundTextView,
                        searchedElement,
                        i,
                        recyclerView
                    )
                }
            }
        })
    }
}