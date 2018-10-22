package br.pro.ramon.dcs.jdbc;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Usuario implements Serializable {

    private Long id;
    private String nome;
    private String email;
    private String senha;
    private byte[] foto;

    protected Usuario() {
    }

    public Usuario(Long id, String nome, String email, String senha, byte[] foto) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.foto = foto;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public byte[] getFoto() {
        return foto;
    }

}
