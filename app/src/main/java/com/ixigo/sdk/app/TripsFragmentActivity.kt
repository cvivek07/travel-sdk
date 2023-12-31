package com.ixigo.sdk.app

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.app.databinding.ActivityMultiModelBinding

class TripsFragmentActivity: FragmentActivity() {

  private lateinit var binding: ActivityMultiModelBinding
  private lateinit var fragment: Fragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    fragment = IxigoSDK.instance.flightsTripsFragment()

    binding = ActivityMultiModelBinding.inflate(layoutInflater)

    supportFragmentManager
      .beginTransaction()
      .add(binding.fragmentContainerView.id, fragment)
      .commit()
    setContentView(binding.root)

    supportFragmentManager.executePendingTransactions()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    fragment.onActivityResult(requestCode, resultCode, data)
  }
}