package com.udb.hotel.config;

import com.udb.hotel.model.Rol;
import com.udb.hotel.model.Usuario;
import com.udb.hotel.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInicializador implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (repo.findByUsername("admin").isEmpty()) {
            repo.save(Usuario.builder()
                    .username("admin")
                    .password(encoder.encode("Admin123*"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build());
        }
    }
}
