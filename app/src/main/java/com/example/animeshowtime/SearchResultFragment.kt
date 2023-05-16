package com.example.animeshowtime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.animeshowtime.databinding.FragmentSearchResultListBinding
import org.json.JSONObject


private const val ARG_SEARCH_STRING = "null"

class SearchResultFragment : RecyclerFragment(
    Array(1) {"null"},
    Array(1) {ArrayList<JSONObject>()},
    Array(1) { 1 },
    Array(1) { false },
) {
    private lateinit var binding: FragmentSearchResultListBinding
    private var searchedElement : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            searchedElement = it.getString(ARG_SEARCH_STRING).toString()
            searchString[0] = "https://api.jikan.moe/v4/anime?q=$searchedElement"
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

        Log.i("mytagArg", searchedElement)
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

        // TODO: Customize parameter argument names
        //const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(arg1 : String) =
            SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SEARCH_STRING, arg1)
                }
            }
    }
}