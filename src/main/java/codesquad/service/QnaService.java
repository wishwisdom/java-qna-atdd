package codesquad.service;

import codesquad.CannotDeleteException;
import codesquad.domain.*;
import codesquad.dto.AnswerDto;
import codesquad.dto.QuestionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public Question update(User loginUser, long id, QuestionDto updatedQuestion) {
        Question question = questionRepository.findOne(id);
        question.update(loginUser, updatedQuestion);
        return question;
    }

    @Transactional
    public int deleteQuestion(User loginUser, long questionId) throws CannotDeleteException {
        // TODO 삭제 기능 구현
        Question question = findById(questionId);

        return question.delete(new DeleteBehavior() {
            @Override
            public User getLoginUser() {
                return loginUser;
            }

            @Override
            public int deleteQuestionAndAnswer(Question deletedQuestion, List<Answer> answers) {
                List<DeleteHistory> deleteHistories = answers.stream()
                        .filter( a -> a.isOwner(loginUser) && !a.isDeleted())
                        .map( a -> {
                            a.delete(loginUser);
                            return  new DeleteHistory(ContentType.ANSWER, a.getId(), loginUser, LocalDateTime.now());
                        }).collect(Collectors.toList());

                deleteHistories.add(new DeleteHistory(ContentType.QUESTION, deletedQuestion.getId(), loginUser, LocalDateTime.now()));
                deleteHistoryService.saveAll(deleteHistories);

                return deleteHistories.size();
            }
        });
    }

    public Iterable<Question> findAll() {
        return questionRepository.findByDeleted(false);
    }

    public List<Question> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable).getContent();
    }

    @Transactional
    public Answer addAnswer(User loginUser, long questionId, String contents) {
        Question question = findById(questionId);
        Answer answer = new Answer(loginUser, question,contents);
        answerRepository.save(answer);

        return answer;
    }

    @Transactional
    public Answer updateAnswer(User loginUser, long id,  AnswerDto answerDto){
        Answer answer = answerRepository.findOne(id);
        answer.update(loginUser, answerDto);

        return answer;
    }

    public Answer findAnserById(long id){
        return answerRepository.findOne(id);
    }

    @Transactional
    public Answer deleteAnswer(User loginUser, long id) {
        // TODO 답변 삭제 기능 구현
        Answer answer = answerRepository.findOne(id);
        answer.delete(loginUser);
        return answer;
    }
}
