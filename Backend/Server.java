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

            String responseJson;
            int statusCode = 200;

            try {
                String body = new String(exchange.getRequestBody().readAllBytes());

                double mPrev = getJsonValue(body, "mPrev");
                double mPres = getJsonValue(body, "mPres");
                double sPrev = getJsonValue(body, "sPrev");
                double sPres = getJsonValue(body, "sPres");
                double amount = getJsonValue(body, "amount");

                double motherUsage = mPres - mPrev;
                double submeterUsage = sPres - sPrev;
                double rate = amount / motherUsage;
                double total = submeterUsage * rate;

                responseJson = "{\"total\":" + total + "}";

                Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:8080/calculate",
                "root",
                "password"
            );

                String sql = """
                INSERT INTO billing_records
                (mPrev, mPres, sPrev, sPres, m_kwh, s_kwh, total_bill_amnt, total_bill_amnt)    
                """;

                PreparedStatement ps =
                conn.prepareStatement(sql);
                ps.setDouble(1, mPrev);
                ps.setDouble(2, mPres);
                ps.setDouble(3, sPrev);
                ps.setDouble(4, sPres);
                ps.setDouble(5, motherUsage);
                ps.setDouble(6, submeterUsage);
                ps.setDouble(7, amount);
                ps.setDouble(8, total);

                ps.executeUpdate();

            } catch (Exception e) {
                statusCode = 400;
                responseJson = "Error: " + e.getMessage();
            }

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseJson.getBytes().length);
            exchange.getResponseBody().write(responseJson.getBytes());
            exchange.getResponseBody().close();

            OutputStream os = exchange.getResponseBody();
            os.write(responseJson.getBytes());
            os.close();
        }
/*
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
        */

    private static double getJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Double.parseDouble(json.substring(start, end).trim());
    }

    }
}
