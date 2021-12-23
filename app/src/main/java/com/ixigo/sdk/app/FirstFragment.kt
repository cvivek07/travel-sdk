package com.ixigo.sdk.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.app.databinding.FragmentFirstBinding
import com.ixigo.sdk.auth.*
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.common.Result
import com.ixigo.sdk.flights.FlightPassengerData
import com.ixigo.sdk.flights.FlightSearchData
import com.ixigo.sdk.flights.flightsStartHome
import com.ixigo.sdk.flights.flightsStartSearch
import com.ixigo.sdk.payment.PaymentCallback
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentProvider
import java.time.LocalDate
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible

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

    binding.buttonRestart.setOnClickListener { restartApp() }

    binding.buttonSSOTest.setOnClickListener {
      initSDK()

      getAuthProvider().login(requireActivity()) {
        Snackbar.make(binding.buttonSSOTest, getSsoAuthMessage(it), Snackbar.LENGTH_LONG).show()
      }
    }

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

    setupPreset()
  }

  private fun setupPreset() {
    val autoCompleteTextView = binding.presetInput.editText as AutoCompleteTextView
    val presetAdapter = ArrayAdapter(requireContext(), R.layout.list_item, presets)
    autoCompleteTextView.setAdapter(presetAdapter)
    autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
      val preset = presets[position]
      if (preset.label == "Other") {
        binding.expansionLayout.expand(true)
      } else {
        binding.expansionLayout.collapse(true)
      }
      loadPreset(preset)
    }
    loadPreset(presets[0])
  }

  private fun getSsoAuthMessage(it: Result<AuthData>): String {
    return when (it) {
      is Ok -> "Auth Successful. Ixigo Token=${it.value.token}"
      is Err -> "Auth Error. Error=${it.value.message}"
    }
  }

  private fun loadPreset(preset: Preset) {
    binding.clientId.setText(preset.clientId, TextView.BufferType.EDITABLE)
    binding.apiKey.setText(preset.apiKey, TextView.BufferType.EDITABLE)
    binding.appVersion.setText(preset.appVersion, TextView.BufferType.EDITABLE)
    binding.ssoPartnerToken.setText(preset.ssoPartnerToken, TextView.BufferType.EDITABLE)
    binding.uuid.setText(preset.uuid, TextView.BufferType.EDITABLE)
    binding.deviceId.setText(preset.deviceId, TextView.BufferType.EDITABLE)
    (binding.presetInput.editText as AutoCompleteTextView).setText(preset.label, false)
  }

  private fun loadSettings() {
    val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
    loadSetting(prefs, binding.clientId, "clientId")
    loadSetting(prefs, binding.apiKey, "apiKey")
    loadSetting(prefs, binding.appVersion, "appVersion")
    loadSetting(prefs, binding.ssoPartnerToken, "ssoPartnerToken")
    loadSetting(prefs, binding.uuid, "uuid")
    loadSetting(prefs, binding.deviceId, "deviceId")
  }

  private fun saveSettings() {
    val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
    with(prefs.edit()) {
      saveSetting(this, binding.clientId, "clientId")
      saveSetting(this, binding.apiKey, "apiKey")
      saveSetting(this, binding.appVersion, "appVersion")
      saveSetting(this, binding.ssoPartnerToken, "ssoPartnerToken")
      saveSetting(this, binding.configInput.editText!!, "config")
      saveSetting(this, binding.uuid, "uuid")
      saveSetting(this, binding.deviceId, "deviceId")
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

  private fun clearSDK() {
    val companionObject = IxigoSDK::class.companionObject!!
    val companionInstance = IxigoSDK::class.companionObjectInstance
    val method = companionObject.declaredFunctions.first { it.name == "clearInstance" }
    method.isAccessible = true
    method.call(companionInstance)
  }

  private fun initSDK(): Boolean {
    clearSDK()

    val clientId = getFieldValue(binding.clientId, "Client Id")
    val apiKey = getFieldValue(binding.apiKey, "Api Key")
    val appVersion = getFieldValue(binding.appVersion, "App Version")
    val uuid = getFieldValue(binding.uuid, "UUID")
    val deviceId = getFieldValue(binding.deviceId, "Device Id")
    val ixigoConfig =
        ixigoConfigs.find { it.label == binding.configInput.editText?.text.toString() }
    if (ixigoConfig == null) {
      binding.configInput.error = "Config can not be empty"
    }
    if (appVersion == null ||
        apiKey == null ||
        clientId == null ||
        uuid == null ||
        deviceId == null ||
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
            uuid = uuid,
            deviceId = deviceId),
        config = ixigoConfig.config)

    sdkInitialized = true
    return true
  }

  private fun getFieldValue(editText: EditText, fieldName: String): String? {
    val value = editText.text.toString()
    return if (value.isNullOrEmpty()) {
      editText.error = "$fieldName can not be empty"
      null
    } else {
      value
    }
  }

  private fun getAuthProvider(): AuthProvider {
    //    val token = binding.ssoPartnerToken.text.toString()
    val token = "D5DCFBD21CF7867B74D5273A57A0254D1785773799EEDD0E683B0EE5C6E56878"
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

  private val ixigoConfigs =
      listOf(IxigoConfig("Prod", Config.ProdConfig)) +
          (1..8).map { IxigoConfig("Build $it", Config.StagingBuildConfig("build$it")) }

  private val presets =
      listOf(
        Preset(
          label = "ConfirmTk",
          clientId = "confirmtckt",
          apiKey = "confirmtckt!2\$",
          ssoPartnerToken = "D5DCFBD21CF7867B74D5273A57A0254D1785773799EEDD0E683B0EE5C6E56878"),
          Preset(
              label = "Abhibus",
              clientId = "abhibus",
              apiKey = "abhibus!2\$",
              ssoPartnerToken = "RQjsRqkORTji8R9+AQkLFyl9yeLQxX2II01n4rvVh1vpoH6pVx4eiw=="),
          Preset(label = "Ixigo Trains", clientId = "iximatr", apiKey = "iximatr!2\$"),
          Preset(
              label = "Other",
              clientId = "",
              apiKey = "",
              ssoPartnerToken = "",
              uuid = "",
              deviceId = "",
              appVersion = ""))
}

data class Preset(
    val label: String,
    val clientId: String,
    val apiKey: String,
    val ssoPartnerToken: String? = null,
    val appVersion: String = "1.0.0",
    val uuid: String = "987654321ABC",
    val deviceId: String = "123456789abcdef"
) {
  override fun toString(): String {
    return label
  }
}

data class IxigoConfig(val label: String, val config: Config) {
  override fun toString(): String {
    return label
  }
}
