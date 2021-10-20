package com.ixigo.sdk.app

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ixigo.sdk.AppInfo
import com.ixigo.sdk.IxigoSDK
import com.ixigo.sdk.app.databinding.ActivityMainBinding
import com.ixigo.sdk.auth.AuthData
import com.ixigo.sdk.flights.FlightPassengerData
import com.ixigo.sdk.flights.FlightSearchData
import com.ixigo.sdk.flights.flightsStartSearch
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        IxigoSDK.init(FakeAuthProvider("token"), AppInfo("abhibus", "abhibus!2\$", "1801", "deviceId", "uuid"))
        val token = "s9736ibca3swimf5bxu8ptr5r0d0gxfx3oltq6l0t9etceuj4bvf23de9g4y5c1583ij8pxwh4ps2nrtqxkupk9grblrpps8fdjqfb2lh6c67459duamk7m5oalbclem0polr5otvj1rdhim9xhn20estj1m999a097a7ib0gjp1tv2w1p6989d3aatginjb2nd"
        IxigoSDK.init(FakeAuthProvider(token, AuthData(token)), FakePaymentProvider("http://www.google.com"), AppInfo("iximatr", "iximatr!2\$", "1801", "33d040f296f87aeb", "8ee37b17-aa86-42d6-a2dc-80598ca35c9f"))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
//            IxigoSDK.getInstance().flightsStartHome(this);
            IxigoSDK.getInstance().flightsStartSearch(
                this,
                FlightSearchData(
                    origin = "DEL",
                    destination = "BOM",
                    departDate = LocalDate.now().plusDays(1),
                    source = "FlightSearchFormFragment",
                    passengerData = FlightPassengerData(adults = 1, children = 0, infants = 0)
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}