
package com.onlineexam.service;

import com.onlineexam.dao.QuestionDAO;
import com.onlineexam.model.Question;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class QuestionService {
    private static final Set<String> VALID_TYPES = Set.of(
            "SINGLE", "MULTIPLE", "TRUE_FALSE", "SHORT_ANSWER"
    );

    private static final Set<String> VALID_STATUSES = Set.of(
            "ACTIVE", "INACTIVE"
    );

    private final QuestionDAO questionDAO;

    public QuestionService() {
        this.questionDAO = new QuestionDAO();
    }

    public int createQuestion(int teacherId, String questionText,
                              String questionType, BigDecimal points,
                              String explanation) throws SQLException {
        validateQuestion(teacherId, questionText, questionType, points);

        Question question = new Question(
                0,
                teacherId,
                questionText.trim(),
                questionType,
                points,
                explanation,
                "ACTIVE",
                null
        );

        return questionDAO.create(question);
    }

    public Question getQuestionById(int questionId) throws SQLException {
        if (questionId <= 0) {
            throw new IllegalArgumentException("ID câu hỏi không hợp lệ.");
        }

        return questionDAO.findById(questionId);
    }

    public List<Question> getQuestionsByTeacher(int teacherId)
            throws SQLException {
        if (teacherId <= 0) {
            throw new IllegalArgumentException("ID giáo viên không hợp lệ.");
        }

        return questionDAO.findByCreator(teacherId);
    }

    public List<Question> getActiveQuestions() throws SQLException {
        return questionDAO.findAllActive();
    }

    public boolean updateQuestion(int questionId, String questionText,
                                  String questionType, BigDecimal points,
                                  String explanation, String status)
            throws SQLException {
        if (questionId <= 0) {
            throw new IllegalArgumentException("ID câu hỏi không hợp lệ.");
        }

        validateQuestion(1, questionText, questionType, points);

        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        }

        Question question = questionDAO.findById(questionId);
        if (question == null) {
            return false;
        }

        question.setQuestionText(questionText.trim());
        question.setQuestionType(questionType);
        question.setDefaultPoints(points);
        question.setExplanation(explanation);
        question.setStatus(status);

        return questionDAO.update(question);
    }

    public boolean deactivateQuestion(int questionId) throws SQLException {
        if (questionId <= 0) {
            throw new IllegalArgumentException("ID câu hỏi không hợp lệ.");
        }

        return questionDAO.deactivate(questionId);
    }

    private void validateQuestion(int teacherId, String questionText,
                                  String questionType, BigDecimal points) {
        if (teacherId <= 0) {
            throw new IllegalArgumentException("ID giáo viên không hợp lệ.");
        }
        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống.");
        }
        if (questionType == null || !VALID_TYPES.contains(questionType)) {
            throw new IllegalArgumentException("Loại câu hỏi không hợp lệ.");
        }
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Điểm câu hỏi phải lớn hơn 0.");
        }
    }
}
