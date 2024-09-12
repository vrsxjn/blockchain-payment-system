package org.example.storage;

import org.example.core.Block;
import org.example.security.cryptography.EncryptionUtil;
import org.example.security.signatures.SignatureUtil;

import java.util.ArrayList;
import java.util.List;

public class TransactionStorage {

    private final List<String> encryptedTransactions = new ArrayList<>();

    public synchronized void addTransaction(Block transaction) {
        String encryptedTransaction = EncryptionUtil.encryptBlock(transaction);
        encryptedTransactions.add(encryptedTransaction);
    }

    public synchronized List<Block> getTransactions() {
        List<Block> decryptedTransactions = new ArrayList<>();
        for (String encryptedTransaction : encryptedTransactions) {
            Block decryptedTransaction = EncryptionUtil.decryptBlock(encryptedTransaction);


            decryptedTransactions.add(decryptedTransaction);
        }
        return decryptedTransactions;
    }

    public synchronized int size() {
        return encryptedTransactions.size();
    }

    public synchronized Block get(int index) {
        if (index < 0 || index >= encryptedTransactions.size()) {
            throw new IndexOutOfBoundsException("Índice fora dos limites: " + index);
        }

        Block decryptedTransaction = EncryptionUtil.decryptBlock(encryptedTransactions.get(index));

        return decryptedTransaction;
    }
}
