package com.example.animeshowtime

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.animeshowtime.databinding.FragmentTopListBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

//private const val ARG_ANIME_ID = "param1"

class TopFragment : RecyclerFragment(
    Array(2) {"null"},
    Array(2) {ArrayList<JSONObject>()},
    Array(2) { 1 },
    Array(2) { false },
) {

    private lateinit var binding : FragmentTopListBinding
    private var rowCount = 1
    private val ANIME = 0
    private val MANGA = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchString[ANIME] = "https://api.jikan.moe/v4/top/anime?type=tv,movie"
        searchString[MANGA] = "https://api.jikan.moe/v4/top/manga?page=1"
        arguments?.let {
            rowCount = it.getInt(ARG_ROW_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_list, container, false)
        binding = FragmentTopListBinding.bind(view)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scaling : Float = if (resources.displayMetrics.run { heightPixels/density } > 450) 4F else 2.7F
        binding.topTvAnime.layoutParams.height = (resources.displayMetrics.heightPixels/scaling).toInt()
        binding.topTvAnime.adapter = TopRecyclerViewAdapter(jsonList[ANIME], this)
        binding.topManga.layoutParams.height = (resources.displayMetrics.heightPixels/scaling).toInt()
        binding.topManga.adapter = TopRecyclerViewAdapter(jsonList[MANGA], this)

        //async coroutine to retrieve data from the jikan API
        if (jsonList[ANIME].isEmpty() || jsonList[MANGA].isEmpty()) {
            //the first call to the API
            manageApi(binding.textViewTopTvAnime, "Anime", ANIME, binding.topTvAnime)

            manageApi(binding.textViewTopManga, "Manga", MANGA, binding.topManga)
        }

        //TODO gestire se jsonList è vuoto se non trova risultati

        else {
            binding.topTvAnime.adapter = TopRecyclerViewAdapter(jsonList[ANIME], this)
            binding.topManga.adapter = TopRecyclerViewAdapter(jsonList[MANGA], this)
        }

        //listeners to add new elements at the end of the list
        addOnRecyclerScrolled(binding.textViewTopTvAnime, "Anime", ANIME, binding.topTvAnime)
        addOnRecyclerScrolled(binding.textViewTopManga, "Manga", MANGA, binding.topManga)

    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_ROW_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, /*arg1 : String*/) =
            TopFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ROW_COUNT, columnCount)
                    //putString(ARG_ANIME_ID, arg1)
                }
            }
    }
}