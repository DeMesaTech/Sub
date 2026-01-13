            
                // Log to database
                String sql = """
                INSERT INTO billing_records
                (mPrevious, mPresent, sPrevious, sPresent, m_kwh, sub_kwh, total_bill_amnt, tenant_amnt)  
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
                    ps.setDouble(5, m_kwh);
                    ps.setDouble(6, sub_kwh);
                    ps.setDouble(7, total_bill_amnt);
                    ps.setDouble(8, tenant_amnt);

                    ps.executeUpdate();
                };
                

