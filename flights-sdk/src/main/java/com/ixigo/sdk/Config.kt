package com.ixigo.sdk

internal data class Config(val apiBaseUrl: String) {

    fun createUrl(path: String): String = apiBaseUrl + path

    companion object {
        val ProdConfig = Config("https://www.ixigo.com/")
    }
}
