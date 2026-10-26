package com.m42.companion

import com.m42.companion.data.CardDataParser

/** The bundled card data, read from src/main/assets (added to the unit test classpath). */
object TestData {
    val eldritch by lazy { CardDataParser.eldritch(resource("eldritch.json")) }
    val arkham by lazy { CardDataParser.arkham(resource("arkham.json")) }

    private fun resource(name: String) =
        checkNotNull(javaClass.classLoader!!.getResourceAsStream(name)) { "$name missing from test classpath" }
}
