package com.example.attendance.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public static boolean addStudent(String regNo, String name) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO students (reg_no, name) VALUES (?, ?)");
            ps.setString(1, regNo);
            ps.setString(2, name);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteStudent(String regNo) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE reg_no = ?");
            ps.setString(1, regNo);
            return ps.executeUpdate() > 0;
        }
    }

    public static String getNameByReg(String regNo) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement("SELECT name FROM students WHERE reg_no = ?");
            ps.setString(1, regNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("name");
            return null;
        }
    }

    public static List<String[]> getAllStudentsWithTodayStatus(java.time.LocalDate date) throws SQLException {
        List<String[]> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT s.reg_no, s.name, COALESCE(a.status, 'Absent') AS status " +
                            "FROM students s LEFT JOIN attendance a ON s.reg_no = a.reg_no AND a.date = ? " +
                            "ORDER BY s.reg_no");
            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(new String[]{rs.getString("reg_no"), rs.getString("name"), rs.getString("status")});
            }
        }
        return out;
    }
}

