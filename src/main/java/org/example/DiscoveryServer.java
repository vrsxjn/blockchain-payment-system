package org.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiscoveryServer {
    private static final int DISCOVERY_PORT = 50000;
    private static final Map<InetAddress, Set<Integer>> knownServers = new HashMap<>();

    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(DISCOVERY_PORT);
            System.out.println("Listening for discovery requests on port " + DISCOVERY_PORT + "...");

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                String message = new String(packet.getData(), 0, packet.getLength()).trim();

                System.out.println("Discovery request received from " + clientAddress + ":" + clientPort);


                if (message.contains("DISCOVERY_REQUEST")) {
                    StringBuilder response = new StringBuilder();
                    String portString = message.substring("DISCOVERY_REQUEST".length()).trim();

                    int webPort;
                    try {
                        webPort = Integer.parseInt(portString);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port number: " + portString);
                        return;
                    }
                    addServer(clientAddress, webPort);


                    synchronized (knownServers) {
                        for (Map.Entry<InetAddress, Set<Integer>> entry : knownServers.entrySet()) {
                            InetAddress address = entry.getKey();
                            Set<Integer> ports = entry.getValue();
                            for (Integer port : ports) {
                                response.append("SERVER_ADDRESS:").append(address.getHostAddress()).append(":").append(port).append(",");
                            }
                        }
                    }
                    if (response.length() > 0) {
                        response.setLength(response.length() - 1); 
                    } 
                    
                    String responseString = response.toString();
                    byte[] responseBytes = responseString.getBytes();

                    DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
                    socket.send(responsePacket);
                    System.out.println("Responded with server info " + responseString + " to " + clientAddress + ":" + clientPort);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create or use DatagramSocket on port " + DISCOVERY_PORT);
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public static void addServer(InetAddress address, int port) {
        synchronized (knownServers) {
            knownServers.computeIfAbsent(address, k -> new HashSet<>()).add(port);
        }
    }

    public static void removeServer(InetAddress address) {
        synchronized (knownServers) {
            knownServers.remove(address);
        }
    }
}
