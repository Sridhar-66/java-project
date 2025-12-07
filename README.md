# Student Management System (CLI)

Project reorganized to a tidy structure:

- `src/` — Java source files (`Student.java`, `StudentManagementSystem.java`)
- `lib/` — third-party JARs (SQLite JDBC + SLF4J)
- `students.db` — SQLite database file (created at runtime)

Quick build & run (from project root):

```bash
javac -d bin -cp "lib/*" src/*.java
java -cp "bin:lib/*" StudentManagementSystem
```

Notes:
- `lib/*` already contains the required jars. If you add/remove jars, update the classpath accordingly.
- `students.db` persists student records between runs.
