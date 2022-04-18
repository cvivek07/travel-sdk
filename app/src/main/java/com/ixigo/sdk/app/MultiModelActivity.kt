package com.ixigo.sdk.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.app.databinding.ActivityMultiModelBinding
import com.ixigo.sdk.flights.FlightPassengerData
import com.ixigo.sdk.flights.FlightSearchData
import java.time.LocalDate

class MultiModelActivity: FragmentActivity() {

  private lateinit var binding: ActivityMultiModelBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val searchData = FlightSearchData(
      origin = "DEL",
      destination = "BOM",
      departDate = LocalDate.now().plusDays(1),
      source = "FlightSearchFormFragment",
      flightClass = "e",
      passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)
    )
    val fragment = IxigoSDK.instance.flightsMultiModelFragment(searchData)

    binding = ActivityMultiModelBinding.inflate(layoutInflater)

    supportFragmentManager
      .beginTransaction()
      .add(binding.fragmentContainerView.id, fragment)
      .commit()
    setContentView(binding.root)

    supportFragmentManager.executePendingTransactions()
  }
}