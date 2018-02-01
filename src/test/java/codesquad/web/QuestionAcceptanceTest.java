package codesquad.web;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import codesquad.domain.Question;
import codesquad.domain.QuestionRepository;
import codesquad.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import support.test.AcceptanceTest;
import support.test.HtmlFormDataBuilder;

public class QuestionAcceptanceTest extends AcceptanceTest {
    @Autowired
    private QuestionRepository questionRepository;

    public static Question createByLoginUser(User loginUser) {
        Question question = new Question("TDD는 의미있는 활동인가?", "당근 엄청 의미있는 활동이고 말고..");
        question.writeBy(loginUser);
        return question;
    }

    @Test
    public void deleteQuestionTest_with_owner() throws Exception {
        Question savedQuestion = questionRepository.save(createByLoginUser(defaultUser()));

        HttpEntity<MultiValueMap<String, Object>> request = HtmlFormDataBuilder
                .urlEncodedForm()
                .delete().build();
        ResponseEntity<String> response = basicAuthTemplate()
                .postForEntity(savedQuestion.generateUrl(), request, String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        assertThat(response.getHeaders().getLocation().getPath(), is("/"));
    }

    @Test
    public void deleteQuestionTest_with_another_user() throws Exception {
        Question savedQuestion = questionRepository.save(createByLoginUser(defaultUser()));

        HttpEntity<MultiValueMap<String, Object>> request = HtmlFormDataBuilder
                .urlEncodedForm()
                .delete().build();
        ResponseEntity<String> response = basicAuthTemplate(findByUserId(AcceptanceTest.OTHER_USER))
                .postForEntity(savedQuestion.generateUrl(), request, String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }
}
