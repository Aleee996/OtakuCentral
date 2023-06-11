package com.example.animeshowtime

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.animeshowtime.databinding.FragmentSearchResultListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
import java.lang.Exception
import java.util.concurrent.TimeUnit

class FollowedFragment : Fragment() {

    private lateinit var binding: FragmentSearchResultListBinding
    var elementType : Int = ANIME
    private val jsonList : ArrayList<JSONObject> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        arguments?.let {
            elementType = it.getInt(ARG_TYPE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_result_list, container, false)
        binding = FragmentSearchResultListBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (elementType == ANIME)
            binding.textResultsFound.text = getText(R.string.animeWatchlist)
        else binding.textResultsFound.text = getText(R.string.mangaReadlist)

        //retrieve last from memory
        if (jsonList.isNotEmpty())
            jsonList.clear()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                getFollowedList()?.first()?.values.let { collection ->
                    collection?.forEach {
                        if (JSONObject(it as String).getString("last").toIntOrNull()
                        != JSONObject(it as String).getString("tot").toIntOrNull()
                        )
                        {jsonList.add(JSONObject(it))}
                    }
                }
                withContext(Dispatchers.Main) {
                    if (jsonList.isEmpty())
                        binding.textResultsFound.text = getString(R.string.emptyFollowedList)
                    binding.list.adapter = SearchResultRecyclerViewAdapter(jsonList, this@FollowedFragment)
                }
            } catch (e: Exception) {
                //Log.e("mytagFollowedList", e.message ?: e.toString())
            }
        }
    }




    private suspend fun getFollowedList(): Flow<Map<Preferences.Key<*>, Any>>? {
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

    fun showKonfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.2)
        )
        binding.konfettiView.start(party)
    }


    companion object {
        private const val ARG_TYPE = "type"
        @JvmStatic
        fun newInstance(elementType: Int) =
            FollowedFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TYPE, elementType)
                }
            }
    }


}