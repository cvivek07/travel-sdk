package com.ixigo.sdk.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.Config
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.app.databinding.FragmentFirstBinding
import com.ixigo.sdk.auth.*
import com.ixigo.sdk.bus.BusConfig
import com.ixigo.sdk.bus.BusSDK
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.covid.covidLaunchAppointments
import com.ixigo.sdk.flights.*
import com.ixigo.sdk.payment.PaymentCallback
import com.ixigo.sdk.payment.PaymentInput
import com.ixigo.sdk.payment.PaymentProvider
import com.ixigo.sdk.payment.processPayment
import com.ixigo.sdk.trains.TrainsSDK
import com.ixigo.sdk.webview.FunnelConfig
import com.ixigo.sdk.webview.InitialPageData
import com.ixigo.sdk.webview.WebActivity
import com.ixigo.sdk.webview.WebViewFragment
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

class FirstFragment : Fragment() {

  private var _binding: FragmentFirstBinding? = null
  private var sdkInitialized: Boolean = false
  private val progressDialog by lazy { ProgressDialog(requireActivity()) }

  private val binding
    get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
    menuInflater.inflate(R.menu.menu_main, menu)
    super.onCreateOptionsMenu(menu, menuInflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_clear_storage -> {
        initSDK()
        IxigoSDK.instance.onLogout()
        Toast.makeText(requireContext(), "Storage cleared", Toast.LENGTH_SHORT).show()
        return true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

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

    binding.buttonSSOTest.setOnClickListener {
      initSDK()

      val enabled =
          getSSOTestAuthProvider().login(requireActivity(), "iximad") {
            progressDialog.hide()
            Snackbar.make(binding.buttonSSOTest, getSsoAuthMessage(it), Snackbar.LENGTH_LONG).show()
          }
      if (enabled) {
        progressDialog.show()
      }
      if (!enabled) {
        Snackbar.make(binding.buttonSSOTest, "No partner token found", Snackbar.LENGTH_LONG).show()
      }
    }

    binding.buttonFlightMultiModule.setOnClickListener {
      if (initSDK()) {
        val intent = Intent(requireContext(), MultiModelActivity::class.java)
        requireContext().startActivity(intent)
      }
    }

    binding.buttonFlightTripsFragment.setOnClickListener {
      if (initSDK()) {
        val intent = Intent(requireContext(), TripsFragmentActivity::class.java)
        requireContext().startActivity(intent)
      }
    }


    binding.buttonFlightHome.setOnClickListener {
      if (initSDK()) {
        IxigoSDK.instance.flightsStartHome(requireContext())
      }
    }

    binding.buttonFlightTrips.setOnClickListener {
      if (initSDK()) {
        IxigoSDK.instance.flightsStartTrips(requireContext())
      }
    }

    binding.buttonBusHome.setOnClickListener {
      if (initSDK() && initBusSDK()) {
        BusSDK.instance.launchHome(requireContext())
      }
    }

    binding.buttonBusTrips.setOnClickListener {
      if (initSDK() && initBusSDK()) {
        BusSDK.instance.launchTrips(requireContext())
      }
    }

    binding.buttonBusMultiModule.setOnClickListener {
      if (initSDK() && initBusSDK()) {
        val intent = Intent(requireContext(), BusMultiModelActivity::class.java)
        requireContext().startActivity(intent)
      }
    }

    binding.buttonTrainsHome.setOnClickListener {
      if (initSDK() && initTrainsSDK()) {
        TrainsSDK.instance.launchHome(requireContext())
      }
    }

    binding.buttonTrainsTrips.setOnClickListener {
      if (initSDK() && initTrainsSDK()) {
        TrainsSDK.instance.launchTrips(requireContext())
      }
    }

    binding.buttonTrainsTripsFragment.setOnClickListener {
      if (initSDK() && initTrainsSDK()) {
        val intent = Intent(requireContext(), TrainsTripsFragmentActivity::class.java)
        requireContext().startActivity(intent)
      }
    }

    binding.buttonCovidAppointment.setOnClickListener {
      if (initSDK()) {
        IxigoSDK.instance.covidLaunchAppointments(requireContext(), FunnelConfig(enableExitBar = false))
      }
    }

    binding.buttonFlightSearch.setOnClickListener {
      if (initSDK()) {
        IxigoSDK.instance
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

    binding.paymentPlayground.setOnClickListener {
      if (initSDK()) {
        val intent = Intent(requireContext(), WebActivity::class.java)
        intent.putExtra(WebViewFragment.INITIAL_PAGE_DATA_ARGS, InitialPageData("file:///android_asset/paymentPlayground.html"))
        startActivity(intent)
      }
    }

    binding.paymentHome.setOnClickListener {
      if (initSDK()) {
        val transactionId = getFieldValue(binding.paymentTransactionId, "Transaction Id")
        val gatewayId = getFieldValue(binding.paymentGatewayId, "Gateway Id")
        if (transactionId != null) {
          if (gatewayId != null) {
            IxigoSDK.instance.processPayment(requireActivity(), transactionId = transactionId, gatewayId = gatewayId)
          } else {
            IxigoSDK.instance.processPayment(requireActivity(), transactionId = transactionId)
          }
        }
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

  private fun getSsoAuthMessage(it: AuthResult): String {
    return when (it) {
      is Ok -> "Auth Successful. Ixigo Token=${it.value.token}"
      is Err -> "Auth Error. Error=${it.value.message}"
    }
  }

  private fun loadPreset(preset: Preset) {
    binding.clientId.setText(preset.clientId, TextView.BufferType.EDITABLE)
    binding.apiKey.setText(preset.apiKey, TextView.BufferType.EDITABLE)
    binding.appVersion.setText(preset.appVersion, TextView.BufferType.EDITABLE)
    binding.appName.setText(preset.label, TextView.BufferType.EDITABLE)
    binding.ssoPartnerToken.setText(preset.ssoPartnerToken, TextView.BufferType.EDITABLE)
    binding.uuid.setText(preset.uuid, TextView.BufferType.EDITABLE)
    binding.deviceId.setText(preset.deviceId, TextView.BufferType.EDITABLE)
    (binding.presetInput.editText as AutoCompleteTextView).setText(preset.label, false)

    binding.buttonFlightMultiModule.isEnabled = preset.buttonsState.flightsMultiModule
    binding.buttonFlightSearch.isEnabled = preset.buttonsState.flightsSearch
    binding.buttonFlightHome.isEnabled = preset.buttonsState.flightsHome
    binding.buttonTrainsHome.isEnabled = preset.buttonsState.trainsHome
    binding.buttonBusHome.isEnabled = preset.buttonsState.busHome
    binding.buttonBusTrips.isEnabled = preset.buttonsState.busTrips
    binding.buttonBusMultiModule.isEnabled = preset.buttonsState.busMultiModel
    binding.buttonTrainsHome.isEnabled = preset.buttonsState.trainsHome
    binding.buttonTrainsTripsFragment.isEnabled = preset.buttonsState.trainsTripsFragment
    binding.buttonTrainsTrips.isEnabled = preset.buttonsState.trainsTrips
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
    // Clear IxigoSDK
    clearSDK(IxigoSDK::class)
    clearSDK(BusSDK::class)
    clearSDK(TrainsSDK::class)
  }

  private fun <T:Any>clearSDK(sdkClass: KClass<T>) {
    val companionObject = sdkClass.companionObject!!
    val companionInstance = sdkClass.companionObjectInstance
    val method = companionObject.functions.first { it.name == "clearInstance" }
    method.isAccessible = true
    method.call(companionInstance)
  }

  private fun initSDK(): Boolean {
    clearSDK()

    val clientId = getFieldValue(binding.clientId, "Client Id")
    val apiKey = getFieldValue(binding.apiKey, "Api Key")
    val appVersion = getFieldValue(binding.appVersion, "App Version")?.toLongOrNull()
    val appName = getFieldValue(binding.appName, "App Name")
    val uuid = getFieldValue(binding.uuid, "UUID")
    val deviceId = getFieldValue(binding.deviceId, "Device Id")
    val ixigoConfig = ixigoConfig()
    if (ixigoConfig == null) {
      binding.configInput.error = "Config can not be empty"
    }
    if (appVersion == null ||
        apiKey == null ||
        clientId == null ||
        appName == null ||
        uuid == null ||
        deviceId == null ||
        ixigoConfig == null) {
      return false
    }

    IxigoSDK.init(
        requireContext(),
        AppInfo(
            clientId = clientId,
            apiKey = apiKey,
            appVersion = appVersion,
            appName = appName,
            uuid = uuid,
            deviceId = deviceId),
        getPartnerTokenProvider(),
        DisabledPaymentProvider,
        analyticsProvider = ToastAnalyticsProvider(requireActivity()),
        config =  ixigoConfig.config.copy(enableExitBar = binding.exitBarSwitch.isChecked))

    sdkInitialized = true
    return true
  }

  private fun initBusSDK(): Boolean {
    val busConfig = if (ixigoConfig()?.config == Config.ProdConfig) BusConfig.PROD else BusConfig.STAGING
    BusSDK.init(config = busConfig)
    return true
  }

  private fun initTrainsSDK(): Boolean {
    TrainsSDK.init()
    return true
  }

  private fun ixigoConfig() = ixigoConfigs.find { it.label == binding.configInput.editText?.text.toString() }

  private fun getFieldValue(editText: EditText, fieldName: String): String? {
    val value = editText.text.toString()
    return if (value.isNullOrEmpty()) {
      editText.error = "$fieldName can not be empty"
      binding.expansionLayout.expand(true)
      null
    } else {
      value
    }
  }

  private fun getSSOTestAuthProvider(): AuthProvider {
    val token = binding.ssoPartnerToken.text.toString()
    return SSOAuthProvider(
      object: PartnerTokenProvider {
        override fun fetchPartnerToken(
          activity: FragmentActivity,
          requester: PartnerTokenProvider.Requester,
          callback: PartnerTokenCallback
        ) {
          if (token.isNullOrBlank()) {
            callback(Err(PartnerTokenErrorUserNotLoggedIn()))
          } else {
            callback(Ok(PartnerToken(token)))
          }
        }
      })
  }

  private fun getPartnerTokenProvider(): PartnerTokenProvider {
    val token = binding.ssoPartnerToken.text.toString()
    return FakePartnerTokenProvider(token)
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
      listOf(IxigoConfig("Prod", Config.ProdConfig)) + IxigoConfig("dev (local)", Config("http://dev.ixigo.com")) +
          (1..8).map { IxigoConfig("Build $it", Config.StagingBuildConfig("build$it")) }

  private val presets =
      listOf(
          Preset(
              label = "ConfirmTkt",
              clientId = "confirmtckt",
              apiKey = "confirmtckt!2\$",
              ssoPartnerToken = "D5DCFBD21CF7867B74D5273A57A0254D1785773799EEDD0E683B0EE5C6E56878",
              buttonsState = ButtonsState(flightsSearch = false, flightsMultiModule = false, trainsHome = false, busHome = false, busMultiModel = false, busTrips = false)),
          Preset(
              label = "Abhibus",
              clientId = "abhibus",
              apiKey = "abhibus!2\$",
              ssoPartnerToken = "RQjsRqkORTji8R9+AQkLFyl9yeLQxX2II01n4rvVh1vpoH6pVx4eiw==",
              buttonsState = ButtonsState(trainsTrips = true, trainsTripsFragment = true, flightsSearch = false, flightsMultiModule = false, busHome = false, busMultiModel = false, busTrips = false)),
          Preset(label = "ixigo trains", clientId = "iximatr", apiKey = "iximatr!2\$", buttonsState = ButtonsState(trainsHome = false), appVersion = "1801"),
          Preset(label = "ixigo flights", clientId = "iximaad", apiKey = "iximaad!2\$", buttonsState = ButtonsState(trainsHome = false, flightsMultiModule = false, flightsHome = false, flightsSearch = false)),
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
    val appVersion: String = "1",
    val uuid: String = "987654321ABC",
    val deviceId: String = "123456789abcdef",
    val buttonsState: ButtonsState = ButtonsState()
) {
  override fun toString(): String {
    return label
  }
}

data class ButtonsState(
    val flightsSearch: Boolean = true,
    val flightsHome: Boolean = true,
    val flightsMultiModule: Boolean = true,
    val trainsHome: Boolean = true,
    val busHome: Boolean = true,
    val busMultiModel: Boolean = true,
    val busTrips: Boolean = true,
    val trainsTripsFragment: Boolean = false,
    val trainsTrips: Boolean = false
)

data class IxigoConfig(val label: String, val config: Config) {
  override fun toString(): String {
    return label
  }
}
