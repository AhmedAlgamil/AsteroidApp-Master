package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.trackmysleepquality.database.AsteroidsDatabase
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    var adapter = AsteroidAdapters(AsteroidAdapters.AsteroidsClickListner { asteroidId ->
        viewModel.onAsteroidClicked(asteroidId)
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        val application = requireNotNull(this.activity).application

        val dataSource = AsteroidsDatabase.getInstance(application).asteroidsDatabaseDao

        val viewModelFactory = AsteroidViewModelFactory(dataSource, application)

        val mainViewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(MainViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.navigateToAsteroidDetails.observe(viewLifecycleOwner, Observer { asteroid ->
            asteroid?.let {
                this.findNavController().navigate(
                    MainFragmentDirections
                        .actionShowDetail(asteroid)
                )
                viewModel.onMainFragmentDetailsNavigated()
            }

        })

        val manager = LinearLayoutManager(activity)
        binding.asteroidRecycler.layoutManager = manager

        adapter = AsteroidAdapters(AsteroidAdapters.AsteroidsClickListner { asteroidId ->
            viewModel.onAsteroidClicked(asteroidId)
        })

        binding.asteroidRecycler.adapter = adapter

        viewModel.asteroids.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val current = formatter.format(time)
        val date = Calendar.getInstance()   // 19-01-2018
        date.add(Calendar.DATE, +7)
        val nextSevenDays = formatter.format(date.time)
        if (item.itemId == R.id.show_all_menu) {
            viewModel.getAllAsteroidsFunction(current,nextSevenDays)
            viewModel.asteroids.observe(viewLifecycleOwner, Observer {
                it?.let {
                    adapter.submitList(it)
                }
            })
        }
        else if(item.itemId == R.id.show_today_menu) {
            viewModel.getAllAsteroidsFunction(current,current)
            viewModel.asteroids.observe(viewLifecycleOwner, Observer {
                it?.let {
                    adapter.submitList(it)
                }
            })
        }
        else if(item.itemId == R.id.show_saved_asteroids) {
            viewModel.getAllAsteroidsFunction(current,nextSevenDays)
            viewModel.asteroids.observe(viewLifecycleOwner, Observer {
                it?.let {
                    adapter.submitList(it)
                }
            })
        }

        return true
    }
}
