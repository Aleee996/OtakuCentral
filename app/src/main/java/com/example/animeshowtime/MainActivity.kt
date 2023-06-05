package com.example.animeshowtime

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.example.animeshowtime.databinding.ActivityMainBinding
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit


val Activity.episodesDataStore: DataStore<Preferences> by preferencesDataStore("episodes")
val Activity.chaptersDataStore: DataStore<Preferences> by preferencesDataStore("chapters")

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

}