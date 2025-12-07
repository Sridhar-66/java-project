# Student Management System (CLI)

This is a small command-line Student Management System written in Java. It stores student records in a local SQLite database (`students.db`) and provides a simple menu-driven interface to add, view, search, update, and delete students.

**Project Layout**
- `src/`: Java source files (`Student.java`, `StudentManagementSystem.java`)
- `lib/`: Third-party libraries (SQLite JDBC driver and SLF4J jars)
- `bin/`: Compiled classes (created by the `javac -d bin ...` command)
- `students.db`: SQLite database file (created at runtime)

**Features**
- Add new students with `id`, `name`, `age`, `grade`, and `email`.
- View all students.
- Search students by ID.
- Update and delete student records.
- Input validation for numeric fields and email format.

**Prerequisites**
- Java 11+ (JDK)
- The `lib/` directory should contain the JDBC and SLF4J jars. The repository currently includes:
	- `lib/sqlite-jdbc-3.45.3.0.jar`
	- `lib/slf4j-api-2.0.13.jar`
	- `lib/slf4j-simple-2.0.13.jar`

**Build & Run (quick)**
From the project root run:

```bash
javac -d bin -cp "lib/*" src/*.java
java -cp "bin:lib/*" StudentManagementSystem
```

If you prefer running without creating `bin/`, you can compile and run directly:

```bash
javac -cp "lib/*" src/*.java
java -cp ".:lib/*:src" StudentManagementSystem
```

**Usage example**
- Start the program, choose option `1` to add a student. Enter numeric `id` and `age`, name/grade/email as prompted.
- Choose option `2` to list all students and confirm records were saved.

**Troubleshooting**
- If you see `NoClassDefFoundError: org/slf4j/LoggerFactory`, make sure `slf4j-api` and a simple binding (`slf4j-simple`) are present in `lib/` and included on the classpath.
- If the program can't find the SQLite JDBC driver, ensure `lib/sqlite-jdbc-*.jar` is present and on the classpath.

**Next steps / Enhancements**
- Add Maven/Gradle build files to manage dependencies automatically.
- Add export/import CSV functionality.
- Add unit tests for core operations.

**License & Notes**
- This project is provided as-is for learning and demonstration purposes.
