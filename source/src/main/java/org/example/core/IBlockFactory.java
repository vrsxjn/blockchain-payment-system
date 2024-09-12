package org.example.core;

import org.example.model.Transaction;
import org.java_websocket.WebSocket;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
public interface IBlockFactory {
    Block createBlock(Transaction transaction, String previousHash);
    void createGenesisBlockIfNotExists();
    Block addBlock(Transaction transaction);
    void sendGenesisBlock(WebSocket conn);
    void processGenesisBlock(String genesisBlockJson);
    String signBlock(Block block);
    boolean verifyBlockSignature(Block block, String signature);
}

