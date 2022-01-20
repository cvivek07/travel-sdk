package com.ixigo.sdk.bus

import com.ixigo.sdk.Config
import com.ixigo.sdk.common.Err
import com.ixigo.sdk.common.Ok
import com.ixigo.sdk.common.Result
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.time.LocalDate
import okhttp3.*
import timber.log.Timber

typealias CheapestFareResult = Result<CheapestFareOutput, CheapestFairError>

typealias CheapestFareCallback = (CheapestFareResult) -> Unit

internal class CheapestFairCall(private val config: Config = BusSDK.instance.config) {
  private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
  private val outputJsonAdapter by lazy { moshi.adapter(CheapestFareOutput::class.java) }

  fun execute(input: CheapestFareInput, callback: CheapestFareCallback) {
    val client = OkHttpClient()

    val request =
        Request.Builder()
            .url(
                config.createUrl(
                    "trainrouteinfo/${input.origin}/${input.destination}/${input.dateString}"))
            .build()
    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                callback(Err(CheapestFairError.HTTP_ERROR))
              }

              override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                try {
                  val output = outputJsonAdapter.fromJson(body)!!
                  callback(Ok(output))
                } catch (e: Exception) {
                  Timber.w(e, "Error trying to parse response")
                  callback(Err(CheapestFairError.JSON_PARSE_ERROR))
                }
              }
            })
  }
}

data class CheapestFareInput(val origin: String, val destination: String, val date: LocalDate) {
  val dateString: String
    get() = BusSearchData.formatter.format(date)
}

data class CheapestFareOutput(
    val minFare: String,
    val minTravelTime: String,
    val busSourceName: String,
    val busSourceID: String,
    val busDestinationName: String,
    val busDestinationID: String
)

enum class CheapestFairError {
  HTTP_ERROR,
  JSON_PARSE_ERROR
}
