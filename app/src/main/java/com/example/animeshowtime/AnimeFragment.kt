package com.example.animeshowtime

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.animeshowtime.databinding.FragmentAnimeListBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.json.JSONObject
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeUnit


class AnimeFragment : ConfirmDialogFragment.NoticeDialogListener, RecyclerFragment(
    Array(1) {"null"},
    Array(1) {ArrayList<JSONObject>()},
    Array(1) { 1 },
    Array(1) { false },
) {

    private lateinit var binding: FragmentAnimeListBinding

    var elementId = 0
    var elementType = 0
    private var last = 0
    private var tot = 0
    private var elementData = JSONObject()
    var inList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            elementId = it.getInt(ARG_ID)
            elementType = it.getInt(ARG_TYPE)
            searchString[0] = "https://api.jikan.moe/v4/anime/$elementId/episodes?page=1"
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

        menu.findItem(R.id.menuSearch).collapseActionView()

        //async coroutine to retrieve anime data from the jikan API
        if (jsonList[0].isEmpty()) {
            val deferred: Deferred<JSONObject> = lifecycleScope.async(Dispatchers.IO) {
                if (elementType == ANIME)
                httpReq("https://api.jikan.moe/v4/anime/$elementId", 1)
                else httpReq("https://api.jikan.moe/v4/manga/$elementId", 1)
            }
            lifecycleScope.launch {
                try {
                    elementData = deferred.await().getJSONObject("data")
                    val synopsis = JSONObject()
                    synopsis.put("synopsis", elementData.getString("synopsis"))
                    jsonList[0].add(0, synopsis)
                    showResult(elementData)
                    binding.episodesList.adapter = EpisodesRecyclerViewAdapter(
                        jsonList[0],
                        this@AnimeFragment,
                        last
                    )
                    //Retrieve ep watched from persistent memory
                    withContext(Dispatchers.IO) {
                        last = getLastWatchedEp()?.first()
                            ?.let {
                                try {
                                    inList = true
                                    JSONObject(it).optInt("last")
                                }//catch if not present in memory
                                catch (_ : Exception) {
                                    inList = false
                                    return@let 0
                                }
                            }  ?: 0
                        withContext(Dispatchers.Main) {
                            if (inList)
                                binding.addButton.
                                setImageResource(R.drawable.ic_check)
                        }
                    }
                    tot = if (elementType== ANIME)
                        elementData.getString("episodes").toIntOrNull() ?: 0
                    else elementData.getString("chapters").toIntOrNull() ?: 0

                    withContext(Dispatchers.Main) {
                        if (elementType == ANIME)
                            showKonfetti(last)
                    }

                    if(elementType == ANIME && elementData.getString("episodes") != "1") {
                       //binding the empty adapter for episodes
                        binding.episodesList.adapter = EpisodesRecyclerViewAdapter(
                            jsonList[0],
                            this@AnimeFragment,
                            last
                        )

                        manageApi(
                            binding.resultErrEpisodes,
                            "Episodes",
                            0,
                            binding.episodesList
                        )
                    }

                    //anime movies or special with 1 episode
                    if(elementType == ANIME && elementData.getInt("episodes") == 1) {
                        val jsonObject = JSONObject()
                        jsonObject.put("mal_id", "1")
                        jsonObject.put("title", elementData.getString("title"))
                        jsonObject.put("aired", elementData.getJSONObject("aired").getString("from"))
                        jsonList[0].add(jsonObject)
                        binding.episodesList.adapter = EpisodesRecyclerViewAdapter(
                            jsonList[0],
                            this@AnimeFragment,
                            last
                        )

                    }

                    //manga
                    else if (elementType == MANGA) {
                        val chapters = JSONObject()
                        chapters.put("chapters", elementData.getString("chapters"))
                        jsonList[0].add(chapters)
                        binding.episodesList.adapter = EpisodesRecyclerViewAdapter(
                            jsonList[0],
                            this@AnimeFragment,
                            last
                        )
                    }
                } catch (e: Exception) {
                    if (e is IOException) {
                        binding.resultErrEpisodes.text = getText(R.string.networkError)
                        binding.resultErrEpisodes.setTextColor(Color.parseColor("#DD2C00"))
                       binding.addButton.visibility = View.GONE
                    }
                }
            }
        }
        else {
            showResult(elementData)
            binding.episodesList.adapter = EpisodesRecyclerViewAdapter(jsonList[0],this@AnimeFragment, last)
        }

        //listener button to add element to followed list
        binding.addButton.setOnClickListener {
            //click su add button quando un elemento è già nella lista
            //TODO messaggio di conferma?? perchè si sta per cancellare la lista di episodi visti (o forse no?)
            if (inList) {
                activity?.supportFragmentManager?.fragments?.last()
                val dialog = ConfirmDialogFragment.newInstance(elementId, elementType)
                activity?.supportFragmentManager?.let { it1 -> dialog.show(it1, "AlertDialog") }
            }
            else {
                lifecycleScope.launch (Dispatchers.IO) {
                    saveLastWatchedEp(0)
                }
                binding.addButton.setImageResource(R.drawable.ic_check)
                inList = true
            }
        }

        addOnRecyclerScrolled(binding.resultErrEpisodes, "Episodes", 0, binding.episodesList)
    }

    //retrieve data when available and display
    private fun showResult(anime: JSONObject) {
        try {
            val imgURL = anime.getJSONObject("images").getJSONObject("jpg")
            val title = anime.getString("title")+ "     [${anime.getString("status")}]"
            var genres = "Genres: "
            for( j in 0 until anime.getJSONArray("genres").length())
                genres += ", " + anime.getJSONArray("genres").getJSONObject(j).getString("name")

            Glide.with(this)
                .load(imgURL.getString("large_image_url"))
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .into(binding.animeImage)
            binding.animeTitle.title = title
            binding.resultErrEpisodes.text = genres.replaceFirst(", ", "")
            //binding.animeTitle.subtitle = anime.getString("status")


            //TODO sinossi tagliata con MOSTRA AlTRO

        } catch (e : Throwable) { Log.e("myDeferredErr", e.message ?: "null e msg") }
    }


    private suspend fun getLastWatchedEp(): kotlinx.coroutines.flow.Flow<String>? {
        val last = stringPreferencesKey(this.elementId.toString())
        return when(elementType) {
            ANIME -> {
                this.activity?.episodesDataStore?.data?.catch {
                    if (it is IOException)
                        emit(emptyPreferences())
                    else throw it
                }
                ?.map { pref : Preferences->
                    // No type safety.
                    pref[last] ?: ""
                }
            }
            else -> {
                this.activity?.chaptersDataStore?.data?.catch {
                    if (it is IOException)
                        emit(emptyPreferences())
                    else throw it
                }
                ?.map { pref : Preferences->
                    // No type safety.
                    pref[last] ?: ""
                }
            }
        }
    }

    suspend fun saveLastWatchedEp(last : Int) {

        val jsonDataStore = JSONObject()
        jsonDataStore.put("type", elementType)
        jsonDataStore.put("mal_id", elementId)
        jsonDataStore.put("title", elementData.getString("title"))
        jsonDataStore.put("img", elementData.getJSONObject("images").getJSONObject("jpg").getString("large_image_url"))
        jsonDataStore.put("last", last)
        jsonDataStore.put("tot",
            if (elementType== ANIME)
                elementData.getString("episodes")
            else elementData.getString("chapters")
            )
        if (elementType == ANIME) {
            val duration = kotlin.time.Duration.parse(
                elementData.optString("duration").replace(" hr", "h").replace(" min", "m")
                    .replace(Regex("m.*"), "m")
            )
            jsonDataStore.put("duration", duration)
        }

        Log.d("mytagSavedLast", last.toString())
        if (elementType == ANIME) {
            activity?.episodesDataStore?.edit { pref ->
                pref[stringPreferencesKey(elementId.toString())] = jsonDataStore.toString()
            }
        }
        else {
            activity?.chaptersDataStore?.edit { pref ->
                pref[stringPreferencesKey(elementId.toString())] = jsonDataStore.toString()
            }
        }
    }

    fun showKonfetti(last: Int) {
        if (last == tot) {
            binding.konfettiViewAnimeFrag.start(
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(100),
                    position = Position.Relative(0.5, 0.2)
                )
            )
            Snackbar.make(
                this.requireView(),
                getString(R.string.finishedElement, elementData.getString("title")),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        else if (inList) {
            Snackbar.make(
                this.requireView(),
                if (tot.toString() == "null")
                    getString(R.string.watchedEpNoMax, last, elementData.getString("title"))
                else getString(R.string.watchedEp, tot - last, elementData.getString("title")),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        private const val ARG_ID = "id"
        private const val ARG_TYPE = "type"
        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(id : Int, type: Int) =
            AnimeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, id)
                    putInt(ARG_TYPE, type)
                }
            }
    }

    //alert dialog methods implementation
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        //confirmation to delete data related to element
        lifecycleScope.launch (Dispatchers.IO) {
            if (elementType == ANIME) {
                activity?.episodesDataStore?.edit {
                    it.remove(stringPreferencesKey(elementId.toString()))
                }
            }
            else {
                activity?.chaptersDataStore?.edit {
                    it.remove(stringPreferencesKey(elementId.toString()))
                }
            }
        }
        //update ui
        inList = false
        last = 0
        (binding.episodesList.adapter as EpisodesRecyclerViewAdapter).updateLast(last)
        binding.addButton.setImageResource(R.drawable.ic_plus_small)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
    }
}