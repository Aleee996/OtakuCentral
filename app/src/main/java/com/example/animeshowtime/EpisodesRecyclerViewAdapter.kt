package com.example.animeshowtime

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.animeshowtime.databinding.FragmentAnimeBinding
import com.example.animeshowtime.databinding.RecyclerHeaderBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat


class EpisodesRecyclerViewAdapter(

    private val values: ArrayList<JSONObject>,
    private val parentFragment : AnimeFragment,
    private var last : Int,
    private val dateFormat: DateFormat = getDateInstance(DateFormat.MEDIUM)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val recFixedHeader: Int = 0
    private val recListContent: Int = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            recFixedHeader -> {
                ViewHolderHeader(
                    RecyclerHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                ViewHolder(
                    FragmentAnimeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        if (holder is ViewHolderHeader) {
            try {
                holder.itemSynopsis.text = item.getString("synopsis")
            }catch (e : Exception) {
                holder.itemSynopsis.text=""
                Log.e("mytagSynopsis", e.message ?: e.toString())
            }
        }
        else if (holder is ViewHolder) {
            try {
                holder.itemEpId.text = item.getString("mal_id")
                holder.itemEpTitle.text = item.getString("title")
                holder.itemEpId.text.toString().toIntOrNull()?.let {
                    holder.itemEpSeen.isChecked = it <= last
                }
                val initDate = SimpleDateFormat("yyyy-MM-dd").parse(item.getString("aired").split('T')[0])
                holder.itemEpDate.text = initDate?.let { dateFormat.format(it) }
            } catch (e : Exception) {
                Log.e("mytagEpisodes", e.message ?: e.toString())
            }
        }
    }

    override fun getItemCount(): Int = values.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            recFixedHeader
        else recListContent
    }

    inner class ViewHolder(binding: FragmentAnimeBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemEpId = binding.listEpId
        val itemEpTitle = binding.listEpTitle
        val itemEpDate = binding.listEpDate
        val itemEpSeen = binding.listEpSeen
        //TODO listener checkbox
        init {
            binding.listEpSeen.setOnClickListener{
                val parent = it.parent as ViewGroup
                val ep = parent.getChildAt(0) as TextView
                var toUpdate = 0
                if (it is CheckBox && ep.id == R.id.listEpId) {
                    toUpdate = last
                    last = ep.text.toString().toIntOrNull() ?: last
                    if (!it.isChecked)
                        last--
                    else toUpdate=last
                }
                notifyItemRangeChanged(0, toUpdate)
                //Unknow bug recycler duplicates itself
                parentFragment.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        Log.d("mytagCheckBox", last.toString())
                        parentFragment.activity?.dataStore?.edit { episodes ->
                            episodes[intPreferencesKey(parentFragment.animeId.toString())] = last

                        }
                    }
                }
            }
        }
    }

    inner class ViewHolderHeader(binding: RecyclerHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemSynopsis = binding.animeSynopsis

        init {
            //TODO VIEW MORE https://stackoverflow.com/questions/19099296/set-text-view-ellipsize-and-add-view-more-at-end
            binding.animeSynopsis.setOnClickListener {
                if (binding.animeSynopsis.ellipsize == null) {
                    binding.animeSynopsis.ellipsize = TextUtils.TruncateAt.END
                    binding.animeSynopsis.maxLines = 4
                } else {
                    binding.animeSynopsis.maxLines = Int.MAX_VALUE
                    binding.animeSynopsis.ellipsize = null
                }
            }
        }
    }

}