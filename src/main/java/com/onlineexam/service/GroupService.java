
package com.onlineexam.service;

import com.onlineexam.dao.ClassDAO;
import com.onlineexam.dao.GroupDAO;
import com.onlineexam.model.Group;

import java.sql.SQLException;
import java.util.List;

public class GroupService {
    private final GroupDAO groupDAO;
    private final ClassDAO classDAO;

    public GroupService() {
        this.groupDAO = new GroupDAO();
        this.classDAO = new ClassDAO();
    }

    public int createGroup(int classId, String groupName,
                           String description) throws SQLException {
        if (classId <= 0) {
            throw new IllegalArgumentException("ID lớp không hợp lệ.");
        }
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Tên nhóm không được để trống.");
        }
        if (classDAO.findById(classId) == null) {
            throw new IllegalArgumentException("Lớp học không tồn tại.");
        }

        Group group = new Group(
                0, classId, groupName.trim(), description
        );

        return groupDAO.create(group);
    }

    public Group getGroupById(int groupId) throws SQLException {
        if (groupId <= 0) {
            throw new IllegalArgumentException("ID nhóm không hợp lệ.");
        }

        return groupDAO.findById(groupId);
    }

    public List<Group> getGroupsByClass(int classId) throws SQLException {
        if (classId <= 0) {
            throw new IllegalArgumentException("ID lớp không hợp lệ.");
        }

        if (classDAO.findById(classId) == null) {
            throw new IllegalArgumentException("Lớp học không tồn tại.");
        }

        return groupDAO.findByClass(classId);
    }

    public boolean updateGroup(int groupId, String groupName,
                              String description) throws SQLException {
        if (groupId <= 0) {
            throw new IllegalArgumentException("ID nhóm không hợp lệ.");
        }
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("Tên nhóm không được để trống.");
        }

        Group group = groupDAO.findById(groupId);
        if (group == null) {
            return false;
        }

        group.setGroupName(groupName.trim());
        group.setDescription(description);

        return groupDAO.update(group);
    }

    public boolean deleteGroup(int groupId) throws SQLException {
        if (groupId <= 0) {
            throw new IllegalArgumentException("ID nhóm không hợp lệ.");
        }

        return groupDAO.delete(groupId);
    }
}
