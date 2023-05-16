/*package com.example.animeshowtime

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.animeshowtime.databinding.FragmentAnimeBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

private const val ARG_PARAM1 = "1"

class AnimeFragmentOld : Fragment() {

    private lateinit var binding : FragmentAnimeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_anime_old, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnimeBinding.bind(view)

        //TODO gestire lo 0 come possibile errore
        val animeId = arguments?.getInt(ARG_PARAM1) ?: 1
        Log.i("mytagArg", arguments?.getInt(ARG_PARAM1).toString() ?: "null arg")
        //async coroutine to retrieve data from the jikan API
        val deferred: Deferred<JSONObject> = viewLifecycleOwner.lifecycleScope.async{
            withContext(Dispatchers.IO) {
                httpReqId("https://api.jikan.moe/v4/anime/$animeId")
            }

        }
        viewLifecycleOwner.lifecycleScope.launch {  showResult(deferred) }

        //TODO VIEW MORE https://stackoverflow.com/questions/19099296/set-text-view-ellipsize-and-add-view-more-at-end
        val viewMoreClickListener = View.OnClickListener {
            if (binding.animeSynopsis.ellipsize == null) {
                binding.animeSynopsis.ellipsize = TextUtils.TruncateAt.END
                binding.animeSynopsis.maxLines = 4
            } else {
                binding.animeSynopsis.maxLines = Int.MAX_VALUE
                binding.animeSynopsis.ellipsize = null
            }

        }

        binding.animeSynopsis.setOnClickListener(viewMoreClickListener)


        val deferredEpisodes: Deferred<JSONObject> = viewLifecycleOwner.lifecycleScope.async{
            withContext(Dispatchers.IO) {
                httpReqId("https://api.jikan.moe/v4/anime/$animeId/episodes")
            }

        }
        //viewLifecycleOwner.lifecycleScope.launch {  showResult(deferredEpisodes) }



        /*Log.i("mytagArg", arguments?.getString(ARG_PARAM1) ?: "null arg")
        val deferred: Deferred<JSONObject> = GlobalScope.async{
            httpReq(arguments?.getString(ARG_PARAM1)?.toIntOrNull() ?: 1)
        }
        GlobalScope.async { showResult(deferred)}*/








        /*val request = Request.Builder()
            .url("")
            .build()

        client.newCall(request).enqueue(object : Callback {
            fun onFailure(call: Call, e: IOException) {
                // Handle network error
            }

            fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val anime = JSONObject(responseBody)

                activity?.runOnUiThread {


                    animeTitle.text = anime.getString("title")
                   animeSynopsis.text = anime.getString("synopsis")
                    animeEpisodes.text = anime.getInt("episodes").toString()
                    animeScore.text = anime.getDouble("score").toString()
                }
            }
        }) */
    }

    override fun onResume() {
        super.onResume()

    }


    //retrieve data from the jikan API
    private fun httpReqId(urlString : String) : JSONObject {
        try {
            //HTTP get request from URL
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            //JSONize the data
            val inputStream = BufferedInputStream(connection.inputStream)
            val response = inputStream.bufferedReader().use { it.readText() }
            val jsonResponse = JSONObject(response)
            return JSONObject(jsonResponse.getString("data"))
            //Log.v("Mylog", anime.getString("title"))
        } catch (e : Exception) {
            Log.e("MyHTTPErr", e.message ?: "null e.msg $e")}
        return JSONObject("")
    }

    //retrieve data when available and display
    private suspend fun showResult(deferred: Deferred<JSONObject>) {
        try {
            val anime = deferred.await()
            var imgURLs = JSONObject(anime.getString("images"))
            imgURLs = JSONObject(imgURLs.getString("jpg"))

            Glide.with(this)
                .load(imgURLs.getString("large_image_url"))
                .centerInside()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .into(binding.animeImage)
            binding.animeTitle.text = anime.getString("title")
            binding.animeSynopsis.text = anime.getString("synopsis")
            /*val deferredInputStream = viewLifecycleOwner.lifecycleScope.async {
                withContext(Dispatchers.IO) {
                    URL(imgURL.getString("large_image_url")).openStream()
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val inputStream = deferredInputStream.await()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    activity?.runOnUiThread(Runnable() {
                        binding.animeImage.setImageBitmap(bitmap)
                        binding.animeTitle.text = anime.getString("title")
                        binding.animeSynopsis.text = anime.getString("synopsis") 
                        //binding.animeSynopsis.updateLayoutParams {height =  ViewGroup.LayoutParams.WRAP_CONTENT }
                    })
                }
            }*/

                //sinossi tagliata con MOSTRA AlTRO

            } catch (e : Throwable) { Log.e("myDeferredErr", e.message ?: "null e msg") }
        }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int) =
            AnimeFragmentOld().apply {
                Log.d("mytag", "passato da newInstance")
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }
}*/