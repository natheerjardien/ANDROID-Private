package com.example.tuniq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Context
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tuniq.adapters.SongRowAdapter
import com.example.tuniq.adapters.TrackAdapter
import com.example.tuniq.api.JamendoApiService
import com.example.tuniq.api.JamendoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchFragment : Fragment() {
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var songRowAdapter: SongRowAdapter
    private lateinit var jamendoApi: JamendoApiService

    private val jamendoClientId = "1f49c990"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_view, container, false)

        val etSearchBox = view.findViewById<EditText>(R.id.etSearchBox)
        rvSearchResults = view.findViewById(R.id.rvSearchResults)

        // Initializes our TrackAdapter with an empty list
        songRowAdapter = SongRowAdapter(emptyList())
        rvSearchResults.adapter = songRowAdapter

        // Build Retrofit for Jamendo
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.jamendo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        jamendoApi = retrofit.create(JamendoApiService::class.java)

        // Trigger search when user presses 'Search' or 'Enter' on their keyboard
        etSearchBox.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = etSearchBox.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)

                    // Hide the keyboard so the user can see the results
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
                true
            } else {
                false
            }
        }

        // Added this to check if a genre query was passed from the Home screen categories
        val prefilledQuery = arguments?.getString("PREFILLED_SEARCH_QUERY")
        if (!prefilledQuery.isNullOrEmpty()) {
            etSearchBox.setText(prefilledQuery)
            performSearch(prefilledQuery) // Automatically fetch the songs for that genre
        }

        return view
    }

    private fun performSearch(query: String) {
        jamendoApi.searchTracks(clientId = jamendoClientId, searchQuery = query).enqueue(object : Callback<JamendoResponse> {
            override fun onResponse(call: Call<JamendoResponse>, response: Response<JamendoResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    if (tracks.isEmpty()) {
                        Toast.makeText(requireContext(), "No results found for '$query'", Toast.LENGTH_SHORT).show()
                    }
                    // Update your TrackAdapter using your exact method name
                    songRowAdapter.updateData(tracks)
                } else {
                    Log.e("JamendoAPI", "Search HTTP Code: ${response.code()}")
                    Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JamendoResponse>, t: Throwable) {
                Log.e("JamendoAPI", "Network error", t)
                Toast.makeText(requireContext(), "Network Error. Check internet connection.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}