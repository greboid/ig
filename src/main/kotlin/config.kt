package com.greboid.scraper

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.File

class Config(var db: String = "", var profiles: Map<String, List<String>> = emptyMap())

fun getConfig(fileName: String) : Config {
    return Yaml(Constructor(Config::class.java))
            .load(File(fileName).reader()) as Config
}