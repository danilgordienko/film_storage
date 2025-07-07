package ru.danilgordienko.film_storage.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE = "movies.queue";
    public static final String POSTER_QUEUE = "movies.poster.queue";
    public static final String MOVIES_PAGE_QUEUE = "movies.page.queue";

    public static final String EXCHANGE = "movies.exchange";

    public static final String ROUTING_KEY = "movies.key";
    public static final String ROUTING_KEY_POSTER = "movies.poster.key";
    public static final String ROUTING_KEY_PAGE = "movies.page.key";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE, false);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue posterQueue() {
        return new Queue(POSTER_QUEUE, false);
    }

    @Bean
    public Queue moviesPageQueue() {
        return new Queue(MOVIES_PAGE_QUEUE, false);
    }

    @Bean
    public Binding posterBinding(DirectExchange exchange) {
        return BindingBuilder.bind(posterQueue()).to(exchange).with(ROUTING_KEY_POSTER);
    }

    @Bean
    public Binding moviesPageBinding(DirectExchange exchange) {
        return BindingBuilder.bind(moviesPageQueue()).to(exchange).with(ROUTING_KEY_PAGE);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

}

