package com.ixigo.sdk.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.ixigo.sdk.app.databinding.ActivityFakeLoginBinding

class FakeLoginActivity: Activity() {

  private lateinit var binding: ActivityFakeLoginBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityFakeLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)
    val token: String = intent.extras?.getString("token")!!
    val partnerId: String = intent.extras?.getString("partnerId")!!

    binding.partnerId.text = "PartnerId=$partnerId"

    binding.loginSuccessfulButton.setOnClickListener {
      val data = Intent()
      data.putExtra("token", token)
      setResult(200, data)
      finish()
    }

    binding.loginErrorButton.setOnClickListener {
      val data = Intent()
      setResult(400, data)
      finish()
    }
  }
}