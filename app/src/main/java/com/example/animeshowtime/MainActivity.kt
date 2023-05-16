package com.example.animeshowtime

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.animeshowtime.databinding.ActivityMainBinding


val Activity.dataStore: DataStore<Preferences> by preferencesDataStore("episodes")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.fragment_container, TopFragment())
                }
            }
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } catch (e : Exception) {Log.e("mytagerr", e.message ?: e.toString())}
    }

    override fun onStart() {
        super.onStart()
        binding.homeButton.setOnClickListener {
            if(supportFragmentManager.findFragmentById(R.id.fragment_container) !is TopFragment) {
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragment_container, TopFragment())
                }
            }
        }
        binding.animeButton.setOnClickListener {
            Toast.makeText(this, "WIP: anime page tracking", Toast.LENGTH_SHORT).show()
        }
        binding.mangaButton.setOnClickListener {
            Toast.makeText(this, "WIP: manga page tracking", Toast.LENGTH_SHORT).show()
        }
        binding.profileButton.setOnClickListener {
            Toast.makeText(this, "WIP: profile page", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        //attaching listener to search view to load in search fragment
        val searchItem = menu?.findItem(R.id.menuSearch)
        val searchView = searchItem?.actionView as SearchView

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                //Focus on the keyboard not working after the first search
                searchView.isIconified = false
                /*search.post { search.requestFocus() }
                val c = search.isFocused
                imm.showSoftInput(search, 0)*/
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("mytagMenu", "clicked menu")
        return super.onOptionsItemSelected(item)
    }

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

}