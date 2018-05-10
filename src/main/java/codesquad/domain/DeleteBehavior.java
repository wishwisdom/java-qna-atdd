package codesquad.domain;

import java.util.List;

public interface DeleteBehavior {
    User getLoginUser();
    int deleteQuestionAndAnswer(Question deletedQuestion, List<Answer> answers);
}
