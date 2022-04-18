package com.ixigo.sdk.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.app.databinding.ActivityMultiModelBinding
import com.ixigo.sdk.bus.BusSDK
import com.ixigo.sdk.bus.BusSearchData
import java.time.LocalDate

class BusMultiModelActivity: FragmentActivity() {

  private lateinit var binding: ActivityMultiModelBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val searchData = BusSearchData(
      sourceId = 3,
      sourceName = "Hyderabad",
      destinationId = 5,
      destinationName = "Vijayawada",
      date = LocalDate.now().plusDays(1)
    )

    val fragment = BusSDK.instance.multiModelFragment(searchData)

    binding = ActivityMultiModelBinding.inflate(layoutInflater)

    supportFragmentManager
      .beginTransaction()
      .add(binding.fragmentContainerView.id, fragment)
      .commit()
    setContentView(binding.root)

    supportFragmentManager.executePendingTransactions()
  }
}