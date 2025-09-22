package Utilities.TestData;

import java.sql.*;

public class DatabaseUtils {

    private static final String DB_URL =
            "jdbc:sqlserver://az-db-gim-uat.database.windows.net:1433;"
            + "databaseName=[Cube2];"  // ✅ use brackets if DB name has a space
            + "encrypt=true;"
            + "trustServerCertificate=false;"
            + "loginTimeout=30;";
    private static final String USER = "###";
    private static final String PASSWORD = "###";

    public static String getSettlementReportData(String dateFrom, String dateTo) {
        StringBuilder result = new StringBuilder();

        String query = "SELECT * " +
                "FROM [dbo].[SettlementData] " +
                "WHERE IsReady = 1 " +
                "AND BankId = '10124' " +
                "AND IssuingDate >= ? " +   // ✅ placeholder
                "AND IssuingDate < ?";      // ✅ placeholder

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, dateFrom);
            stmt.setString(2, dateTo);

            ResultSet rs = stmt.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            int columns = rsmd.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columns; i++) {
                    result.append(rs.getString(i)).append(" | ");
                }
                result.append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("❌ DB query failed!", e);
        }

        return result.toString();
    }
}
