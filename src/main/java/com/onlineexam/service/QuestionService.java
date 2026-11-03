// package com.onlineexam.service;

// import com.onlineexam.dao.ExamDAO;
// import com.onlineexam.dao.QuestionDAO;
// import com.onlineexam.model.Exam;
// import com.onlineexam.model.Option;
// import com.onlineexam.model.Question;

// import java.sql.SQLException;
// import java.util.List;
// import java.util.Map;

// /**
//  * Service xử lý nghiệp vụ cho Ngân hàng câu hỏi.
//  */
// public class QuestionService {

//     private final QuestionDAO questionDAO;
//     private final ExamDAO examDAO;

//     public QuestionService() {
//         this.questionDAO = new QuestionDAO();
//         this.examDAO = new ExamDAO();
//     }

//     public QuestionService(QuestionDAO questionDAO, ExamDAO examDAO) {
//         this.questionDAO = questionDAO;
//         this.examDAO = examDAO;
//     }

//     public List<Question> getFilteredQuestions(String keyword, String type, Integer examId) throws SQLException {
//         return questionDAO.findFiltered(keyword, type, examId);
//     }

//     public Question getQuestionById(int questionId) throws SQLException {
//         return questionDAO.findById(questionId);
//     }

//     public int createQuestion(Question question, List<Option> options, String shortAnswer) throws SQLException {
//         return questionDAO.insert(question, options, shortAnswer);
//     }

//     public boolean updateQuestion(Question question, List<Option> options, String shortAnswer) throws SQLException {
//         return questionDAO.update(question, options, shortAnswer);
//     }

//     public boolean deleteQuestion(int questionId) throws SQLException {
//         return questionDAO.delete(questionId);
//     }

//     public Map<String, Integer> getStatistics() throws SQLException {
//         return questionDAO.getStatistics();
//     }

//     public List<Exam> getAllExams() throws SQLException {
//         return examDAO.findAll();
//     }

//     public int createExamFromQuestions(Exam exam, List<Integer> questionIds) throws SQLException {
//         return questionDAO.createExamFromQuestions(exam, questionIds);
//     }

//     public int addQuestionsToExam(int examId, List<Integer> questionIds) throws SQLException {
//         return questionDAO.addQuestionsToExam(examId, questionIds);
//     }
// }


package com.onlineexam.service;

