package com.winsupply.mdmcustomertoecomsubscriber.config;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.mq.spring.boot.MQConnectionFactoryCustomizer;
import com.ibm.mq.spring.boot.MQConnectionFactoryFactory;
import jakarta.jms.ConnectionFactory;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

/**
 * MQ Configuration
 *
 * @author Amritranshu
 *
 */
@Configuration
public class QueueManagerConfig {

    /**
     * <b>mdmCustomerConfigProperties</b> - Configuring properties for
     * customer updates
     *
     * @return MQConfigurationProperties
     */
    @Bean
    @ConfigurationProperties("winsupply.mq")
    public MQConfigurationProperties mdmCustomerConfigProperties() {
        return new MQConfigurationProperties();
    }

    /**
     * <b>mdmCustomerConnectionFactory</b> - MDM-CUSTOMER connection factory
     *
     * @param pProperties         - the Configuration Properties
     * @param pFactoryCustomizers - the Factory Customizers
     * @return MQConnectionFactory object
     */
    @Bean
    public MQConnectionFactory mdmCustomerConnectionFactory(final MQConfigurationProperties pProperties,
            ObjectProvider<List<MQConnectionFactoryCustomizer>> pFactoryCustomizers) {
        return new MQConnectionFactoryFactory(pProperties, pFactoryCustomizers.getIfAvailable()).createConnectionFactory(MQConnectionFactory.class);
    }

    /**
     * <b>mdmCustomerJmsListenerContainerFactory</b> - MDM-CUSTOMER Jms listener
     * container factory
     *
     * @param pConnectionFactory - the Connection Factory
     * @param pConfigurer        - the Container Factory Configurer
     * @return JmsListenerContainerFactory
     */
    @Bean
    public JmsListenerContainerFactory<?> mdmCustomerJmsListenerContainerFactory(
            @Qualifier("mdmCustomerConnectionFactory") ConnectionFactory pConnectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer pConfigurer) {
        final DefaultJmsListenerContainerFactory lFactory = new DefaultJmsListenerContainerFactory();
        lFactory.setSubscriptionDurable(true);
        pConfigurer.configure(lFactory, pConnectionFactory);
        return lFactory;
    }

}
