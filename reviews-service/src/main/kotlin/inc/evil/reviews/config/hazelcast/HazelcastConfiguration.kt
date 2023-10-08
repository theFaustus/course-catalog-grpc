package inc.evil.reviews.config.hazelcast

import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.config.ClientConnectionStrategyConfig
import com.hazelcast.spring.context.SpringManagedContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HazelcastConfiguration {


    @Bean
    fun managedContext(): SpringManagedContext {
        return SpringManagedContext()
    }

    @Bean
    fun hazelcastClientConfig(managedContext: SpringManagedContext): ClientConfig {
        val config = ClientConfig()
        config.managedContext = managedContext
        config.connectionStrategyConfig.isAsyncStart = true
        config.connectionStrategyConfig.reconnectMode = ClientConnectionStrategyConfig.ReconnectMode.ON
        config.networkConfig.isSmartRouting = true

        config.setProperty("hazelcast.client.invocation.timeout.seconds", "5")
        config.setProperty("hazelcast.client.heartbeat.interval", "3000")
        config.setProperty("hazelcast.client.heartbeat.timeout", "30000")
        config.setProperty("hazelcast.socket.connect.timeout.seconds", "10")
        return config
    }

}

