package com.example.animeshowtime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.animeshowtime.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import kotlin.time.Duration

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private val jsonListAnime : ArrayList<JSONObject> = ArrayList()
    private val jsonListManga : ArrayList<JSONObject> = ArrayList()
    private var viewMinutes = 0L
    private var totEpisodes = 0
    private var totChapters = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                try {
                    /*childFragmentManager.commit {
                        setReorderingAllowed(true)
                        childFragmentManager.findFragmentById(R.id.pref_container)
                            ?.let { remove(it) }
                    }*/
                    activity?.supportFragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    activity?.supportFragmentManager?.commit {
                        setReorderingAllowed(true)
                        replace(R.id.fragment_container, TopFragment())
                    }
                    menu.findItem(R.id.menuSearch).collapseActionView()
                } catch (e: Exception) {/*Log.e("mytagFragManager", e.message ?: e.toString())*/}
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this, // LifecycleOwner
            callback
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*childFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.pref_container, SettingsFragment())
        }*/
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        if (jsonListAnime.isNotEmpty() || jsonListManga.isNotEmpty()) {
            jsonListAnime.clear()
            jsonListManga.clear()
            viewMinutes = 0
        }
        //get Anime in list completed
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                getFollowedList(ANIME)?.first()?.values.let { collection ->
                    collection?.forEach {
                        totEpisodes += JSONObject(it as String).getString("last").toIntOrNull() ?: 0
                        val duration = Duration.parse(JSONObject(it).optString("duration"))
                        viewMinutes += (JSONObject(it).getString("last").toIntOrNull() ?: 0) * duration.inWholeMinutes
                        if (JSONObject(it).getString("last").toIntOrNull()
                            == JSONObject(it).getString("tot").toIntOrNull()
                        )
                        {jsonListAnime.add(JSONObject(it))}
                    }
                }
                withContext(Dispatchers.Main) {
                    if (jsonListAnime.isEmpty())
                        binding.textViewAnimeCompleted.text = getString(R.string.emptyFollowedList)
                    else {
                        binding.animeCompleted.adapter = TopRecyclerViewAdapter(jsonListAnime, this@ProfileFragment)
                    }
                    binding.hoursWatched.text = Duration.parse(viewMinutes.toString()+"m").toString()
                    binding.episodesWatched.text = totEpisodes.toString()
                }
            } catch (e: Exception) {
                //Log.e("mytagFollowedList", e.message ?: e.toString())
            }
        }

        //get Manga in list completed
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                getFollowedList(MANGA)?.first()?.values.let { collection ->
                    collection?.forEach {
                        totChapters += JSONObject(it as String).getString("last").toIntOrNull() ?: 0
                        if (JSONObject(it).getString("last").toIntOrNull()
                            == JSONObject(it).getString("tot").toIntOrNull()
                        )
                        {jsonListManga.add(JSONObject(it))}
                    }
                }
                withContext(Dispatchers.Main) {
                    if (jsonListManga.isEmpty())
                        binding.textViewMangaCompleted.text = getString(R.string.emptyFollowedList)
                    else {
                        binding.mangaCompleted.adapter = TopRecyclerViewAdapter(jsonListManga, this@ProfileFragment)
                    }
                    binding.chaptersRead.text = totChapters.toString()
                }
            } catch (e: Exception) {
                //Log.e("mytagFollowedList", e.message ?: e.toString())
            }
        }

        binding.settingsButton.setOnClickListener {
            activity?.supportFragmentManager?.commit {
                setReorderingAllowed(true)
                addToBackStack("settings")
                replace(R.id.fragment_container, SettingsFragment())
            }
        }
    }

    private suspend fun getFollowedList(elementType: Int): Flow<Map<Preferences.Key<*>, Any>>? {
        return when(elementType) {
            ANIME -> {
                this.activity?.episodesDataStore?.data?.catch {
                    if (it is IOException)
                        emit(emptyPreferences())
                    else throw it
                }
                    ?.map {
                        it.asMap()
                    }
            }
            else -> {
                this.activity?.chaptersDataStore?.data?.catch {
                    if (it is IOException)
                        emit(emptyPreferences())
                    else throw it
                }
                    ?.map {
                        it.asMap()
                    }
            }
        }
    }

}