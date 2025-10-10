package com.example.attendance.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    // Mark attendance for a regNo (uses students table to get name)
    public static boolean markAttendanceByReg(String regNo, LocalDate date, String status) throws SQLException {
        String name = StudentDAO.getNameByReg(regNo);
        if (name == null) return false; // student not found
        try (Connection c = DBConnection.getConnection()) {
            // REPLACE INTO will insert or update because (date, reg_no) primary key
            PreparedStatement ps = c.prepareStatement(
                    "REPLACE INTO attendance (date, reg_no, name, status) VALUES (?, ?, ?, ?)"
            );
            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, regNo);
            ps.setString(3, name);
            ps.setString(4, status);
            ps.executeUpdate();
            return true;
        }
    }

    // Fetch attendance rows for a specific date as [date, regNo, name, status]
    public static List<String[]> getAttendanceForDate(LocalDate date) throws SQLException {
        List<String[]> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT date, reg_no, name, status FROM attendance WHERE date = ? ORDER BY reg_no");
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(new String[]{
                        rs.getDate("date").toString(),
                        rs.getString("reg_no"),
                        rs.getString("name"),
                        rs.getString("status")
                });
            }
        }
        return out;
    }

    // Clear attendance rows for a date
    public static void clearAttendanceForDate(LocalDate date) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM attendance WHERE date = ?");
            ps.setDate(1, Date.valueOf(date));
            ps.executeUpdate();
        }
    }
}
