package org.example.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Transaction;
import org.example.security.cryptography.EncryptionUtil;
import org.example.security.signatures.SignatureUtil;
import org.example.storage.TransactionStorage;
import org.java_websocket.WebSocket;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
@Component
public class Blockchain implements IBlockFactory  {

    private final TransactionStorage transactionStorage;

    public Blockchain(TransactionStorage transactionStorage) {
        this.transactionStorage = transactionStorage;
    }


    @Override
    public Block createBlock(Transaction transaction, String previousHash) {
        return new Block(transaction, previousHash);
    }

    @Override
    public void createGenesisBlockIfNotExists() {
        synchronized (transactionStorage) {
            if (transactionStorage.getTransactions().isEmpty()) {
                Block genesisBlock = new Block(null, "0");
                genesisBlock.mineBlock();
                transactionStorage.addTransaction(genesisBlock);
                System.out.println("Genesis Block created.");
            }
        }
    }
    @Override
    public Block addBlock(Transaction transaction) {
        String previousHash = transactionStorage.get(transactionStorage.size() - 1).getHash();

        Block newBlock = new Block(transaction, previousHash);

        newBlock.mineBlock();

        String signature = SignatureUtil.signTransaction(newBlock);

        newBlock.setSignature(signature);

        transactionStorage.addTransaction(newBlock);

        return newBlock;
    }

    @Override
    public void sendGenesisBlock(WebSocket conn) {
        synchronized (transactionStorage) {
            if (!transactionStorage.getTransactions().isEmpty()) {
                Block genesisBlock = transactionStorage.getTransactions().get(0);
                String encryptedGenesisBlock = EncryptionUtil.encryptBlock(genesisBlock);
                conn.send("GENESIS_BLOCK:" + encryptedGenesisBlock);
                System.out.println("Genesis Block sent to " + conn.getRemoteSocketAddress());
            }
        }
    }

    @Override
    public String signBlock(Block block) {
        String signature = SignatureUtil.signTransaction(block);
        block.setSignature(signature);
        return signature;
    }

    @Override
    public boolean verifyBlockSignature(Block block, String signature) {
        return SignatureUtil.verifySignature(block, signature);

    }

    public void processGenesisBlock(String genesisBlockJson) {
        Block genesisBlock = EncryptionUtil.decryptBlock(genesisBlockJson);
        synchronized (transactionStorage) {
            if (transactionStorage.getTransactions().stream()
                    .noneMatch(b -> b.getHash().equals(genesisBlock.getHash()))) {

                transactionStorage.addTransaction(genesisBlock);
                System.out.println("Genesis Block received and saved.");
            }
        }
    }


}
