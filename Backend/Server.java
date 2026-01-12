import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

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

            String responseJson = "{}";
            int statusCode = 200;

            // Always add CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            // Handle preflight OPTIONS request
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1); // 204 No Content
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes());

                // Example: parse JSON params
                double mPrev = getJsonValue(body, "mPrev");
                double mPres = getJsonValue(body, "mPres");
                double sPrev = getJsonValue(body, "sPrev");
                double sPres = getJsonValue(body, "sPres");
                double amount = getJsonValue(body, "amount");

                // Your computation logic
                double motherUsage = mPres - mPrev;
                double submeterUsage = sPres - sPrev;
                double rate = amount / motherUsage;
                double total = submeterUsage * rate;

                // Respond
                responseJson = "{\"total\":" + total + "}";

                // Log to database
                String sql = """
                INSERT INTO billing_records
                (mPrevious, mPresent, sPrevious, sPresent, m_kwh, sub_kwh, total_bill_amnt, total_amnt)  
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)  
                """;
                
                try (
                Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/subMeter_cal",
                "root",
                "password"  
            );

                PreparedStatement ps =
                conn.prepareStatement(sql)) {
                    ps.setDouble(1, mPrev);
                    ps.setDouble(2, mPres);
                    ps.setDouble(3, sPrev);
                    ps.setDouble(4, sPres);
                    ps.setDouble(5, motherUsage);
                    ps.setDouble(6, submeterUsage);
                    ps.setDouble(7, amount);
                    ps.setDouble(8, total);

                    ps.executeUpdate();
                };
                
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] bytes = responseJson.getBytes();
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);

                
                

            } catch (Exception e) {
                statusCode = 400;
                responseJson = "{\"error\":\"" + e.getMessage() + "\"}";
                byte[] bytes = responseJson.getBytes();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(statusCode, bytes.length);
                exchange.getResponseBody().write(bytes);
            } finally {
                exchange.getResponseBody().close();
            }
        }

        private static double getJsonValue(String json, String key) {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf(",", start);
            if (end == -1)
                end = json.indexOf("}", start);
            return Double.parseDouble(json.substring(start, end).trim());
        }

    }
}
