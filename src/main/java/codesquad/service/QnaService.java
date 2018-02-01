package codesquad.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import codesquad.UnAuthorizedException;
import codesquad.domain.*;
import codesquad.dto.QuestionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.CannotDeleteException;

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

    public Question update(User loginUser, long id, QuestionDto updatedQuestion) {
        Question question = questionRepository.findOne(id);
        if (!question.isOwner(loginUser)) {
            throw new UnAuthorizedException("다른 사람이 쓴 글을 수정할 수 없다.");
        }
        return question.update(updatedQuestion);
    }

    @Transactional
    public void deleteQuestion(User loginUser, long questionId) throws CannotDeleteException {
        Question question = questionRepository.getOne(questionId);
        if (!question.isOwner(loginUser)) {
            throw new CannotDeleteException("다른 사람의 글은 삭제할 수 없다.");
        }

        List<Answer> answers = question.getAnswers();
        for (Answer answer : answers) {
            if (!answer.isOwner(loginUser)) {
                throw new CannotDeleteException("다른 사용자가 작성한 답변을 삭제할 수 없습니다.");
            }
        }

        question.delete();

        List<DeleteHistory> histories = new ArrayList<>();
        histories.add(new DeleteHistory(ContentType.QUESTION, question.getId(), loginUser, LocalDateTime.now()));
        for (Answer answer : answers) {
            answer.delete();
            histories.add(new DeleteHistory(ContentType.ANSWER, answer.getId(), loginUser, LocalDateTime.now()));
        }
        deleteHistoryService.saveAll(histories);
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
