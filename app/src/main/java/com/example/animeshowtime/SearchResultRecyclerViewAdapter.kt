package com.example.animeshowtime

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animeshowtime.databinding.FragmentSearchResultBinding
import com.example.animeshowtime.placeholder.PlaceholderContent.PlaceholderItem
import org.json.JSONObject


/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class SearchResultRecyclerViewAdapter(
    private var values: ArrayList<JSONObject>,
    private val parentFragment: Fragment
) : RecyclerView.Adapter<SearchResultRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        val url = item.getJSONObject("images").getJSONObject("jpg").getString("image_url")
        Glide.with(holder.itemImage.context)
            .load(url)
            //.fitCenter()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_broken_image)
            .into(holder.itemImage)
        holder.itemTitle.text = item.getString("title")
        holder.itemType.text = item.getString("type")
        holder.itemId.text = item.getString("mal_id")
    }

    override fun getItemCount(): Int = values.size

    /*fun addValues (newList : ArrayList<JSONObject>) {
        val startPosition = itemCount
        values.addAll(newList)
        notifyItemRangeInserted(startPosition, newList.size)
    }*/
    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        //recycle imageview TODO check fragment as context
        holder.itemImage.layout(0,0,0,0)
        Glide.with(holder.itemImage.context)
            .clear(holder.itemImage)
    }

    inner class ViewHolder(binding: FragmentSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val itemImage = binding.listImage
        val itemTitle = binding.listTitle
        val itemType = binding.listType
        val itemId = binding.listId

        init {
            binding.root.setOnClickListener {
                Log.i("myTagSearch", itemTitle.text.toString() + " " + itemId.text.toString())
                parentFragment.activity?.supportFragmentManager?.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace(
                        R.id.fragment_container,
                        AnimeFragment.newInstance(itemId.text.toString().toIntOrNull() ?: 0)
                    )
                }
            }
        }


        override fun toString(): String {
            return super.toString() + " '" + itemTitle.text + "'"
        }
    }
}