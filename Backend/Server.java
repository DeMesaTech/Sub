// ========================
// Backend Template - Java
// ========================

import com.sun.net.httpserver.HttpServer;

import handlers.InsertDataHandler;
import handlers.GetDataHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

// ========================
// Main Server Class
// ========================
public class Server {

    // Server port
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // ======== REST Endpoints ========
        server.createContext("/calculate", new InsertDataHandler());
        server.createContext("/get-data", new GetDataHandler());

        // Default executor
        server.setExecutor(null);
        server.start();

        System.out.println("Server started at http://localhost:" + PORT);
    }

}