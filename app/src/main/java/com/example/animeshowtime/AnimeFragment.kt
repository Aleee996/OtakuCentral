package com.example.animeshowtime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.animeshowtime.databinding.FragmentAnimeListBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException


class AnimeFragment : RecyclerFragment(
    Array(1) {"null"},
    Array(1) {ArrayList<JSONObject>()},
    Array(1) { 1 },
    Array(1) { false },
) {

    private lateinit var binding: FragmentAnimeListBinding

    var animeId = 0
    private var lastWatchedEp = 0
    private var animeData = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            animeId = it.getInt(ARG_ANIME_ID)
            searchString[0] = "https://api.jikan.moe/v4/anime/$animeId/episodes?page=1"
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anime_list, container, false)
        binding = FragmentAnimeListBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val searchItem = activity?.findViewById(R.id.menuSearch) as ActionMenuItemView
        //val searchView = searchItem.actionView as SearchView


        //async coroutine to retrieve anime data from the jikan API
        if (jsonList[0].isEmpty()) {
            val deferred: Deferred<JSONObject> = lifecycleScope.async {
                withContext(Dispatchers.IO) {
                    httpReq("https://api.jikan.moe/v4/anime/$animeId", 1)
                }
            }
            lifecycleScope.launch {
                try {
                    animeData = deferred.await().getJSONObject("data")
                    val synopsis = JSONObject()
                    synopsis.put("synopsis", animeData.getString("synopsis"))
                    jsonList[0].add(0, synopsis)
                    showResult(animeData)
                    binding.episodesList.adapter = EpisodesRecyclerViewAdapter(
                        jsonList[0],
                        this@AnimeFragment,
                        lastWatchedEp
                    )

                    if(animeData.getInt("episodes") > 1) {
                        //Retrieve ep watched from persistent memory and display episodes
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                lastWatchedEp = getLastWatchedEp()?.first() ?: 0
                            }
                            withContext(Dispatchers.Main) {   //binding the empty adapter for episodes
                                binding.episodesList.adapter = EpisodesRecyclerViewAdapter(
                                    jsonList[0],
                                    this@AnimeFragment,
                                    lastWatchedEp
                                )

                                manageApi(
                                    binding.resultErrEpisodes,
                                    "Episodes",
                                    0,
                                    binding.episodesList
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (e is IOException) {
                        binding.resultErrEpisodes.text = getText(R.string.networkError)
                        binding.resultErrEpisodes.isVisible = true
                    }
                }
            }
        }
        else {
            showResult(animeData)
            binding.episodesList.adapter = EpisodesRecyclerViewAdapter(jsonList[0],this@AnimeFragment, lastWatchedEp)
        }

        addOnRecyclerScrolled(binding.resultErrEpisodes, "Episodes", 0, binding.episodesList)
    }

    //retrieve data when available and display
    private fun showResult(anime: JSONObject) {
        try {
            val imgURL = anime.getJSONObject("images").getJSONObject("jpg")
            val title = anime.getString("title")+ "     [${anime.getString("status")}]"

            Glide.with(this)
                .load(imgURL.getString("large_image_url"))
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .into(binding.animeImage)
            binding.animeTitle.title = title
            //binding.animeTitle.subtitle = anime.getString("status")


            //TODO sinossi tagliata con MOSTRA AlTRO

        } catch (e : Throwable) { Log.e("myDeferredErr", e.message ?: "null e msg") }
    }


    private suspend fun getLastWatchedEp(): kotlinx.coroutines.flow.Flow<Int>? {
        val LAST_WATCHED_EP = intPreferencesKey(this.animeId.toString())
        return this.activity?.dataStore?.data?.catch {
            if (it is IOException)
                emit(emptyPreferences())
            else throw it
        }
            ?.map { episodes : Preferences->
                // No type safety.
                episodes[LAST_WATCHED_EP] ?: 0
            }
    }

    /*suspend fun setLastWatchedEp(ep : Int) {
        val LAST_WATCHED_EP = intPreferencesKey(this.animeId.toString())
        this.activity?.dataStore?.edit { episodes ->
            episodes[LAST_WATCHED_EP] = ep
        }
    }*/

    companion object {

        // TODO: Customize parameter argument names
        private const val ARG_ANIME_ID = "1"
        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(animeId : Int) =
            AnimeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ANIME_ID, animeId)
                }
            }


    }
}