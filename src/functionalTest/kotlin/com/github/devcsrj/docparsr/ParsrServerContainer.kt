package com.github.devcsrj.docparsr

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.net.URI

class ParsrServerContainer : GenericContainer<ParsrServerContainer>("axarev/parsr") {

    companion object {
        const val PORT = 3001
    }

    override fun configure() {
        withExposedPorts(PORT)
        setWaitStrategy(HostPortWaitStrategy())
    }

    fun getUrl(): URI = URI.create("http://$containerIpAddress:${firstMappedPort}")
}