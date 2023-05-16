package com.example.animeshowtime

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animeshowtime.databinding.FragmentTopBinding
import com.example.animeshowtime.placeholder.PlaceholderContent.PlaceholderItem
import org.json.JSONObject

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class TopRecyclerViewAdapter(
    private val values: ArrayList<JSONObject>,
    private val parentFragment: Fragment
) : RecyclerView.Adapter<TopRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentTopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        val url = item.getJSONObject("images").getJSONObject("jpg").getString("image_url")
        //
        Glide.with(holder.itemImage.context)
            .load(url)
            //.fitCenter()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_broken_image)
            .into(holder.itemImage)
        holder.itemId.text = item.getString("mal_id")
    }

    override fun getItemCount(): Int = values.size

    override fun onViewRecycled(holder: TopRecyclerViewAdapter.ViewHolder) {
        super.onViewRecycled(holder)
        //recycle imageview TODO check fragment as context
        holder.itemImage.layout(0,0,0,0)
        Glide.with(holder.itemImage.context)
            .clear(holder.itemImage)

    }
    inner class ViewHolder(binding: FragmentTopBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemImage = binding.listImgTop
        val itemId = binding.listIdTop

        init {
            binding.root.setOnClickListener {
                if (binding.root.parent is RecyclerView && (binding.root.parent as RecyclerView).id==R.id.topTvAnime ) {
                    parentFragment.activity?.supportFragmentManager?.commit {
                        setReorderingAllowed(true)
                        addToBackStack("topIntoAnime")
                        replace(
                            R.id.fragment_container,
                            AnimeFragment.newInstance(itemId.text.toString().toIntOrNull() ?: 0)
                        )
                    }
                }
                else {
                    Toast.makeText(parentFragment.activity, "WIP manga page", Toast.LENGTH_SHORT).show()
                }
            }
        }


        /*override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }*/
    }

}