package com.exam.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableAsync
public class OnlineExamPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineExamPlatformApplication.class, args);
    }
}

// ==========================================
// DTO & DATA MODELS
// ==========================================

class Option {
    private String optionId;
    private String text;

    public Option() {}
    public Option(String optionId, String text) {
        this.optionId = optionId;
        this.text = text;
    }

    public String getOptionId() { return optionId; }
    public void setOptionId(String optionId) { this.optionId = optionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}

class Question {
    private String questionId;
    private String questionText;
    private List<Option> options;
    private int marks;

    public Question() {}
    public Question(String questionId, String questionText, List<Option> options, int marks) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.options = options;
        this.marks = marks;
    }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }
    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }
}

class ExamDetails {
    private String examId;
    private String title;
    private int durationMinutes;
    private int totalQuestions;
    private int totalMarks;

    public ExamDetails() {}
    public ExamDetails(String examId, String title, int durationMinutes, int totalQuestions, int totalMarks) {
        this.examId = examId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.totalQuestions = totalQuestions;
        this.totalMarks = totalMarks;
    }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }
}

class AnswerSubmitRequest {
    private String candidateId;
    private String questionId;
    private String selectedOptionId;
    private String clientTimestamp;

    public String getCandidateId() { return candidateId; }
    public void setCandidateId(String candidateId) { this.candidateId = candidateId; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(String selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    public String getClientTimestamp() { return clientTimestamp; }
    public void setClientTimestamp(String clientTimestamp) { this.clientTimestamp = clientTimestamp; }
}

class AnswerSubmitResponse {
    private String submissionId;
    private String questionId;
    private String savedOptionId;
    private String serverTimestamp;
    private boolean isAutoSaved;

    public AnswerSubmitResponse(String submissionId, String questionId, String savedOptionId, String serverTimestamp, boolean isAutoSaved) {
        this.submissionId = submissionId;
        this.questionId = questionId;
        this.savedOptionId = savedOptionId;
        this.serverTimestamp = serverTimestamp;
        this.isAutoSaved = isAutoSaved;
    }

    public String getSubmissionId() { return submissionId; }
    public String getQuestionId() { return questionId; }
    public String getSavedOptionId() { return savedOptionId; }
    public String getServerTimestamp() { return serverTimestamp; }
    public boolean isAutoSaved() { return isAutoSaved; }
}

class ExamSubmitResponse {
    private String examId;
    private String candidateId;
    private String status;
    private String submittedAt;
    private String message;

    public ExamSubmitResponse(String examId, String candidateId, String status, String submittedAt, String message) {
        this.examId = examId;
        this.candidateId = candidateId;
        this.status = status;
        this.submittedAt = submittedAt;
        this.message = message;
    }

    public String getExamId() { return examId; }
    public String getCandidateId() { return candidateId; }
    public String getStatus() { return status; }
    public String getSubmittedAt() { return submittedAt; }
    public String getMessage() { return message; }
}

class ExamResultResponse {
    private String examId;
    private String candidateId;
    private double totalScore;
    private double maxScore;
    private double percentage;
    private String status;
    private Map<String, String> detailedResults;

    public ExamResultResponse(String examId, String candidateId, double totalScore, double maxScore, double percentage, String status, Map<String, String> detailedResults) {
        this.examId = examId;
        this.candidateId = candidateId;
        this.totalScore = totalScore;
        this.maxScore = maxScore;
        this.percentage = percentage;
        this.status = status;
        this.detailedResults = detailedResults;
    }

    public String getExamId() { return examId; }
    public String getCandidateId() { return candidateId; }
    public double getTotalScore() { return totalScore; }
    public double getMaxScore() { return maxScore; }
    public double getPercentage() { return percentage; }
    public String getStatus() { return status; }
    public Map<String, String> getDetailedResults() { return detailedResults; }
}

// ==========================================
// ASYNCHRONOUS RESULT EVALUATION SERVICE
// ==========================================

@Service
class ResultEvaluationService {

    @Async
    public void evaluateResultsAsync(String examId, String candidateId,
                                    Map<String, Map<String, String>> candidateSubmissions,
                                    Map<String, List<Question>> questionsDb,
                                    Map<String, Map<String, String>> answerKeys,
                                    Map<String, Map<String, ExamResultResponse>> resultsDb) {
        try {
            // Simulate processing delay (asynchronous worker)
            Thread.sleep(1000);

            Map<String, String> candidateAnswers = candidateSubmissions
                    .getOrDefault(examId, Collections.emptyMap())
                    .getOrDefault(candidateId, Collections.emptyMap());

            Map<String, String> answerKey = answerKeys.getOrDefault(examId, Collections.emptyMap());
            List<Question> questions = questionsDb.getOrDefault(examId, Collections.emptyList());

            double score = 0.0;
            double totalMarks = questions.stream().mapToDouble(Question::getMarks).sum();
            Map<String, String> details = new HashMap<>();

            for (Question q : questions) {
                String qId = q.getQuestionId();
                String selected = candidateAnswers.get(qId);
                String correct = answerKey.get(qId);

                if (selected != null && selected.equals(correct)) {
                    score += q.getMarks();
                    details.put(qId, "CORRECT");
                } else if (selected == null) {
                    details.put(qId, "UNANSWERED");
                } else {
                    details.put(qId, "INCORRECT");
                }
            }

            double percentage = totalMarks > 0 ? (score / totalMarks) * 100 : 0.0;
            String passStatus = percentage >= 40.0 ? "PASSED" : "FAILED";

            ExamResultResponse result = new ExamResultResponse(
                    examId, candidateId, score, totalMarks,
                    Math.round(percentage * 100.0) / 100.0, passStatus, details
            );

            resultsDb.computeIfAbsent(examId, k -> new ConcurrentHashMap<>()).put(candidateId, result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// ==========================================
// REST CONTROLLER
// ==========================================

@RestController
@RequestMapping("/api/v1/exams")
class ExamController {

    private final ResultEvaluationService evaluationService;

    // In-memory data structures (Thread-safe)
    private final Map<String, ExamDetails> examsDb = new ConcurrentHashMap<>();
    private final Map<String, List<Question>> questionsDb = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> answerKeys = new ConcurrentHashMap<>();

    // State Tracking
    private final Map<String, Map<String, Map<String, String>>> candidateSubmissions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Boolean>> examStatus = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ExamResultResponse>> resultsDb = new ConcurrentHashMap<>();

    public ExamController(ResultEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
        initMockData();
    }

    private void initMockData() {
        examsDb.put("EXAM-101", new ExamDetails("EXAM-101", "Java Fundamentals Exam", 60, 2, 20));

        List<Question> qList = Arrays.asList(
            new Question("Q-1", "Which keyword is used to define a constant in Java?",
                Arrays.asList(
                    new Option("OPT-A", "static"),
                    new Option("OPT-B", "final"),
                    new Option("OPT-C", "const"),
                    new Option("OPT-D", "abstract")
                ), 10),
            new Question("Q-2", "What is the default value of a boolean variable in Java?",
                Arrays.asList(
                    new Option("OPT-A", "true"),
                    new Option("OPT-B", "false"),
                    new Option("OPT-C", "null"),
                    new Option("OPT-D", "0")
                ), 10)
        );
        questionsDb.put("EXAM-101", qList);

        Map<String, String> key = new HashMap<>();
        key.put("Q-1", "OPT-B");
        key.put("Q-2", "OPT-B");
        answerKeys.put("EXAM-101", key);
    }

    // 1. Get Exam Details
    @GetMapping("/{examId}")
    public ResponseEntity<?> getExamDetails(@PathVariable String examId) {
        ExamDetails exam = examsDb.get(examId);
        if (exam == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Exam with ID '" + examId + "' not found."));
        }
        return ResponseEntity.ok(exam);
    }

    // 2. Get Questions
    @GetMapping("/{examId}/questions")
    public ResponseEntity<?> getQuestions(@PathVariable String examId) {
        if (!examsDb.containsKey(examId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Exam with ID '" + examId + "' not found."));
        }
        return ResponseEntity.ok(questionsDb.getOrDefault(examId, Collections.emptyList()));
    }

    // 3. Submit Answer API
    @PostMapping("/{examId}/answers")
    public ResponseEntity<?> submitAnswer(@PathVariable String examId, @RequestBody AnswerSubmitRequest payload) {
        if (!examsDb.containsKey(examId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Exam '" + examId + "' not found."));
        }

        Boolean isSubmitted = examStatus.getOrDefault(examId, Collections.emptyMap()).get(payload.getCandidateId());
        if (Boolean.TRUE.equals(isSubmitted)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", "Exam has already been submitted. Cannot modify answers."));
        }

        candidateSubmissions
                .computeIfAbsent(examId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(payload.getCandidateId(), k -> new ConcurrentHashMap<>())
                .put(payload.getQuestionId(), payload.getSelectedOptionId());

        AnswerSubmitResponse response = new AnswerSubmitResponse(
                "SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                payload.getQuestionId(),
                payload.getSelectedOptionId(),
                Instant.now().toString(),
                true
        );

        return ResponseEntity.ok(response);
    }

    // 4. Submit Exam
    @PostMapping("/{examId}/submit")
    public ResponseEntity<?> submitExam(@PathVariable String examId, @RequestParam String candidateId) {
        if (!examsDb.containsKey(examId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Exam '" + examId + "' not found."));
        }

        Boolean isSubmitted = examStatus.getOrDefault(examId, Collections.emptyMap()).get(candidateId);
        if (Boolean.TRUE.equals(isSubmitted)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", "Exam has already been submitted."));
        }

        examStatus.computeIfAbsent(examId, k -> new ConcurrentHashMap<>()).put(candidateId, true);

        // Trigger Asynchronous Evaluation Task
        evaluationService.evaluateResultsAsync(examId, candidateId, candidateSubmissions, questionsDb, answerKeys, resultsDb);

        ExamSubmitResponse response = new ExamSubmitResponse(
                examId,
                candidateId,
                "SUBMITTED",
                Instant.now().toString(),
                "Exam submitted successfully. Score calculation in progress."
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // 6. Get Exam Result API
    @GetMapping("/{examId}/results")
    public ResponseEntity<?> getExamResult(@PathVariable String examId, @RequestParam String candidateId) {
        if (!examsDb.containsKey(examId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Exam '" + examId + "' not found."));
        }

        ExamResultResponse result = resultsDb.getOrDefault(examId, Collections.emptyMap()).get(candidateId);
        if (result == null) {
            Boolean isSubmitted = examStatus.getOrDefault(examId, Collections.emptyMap()).get(candidateId);
            if (!Boolean.TRUE.equals(isSubmitted)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "Exam has not been submitted yet."));
            }
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Collections.singletonMap("message", "Result evaluation in progress. Please try again in a few seconds."));
        }

        return ResponseEntity.ok(result);
    }
}
