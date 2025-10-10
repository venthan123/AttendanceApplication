package com.example.attendance;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dao.StudentDAO;
import com.example.attendance.util.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main extends JFrame {

    private final JTextField regField = new JTextField(10);
    private final JTextField nameField = new JTextField(18);
    private final JTextField markRegField = new JTextField(10);
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"RegNo","Name","Status"}, 0);

    public Main() {
        setTitle("Attendance Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8,8));

        // header
        JLabel title = new JLabel("📋 Daily Attendance", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        // center table
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // left panel: add/delete student
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        left.setPreferredSize(new Dimension(420, getHeight()));

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,8));
        addPanel.add(new JLabel("RegNo:"));
        addPanel.add(regField);
        addPanel.add(new JLabel("Name:"));
        addPanel.add(nameField);
        JButton addBtn = new JButton("Add");
        addBtn.setBackground(new Color(60,179,113)); addBtn.setForeground(Color.WHITE);
        JButton delBtn = new JButton("Delete");
        delBtn.setBackground(new Color(220,20,60)); delBtn.setForeground(Color.WHITE);
        addPanel.add(addBtn); addPanel.add(delBtn);
        left.add(addPanel);

        left.add(Box.createVerticalStrut(12));

        JPanel markPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,8));
        markPanel.add(new JLabel("Enter RegNo:"));
        markPanel.add(markRegField);
        JButton presentBtn = new JButton("Present");
        presentBtn.setBackground(new Color(34,139,34)); presentBtn.setForeground(Color.WHITE);
        JButton absentBtn = new JButton("Absent");
        absentBtn.setBackground(new Color(255,99,71)); absentBtn.setForeground(Color.WHITE);
        markPanel.add(presentBtn); markPanel.add(absentBtn);
        left.add(markPanel);

        left.add(Box.createVerticalStrut(12));

        JButton exportBtn = new JButton("Export Today -> Excel & Clear");
        exportBtn.setBackground(new Color(30,144,255)); exportBtn.setForeground(Color.WHITE);
        left.add(exportBtn);

        add(left, BorderLayout.WEST);

        // load table
        Runnable reload = () -> {
            try {
                List<String[]> rows = StudentDAO.getAllStudentsWithTodayStatus(LocalDate.now());
                tableModel.setRowCount(0);
                for (String[] r : rows) tableModel.addRow(r);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "DB error: "+e.getMessage());
            }
        };
        reload.run();

        // Add student
        addBtn.addActionListener(ev -> {
            String reg = regField.getText().trim();
            String name = nameField.getText().trim();
            if (reg.isEmpty() || name.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter RegNo and Name"); return; }
            try {
                boolean ok = StudentDAO.addStudent(reg, name);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Student added");
                    regField.setText(""); nameField.setText("");
                    reload.run();
                } else JOptionPane.showMessageDialog(this, "Failed to add");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "DB error: "+ex.getMessage());
            }
        });

        // Delete student
        delBtn.addActionListener(ev -> {
            String reg = regField.getText().trim();
            if (reg.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter RegNo to delete"); return; }
            int choice = JOptionPane.showConfirmDialog(this, "Delete student " + reg + " permanently?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
            try {
                boolean ok = StudentDAO.deleteStudent(reg);
                if (ok) JOptionPane.showMessageDialog(this, "Deleted");
                else JOptionPane.showMessageDialog(this, "No student found");
                reload.run();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "DB error: "+ex.getMessage());
            }
        });

        // Present
        presentBtn.addActionListener(ev -> {
            markAttendance("Present", reload);
        });

        // Absent
        absentBtn.addActionListener(ev -> {
            markAttendance("Absent", reload);
        });

        // Export & clear
        exportBtn.addActionListener(ev -> {
            try {
                List<String[]> data = AttendanceDAO.getAttendanceForDate(LocalDate.now());
                if (data.isEmpty()) {
                    int ans = JOptionPane.showConfirmDialog(this, "No attendance recorded today. Still export empty file?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (ans != JOptionPane.YES_OPTION) return;
                }
                String fileName = "Attendance_" + LocalDate.now() + ".xlsx";
                ExcelExporter.exportAttendanceToFile(data, fileName);
                AttendanceDAO.clearAttendanceForDate(LocalDate.now());
                JOptionPane.showMessageDialog(this, "Exported to " + fileName + " and cleared today's attendance.");
                reload.run();
            } catch (SQLException | IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage());
            }
        });
    }

    private void markAttendance(String status, Runnable reload) {
        String reg = markRegField.getText().trim();
        if (reg.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter RegNo"); return; }
        try {
            boolean ok = AttendanceDAO.markAttendanceByReg(reg, LocalDate.now(), status);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "RegNo not found. Add student first.");
                return;
            }
            markRegField.setText("");
            reload.run();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB error: "+ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main m = new Main();
            m.setVisible(true);
        });
    }
}
