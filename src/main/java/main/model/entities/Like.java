package main.model.entities;

import lombok.Data;
import main.model.enums.LikeTypes;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime time;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Any(metaDef = "likesMetaDef",metaColumn = @Column(name = "type"), fetch = FetchType.EAGER)
    @AnyMetaDef(name = "likesMetaDef", idType = "long", metaType = "string", metaValues = {
            @MetaValue(targetEntity = Post.class, value = "POST"),
            @MetaValue(targetEntity = Comment.class, value = "COMMENT")
    })
    @JoinColumn(name = "entity_id")
    private Liked entity;

    @Column(nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private LikeTypes type;

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", personId=" + person.getId() +
                ", entity=" + entity.getId() +
                ", type=" + type +
                '}';
    }
}
