package com.ixigo.sdk.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.app.databinding.ActivityMultiModelBinding
import com.ixigo.sdk.bus.BusSDK
import com.ixigo.sdk.bus.BusSearchData
import com.ixigo.sdk.flights.FlightPassengerData
import com.ixigo.sdk.flights.FlightSearchData
import com.ixigo.sdk.flights.flightsMultiModelFragment
import com.ixigo.sdk.flights.flightsTripsFragment
import com.ixigo.sdk.trains.TrainsSDK
import java.time.LocalDate

class TrainsTripsFragmentActivity: FragmentActivity() {

  private lateinit var binding: ActivityMultiModelBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val fragment = TrainsSDK.instance.tripsFragment()

    binding = ActivityMultiModelBinding.inflate(layoutInflater)

    supportFragmentManager
      .beginTransaction()
      .add(binding.fragmentContainerView.id, fragment)
      .commit()
    setContentView(binding.root)

    supportFragmentManager.executePendingTransactions()
  }
}