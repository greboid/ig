package com.greboid.scraper

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result

fun getUrl(url: String): Result<String, FuelError> {
    return getUrl(url, emptyMap())
}

fun getUrl(url: String, headers: Map<String, String>): Result<String, FuelError> {
    return Fuel.get(url).header(headers).responseString().third
}