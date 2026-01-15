package handlers;

import com.sun.net.httpserver.HttpHandler;

import db.DBConnection;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// ========================
    // Get Data Handler (GET)
    // ========================
    public class GetDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    Class.forName("org.mariadb.jdbc.Driver");
                    try (Connection conn = DBConnection.getConnection()) {
                        String mainMeter = """
                                SELECT mPrevious, mPresent, (mPrevious-mPresent) AS m_kwh, total_bill_amnt
                        """

                        PreparedStatement ps = conn.prepareStatement(mainMeter);
                        ResultSet rs = ps.executeQuery();

                        StringBuilder json = new StringBuilder("[");
                        boolean first = true;
                        while (rs.next()) {
                            if (!first) json.append(",");
                            json.append("{")
                                    .append("\"col1\":\"").append(rs.getString("col1")).append("\",")
                                    .append("\"col2\":").append(rs.getDouble("col2"))
                                    .append("}");
                            first = false;
                        }
                        json.append("]");

                        byte[] response = json.toString().getBytes();
                        exchange.sendResponseHeaders(200, response.length);
                        exchange.getResponseBody().write(response);
                    }
                } catch (Exception e) {
                    byte[] response = ("{\"error\":\""+e.getMessage()+"\"}").getBytes();
                    exchange.sendResponseHeaders(500, response.length);
                    exchange.getResponseBody().write(response);
                } finally {
                    exchange.getResponseBody().close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }