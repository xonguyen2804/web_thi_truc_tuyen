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