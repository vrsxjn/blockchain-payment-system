package org.example.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.core.Block;
import org.example.core.IBlockFactory;
import org.example.security.cryptography.EncryptionUtil;
import org.example.security.signatures.SignatureUtil;
import org.example.storage.TransactionStorage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Peer {

    private WebSocketServer webSocketServer;
    private static final Map<String, WebSocket> peers = new ConcurrentHashMap<>();
    private final TransactionStorage transactionStorage;
    private final IBlockFactory blockController;
    private final int discoveryPort = 50000;
    private static final Map<String, String> processedMessages = new ConcurrentHashMap<>();
    private static final Map<String, InetSocketAddress> discoveredPeers = new ConcurrentHashMap<>();

    public Peer(int webPort,TransactionStorage transactionStorage, IBlockFactory blockController) {
        this.transactionStorage = transactionStorage;
        this.blockController = blockController;
        blockController.createGenesisBlockIfNotExists();
        startServer(webPort);
        startPeerDiscovery(webPort);
    }

    public void startServer(int webPort) {
        webSocketServer = new WebSocketServer(new InetSocketAddress(webPort)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                String peerAddress = conn.getRemoteSocketAddress().toString();
                peers.put(peerAddress, conn);
                System.out.println("New connection: " + peerAddress);
                broadcast("New peer connected: " + peerAddress);
                blockController.sendGenesisBlock(conn);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                String peerAddress = conn.getRemoteSocketAddress().toString();
                peers.remove(peerAddress);
                System.out.println("Connection closed: " + peerAddress);
                broadcast("Peer disconnected: " + peerAddress);
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                String[] parts = message.split(":", 2);
                String messageId = parts[0];
                String messageContent = parts[1];

                if (processedMessages.containsKey(messageId)) {
                    return;
                }


                if ("GENESIS_BLOCK".equals(message)) {
                    blockController.processGenesisBlock(messageContent);
                }


                processedMessages.put(messageId, messageContent);

                String peerAddress = conn.getRemoteSocketAddress().toString();
                System.out.println("Message from " + peerAddress + ": " + messageContent);

                for (WebSocket peer : peers.values()) {
                    if (!peer.equals(conn)) {
                        peer.send(message);
                    }
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println("WebSocket server started successfully");
            }
        };

        try {
            webSocketServer.start();
            System.out.println("WebSocket server started on port " + webPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectToPeer(InetSocketAddress serverAddress) {
        try {
            URI uri = new URI("ws://" + serverAddress.getHostString() + ":" + serverAddress.getPort());

            WebSocketClient client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to server: " + serverAddress);
                    peers.put(serverAddress.toString(), this);
                    broadcast("New peer connected: " + serverAddress.toString());
                }

                @Override
                public void onMessage(String message) {
                    String[] parts = message.split(":", 2);
                    String messageId = parts[0];
                    String messageContent = parts[1];

                    if ("TRANSACTION".equals(messageId)) {
                        Block receivedBlock = EncryptionUtil.decryptBlock(messageContent);

                        if (blockController.verifyBlockSignature(receivedBlock, receivedBlock.getSignature())) {
                            synchronized (transactionStorage) {
                                transactionStorage.addTransaction(receivedBlock);
                            }
                        } else {
                            System.out.println("Invalid block signature received. Ignoring block.");
                        }
                        return;
                    }

                    if (processedMessages.containsKey(messageId)) {
                        return;
                    }

                    processedMessages.put(messageId, messageContent);

                    System.out.println("Processed message: " + messageContent);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from server: " + serverAddress + " Reason: " + reason);
                    peers.remove(serverAddress.toString());
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            client.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public  void startPeerDiscovery(int webPort) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);

                while (true) {
                    String discoveryMessage = "DISCOVERY_REQUEST" + webPort;

                    byte[] sendData = discoveryMessage.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), discoveryPort);
                    socket.send(sendPacket);
                    System.out.println("Discovery message sent.");

                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    socket.setSoTimeout(5000);
                    try {
                        socket.receive(receivePacket);

                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                        System.out.println("Discovered server at " + receivePacket.getAddress() + ": " + response);
                        processServerPorts(response, receivePacket.getAddress(), webPort);

                    } catch (java.net.SocketTimeoutException e) {
                        System.out.println("No response received within timeout.");
                    }

                    Thread.sleep(5000);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public  void processServerPorts(String response, InetAddress serverAddress, int webPort) throws UnknownHostException {
        String[] responseParts = response.split(",");
        String localAddress = InetAddress.getLocalHost().getHostAddress();

        for (String part : responseParts) {
            if (part.startsWith("SERVER_ADDRESS:")) {
                String[] addressParts = part.split(":");
                String address = addressParts[1];
                int serverPort = Integer.parseInt(addressParts[2]);

                InetSocketAddress discoveredAddress = new InetSocketAddress(address, serverPort);
                if (address.equals(localAddress) && serverPort == webPort) {
                    System.out.println("Skipping self connection attempt.");
                    continue;
                }
                if (discoveredPeers.containsKey(discoveredAddress.toString())) {
                    System.out.println("Already connected to: " + discoveredAddress);
                    continue;
                }

                discoveredPeers.put(discoveredAddress.toString(), discoveredAddress);
                System.out.println("WebSocket server discovered: " + discoveredAddress);

                connectToPeer(discoveredAddress);
            }
        }
    }

    public void broadcastTransaction(Block transaction) {
        ObjectMapper objectMapper = new ObjectMapper();
        String encryptedTransaction = EncryptionUtil.encryptBlock(transaction);
        broadcastMessage(encryptedTransaction);
    }

    public void broadcastMessage(String content) {
        String message = "TRANSACTION" + ":" + content;

        broadcast(message);
    }

    public static void broadcast(String message) {
        for (WebSocket peer : peers.values()) {
            peer.send(message);
        }
    }

    public static Map<String, InetSocketAddress> getDiscoveredPeers() {
        return discoveredPeers;
    }
}
