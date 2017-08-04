package ro.cs.tao.persistence.data;

import java.util.Collection;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Group persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.\"group\"")
public class Group {

    /**
     * Group name column maximum length
     */
    private static final int GROUP_NAME_COLUMN_MAX_LENGTH = 50;

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "group_identifier", sequenceName = "tao.group_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "group_identifier")
    @Column(name = "id")
    @NotNull
    private Integer id;

    /**
     * Data product name
     */
    @Column(name = "name")
    @NotNull
    @Size(min = 1, max = GROUP_NAME_COLUMN_MAX_LENGTH)
    private String name;

    /**
     * The users within a group
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "group")
    @Fetch(value = FetchMode.SUBSELECT)
    private Collection<User> users;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }
}
