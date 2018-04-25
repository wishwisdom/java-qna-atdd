package codesquad.web;

import codesquad.CannotDeleteException;
import codesquad.domain.QuestionRepository;
import codesquad.domain.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import support.helper.HtmlFormDataBuilder;
import support.test.AcceptanceTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class QuestionAcceptanceTest extends AcceptanceTest {
    private static final Logger log = LoggerFactory.getLogger(QuestionAcceptanceTest.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    public void createForm() throws Exception {
        ResponseEntity<String> response = template().getForEntity("/question/form", String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        log.debug("body : {}", response.getBody());
    }

    @Test
    public void create() throws Exception {
        HtmlFormDataBuilder builder = HtmlFormDataBuilder.urlEncodedForm();

        HttpEntity<MultiValueMap<String, Object>> request = builder
                    .addParameter("title","title")
                    .addParameter("contents","contents")
                .build();

        User loginUser = defaultUser();
        ResponseEntity<String> response = basicAuthTemplate(loginUser)
                .postForEntity("/question", request, String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        assertThat(response.getHeaders().getLocation().getPath(), is("/question"));
    }

    @Test
    public void list() throws Exception {
        // ResponseEntity<String> response =template().getForEntity("/qna", String.class);
        User loginUser = defaultUser();
        ResponseEntity<String> response = basicAuthTemplate(loginUser)
                .getForEntity("/question", String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void updateForm_login() throws Exception {
        ResponseEntity<String> response = template().getForEntity(String.format("/question/%d/form", 1),
                String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void update_no_login() throws Exception {
        ResponseEntity<String> response = update(template());
        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }

    @Test
    public void update() throws Exception {
        ResponseEntity<String> response = update(basicAuthTemplate());
        assertThat(response.getStatusCode(), is(HttpStatus.FOUND));
        assertTrue(response.getHeaders().getLocation().getPath().startsWith("/question"));
    }

    private ResponseEntity<String> update(TestRestTemplate template) throws Exception {
        HtmlFormDataBuilder builder = HtmlFormDataBuilder.urlEncodedForm();

        HttpEntity<MultiValueMap<String, Object>> request = builder
                //.addParameter("id","1")
                .addParameter("title","title2")
                .addParameter("contents","contents")
                .build();

        return template.postForEntity("/question/1", request, String.class);
    }

    @Test
    public void can_not_delete() throws CannotDeleteException{
        ResponseEntity<String> response = basicAuthTemplate().getForEntity("/question/3/delete", String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR)); // CannotDeleteException을 잡아내지 못함.
    }

    @Test
    public void delete() {
        ResponseEntity<String> response = basicAuthTemplate().getForEntity("/question/1/delete", String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }
}
