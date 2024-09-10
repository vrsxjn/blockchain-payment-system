package org.example.security.access;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.Set;

public class AccessControl {

    private final Set<String> authorizedEndpoints;

    public AccessControl() {
        this.authorizedEndpoints = new HashSet<>();
        authorizedEndpoints.add("localhost:6000");
        authorizedEndpoints.add("192.168.1.1:8080");
    }

    public void addAuthorizedEndpoint(String endpoint) {
        authorizedEndpoints.add(endpoint);
    }

    public boolean isAuthorized(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        int clientPort = request.getRemotePort();

        String clientEndpoint = clientIp + ":" + clientPort;

        return authorizedEndpoints.contains(clientEndpoint);
    }
}
