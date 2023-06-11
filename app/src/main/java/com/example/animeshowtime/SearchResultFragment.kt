package com.example.animeshowtime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.animeshowtime.databinding.FragmentSearchResultListBinding
import org.json.JSONObject
import java.lang.annotation.ElementType

class SearchResultFragment : RecyclerFragment(
    Array(1) {"null"},
    Array(1) {ArrayList<JSONObject>()},
    Array(1) { 1 },
    Array(1) { false },
) {
    private lateinit var binding: FragmentSearchResultListBinding
    private var searchedElement : String = ""
    var elementType : Int = ANIME
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            elementType = it.getInt(ARG_TYPE)
            searchedElement = it.getString(ARG_SEARCH_STRING).toString()
            if (elementType == ANIME)
            searchString[0] = "https://api.jikan.moe/v4/anime?q=$searchedElement"
            else searchString[0] = "https://api.jikan.moe/v4/manga?q=$searchedElement"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_result_list, container, false)
        binding = FragmentSearchResultListBinding.bind(view)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding the empty adapter
        binding.list.adapter = SearchResultRecyclerViewAdapter(jsonList[0], this)

        //Log.i("mytagArg", searchedElement)
        if (searchedElement != "")
            binding.textResultsFound.text = getString(R.string.textResultsFound, searchedElement)

        if (jsonList[0].isEmpty())
            manageApi(
                binding.textResultsFound,
                searchedElement,
                0,
                binding.list
            )

        //TODO gestire se jsonList è vuoto se non trova risultati o errori di rete
        //else to manage the returning back to search after checking an anime
        else {
            binding.list.adapter = SearchResultRecyclerViewAdapter(jsonList[0], this@SearchResultFragment)
        }
        addOnRecyclerScrolled(
            binding.textResultsFound,
            searchedElement,
            0,
            binding.list
        )
    }


    companion object {


        private const val ARG_SEARCH_STRING = "searched"
        private const val ARG_TYPE = "type"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(searchedElement : String, elementType: Int) =
            SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SEARCH_STRING, searchedElement)
                    putInt(ARG_TYPE, elementType)
                }
            }
    }
}