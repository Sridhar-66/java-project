// StudentManagementSystem.java
// Main class for the CLI-based Student Management System
// Provides menu-driven interface for managing students
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class StudentManagementSystem {
    // SQLite database connection
    private static Connection conn = null;
    // Scanner for user input
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Main method: runs the menu loop and handles user choices.
     */
    public static void main(String[] args) {
        // Initialize database connection and table
        try {
            connectDatabase();
            createStudentTable();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        while (true) {
            printMenu();
            int choice = getIntInput("Enter your choice: ");
            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    viewAllStudents();
                    break;
                case 3:
                    searchStudentById();
                    break;
                case 4:
                    updateStudent();
                    break;
                case 5:
                    deleteStudentById();
                    break;
                case 6:
                    System.out.println("Exiting program. Goodbye!");
                    closeDatabase();
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Connects to the SQLite database (creates file if not exists)
     */
    private static void connectDatabase() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found.");
        }
        conn = DriverManager.getConnection("jdbc:sqlite:students.db");
    }

    /**
     * Creates the students table if it does not exist
     */
    private static void createStudentTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS students (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "age INTEGER NOT NULL, " +
                "grade TEXT NOT NULL, " +
                "email TEXT NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Closes the database connection
     */
    private static void closeDatabase() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing database: " + e.getMessage());
        }
    }

    /**
     * Prints the main menu options to the user.
     */
    private static void printMenu() {
        System.out.println("\n===== Student Management System =====");
        System.out.println("1. Add a new student");
        System.out.println("2. View all students");
        System.out.println("3. Search for a student by ID");
        System.out.println("4. Update student information");
        System.out.println("5. Delete a student by ID");
        System.out.println("6. Exit the program");
    }

    /**
     * Gets validated integer input from the user.
     * @param prompt The prompt message
     * @return Valid integer entered by user
     */
    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Gets validated non-empty string input from the user.
     * @param prompt The prompt message
     * @return Non-empty string
     */
    private static String getStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            } else {
                System.out.println("Input cannot be empty. Please try again.");
            }
        }
    }

    /**
     * Gets validated email input from the user (simple regex validation).
     * @param prompt The prompt message
     * @return Valid email string
     */
    private static String getEmailInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return input;
            } else {
                System.out.println("Invalid email format. Please try again.");
            }
        }
    }

    /**
     * Adds a new student to the list after validating input and checking for duplicate ID.
     */
    private static void addStudent() {
        System.out.println("\n--- Add New Student ---");
        int id = getIntInput("Enter student ID: ");
        // Check for duplicate ID in DB
        try (var pstmt = conn.prepareStatement("SELECT id FROM students WHERE id = ?")) {
            pstmt.setInt(1, id);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("A student with this ID already exists.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        String name = getStringInput("Enter student name: ");
        int age = getIntInput("Enter student age: ");
        String grade = getStringInput("Enter student grade: ");
        String email = getEmailInput("Enter student email: ");
        try (var pstmt = conn.prepareStatement("INSERT INTO students (id, name, age, grade, email) VALUES (?, ?, ?, ?, ?);")) {
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setInt(3, age);
            pstmt.setString(4, grade);
            pstmt.setString(5, email);
            pstmt.executeUpdate();
            System.out.println("Student added successfully.");
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Displays all students in the system, or a message if none exist.
     */
    private static void viewAllStudents() {
        System.out.println("\n--- All Students ---");
        try (var stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT * FROM students");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Age: " + rs.getInt("age") + ", Grade: " + rs.getString("grade") + ", Email: " + rs.getString("email"));
            }
            if (!found) {
                System.out.println("No students found.");
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Searches for a student by ID and displays their information if found.
     */
    private static void searchStudentById() {
        System.out.println("\n--- Search Student By ID ---");
        int id = getIntInput("Enter student ID to search: ");
        try (var pstmt = conn.prepareStatement("SELECT * FROM students WHERE id = ?")) {
            pstmt.setInt(1, id);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("Student found: ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Age: " + rs.getInt("age") + ", Grade: " + rs.getString("grade") + ", Email: " + rs.getString("email"));
            } else {
                System.out.println("Student with ID " + id + " not found.");
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Updates information for an existing student, allowing fields to be skipped.
     */
    private static void updateStudent() {
        System.out.println("\n--- Update Student Information ---");
        int id = getIntInput("Enter student ID to update: ");
        try (var pstmt = conn.prepareStatement("SELECT * FROM students WHERE id = ?")) {
            pstmt.setInt(1, id);
            var rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Student with ID " + id + " not found.");
                return;
            }
            System.out.println("Current info: ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Age: " + rs.getInt("age") + ", Grade: " + rs.getString("grade") + ", Email: " + rs.getString("email"));
            String name = getStringInput("Enter new name (leave blank to keep current): ");
            if (name.isEmpty()) name = rs.getString("name");
            String ageStr = getStringInput("Enter new age (leave blank to keep current): ");
            int age = rs.getInt("age");
            if (!ageStr.isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid age. Keeping current value.");
                }
            }
            String grade = getStringInput("Enter new grade (leave blank to keep current): ");
            if (grade.isEmpty()) grade = rs.getString("grade");
            String email = getStringInput("Enter new email (leave blank to keep current): ");
            if (email.isEmpty()) email = rs.getString("email");
            else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                System.out.println("Invalid email format. Keeping current value.");
                email = rs.getString("email");
            }
            try (var upstmt = conn.prepareStatement("UPDATE students SET name=?, age=?, grade=?, email=? WHERE id=?")) {
                upstmt.setString(1, name);
                upstmt.setInt(2, age);
                upstmt.setString(3, grade);
                upstmt.setString(4, email);
                upstmt.setInt(5, id);
                upstmt.executeUpdate();
                System.out.println("Student updated successfully.");
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Deletes a student from the list by their ID.
     */
    private static void deleteStudentById() {
        System.out.println("\n--- Delete Student By ID ---");
        int id = getIntInput("Enter student ID to delete: ");
        try (var pstmt = conn.prepareStatement("DELETE FROM students WHERE id = ?")) {
            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("Student deleted successfully.");
            } else {
                System.out.println("Student with ID " + id + " not found.");
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
