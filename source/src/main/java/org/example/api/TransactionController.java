package org.example.api;

import jakarta.servlet.http.HttpServletRequest;
import org.example.core.Block;
import org.example.core.IBlockFactory;
import org.example.model.Transaction;
import org.example.network.Peer;
import org.example.security.access.AccessControl;
import org.example.security.jwt.JwtAuthFilter;
import org.example.storage.TransactionStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final Peer peer;

    private final TransactionStorage transactionStorage;

    private final IBlockFactory blockFactory;

    public TransactionController(Peer peer, TransactionStorage transactionStorage, IBlockFactory blockFactory) {
        this.peer = peer;
        this.blockFactory = blockFactory;
        this.transactionStorage = transactionStorage;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if ("user".equals(username) && "password".equals(password)) {
            String token = JwtAuthFilter.generateToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/process")
    public ResponseEntity<String> sendTransactionToClients(@RequestBody Transaction transaction, HttpServletRequest request) {
        try {
            if (transaction == null) {
                return ResponseEntity.badRequest().body("Invalid transaction");
            }

            AccessControl accessControl = new AccessControl();

            /*if (!accessControl.isAuthorized(request)) {
                return ResponseEntity.status(403).body("Unauthorized");
            }*/


            Block block = blockFactory.addBlock(transaction);

            peer.broadcastTransaction(block);

            return ResponseEntity.ok("Block verificado e enviado");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error sending transaction");
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Block>> getTransactions() {
        try {
            List<Block> transactions = transactionStorage.getTransactions();
            if (transactions.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/all")
    public Map<String, Object> getAllPeersAndTransactions() {
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactionStorage.getTransactions());
        return response;
    }

    @GetMapping("/discovered")
    public Map<String, String> getDiscoveredPeers() {
        return peer.getDiscoveredPeers().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                ));
    }
}
