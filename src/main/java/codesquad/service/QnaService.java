package codesquad.service;

import codesquad.CannotDeleteException;
import codesquad.domain.*;
import codesquad.dto.QuestionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service("qnaService")
public class QnaService {
    private static final Logger log = LoggerFactory.getLogger(QnaService.class);

    @Resource(name = "questionRepository")
    private QuestionRepository questionRepository;

    @Resource(name = "answerRepository")
    private AnswerRepository answerRepository;

    @Resource(name = "deleteHistoryService")
    private DeleteHistoryService deleteHistoryService;

    public Question create(User loginUser, Question question) {
        question.writeBy(loginUser);
        log.debug("question : {}", question);
        return questionRepository.save(question);
    }

    public Question findById(long id) {
        return questionRepository.findOne(id);
    }

    @Transactional
    public Question update(User loginUser, long id, Question updatedQuestion) {
        // TODO 수정 기능 구현
        Question question = questionRepository.findOne(id);
        question.update(loginUser, updatedQuestion.getTitle(), updatedQuestion.getContents());
        return question;
    }

    @Transactional
    public Question update(User loginUser, long id, QuestionDto updatedQuestion) {
        Question question = questionRepository.findOne(id);
        question.update(loginUser, updatedQuestion.getTitle(), updatedQuestion.getContents());
        return question;
    }

    @Transactional
    public void deleteQuestion(User loginUser, long questionId) throws CannotDeleteException {
        // TODO 삭제 기능 구현
        Question question = findById(questionId);

        if (!question.isOwner(loginUser) || question.isDeleted()) {
            throw new CannotDeleteException("권한이 없거나, 이미 삭제된 질문입니다.");
        }

        DeleteHistory deleteHistory = new DeleteHistory(ContentType.QUESTION, questionId, loginUser, LocalDateTime.now());
        deleteHistoryService.saveAll(Arrays.asList(deleteHistory));
        question.delete();
    }

    public Iterable<Question> findAll() {
        return questionRepository.findByDeleted(false);
    }

    public List<Question> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable).getContent();
    }

    public Answer addAnswer(User loginUser, long questionId, String contents) {
        return null;
    }

    public Answer deleteAnswer(User loginUser, long id) {
        // TODO 답변 삭제 기능 구현 
        return null;
    }
}
