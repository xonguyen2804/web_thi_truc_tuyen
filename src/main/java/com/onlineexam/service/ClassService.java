
package com.onlineexam.service;

import com.onlineexam.dao.ClassDAO;
import com.onlineexam.model.SchoolClass;

import java.sql.SQLException;
import java.util.List;

public class ClassService {
    private final ClassDAO classDAO;

    public ClassService() {
        this.classDAO = new ClassDAO();
    }

    public int createClass(String className, String description,
                           int teacherId) throws SQLException {
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Tên lớp không được để trống.");
        }
        if (teacherId <= 0) {
            throw new IllegalArgumentException("Giáo viên không hợp lệ.");
        }

        SchoolClass schoolClass = new SchoolClass(
                0, className.trim(), description, teacherId
        );

        return classDAO.create(schoolClass);
    }

    public SchoolClass getClassById(int classId) throws SQLException {
        if (classId <= 0) {
            throw new IllegalArgumentException("ID lớp không hợp lệ.");
        }

        return classDAO.findById(classId);
    }

    public List<SchoolClass> getClassesByTeacher(int teacherId)
            throws SQLException {
        if (teacherId <= 0) {
            throw new IllegalArgumentException("ID giáo viên không hợp lệ.");
        }

        return classDAO.findByTeacher(teacherId);
    }

    public boolean updateClass(int classId, String className,
                               String description) throws SQLException {
        if (classId <= 0) {
            throw new IllegalArgumentException("ID lớp không hợp lệ.");
        }
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Tên lớp không được để trống.");
        }

        SchoolClass schoolClass = classDAO.findById(classId);
        if (schoolClass == null) {
            return false;
        }

        schoolClass.setClassName(className.trim());
        schoolClass.setDescription(description);

        return classDAO.update(schoolClass);
    }

    public boolean deleteClass(int classId) throws SQLException {
        if (classId <= 0) {
            throw new IllegalArgumentException("ID lớp không hợp lệ.");
        }

        return classDAO.delete(classId);
    }
}