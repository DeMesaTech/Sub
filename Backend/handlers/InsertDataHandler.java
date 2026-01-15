package handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import db.DBConnection;
import util.JsonUtil;

public class InsertDataHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // ===== CORS =====
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Handle preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes());

        // ===== JSON parsing =====
        double mPrev = JsonUtil.getDouble(body, "mPrev");
        double mPres = JsonUtil.getDouble(body, "mPres");
        double sPrev = JsonUtil.getDouble(body, "sPrev");
        double sPres = JsonUtil.getDouble(body, "sPres");
        double total_bill_amnt = JsonUtil.getDouble(body, "amount");

        // Your computation logic
        double m_kwh = mPres - mPrev;
        double sub_kwh = sPres - sPrev;
        double rate = total_bill_amnt / m_kwh;
        double tenant_amnt = sub_kwh * rate;

        // ===== DB insert =====
        String sql = """
                INSERT INTO billing_records
                (mPrevious, mPresent, sPrevious, sPresent, total_bill_amnt)  
                VALUES (?, ?, ?, ?, ?)  
                """;

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setDouble(1, mPrev);
            ps.setDouble(2, mPres);
            ps.setDouble(3, sPrev);
            ps.setDouble(4, sPres);
            ps.setDouble(5, total_bill_amnt);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        String response = "{\"total\":" + tenant_amnt + "}";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
    
}
