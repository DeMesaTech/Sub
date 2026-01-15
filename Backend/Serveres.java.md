import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Serveres {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        

        server.createContext("/calculate", new CalculateHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("Server started at http://localhost:" + PORT + "/calculate");
    }

    static class CalculateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String responseJson = "{}";
            int statusCode = 200;

            // Always add CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST");
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
                double total_bill_amnt = getJsonValue(body, "amount");

                // Your computation logic
                double m_kwh = mPres - mPrev;
                double sub_kwh = sPres - sPrev;
                double rate = total_bill_amnt / m_kwh;
                double tenant_amnt = sub_kwh * rate;

                // Log to database
                String sql = """
                INSERT INTO billing_records
                (mPrevious, mPresent, sPrevious, sPresent, m_kwh, sub_kwh, total_bill_amnt, tenant_amnt)  
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)  
                """;

                Class.forName("org.mariadb.jdbc.Driver");
                try (
                Connection conn = DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/subMeter_cal",
                "User",
                "pass143"
            )){

                PreparedStatement ps =
                conn.prepareStatement(sql);
                    ps.setDouble(1, mPrev);
                    ps.setDouble(2, mPres);
                    ps.setDouble(3, sPrev);
                    ps.setDouble(4, sPres);
                    ps.setDouble(5, m_kwh);
                    ps.setDouble(6, sub_kwh);
                    ps.setDouble(7, total_bill_amnt);
                    ps.setDouble(8, tenant_amnt);

                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                
                // Respond
                responseJson = "{\"total\":" + tenant_amnt + "}";
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