import com.onlineexam.dao.QuestionDAO;
import com.onlineexam.model.Question;
import com.onlineexam.model.Option;
import com.onlineexam.model.Exam;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestionService {

    private static final Set<String> VALID_TYPES = Set.of(
            "SINGLE", "TRUE_FALSE", "SHORT_ANSWER"
    );

    private final QuestionDAO questionDAO;

    public QuestionService() {
        this.questionDAO = new QuestionDAO();
    }

    // ==================== GET QUESTIONS ====================

    public List<Question> getFilteredQuestions(
            int teacherId,
            String keyword,
            String type,
            Integer examId) throws SQLException {

        if (teacherId <= 0) {
            throw new IllegalArgumentException("ID giáo viên không hợp lệ.");
        }

        if (type != null && !type.isBlank()
                && !"ALL".equalsIgnoreCase(type)
                && !VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException("Loại câu hỏi không hợp lệ.");
        }

        if (examId != null && examId <= 0) {
            throw new IllegalArgumentException("ID đề thi không hợp lệ.");
        }

        return questionDAO.findFiltered(
                teacherId, keyword, type, examId
        );
    }

    public List<Question> getQuestionsByTeacher(int teacherId)
            throws SQLException {
        return getFilteredQuestions(teacherId, null, null, null);
    }

    public Question getQuestionById(int questionId)
            throws SQLException {

        if (questionId <= 0) {
            throw new IllegalArgumentException("ID câu hỏi không hợp lệ.");
        }

        return questionDAO.findById(questionId);
    }

    // ==================== STATISTICS / EXAMS ====================

    public Map<String, Integer> getStatistics(int teacherId)
            throws SQLException {
        return questionDAO.getStatistics(teacherId);
    }

    public List<Exam> getExamsByTeacher(int teacherId)
            throws SQLException {
        return questionDAO.findExamsByTeacher(teacherId);
    }

    // ==================== CREATE ====================

    public int createQuestion(
            int teacherId,
            String questionText,
            String questionType,
            BigDecimal points,
            String explanation) throws SQLException {

        validateQuestion(
                teacherId, questionText, questionType, points
        );

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

    // ==================== UPDATE ====================

    public boolean updateQuestion(
            int questionId,
            int teacherId,
            String questionText,
            String questionType,
            BigDecimal points,
            String explanation,
            List<Option> options,
            String shortAnswer) throws SQLException {

        if (questionId <= 0) {
            throw new IllegalArgumentException("ID câu hỏi không hợp lệ.");
        }

        validateQuestion(
                teacherId, questionText, questionType, points
        );

        Question question = questionDAO.findById(questionId);

        if (question == null || question.getCreatedBy() != teacherId) {
            return false;
        }

        if ("SINGLE".equals(questionType)) {
            validateSingle(options);
        } else if ("TRUE_FALSE".equals(questionType)) {
            validateTrueFalse(options);
        } else if ("SHORT_ANSWER".equals(questionType)) {
            validateShortAnswer(shortAnswer);
        }

        question.setQuestionText(questionText.trim());
        question.setQuestionType(questionType);
        question.setDefaultPoints(points);
        question.setExplanation(
                explanation == null ? null : explanation.trim()
        );

        return questionDAO.update(question, options, shortAnswer);
    }

    // ==================== DEACTIVATE ====================

    public boolean deactivateQuestion(int questionId)
            throws SQLException {

        if (questionId <= 0) {
            throw new IllegalArgumentException("ID câu hỏi không hợp lệ.");
        }

        return questionDAO.deactivate(questionId);
    }

    // ==================== VALIDATION ====================

    private void validateQuestion(
            int teacherId,
            String questionText,
            String questionType,
            BigDecimal points) {

        if (teacherId <= 0) {
            throw new IllegalArgumentException("ID giáo viên không hợp lệ.");
        }

        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException(
                    "Nội dung câu hỏi không được để trống."
            );
        }

        if (questionType == null || !VALID_TYPES.contains(questionType)) {
            throw new IllegalArgumentException(
                    "Loại câu hỏi không hợp lệ."
            );
        }

        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Điểm câu hỏi phải lớn hơn 0."
            );
        }
    }

    private void validateSingle(List<Option> options) {
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException(
                    "Câu hỏi SINGLE phải có đúng 4 phương án."
            );
        }

        int correctCount = 0;

        for (Option option : options) {
            if (option.getOptionText() == null
                    || option.getOptionText().isBlank()) {
                throw new IllegalArgumentException(
                        "Không được để trống phương án."
                );
            }

            if (option.isCorrect()) {
                correctCount++;
            }
        }

        if (correctCount != 1) {
            throw new IllegalArgumentException(
                    "Câu hỏi SINGLE phải có đúng 1 đáp án đúng."
            );
        }
    }

    private void validateTrueFalse(List<Option> options) {
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException(
                    "Câu hỏi TRUE_FALSE phải có đúng 4 ý."
            );
        }

        for (Option option : options) {
            if (option.getOptionText() == null
                    || option.getOptionText().isBlank()) {
                throw new IllegalArgumentException(
                        "Không được để trống nội dung ý Đúng/Sai."
                );
            }
        }
    }

    private void validateShortAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException(
                    "Đáp án ngắn không được để trống."
            );
        }

        if (answer.trim().length() > 4) {
            throw new IllegalArgumentException(
                    "Đáp án ngắn chỉ được tối đa 4 ký tự."
            );
        }
    }
}