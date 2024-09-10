package org.example.config;

import org.example.core.Blockchain;
import org.example.core.IBlockFactory;
import org.example.network.Peer;
import org.example.storage.TransactionStorage;
import org.example.utils.PortUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AppConfig {

    @Bean
    public TransactionStorage transactionStorage() {
        return new TransactionStorage();
    }

    @Bean
    public IBlockFactory blockFactory(TransactionStorage transactionStorage) {
        return new Blockchain(transactionStorage);
    }

    @Bean
    public Peer peer(TransactionStorage transactionStorage, IBlockFactory blockFactory) throws IOException {
        int webPort = PortUtils.findAvailablePort();

        return new Peer(webPort, transactionStorage, blockFactory);
    }

}