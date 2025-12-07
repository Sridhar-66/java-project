import java.util.Scanner;
import java.sql.*;

public class StudentManagementSystem {
    private static Connection conn;
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Main method: runs the menu loop and handles user choices.
     */
    public static void main(String[] args) {
        try {
            connectDatabase();
            createStudentTable();
            for (boolean running = true; running;) {
                printMenu();
                switch (getIntInput("Enter your choice: ")) {
                    case 1 -> addStudent();
                    case 2 -> viewAllStudents();
                    case 3 -> searchStudentById();
                    case 4 -> updateStudent();
                    case 5 -> deleteStudentById();
                    case 6 -> { System.out.println("Exiting program. Goodbye!"); running = false; }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
        finally { closeDatabase(); scanner.close(); }
    }

    private static void connectDatabase() throws Exception {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:students.db");
    }
    private static void createStudentTable() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY, name TEXT NOT NULL, age INTEGER NOT NULL, grade TEXT NOT NULL, email TEXT NOT NULL)");
        }
    }
    private static void closeDatabase() {
        try { if (conn != null && !conn.isClosed()) conn.close(); } catch (Exception e) { System.out.println("Error closing database: " + e.getMessage()); }
    }

    private static void printMenu() {
        System.out.println("\n===== Student Management System =====\n1. Add a new student\n2. View all students\n3. Search for a student by ID\n4. Update student information\n5. Delete a student by ID\n6. Exit the program");
    }

    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Invalid input. Please enter a number."); }
        }
    }

    private static String getStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    private static String getEmailInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) return input;
            System.out.println("Invalid email format. Please try again.");
        }
    }

    private static void addStudent() {
        System.out.println("\n--- Add New Student ---");
        int id = getIntInput("Enter student ID: ");
        if (studentExists(id)) { System.out.println("A student with this ID already exists."); return; }
        String name = getStringInput("Enter student name: ");
        int age = getIntInput("Enter student age: ");
        String grade = getStringInput("Enter student grade: ");
        String email = getEmailInput("Enter student email: ");
        try (var pstmt = conn.prepareStatement("INSERT INTO students (id, name, age, grade, email) VALUES (?, ?, ?, ?, ?);")) {
            pstmt.setInt(1, id); pstmt.setString(2, name); pstmt.setInt(3, age); pstmt.setString(4, grade); pstmt.setString(5, email);
            pstmt.executeUpdate();
            System.out.println("Student added successfully.");
        } catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }

    private static void viewAllStudents() {
        System.out.println("\n--- All Students ---");
        try (var stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT * FROM students");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d, Name: %s, Age: %d, Grade: %s, Email: %s\n", rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getString("grade"), rs.getString("email"));
            }
            if (!found) System.out.println("No students found.");
        } catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }

    private static void searchStudentById() {
        System.out.println("\n--- Search Student By ID ---");
        int id = getIntInput("Enter student ID to search: ");
        try (var pstmt = conn.prepareStatement("SELECT * FROM students WHERE id = ?")) {
            pstmt.setInt(1, id);
            var rs = pstmt.executeQuery();
            if (rs.next()) System.out.printf("Student found: ID: %d, Name: %s, Age: %d, Grade: %s, Email: %s\n", rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getString("grade"), rs.getString("email"));
            else System.out.println("Student with ID " + id + " not found.");
        } catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }

    private static void updateStudent() {
        System.out.println("\n--- Update Student Information ---");
        int id = getIntInput("Enter student ID to update: ");
        StudentRecord r = getStudentRecord(id);
        if (r == null) { System.out.println("Student with ID " + id + " not found."); return; }
        System.out.printf("Current info: ID: %d, Name: %s, Age: %d, Grade: %s, Email: %s\n", r.id, r.name, r.age, r.grade, r.email);
        String name = getStringInput("Enter new name (leave blank to keep current): ");
        if (name.isEmpty()) name = r.name;
        String ageStr = getStringInput("Enter new age (leave blank to keep current): ");
        int age = r.age;
        if (!ageStr.isEmpty()) try { age = Integer.parseInt(ageStr); } catch (NumberFormatException e) { System.out.println("Invalid age. Keeping current value."); }
        String grade = getStringInput("Enter new grade (leave blank to keep current): ");
        if (grade.isEmpty()) grade = r.grade;
        String email = getStringInput("Enter new email (leave blank to keep current): ");
        if (email.isEmpty()) email = r.email;
        else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) { System.out.println("Invalid email format. Keeping current value."); email = r.email; }
        try (var upstmt = conn.prepareStatement("UPDATE students SET name=?, age=?, grade=?, email=? WHERE id=?")) {
            upstmt.setString(1, name); upstmt.setInt(2, age); upstmt.setString(3, grade); upstmt.setString(4, email); upstmt.setInt(5, id);
            upstmt.executeUpdate();
            System.out.println("Student updated successfully.");
        } catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }
    private static boolean studentExists(int id) {
        try (var pstmt = conn.prepareStatement("SELECT 1 FROM students WHERE id = ?")) {
            pstmt.setInt(1, id); return pstmt.executeQuery().next();
        } catch (Exception e) { return false; }
    }
    private static StudentRecord getStudentRecord(int id) {
        try (var pstmt = conn.prepareStatement("SELECT * FROM students WHERE id = ?")) {
            pstmt.setInt(1, id); var rs = pstmt.executeQuery();
            if (rs.next()) return new StudentRecord(rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getString("grade"), rs.getString("email"));
        } catch (Exception e) {}
        return null;
    }
    private static class StudentRecord {
        int id; String name; int age; String grade; String email;
        StudentRecord(int id, String name, int age, String grade, String email) { this.id = id; this.name = name; this.age = age; this.grade = grade; this.email = email; }
    }
    }

    private static void deleteStudentById() {
        System.out.println("\n--- Delete Student By ID ---");
        int id = getIntInput("Enter student ID to delete: ");
        try (var pstmt = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
            pstmt.setInt(1, id);
            System.out.println(pstmt.executeUpdate() > 0 ? "Student deleted successfully." : "Student with ID " + id + " not found.");
        } catch (Exception e) { System.out.println("Database error: " + e.getMessage()); }
    }
}
