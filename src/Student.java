// Student.java
// Represents a student in the management system

public class Student {
    private int id;
    private String name;
    private int age;
    private String grade;
    private String email;

    public Student(int id, String name, int age, String grade, String email) {
        this.id = id; this.name = name; this.age = age; this.grade = grade; this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "ID: " + id + ", Name: " + name + ", Age: " + age + ", Grade: " + grade + ", Email: " + email;
    }
}
