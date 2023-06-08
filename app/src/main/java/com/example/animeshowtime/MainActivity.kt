package com.example.animeshowtime

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.MenuProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.animeshowtime.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.util.concurrent.TimeUnit


val Activity.episodesDataStore: DataStore<Preferences> by preferencesDataStore("episodes")
val Activity.chaptersDataStore: DataStore<Preferences> by preferencesDataStore("chapters")
const val CHANNEL_ID = "OtakuCentralChannelId"
var notificationInterval = "24h"
const val notificationId = 1

lateinit var menu: Menu
const val ANIME = 0
const val MANGA = 1
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //code just to clear pref if needed
        /*lifecycleScope.launch {
            episodesDataStore.edit { it.clear() }
            chaptersDataStore.edit { it.clear() }
        }*/

        createNotificationChannel()
        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences?.getBoolean("notificationOn", true).let {
            if (it == true) {
                sharedPreferences?.getString("notificationInterval", "24h").let { string ->
                    if (string != null) {
                        notificationInterval = string
                    }
                }
                createNotificationWorker(kotlin.time.Duration.parse(notificationInterval))
            }
        }



        try {
            //restore old state or homepage
            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.fragment_container, TopFragment())
                }
            }
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            //set menu
            setMenuProvider()
        } catch (e : Exception) {Log.e("mytagerr", e.message ?: e.toString())}
    }


    override fun onStart() {
        super.onStart()

        //Homepage
        binding.homeButton.setOnClickListener {
            if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is TopFragment) {
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragment_container, TopFragment())

                }
            }
            menu.findItem(R.id.menuSearch).collapseActionView()
        }

        //Followed Anime Page
        binding.animeButton.setOnClickListener {
            //if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is TopFragment) {
                //supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    replace(R.id.fragment_container, FollowedFragment.newInstance(ANIME))
                }
            //}
            menu.findItem(R.id.menuSearch).collapseActionView()
        }

        //Followed Manga Page
        binding.mangaButton.setOnClickListener {
            //if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is TopFragment) {
            //supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                replace(R.id.fragment_container, FollowedFragment.newInstance(MANGA))
            }
            //}
            menu.findItem(R.id.menuSearch).collapseActionView()
        }

        //Profile page
        binding.profileButton.setOnClickListener {
            //if (supportFragmentManager.findFragmentById(R.id.fragment_container) !is TopFragment) {
            //supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                replace(R.id.fragment_container, ProfileFragment())
            }
            //}
            menu.findItem(R.id.menuSearch).collapseActionView()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun setMenuProvider() {
        addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                com.example.animeshowtime.menu = menu
                menuInflater.inflate(R.menu.menu, menu)
                val searchItem = menu.findItem(R.id.menuSearch)
                val searchView = searchItem?.actionView as SearchView
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                //attaching listener to search view to load in search fragment
                searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                        searchView.isIconified = false
                        searchView.post {
                            searchView.requestFocus()
                        }
                        if (supportFragmentManager.findFragmentById(R.id.fragment_container) is SearchFragment ||
                            supportFragmentManager.findFragmentById(R.id.fragment_container)  is SearchResultFragment)
                            return true
                        imm.showSoftInput(searchView, 0)

                        try {
                            supportFragmentManager.popBackStack(
                                "topIntoAnime",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            supportFragmentManager.popBackStack(
                                "search",
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            supportFragmentManager.commit {
                                setReorderingAllowed(true)
                                addToBackStack("search")
                                replace(R.id.fragment_container, SearchFragment())
                            }
                        } catch (e: Exception) {Log.e("mytagFragManager", e.message ?: e.toString())}
                        return true
                    }

                        override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                            imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                            searchView.setQuery("", false)
                            //supportFragmentManager.popBackStack()
                            return true
                        }
                })

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        })
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotificationWorker(interval: kotlin.time.Duration) {

        val notificationRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(interval.inWholeMinutes, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()

        /*WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(notificationRequest.id)
            .observeForever { value ->
                if (value == null)
                    Log.d("worker", "workinfo is null")
                else
                    Log.d("worker","workInfo != null: " + value.state.toString())
            }*/

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("notify", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, notificationRequest)

    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            com.example.animeshowtime.menu = menu
        }
        menuInflater.inflate(R.menu.menu, menu)
        //attaching listener to search view to load in search fragment
        val searchItem = menu?.findItem(R.id.menuSearch)
        val searchView = searchItem?.actionView as SearchView

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                searchView.isIconified = false
                searchView.post {
                    val b = searchView.requestFocus()
                    val c = searchView.isFocused
                }
                imm.showSoftInput(searchView, 0)

                supportFragmentManager.popBackStack("topIntoAnime", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.popBackStack("search", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack("search")
                    replace(R.id.fragment_container, SearchFragment())
                }
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                //supportFragmentManager.popBackStack()
                return true
            }

        })


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchString: String): Boolean {
                imm.hideSoftInputFromWindow(searchView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                supportFragmentManager.popBackStack("searchResult", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack("searchResult")
                    replace(R.id.fragment_container, SearchResultFragment.newInstance(searchString))
                }
                return true
            }
            override fun onQueryTextChange(searchString: String?): Boolean {

                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("mytagMenu", "clicked menu")
        return super.onOptionsItemSelected(item)
    }*/

    override fun onPause() {
        super.onPause()
        Log.d("MystatusTag", "act on pause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MystatusTag", "act on stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MystatusTag", "act on destroy")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    /*fun turnOffScroll(){
        turnOffScrollBars(binding.root)
    }
    private fun turnOffScrollBars(view: View) {
        view.isHorizontalScrollBarEnabled = false
        view.isVerticalScrollBarEnabled = false
        if(view is ViewGroup)
            for(child in view.children) {
                turnOffScrollBars(child)
            }
    }*/


    /*class NotificationWorker(appContext: Context, workerParams: WorkerParameters):
        Worker(appContext, workerParams) {

        override fun doWork(): Result {

            Toast.makeText(applicationContext, "Worker on", Toast.LENGTH_LONG).show()
            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_image)
                .setContentTitle("textTitle")
                .setContentText("textContent $notificationId")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(applicationContext)) {
                // notificationId is a unique int for each notification that you must define
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return Result.failure()
                }
                notify(notificationId, builder.build())
            }

            notificationId++
            // Indicate whether the work finished successfully with the Result
            return Result.success()
        }
    }*/



}


class NotificationWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            notificationId, createNotification()
        )
    }
    override fun doWork(): Result {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(applicationContext.getText(R.string.notificationTitle))
            .setContentText(applicationContext.getText(R.string.notifcationContent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return Result.failure()
            } else {
                notify(notificationId, builder.build())
                // Indicate whether the work finished successfully with the Result
                return Result.success()
            }

        }
        return Result.failure()
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_image)
            .setContentTitle("OtakuCentral is working in the background")
            .setContentText("Retrieving info...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        return builder.build()
    }

}