package tcp5;

import java.io.OutputStream;
import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;

public class IPv6TunnelServerTCP {
    private static final int IPV4_HEADER_LENGTH = 20;
    private static final int IPV6_HEADER_LENGTH = 40;

    public static void main(String[] args) {
        int serverPort = 9999;

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server listening on port " + serverPort);

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    byte[] buffer = clientSocket.getInputStream().readAllBytes();
                    handlePacket(buffer);

                    // Acknowledge back to the client (optional)
                    OutputStream out = clientSocket.getOutputStream();
                    out.write("ACK".getBytes());
                    out.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Processes the received IPv4 packet, extracts the encapsulated IPv6 packet, and prints the details
    private static void handlePacket(byte[] packet) {
        if (packet.length > IPV4_HEADER_LENGTH + IPV6_HEADER_LENGTH) {
            ByteBuffer buffer = ByteBuffer.wrap(packet);
    
            buffer.position(IPV4_HEADER_LENGTH);  // Skip IPv4 header
            int versionTrafficClassFlowLabel = buffer.getInt();  // Read IPv6 version info
            byte[] srcIPv6 = new byte[16];
            byte[] destIPv6 = new byte[16];
            buffer.get(srcIPv6);
            buffer.get(destIPv6);
    
            byte customPriority = packet[IPV4_HEADER_LENGTH + IPV6_HEADER_LENGTH];
            byte customReliability = packet[IPV4_HEADER_LENGTH + IPV6_HEADER_LENGTH + 1];
            byte customLatency = packet[IPV4_HEADER_LENGTH + IPV6_HEADER_LENGTH + 2];
    
            String message = new String(packet, IPV4_HEADER_LENGTH + IPV6_HEADER_LENGTH + 3, 
                packet.length - IPV4_HEADER_LENGTH - IPV6_HEADER_LENGTH - 3, StandardCharsets.UTF_8);
    
            System.out.println("Received IPv6 packet encapsulated in IPv4:");
            
            try {
                // Handle IPv6 addresses and convert them to string representations
                System.out.println("Source IPv6: " + InetAddress.getByAddress(srcIPv6).getHostAddress());
                System.out.println("Destination IPv6: " + InetAddress.getByAddress(destIPv6).getHostAddress());
            } catch (UnknownHostException e) {
                System.out.println("Error resolving IPv6 address: " + e.getMessage());
            }
            
            // Print QoS information
            System.out.println("Custom QoS - Priority: " + customPriority + ", Reliability: " + customReliability + ", Latency: " + customLatency);
            System.out.println("Message: " + message);
        } else {
            System.out.println("Received packet with no data.");
        }
    }
    
}
