package org.example.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.Transaction;
import org.example.security.cryptography.HashingUtil;
import org.example.storage.TransactionStorage;
import org.example.utils.BlockchainUtils;

import java.util.Date;
import java.util.Optional;

public class Block {
    private String hash;
    private String previousHash;
    private long timestamp;
    private Transaction transaction;
    private int nonce;
    private String signature;
    public Block(Transaction transaction, String previousHash) {
        this.transaction = transaction;
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    @JsonCreator
    public Block(
            @JsonProperty("hash") String hash,
            @JsonProperty("previousHash") String previousHash,
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("transaction") Transaction transaction,
            @JsonProperty("nonce") int nonce,
            @JsonProperty("signature") String signature
    ) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.timestamp = timestamp;
        this.transaction = transaction;
        this.nonce = nonce;
        this.signature = signature;
    }

    public String calculateHash() {
        String dataToHash = previousHash + Long.toString(timestamp) + Integer.toString(nonce);
        return HashingUtil.sha256(dataToHash);
    }


    public void mineBlock() {
        String target = new String(new char[BlockchainUtils.getDifficulty()]).replace('\0', '0');
        while(!hash.substring(0, BlockchainUtils.getDifficulty()).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined: " + hash);
    }

    // Getters e Setters
    public String getHash() {
        return hash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public long getTimestamp() { // Corrigido para timestamp
        return timestamp;
    }

    public void setTimestamp(long timestamp) { // Corrigido para timestamp
        this.timestamp = timestamp;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }
}
