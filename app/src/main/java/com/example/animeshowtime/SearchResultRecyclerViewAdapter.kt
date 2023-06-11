package com.example.animeshowtime

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.res.TypedArrayUtils.getText
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.animeshowtime.databinding.FragmentSearchResultBinding
import com.example.animeshowtime.placeholder.PlaceholderContent.PlaceholderItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        try {
            if (parentFragment is SearchResultFragment) {
                holder.itemAddNext.visibility = View.GONE
            }
            else {
                holder.itemPosition = position
                holder.jsonItem = item
                holder.next = item.getString("last").toIntOrNull()?.plus(1) ?: 0
                holder.tot = item.getString("tot")
            }
            holder.itemMalId = item.getString("mal_id")
            val url = if (parentFragment is SearchResultFragment)
                item.getJSONObject("images").getJSONObject("jpg").getString("large_image_url")
            else item.getString("img")
            Glide.with(holder.itemImage.context)
                .load(url)
                //.fitCenter()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .into(holder.itemImage)
            holder.itemTitle.text = item.getString("title")
            holder.itemType.text = if (parentFragment is SearchResultFragment)
                item.getString("type")
            else holder.next.toString()
            holder.itemId.text = item.getString("mal_id")
        } catch (e: Exception) {
            //Log.e("mytagFragManager", e.message ?: e.toString())
        }
    }

    override fun getItemCount(): Int = values.size

    /*fun addValues (newList : ArrayList<JSONObject>) {
        val startPosition = itemCount
        values.addAll(newList)
        notifyItemRangeInserted(startPosition, newList.size)
    }*/
    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        //recycle imageview
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
        val itemAddNext = binding.addNextCheckbox
        var itemMalId = "0"
        var next = 0
        var tot = "0"
        var jsonItem = JSONObject()
        var itemPosition = 0

        init {
            val elementType = when (parentFragment) {
                is SearchResultFragment -> parentFragment.elementType
                is FollowedFragment -> parentFragment.elementType
                else -> 0
            }

            binding.root.setOnClickListener {
                //Log.i("myTagSearch", itemTitle.text.toString() + " " + itemId.text.toString())
                parentFragment.activity?.supportFragmentManager?.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace(
                        R.id.fragment_container,
                        AnimeFragment.newInstance(itemMalId.toIntOrNull() ?: 0, elementType)
                    )
                }
            }

            //adding next ep and saving it
            if (parentFragment is FollowedFragment) {
                binding.addNextCheckbox.setOnClickListener {
                    binding.root.isClickable = false
                    parentFragment.lifecycleScope.launch (Dispatchers.IO) {
                        jsonItem.put("last", next)
                        if (elementType == ANIME) {
                            parentFragment.activity?.episodesDataStore?.edit {
                                it[stringPreferencesKey(itemMalId)] = jsonItem.toString()
                            }
                        }
                        else {
                            parentFragment.activity?.chaptersDataStore?.edit {
                                it[stringPreferencesKey(itemMalId)] = jsonItem.toString()
                            }
                        }
                        withContext(Dispatchers.Main) {
                            if (next == tot.toIntOrNull()) {
                                parentFragment.showKonfetti()
                                Snackbar.make(
                                    parentFragment.requireView(),
                                    parentFragment.getString(R.string.finishedElement, itemTitle.text),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                values.removeAt(itemPosition)
                                notifyItemRemoved(itemPosition)
                                notifyItemRangeChanged(0, itemCount)
                            }
                            else {
                                Snackbar.make(
                                    parentFragment.requireView(),
                                    if (tot == "null")
                                        parentFragment.getString(R.string.watchedEpNoMax, next, itemTitle.text)
                                    else parentFragment.getString(R.string.watchedEp, tot.toInt() - next, itemTitle.text),
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                next++
                                itemType.text = next.toString()
                                binding.root.isClickable = true
                                (it as CheckBox).isChecked = false
                                notifyItemChanged(itemPosition)
                            }
                        }
                    }
                }
            }


            /*binding.addButtonInSearch.setOnClickListener {
                val jsonDataStore = JSONObject()
                jsonDataStore.put("type", elementType)
                //jsonDataStore.put("id", elementId) id già presente come chiave
                jsonDataStore.put("title", itemTitle)
                //jsonDataStore.put("img", itemImage)
                jsonDataStore.put("last", 0)
                jsonDataStore.put("tot",
                    if (elementType== ANIME)
                        elementData.getString("episodes")
                    else elementData.getString("chapters")
                )
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

            }*/
        }


        override fun toString(): String {
            return super.toString() + " '" + itemTitle.text + "'"
        }
    }
}