package com.example.animeshowtime

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.animeshowtime.databinding.FragmentExploreGenresBinding
import com.example.animeshowtime.databinding.FragmentProfileBinding
import com.example.animeshowtime.databinding.FragmentTopBinding
import com.example.animeshowtime.placeholder.PlaceholderContent.PlaceholderItem
import org.json.JSONObject

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class ExploreGenresRecyclerViewAdapter(
    //private val values: ArrayList<JSONObject>,
    private val parentFragment: Fragment
) : RecyclerView.Adapter<ExploreGenresRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentProfileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //val item = values[position]

    }

    override fun getItemCount(): Int = 1

    inner class ViewHolder(binding: FragmentProfileBinding) : RecyclerView.ViewHolder(binding.root) {


    }

}