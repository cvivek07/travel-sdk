package com.ixigo.sdk.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.app.databinding.FragmentFirstBinding
import com.ixigo.sdk.auth.AuthProvider
import com.ixigo.sdk.auth.PartnerToken
import com.ixigo.sdk.auth.PartnerTokenProvider
import com.ixigo.sdk.auth.SSOAuthProvider
import com.ixigo.sdk.flights.FlightPassengerData
import com.ixigo.sdk.flights.FlightSearchData
import com.ixigo.sdk.flights.flightsStartHome
import com.ixigo.sdk.flights.flightsStartSearch
import com.ixigo.sdk.payment.PaymentCallback
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentProvider
import java.time.LocalDate

/** A simple [Fragment] subclass as the default destination in the navigation. */
class FirstFragment : Fragment() {

  private var _binding: FragmentFirstBinding? = null
  private var sdkInitialized: Boolean = false

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {

    _binding = FragmentFirstBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    loadSettings()

    binding.buttonRestart.setOnClickListener { restartApp() }

    binding.buttonFlightHome.setOnClickListener {
      if (initSDK()) {
        IxigoSDK.getInstance().flightsStartHome(requireContext())
      }
    }

    binding.buttonFlightSearch.setOnClickListener {
      if (initSDK()) {
        IxigoSDK.getInstance()
            .flightsStartSearch(
                requireContext(),
                FlightSearchData(
                    origin = "DEL",
                    destination = "BOM",
                    departDate = LocalDate.now().plusDays(1),
                    source = "FlightSearchFormFragment",
                    flightClass = "e",
                    passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)))
      }
    }

    val adapter = ArrayAdapter(requireContext(), R.layout.list_item, ixigoConfigs)
    (binding.configInput.editText as? AutoCompleteTextView)?.setAdapter(adapter)
  }

  private fun loadSettings() {
    val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
    loadSetting(prefs, binding.clientId, "clientId")
    loadSetting(prefs, binding.apiKey, "apiKey")
    loadSetting(prefs, binding.appVersion, "appVersion")
    loadSetting(prefs, binding.ssoPartnerToken, "ssoPartnerToken")
    loadSetting(prefs, binding.configInput.editText!!, "config")
  }

  private fun saveSettings() {
    val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
    with(prefs.edit()) {
      saveSetting(this, binding.clientId, "clientId")
      saveSetting(this, binding.apiKey, "apiKey")
      saveSetting(this, binding.appVersion, "appVersion")
      saveSetting(this, binding.ssoPartnerToken, "ssoPartnerToken")
      saveSetting(this, binding.configInput.editText!!, "config")
      commit()
    }
  }

  private fun loadSetting(prefs: SharedPreferences, editText: EditText, key: String) {
    val value = prefs.getString(key, null)
    if (value != null) {
      editText.setText(value, TextView.BufferType.EDITABLE)
    }
  }

  private fun saveSetting(editor: SharedPreferences.Editor, editText: EditText, key: String) {
    editor.putString(key, editText.text.toString())
  }

  private fun restartApp() {
    val context = requireContext()
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
    val componentName = intent.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    context.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
  }

  private fun initSDK(): Boolean {
    if (sdkInitialized) {
      return true
    }
    val clientId = binding.clientId.text.toString()
    if (clientId.isNullOrEmpty()) {
      binding.clientId.error = "Client Id can not be empty"
    }
    val apiKey = binding.apiKey.text.toString()
    if (apiKey.isNullOrEmpty()) {
      binding.apiKey.error = "ApiKey can not be empty"
    }
    val appVersion = binding.appVersion.text.toString()
    if (appVersion.isNullOrEmpty()) {
      binding.appVersion.error = "App Version can not be empty"
    }
    val ixigoConfig =
        ixigoConfigs.find { it.label == binding.configInput.editText?.text.toString() }
    if (ixigoConfig == null) {
      binding.configInput.error = "Config can not be empty"
    }
    if (appVersion.isNullOrEmpty() ||
        apiKey.isNullOrEmpty() ||
        clientId.isNullOrEmpty() ||
        ixigoConfig == null) {
      return false
    }
    IxigoSDK.init(
        requireContext(),
        getAuthProvider(),
        DisabledPaymentProvider,
        AppInfo(
            clientId = clientId,
            apiKey = apiKey,
            appVersion = appVersion,
            uuid = "uuid",
            deviceId = "deviceId"),
        config = ixigoConfig.config)
    binding.clientId.isEnabled = false
    binding.apiKey.isEnabled = false
    binding.appVersion.isEnabled = false
    binding.ssoPartnerToken.isEnabled = false
    binding.configInput.isEnabled = false
    binding.buttonRestart.visibility = VISIBLE
    saveSettings()
    sdkInitialized = true
    return true
  }

  private fun getAuthProvider(): AuthProvider {
    val token = binding.ssoPartnerToken.text.toString()
    return SSOAuthProvider(
        object : PartnerTokenProvider {
          override val partnerToken: PartnerToken?
            get() =
                if (token.isNullOrEmpty()) {
                  null
                } else {
                  PartnerToken(token)
                }
        })
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  object DisabledPaymentProvider : PaymentProvider {
    override fun startPayment(
        activity: FragmentActivity,
        input: PaymentInput,
        callback: PaymentCallback
    ): Boolean {
      return false
    }
  }

  data class IxigoConfig(val label: String, val config: Config) {
    override fun toString(): String {
      return label
    }
  }

  val ixigoConfigs =
      listOf(IxigoConfig("Prod", Config.ProdConfig)) +
          (1..8).map { IxigoConfig("Build $it", Config.StagingBuildConfig("build$it")) }
}
