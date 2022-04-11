package com.ixigo.sdk.app

import android.app.Activity
import android.os.Bundle
import com.ixigo.sdk.app.databinding.DeeplinkLayoutBinding

class FakeDeeplinkActivity: Activity() {
  private lateinit var binding: DeeplinkLayoutBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = DeeplinkLayoutBinding.inflate(layoutInflater)
    setContentView(binding.root)
  }
}