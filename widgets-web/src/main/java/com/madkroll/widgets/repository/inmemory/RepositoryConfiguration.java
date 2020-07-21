package com.madkroll.widgets.repository.inmemory;

import com.madkroll.widgets.repository.entity.Widget;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public ConcurrentHashMap<String, Widget> widgets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Supplies beans with a new instance of Read-Write lock.
     * */
    @Bean
    public Supplier<ReadWriteLock> lockSupplier() {
        return ReentrantReadWriteLock::new;
    }
}
