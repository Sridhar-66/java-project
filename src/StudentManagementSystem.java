import java.util.Scanner;
import java.sql.*;
import java.io.*;

public class StudentManagementSystem {
    private static Connection conn;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:students.db");
            try (Statement s = conn.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY, name TEXT NOT NULL, age INTEGER NOT NULL, grade TEXT NOT NULL, email TEXT NOT NULL)");
            }
            boolean running = true;
            while (running) {
                System.out.println("\n===== Student Management System =====\n1. Add a new student\n2. View all students\n3. Search for a student by ID\n4. Update student information\n5. Delete a student by ID\n6. Export all students to CSV\n7. Exit the program");
                    switch (getIntInput("Enter your choice: ")) {
                        case 1 -> addStudent();
                        case 2 -> viewAllStudents();
                        case 3 -> searchStudentById();
                        case 4 -> updateStudent();
                        case 5 -> deleteStudentById();
                        case 6 -> exportToCSV();
                        case 7 -> { System.out.println("Exiting program. Goodbye!"); running = false; }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) { System.out.println("Startup error: " + e.getMessage()); }
        finally { try { if (conn != null && !conn.isClosed()) conn.close(); } catch (Exception ignored) {} scanner.close(); }
    }

    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException e) { System.out.println("Invalid input. Please enter a number."); }
        }
    }
    private static String getStringInput(String prompt) {
        while (true) { System.out.print(prompt); String in = scanner.nextLine().trim(); if (!in.isEmpty()) return in; System.out.println("Input cannot be empty. Please try again."); }
    }
    // Allows empty input (used for optional fields when updating)
    private static String getOptionalInput(String prompt) { System.out.print(prompt); return scanner.nextLine().trim(); }

    // Get integer within range (inclusive)
    private static int getIntInputInRange(String prompt, int min, int max) {
        while (true) {
            int v = getIntInput(prompt);
            if (v < min || v > max) System.out.println("Value must be between " + min + " and " + max + ".");
            else return v;
        }
    }

    // Get positive integer (>0)
    private static int getPositiveIntInput(String prompt) {
        while (true) {
            int v = getIntInput(prompt);
            if (v <= 0) System.out.println("Please enter a positive integer.");
            else return v;
        }
    }
    private static String getEmailInput(String prompt) { while (true) { System.out.print(prompt); String in = scanner.nextLine().trim(); if (in.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) return in; System.out.println("Invalid email format. Please try again."); } }

    private static void addStudent() {
        System.out.println("\n--- Add New Student ---");
        int id;
        // Prompt until a unique, positive ID is provided
        while (true) {
            id = getPositiveIntInput("Enter student ID: ");
            if (studentExists(id)) System.out.println("A student with this ID already exists. Please enter a different ID.");
            else break;
        }
        String name = getStringInput("Enter student name: ");
        int age = getIntInputInRange("Enter student age: ", 5, 100);
        String grade = getStringInput("Enter student grade: ");
        String email = getEmailInput("Enter student email: ");
        try (var p = conn.prepareStatement("INSERT INTO students (id,name,age,grade,email) VALUES (?,?,?,?,?)")) { p.setInt(1,id); p.setString(2,name); p.setInt(3,age); p.setString(4,grade); p.setString(5,email); p.executeUpdate(); System.out.println("Student added successfully."); }
        catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }
    private static void viewAllStudents() {
        System.out.println("\n--- All Students ---");
        try (var s = conn.createStatement(); var rs = s.executeQuery("SELECT * FROM students")) {
            boolean found = false; while (rs.next()) { found = true; System.out.printf("ID: %d, Name: %s, Age: %d, Grade: %s, Email: %s\n", rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getString("grade"), rs.getString("email")); }
            if (!found) System.out.println("No students found.");
        } catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }
    private static void searchStudentById() {
        int id = getIntInput("Enter student ID to search: ");
        try (var p = conn.prepareStatement("SELECT * FROM students WHERE id=?")) { p.setInt(1,id); var rs = p.executeQuery(); if (rs.next()) System.out.printf("Student found: ID: %d, Name: %s, Age: %d, Grade: %s, Email: %s\n", rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getString("grade"), rs.getString("email")); else System.out.println("Student with ID " + id + " not found."); }
        catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }
    private static void updateStudent() {
        int id = getIntInput("Enter student ID to update: "); StudentRecord r = getStudentRecord(id); if (r==null) { System.out.println("Student with ID " + id + " not found."); return; }
        System.out.printf("Current info: ID: %d, Name: %s, Age: %d, Grade: %s, Email: %s\n", r.id,r.name,r.age,r.grade,r.email);
        String name = getOptionalInput("Enter new name (leave blank to keep current): "); if (name.isEmpty()) name = r.name; else name = name.trim();
        String ageStr = getOptionalInput("Enter new age (leave blank to keep current): "); int age = r.age; if (!ageStr.isEmpty()) {
            try { int parsed = Integer.parseInt(ageStr.trim()); if (parsed < 5 || parsed > 100) { System.out.println("Age must be between 5 and 100. Keeping current value."); } else age = parsed; } catch (NumberFormatException e) { System.out.println("Invalid age. Keeping current value."); }
        }
        String grade = getOptionalInput("Enter new grade (leave blank to keep current): "); if (grade.isEmpty()) grade = r.grade; else grade = grade.trim();
        String email = getOptionalInput("Enter new email (leave blank to keep current): "); if (email.isEmpty()) email = r.email; else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) { System.out.println("Invalid email format. Keeping current value."); email = r.email; }
        try (var p = conn.prepareStatement("UPDATE students SET name=?,age=?,grade=?,email=? WHERE id=?")) { p.setString(1,name); p.setInt(2,age); p.setString(3,grade); p.setString(4,email); p.setInt(5,id); p.executeUpdate(); System.out.println("Student updated successfully."); }
        catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }
    private static void deleteStudentById() {
        int id = getIntInput("Enter student ID to delete: "); try (var p = conn.prepareStatement("DELETE FROM students WHERE id=?")) { p.setInt(1,id); System.out.println(p.executeUpdate()>0?"Student deleted successfully.":"Student with ID " + id + " not found."); } catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }
    private static boolean studentExists(int id) { try (var p = conn.prepareStatement("SELECT 1 FROM students WHERE id=?")) { p.setInt(1,id); return p.executeQuery().next(); } catch (Exception e) { return false; } }
    private static StudentRecord getStudentRecord(int id) { try (var p = conn.prepareStatement("SELECT * FROM students WHERE id=?")) { p.setInt(1,id); var rs = p.executeQuery(); if (rs.next()) return new StudentRecord(rs.getInt("id"),rs.getString("name"),rs.getInt("age"),rs.getString("grade"),rs.getString("email")); } catch (Exception e) {} return null; }
    private static class StudentRecord { int id; String name; int age; String grade; String email; StudentRecord(int id,String name,int age,String grade,String email){this.id=id;this.name=name;this.age=age;this.grade=grade;this.email=email;} }

    // Export all students to CSV
    private static void exportToCSV() {
        String fileName = "students_export.csv";
        try (var stmt = conn.createStatement(); var rs = stmt.executeQuery("SELECT * FROM students"); var writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("ID,Name,Age,Grade,Email\n");
            boolean found = false;
            while (rs.next()) {
                found = true;
                String line = String.format("%d,%s,%d,%s,%s", rs.getInt("id"), escapeCsv(rs.getString("name")), rs.getInt("age"), escapeCsv(rs.getString("grade")), escapeCsv(rs.getString("email")));
                writer.write(line + "\n");
            }
            if (!found) System.out.println("No students to export.");
            else System.out.println("Exported students to " + fileName);
        } catch (Exception e) { System.out.println("Failed to export CSV: " + e.getMessage()); }
    }

    // Escape CSV fields that contain commas or quotes
    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

}
