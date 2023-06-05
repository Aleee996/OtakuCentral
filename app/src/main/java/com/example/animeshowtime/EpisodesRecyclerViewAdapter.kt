package com.example.animeshowtime

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.animeshowtime.databinding.FragmentAnimeBinding
import com.example.animeshowtime.databinding.FragmentMangaBinding
import com.example.animeshowtime.databinding.RecyclerHeaderBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private val recFixedHeader = 0
    private val recListContent = 1
    private val recMangaList = 2
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

            recMangaList -> {
                ViewHolderManga(
                    FragmentMangaBinding.inflate(
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
        when (holder) {
            is ViewHolderHeader -> {
                try {
                    holder.itemSynopsis.text = item.getString("synopsis")
                }catch (e : Exception) {
                    holder.itemSynopsis.text=""
                    Log.e("mytagSynopsis", e.message ?: e.toString())
                }
            }

            is ViewHolder -> {
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

            is ViewHolderManga -> {
                if (item.getString("chapters")=="null") {
                    holder.itemChapters.error = parentFragment.getText(R.string.nullChapters)
                    holder.itemChapters.isErrorEnabled = true
                }
                else {
                    holder.itemChapters.isErrorEnabled = false
                    holder.itemChapters.suffixText = "/" + item.getString("chapters")
                }
                holder.itemChapters.editText?.setText(last.toString())
            }
        }
    }

    override fun getItemCount(): Int = values.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            recFixedHeader
        else if (parentFragment.elementType == MANGA)
            recMangaList
        else recListContent
    }

    fun updateLast (last: Int) {
        this.last = last
        notifyItemRangeChanged(0, values.size)
    }

    //synopsis header viewholder
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

    //anime episodes view holder
    inner class ViewHolder(binding: FragmentAnimeBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemEpId = binding.listEpId
        val itemEpTitle = binding.listEpTitle
        val itemEpDate = binding.listEpDate
        val itemEpSeen = binding.listEpSeen

        //listener checkbox ep added
        init {
            binding.listEpSeen.setOnClickListener{
                val parent = it.parent as ViewGroup
                val ep = parent.getChildAt(0) as TextView
                if (it is CheckBox && ep.id == R.id.listEpId) {
                    last = ep.text.toString().toIntOrNull() ?: last
                    if (!it.isChecked)
                        last--
                }
                notifyItemRangeChanged(0, values.size)
                //saving new episode watched into local storage
                parentFragment.lifecycleScope.launch(Dispatchers.IO) {
                    parentFragment.saveLastWatchedEp(last)
                }
                //change fab icon
                if (!parentFragment.inList) {
                    parentFragment.inList = true
                    val fab = parentFragment.activity?.findViewById<View>(R.id.addButton) as FloatingActionButton
                    fab.setImageResource(R.drawable.ic_check)
                }
                parentFragment.showKonfetti(last)
            }
        }
    }



    //manga view holder
    inner class ViewHolderManga(binding: FragmentMangaBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemChapters = binding.chapterNumber

        init {
            //listener to check if chapter value inserted is ok
            itemChapters.editText?.doAfterTextChanged {
                last = it.toString().toIntOrNull() ?: 0
                val maxChapter = itemChapters.suffixText?.toString()?.substring(1)?.toIntOrNull() ?: 0
                //if not valid value
                if (maxChapter != 0 && ((it.toString().toIntOrNull() ?: 0) > maxChapter)) {
                    itemChapters.error = parentFragment.getText(R.string.wrongChapter)
                    itemChapters.isErrorEnabled = true
                    return@doAfterTextChanged
                    //itemChapters.editText?.setText(maxChapter.toString())
                }
                //else if valid value but no info on max chapter
                else if (maxChapter == 0) {
                    itemChapters.hint = parentFragment.getText(R.string.chaptersHintNoMax)
                    itemChapters.suffixText = ""
                }
                else {
                    itemChapters.isErrorEnabled = false
                }
                if (last != 0 || parentFragment.inList) {
                    parentFragment.lifecycleScope.launch(Dispatchers.IO) {
                        parentFragment.saveLastWatchedEp(last)
                    }
                    if (!parentFragment.inList) {
                        parentFragment.inList = true
                        val fab = parentFragment.activity?.findViewById<View>(R.id.addButton) as FloatingActionButton
                        fab.setImageResource(R.drawable.ic_check)
                    }
                    parentFragment.showKonfetti(last)
                }
            }
        }
    }

}