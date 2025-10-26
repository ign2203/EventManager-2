package org.example.eventmanagermodule.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true)
    @NotBlank(message = "login must not be blank")
    @Size(min = 4)
    private String login;
    @Column(name = "password")

    @NotBlank(message = "password must not be blank")
    private String password;

    @Column(name = "age")
    @Min(18)
    @Max(116)
    private int age;

    @NotNull(message = "Role must not be blank")
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role;


    public UserEntity() {
    }

    public UserEntity(Long id, String login, String password, int age, UserRole role) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.age = age;
        this.role = role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}