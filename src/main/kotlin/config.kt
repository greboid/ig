package com.greboid.scraper

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.error.YAMLException
import java.io.File
import java.io.Reader

class Config(var db: String = "", var profiles: Map<String, List<String>> = emptyMap())

fun getConfig(fileName: String) : Config? {
    return getConfig(File(fileName).reader())
}

fun getConfig(stream: Reader) : Config? {
    try {
        return Yaml(Constructor(Config::class.java)).load(stream) as Config
    } catch (e: YAMLException) {
        return null
    }
}