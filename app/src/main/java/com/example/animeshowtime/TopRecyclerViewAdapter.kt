package com.example.animeshowtime

import android.app.ActionBar.LayoutParams
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
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
        val url = if (parentFragment is ProfileFragment)
            item.getString("img")
        else item.getJSONObject("images").getJSONObject("jpg").getString("large_image_url")
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
        //recycle imageview
        //TODO check if cause of fast scroll back
        holder.itemImage.layout(0,0,0,0)
        Glide.with(holder.itemImage.context)
            .clear(holder.itemImage)

    }
    inner class ViewHolder(binding: FragmentTopBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemImage = binding.listImgTop
        val itemId = binding.listIdTop

        init {

            if (parentFragment is SearchFragment) {
                var dens = 0F
                val scaling : Float = if (parentFragment.resources.displayMetrics.run { dens = density
                        heightPixels/density } > 450) 4F else 2.7F
                binding.linearLayoutTop.layoutParams?.height = (parentFragment.resources.displayMetrics.heightPixels/scaling).toInt()
                binding.linearLayoutTop.layoutParams?.width = (parentFragment.resources.displayMetrics.widthPixels/3.5F).toInt()
                //TODO margins togethaaa
                (binding.linearLayoutTop.layoutParams as ViewGroup.MarginLayoutParams).setMargins((dens *3).toInt())
                binding.listImgTop.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.listImgTop.layoutParams?.width = LayoutParams.MATCH_PARENT
            }

            binding.root.setOnClickListener {

                val elementType =
                    //if anime recycler
                    if (binding.root.parent is RecyclerView && (binding.root.parent as RecyclerView).id==R.id.topTvAnime) {
                    ANIME
                }
                else if(parentFragment is SearchFragment)
                parentFragment.elementType
                //else manga recycler
                else {
                    MANGA
                }

                parentFragment.activity?.supportFragmentManager?.commit {
                    setReorderingAllowed(true)
                    addToBackStack("topIntoAnime")
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    replace(
                        R.id.fragment_container,
                        AnimeFragment.newInstance(itemId.text.toString().toIntOrNull() ?: 0, elementType)
                    )
                }
            }
        }


        /*override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }*/
    }

}