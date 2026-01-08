import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/calculate", new CalculateHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("Server started at http://localhost:8080/calculate");
    }

    static class CalculateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String response;
            int statusCode = 200;

            try {
                String query = exchange.getRequestURI().getQuery();

                if (query == null) {
                    throw new IllegalArgumentException("No query parameters provided");
                }

                double mPrev = getParam(query, "mPrev");
                double mPres = getParam(query, "mPres");
                double sPrev = getParam(query, "sPrev");
                double sPres = getParam(query, "sPres");
                double amount = getParam(query, "amount");

                response =
                    "Parsed values:\n" +
                    "mPrev=" + mPrev + "\n" +
                    "mPres=" + mPres + "\n" +
                    "sPrev=" + sPrev + "\n" +
                    "sPres=" + sPres + "\n" +
                    "amount=" + amount;

            } catch (Exception e) {
                statusCode = 400;
                response = "Error: " + e.getMessage();
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    private static double getParam(String query, String key) {
        String[] pairs = query.split("&");

        for (String pair : pairs) {
            String[] parts = pair.split("=");
            if (parts.length == 2 && parts[0].equals(key)) {
                return Double.parseDouble(parts[1]);
            }
        }

        throw new IllegalArgumentException("Missing parameter: " + key);
    }
    }
}
