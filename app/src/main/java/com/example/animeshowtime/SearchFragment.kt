package com.example.animeshowtime

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.animeshowtime.databinding.FragmentSearchBinding
import com.google.android.material.color.MaterialColors
import org.json.JSONObject
import kotlin.random.Random


class SearchFragment : RecyclerFragment(
    Array(1) {"null"},
    Array(1) {ArrayList<JSONObject>()},
    Array(1) { 1 },
    Array(1) { false },
) {

    var elementType = ANIME
    private lateinit var binding : FragmentSearchBinding
    private val columnCount = 3
    private var sharedPreferences: SharedPreferences? = null
    private var animeGenres = ArrayList<String>()
    private var mangaGenres = ArrayList<String>()
    var rand = 0
    var genreName = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        (sharedPreferences?.getStringSet("genresAnime", null) as Set<String>?).let { stringSet ->
            stringSet?.forEach{
                animeGenres.add(it)
            }
        } ?: run {
            animeGenres.addAll(resources.getStringArray(R.array.genres_default))
        }
        (sharedPreferences?.getStringSet("genresManga", null) as Set<String>?).let { stringSet ->
            stringSet?.forEach{
                mangaGenres.add(it)
            }
        } ?: run {
            animeGenres.addAll(resources.getStringArray(R.array.genres_default))
        }

        val callback = object : OnBackPressedCallback(
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {
                try {
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        binding.listExplore.layoutManager = GridLayoutManager(context, columnCount)
        binding.listExplore.adapter = TopRecyclerViewAdapter(jsonList[0], this)

        /*var rand = Random.nextInt(animeGenres.size)
        searchString[0] = "https://api.jikan.moe/v4/anime?q=&genres=${animeGenres[rand]}"
        var genresValues = resources.getStringArray(R.array.genres_values)
        var genreName = resources.getStringArray(R.array.genres_entries)[genresValues.indexOf(animeGenres[rand])]
        binding.textExplore.text = getString(R.string.genresSuggestion, genreName)*/


        //get searchview from toolbar and attach listener
        val searchView = menu.findItem(R.id.menuSearch)?.actionView as SearchView
        searchView.queryHint = "Anime"

        //serchview listener
        addQueryTextListener(searchView)

        val highlightedColor = MaterialColors.getColor(binding.root, androidx.appcompat.R.attr.colorPrimary)
        val defaultTextColor = binding.exploreAnimeButton.textColors
        binding.exploreAnimeButton.setTextColor(highlightedColor)
        binding.exploreAnimeButton.foreground = AppCompatResources.getDrawable(requireContext(), R.drawable.explore_button_foreground)

        //restore the status to manga if previously chosen
        if (elementType == MANGA) {
            binding.exploreMangaButton.setTextColor(highlightedColor)
            binding.exploreMangaButton.foreground = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.explore_button_foreground
            )
            binding.exploreAnimeButton.foreground =
                AppCompatResources.getDrawable(requireContext(), R.color.transparent)
            binding.exploreAnimeButton.setTextColor(defaultTextColor)
            searchView.queryHint = "Manga"
        }

        //buttons listener
        val btnListener = View.OnClickListener {
            when (it.id) {
                R.id.exploreAnimeButton -> {
                    if (elementType == MANGA) {
                        elementType = ANIME
                        if (animeGenres.size != 0)
                            rand = Random.nextInt(animeGenres.size)
                        else {
                            rand = 0
                            animeGenres.add("1")
                        }
                        searchString[0] = "https://api.jikan.moe/v4/anime?q=&genres=${animeGenres[rand]}"
                        val genresValues = resources.getStringArray(R.array.genres_values)
                        genreName = resources.getStringArray(R.array.genres_entries)[genresValues.indexOf(animeGenres[rand])]
                        binding.textExplore.text = getString(R.string.genresSuggestion, genreName)
                        binding.textExplore.setTextColor(defaultTextColor)
                        nScrolls[0] = 1
                        jsonList[0].clear()
                        manageApi(binding.textExplore, getString(R.string.textGenresError), 0, binding.listExplore)
                        binding.exploreAnimeButton.setTextColor(highlightedColor)
                        binding.exploreAnimeButton.foreground = AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.explore_button_foreground
                        )
                        binding.exploreMangaButton.foreground =
                            AppCompatResources.getDrawable(requireContext(), R.color.transparent)
                        binding.exploreMangaButton.setTextColor(defaultTextColor)
                        searchView.queryHint = "Anime"
                    }
                }
                R.id.exploreMangaButton -> {
                    if (elementType == ANIME) {
                        elementType = MANGA
                        if (mangaGenres.size != 0)
                            rand = Random.nextInt(mangaGenres.size)
                        else {
                            rand = 0
                            mangaGenres.add("1")
                        }
                        searchString[0] = "https://api.jikan.moe/v4/manga?q=&genres=${mangaGenres[rand]}"
                        val genresValues = resources.getStringArray(R.array.genres_values)
                        genreName = resources.getStringArray(R.array.genres_entries)[genresValues.indexOf(mangaGenres[rand])]
                        binding.textExplore.text = getString(R.string.genresSuggestion, genreName)
                        binding.textExplore.setTextColor(defaultTextColor)
                        nScrolls[0] = 1
                        jsonList[0].clear()
                        manageApi(binding.textExplore, getString(R.string.textGenresError), 0, binding.listExplore)
                        binding.exploreMangaButton.setTextColor(highlightedColor)
                        binding.exploreMangaButton.foreground = AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.explore_button_foreground
                        )
                        binding.exploreAnimeButton.foreground =
                            AppCompatResources.getDrawable(requireContext(), R.color.transparent)
                        binding.exploreAnimeButton.setTextColor(defaultTextColor)
                        searchView.queryHint = "Manga"
                    }
                }
            }
        }
        binding.exploreAnimeButton.setOnClickListener(btnListener)
        binding.exploreMangaButton.setOnClickListener(btnListener)

        if (jsonList[0].isEmpty()) {
            if (animeGenres.size != 0)
                rand = Random.nextInt(animeGenres.size)
            else {
                rand = 0
                animeGenres.add("1")
            }
            searchString[0] = "https://api.jikan.moe/v4/anime?q=&genres=${animeGenres[rand]}"
            val genresValues = resources.getStringArray(R.array.genres_values)
            genreName = resources.getStringArray(R.array.genres_entries)[genresValues.indexOf(animeGenres[rand])]
            manageApi(binding.textExplore, getString(R.string.textGenresError), 0, binding.listExplore)
        }
        else {
            binding.listExplore.adapter = TopRecyclerViewAdapter(jsonList[0], this)
        }
        binding.textExplore.text = getString(R.string.genresSuggestion, genreName)
        addOnRecyclerScrolled(binding.textExplore, getString(R.string.textGenresError), 0, binding.listExplore)

    }

    //text listener for the search
    private fun addQueryTextListener(searchView: SearchView) {
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //TODO not getting triggered after config changes
            override fun onQueryTextSubmit(searchString: String): Boolean {
                imm.hideSoftInputFromWindow(searchView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                requireActivity().supportFragmentManager.popBackStack("searchResult", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack("searchResult")
                    replace(R.id.fragment_container, SearchResultFragment.newInstance(searchString, elementType))
                }
                return true
            }
            override fun onQueryTextChange(searchString: String?): Boolean {
                //Log.d("mytagsearchView", searchString ?: "")
                return false
            }
        })
    }


    private fun setMenu() {
        activity?.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }



    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                //Log.d("mytag", "passato da newInstance")
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}