package codesquad.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.Size;

import codesquad.CannotDeleteException;
import codesquad.UnAuthorizedException;
import org.hibernate.annotations.Where;

import codesquad.dto.QuestionDto;
import support.domain.AbstractEntity;
import support.domain.UrlGeneratable;

@Entity
public class Question extends AbstractEntity implements UrlGeneratable {
    @Size(min = 3, max = 100)
    @Column(length = 100, nullable = false)
    private String title;

    @Size(min = 3)
    @Lob
    private String contents;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_question_writer"))
    private User writer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Where(clause = "deleted = false")
    @OrderBy("id ASC")
    private List<Answer> answers = new ArrayList<>();

    private boolean deleted = false;

    public Question() {
    }

    public Question(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }

    public User getWriter() {
        return writer;
    }

    public void writeBy(User loginUser) {
        this.writer = loginUser;
    }

    public void addAnswer(Answer answer) {
        answer.toQuestion(this);
        answers.add(answer);
    }

    private boolean isOwner(User loginUser) {
        return writer.equals(loginUser);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete(User loginUser) {
        if (!isOwner(loginUser) || isDeleted()) {
            return;
        }
        this.deleted = true;
    }

    public int delete(DeleteBehavior behavior){
        if(!isDeleted() && isOwner(behavior.getLoginUser())){
            this.delete(behavior.getLoginUser());
            return behavior.deleteQuestionAndAnswer(this, this.answers);
        }
        return 0;
    }

    public void update(User loginUser,QuestionDto questionDto){
        if (!isOwner( loginUser)) {
            throw new UnAuthorizedException();
        }

        this.title = questionDto.getTitle();
        this.contents = questionDto.getContents();
    }

    @Override
    public String generateUrl() {
        return String.format("/questions/%d", getId());
    }

    public QuestionDto toQuestionDto() {
        return new QuestionDto(getId(), this.title, this.contents);
    }

    @Override
    public String toString() {
        return "Question [id=" + getId() + ", title=" + title + ", contents=" + contents + ", writer=" + writer + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        if (!super.equals(o)) return false;
        Question question = (Question) o;
        return deleted == question.deleted &&
                Objects.equals(title, question.title) &&
                Objects.equals(contents, question.contents) &&
                Objects.equals(writer, question.writer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, contents, writer, deleted);
    }
}
