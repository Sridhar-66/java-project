# Student Management (Spring Boot + Thymeleaf + SQLite)

This is a beginner-friendly Student Management System that runs in the browser using Spring Boot and Thymeleaf. It uses an SQLite database file `students.db` created at the project root.

Quick start (Codespaces / any environment with Maven & Java 17+):

1. Open a terminal in the project root.
2. Run:

```bash
mvn spring-boot:run
```

3. Open the forwarded port `8080` in the browser (Codespaces forwards automatically).

What you can do:
- Add new students
- List all students
- Edit existing students
- Delete students

Notes:
- The database table is created automatically at startup if it does not exist.
- If a form submission fails validations, a friendly error message is shown.
# java-project
