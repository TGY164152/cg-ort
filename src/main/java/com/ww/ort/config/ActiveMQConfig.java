package com.ww.ort.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;

/**
 * 配置类
 */
@Configuration
public class ActiveMQConfig {
    /**
     * 创建 ActiveMQ 连接工厂的通用方法
     *
     * @param brokerUrl ActiveMQ 服务器地址
     * @param username  用户名
     * @param password  密码
     * @return 配置好的连接工厂
     */
    public ActiveMQConnectionFactory createConnectionFactory(String brokerUrl, String username, String password) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        factory.setUserName(username);
        factory.setPassword(password);
        return factory;
    }

    // ================================ 默认配置 ================================
    /**
     * 实例连接工厂
     * 连接到默认 ActiveMQ 服务器（可能是备用或特定业务服务器）
     *
     * @param brokerUrl ActiveMQ 服务器地址，从配置读取
     * @param username  ActiveMQ 用户名，从配置读取
     * @param password  ActiveMQ 密码，从配置读取
     * @return 实例的连接工厂
     */
    @Bean(name = "defaultConnectionFactory")
    public ActiveMQConnectionFactory connectionFactory(
            @Value("${spring.activemq.broker-url}") String brokerUrl,
            @Value("${spring.activemq.user}") String username,
            @Value("${spring.activemq.password}") String password) {
        return createConnectionFactory(brokerUrl, username, password);
    }

    /**
     * 默认消息发送模板（主要模板）
     * 用于向默认 ActiveMQ 实例发送消息
     *
     * @param connectionFactory 默认连接工厂
     * @return 消息发送模板
     */
    @Bean(name = "defaultJmsTemplate")
    @Primary // 标记为主要Bean，当有多个同类型Bean时默认使用此模板
    public JmsMessagingTemplate defaultActivemqTemplate(
            @Qualifier("defaultConnectionFactory") ActiveMQConnectionFactory connectionFactory) {
        JmsMessagingTemplate template = new JmsMessagingTemplate(connectionFactory);
        return template;
    }

    /**
     * 监听工厂（队列模式）
     * 用于监听JT ActiveMQ 实例的队列消息
     *
     * @param connectionFactory 连接工厂
     * @return 队列监听工厂
     */
    @Bean(name = "defaultJmsListenerContainerFactoryQueue")
    public JmsListenerContainerFactory defaultFactoryQueue(
            @Qualifier("defaultConnectionFactory") ActiveMQConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false); // 默认是true，如果要使用topic，需要设置为true
        return factory;
    }

    /**
     * 监听工厂（主题模式）
     * 用于监听JT ActiveMQ 实例的主题消息
     *
     * @param connectionFactory 连接工厂
     * @return 主题监听工厂
     */
    @Bean(name = "defaultJmsListenerContainerFactoryTopic")
    public JmsListenerContainerFactory defaultFactoryTopic(
            @Qualifier("defaultConnectionFactory") ActiveMQConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true); // 默认是true，如果要使用topic，需要设置为true
        return factory;
    }


//    // ================================ JT配置 ================================
//    /**
//     * JT实例连接工厂
//     * 连接到第二个 ActiveMQ 服务器（可能是备用或特定业务服务器）
//     *
//     * @param brokerUrl JT ActiveMQ 服务器地址，从配置读取
//     * @param username  JT ActiveMQ 用户名，从配置读取
//     * @param password  JT ActiveMQ 密码，从配置读取
//     * @return JT实例的连接工厂
//     */
//    @Bean(name = "jtConnectionFactory")
//    public ActiveMQConnectionFactory secondConnectionFactory(
//            @Value("${spring.jtmq.broker-url}") String brokerUrl,
//            @Value("${spring.jtmq.user}") String username,
//            @Value("${spring.jtmq.password}") String password) {
//        return createConnectionFactory(brokerUrl, username, password);
//    }
//
//    /**
//     * 消息发送模板（主要模板）
//     * 用于向JT ActiveMQ 实例发送消息
//     *
//     * @param connectionFactory 默认连接工厂
//     * @return 消息发送模板
//     */
//    @Bean(name = "jtJmsTemplate")
//    @Primary  // 标记为主要Bean，当有多个同类型Bean时默认使用此模板
//    public JmsMessagingTemplate jtActivemqTemplate(
//            @Qualifier("jtConnectionFactory") ActiveMQConnectionFactory connectionFactory) {
//        JmsMessagingTemplate template = new JmsMessagingTemplate(connectionFactory);
//        return template;
//    }
//
//    /**
//     * JT监听工厂（队列模式）
//     * 用于监听JT ActiveMQ 实例的队列消息
//     *
//     * @param connectionFactory 默认连接工厂
//     * @return 队列监听工厂
//     */
//    @Bean(name = "jtJmsListenerContainerFactoryQueue")
//    public JmsListenerContainerFactory jtFactoryQueue(
//            @Qualifier("jtConnectionFactory") ActiveMQConnectionFactory connectionFactory) {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setPubSubDomain(false);
//        factory.setSessionTransacted(true); // 开启事务，关键！
//        return factory;
//    }
//
//    /**
//     * JT监听工厂（主题模式）
//     * 用于监听JT ActiveMQ 实例的主题消息
//     *
//     * @param connectionFactory 默认连接工厂
//     * @return 主题监听工厂
//     */
//    @Bean(name = "jtJmsListenerContainerFactoryTopic")
//    public JmsListenerContainerFactory jtFactoryTopic(
//            @Qualifier("jtConnectionFactory") ActiveMQConnectionFactory connectionFactory
//    ) {
//        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setPubSubDomain(true); // 默认是true，如果要使用topic，需要设置为true
//        factory.setSessionTransacted(true); // 开启事务，关键！
//        return factory;
//    }


}
