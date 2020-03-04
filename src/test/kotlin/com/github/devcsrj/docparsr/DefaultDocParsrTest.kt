package com.github.devcsrj.docparsr

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

internal class DefaultDocParsrTest : Spek({

    Feature("DefaultDocParsr instance") {

        lateinit var mockWebServer: MockWebServer
        beforeEachScenario {
            mockWebServer = MockWebServer()
        }
        afterEachScenario { mockWebServer.shutdown() }

        Scenario("Getting default configuration") {
            lateinit var actual: Configuration
            Given("a server") {
                val body = javaClass.getResourceAsStream("/config.json").use {
                    it.bufferedReader().readText()
                }
                mockWebServer.enqueue(
                    MockResponse()
                        .setResponseCode(200)
                        .setBody(body)
                )
            }
            When("getDefaultConfig() is called") {
                val parsr = DefaultDocParsr(mockWebServer.url("/"))
                actual = parsr.getDefaultConfig()
            }
            Then("Default configuration is returned") {
                assertThat(actual).isEqualTo(GoldenConfiguration.INSTANCE)
            }
        }
    }
})