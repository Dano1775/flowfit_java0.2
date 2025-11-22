package com.example.flowfit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.flowfit.model.Usuario;

@Repository
public interface UsuarioGroupRepository extends JpaRepository<Usuario, Integer> {
}
