// package com.onlineexam.model;

// import java.time.LocalDateTime;

// /**
//  * Model for SchoolClass.
//  * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
//  */
// public class SchoolClass {
//     private int classId;
//     private String className;
//     private String description;
//     private Integer createdBy;
//     private LocalDateTime createdAt;
//     // Thuộc tính phụ trợ hiển thị số lượng học sinh trong lớp
//     private int studentCount;
//     // 1. Constructor mặc định (No-args constructor)
//     public SchoolClass() {
//     }
//     // 2. Constructor đầy đủ tham số
//     public SchoolClass(int classId, String className, String description, Integer createdBy, LocalDateTime createdAt) {
//         this.classId = classId;
//         this.className = className;
//         this.description = description;
//         this.createdBy = createdBy;
//         this.createdAt = createdAt;
//     }
//     // 3. Constructor thuận tiện cho việc tạo mới lớp (không cần truyền ID và createdAt)
//     public SchoolClass(String className, String description, Integer createdBy) {
//         this.className = className;
//         this.description = description;
//         this.createdBy = createdBy;
//     }
//     // 4. Getters và Setters
//     public int getClassId() {
//         return classId;
//     }
//     public void setClassId(int classId) {
//         this.classId = classId;
//     }
//     public String getClassName() {
//         return className;
//     }
//     public void setClassName(String className) {
//         this.className = className;
//     }
//     public String getDescription() {
//         return description;
//     }
//     public void setDescription(String description) {
//         this.description = description;
//     }
//     public Integer getCreatedBy() {
//         return createdBy;
//     }
//     public void setCreatedBy(Integer createdBy) {
//         this.createdBy = createdBy;
//     }
//     public LocalDateTime getCreatedAt() {
//         return createdAt;
//     }
//     public void setCreatedAt(LocalDateTime createdAt) {
//         this.createdAt = createdAt;
//     }
//     public int getStudentCount() {
//         return studentCount;
//     }
//     public void setStudentCount(int studentCount) {
//         this.studentCount = studentCount;
//     }
// }

package com.onlineexam.model;

/**
 * Model for SchoolClass.
 * TODO: Add fields, constructors, getters, and setters based on the agreed database schema.
 */
public class SchoolClass {
    private int classId;
    private String className;
    private String description;
    private int createdBy;

    public SchoolClass() {
    }

    public SchoolClass(int classId, String className,
                       String description, int createdBy) {
        this.classId = classId;
        this.className = className;
        this.description = description;
        this.createdBy = createdBy;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}