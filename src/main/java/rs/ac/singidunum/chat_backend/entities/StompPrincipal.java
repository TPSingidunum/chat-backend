package rs.ac.singidunum.chat_backend.entities;

import lombok.RequiredArgsConstructor;

import javax.security.auth.Subject;
import java.security.Principal;

public class StompPrincipal implements Principal {
    private String username;

    public StompPrincipal(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object another) {
        return false;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}
