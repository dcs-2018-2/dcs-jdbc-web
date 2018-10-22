package br.pro.ramon.dcs.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/usuarios")
public class UsuarioResource {

    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL = "jdbc:sqlserver://facenac.database.windows.net;database=facenac";
    private static final String USER = "";
    private static final String PASS = "";

    @GET
    @Produces("applications/json")
    public Response getUsuarios() {
        Response response;

        try {
            Class.forName(DRIVER);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                    PreparedStatement stmt = conn.prepareStatement("select * from usuario order by nome");
                    ResultSet rs = stmt.executeQuery()) {
                List<Usuario> usuarios = new ArrayList<>();
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String nome = rs.getString("nome");
                    String email = rs.getString("email");
                    String senha = rs.getString("senha");
                    byte[] foto = rs.getBytes("foto");

                    Usuario usuario = new Usuario(id, nome, email, senha, foto);
                    usuarios.add(usuario);
                }
                response = Response.ok(usuarios).build();
            }
        } catch (ClassNotFoundException | SQLException ex) {
            response = Response.serverError().entity(ex.getMessage()).build();
        }

        return response;
    }

    @POST
    @Consumes("multipart/form-data")
    public Response postUsuario(
            @FormDataParam("nome") String nome,
            @FormDataParam("email") String email,
            @FormDataParam("senha") String senha,
            @FormDataParam("foto") InputStream foto
    ) {
        Response response;

        try {
            Class.forName(DRIVER);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                    PreparedStatement stmt = conn.prepareStatement("insert into usuario (nome, email, senha, foto) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nome);
                stmt.setString(2, email);
                stmt.setString(3, senha);
                stmt.setBytes(4, IOUtils.toByteArray(foto));

                int n = stmt.executeUpdate();
                if (n == 0) {
                    response = Response.serverError().entity("Usuário não foi cadastrado!").build();
                } else {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            Long id = rs.getLong(1);

                            URI location = URI.create("usuarios/" + id);
                            response = Response.created(location).entity(location.toString()).build();
                        } else {
                            response = Response.serverError().entity("Id do usuário não foi criado!").build();
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            response = Response.serverError().entity(ex.getMessage()).build();
        }

        return response;
    }

    @GET
    @Path("/{id}")
    @Produces("applications/json")
    public Response getUsuario(@PathParam("id") Long id) {
        Response response;

        try {
            Class.forName(DRIVER);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                    PreparedStatement stmt = conn.prepareStatement("select * from usuario where id = ?")) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String nome = rs.getString("nome");
                        String email = rs.getString("email");
                        String senha = rs.getString("senha");
                        byte[] foto = rs.getBytes("foto");

                        Usuario usuario = new Usuario(id, nome, email, senha, foto);
                        response = Response.ok(usuario).build();
                    } else {
                        response = Response.status(NOT_FOUND).build();
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            response = Response.serverError().entity(ex.getMessage()).build();
        }

        return response;
    }

    @GET
    @Path("/{id}/foto")
    @Produces("image/png")
    public Response getFoto(@PathParam("id") Long id) {
        Response response;

        try {
            Class.forName(DRIVER);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                    PreparedStatement stmt = conn.prepareStatement("select * from usuario where id = ?")) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    byte[] foto = null;

                    if (rs.next()) {
                        foto = rs.getBytes("foto");
                    }

                    if (foto == null || foto.length == 0) {
                        response = Response.status(NOT_FOUND).build();
                    } else {
                        response = Response.ok(foto).build();
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            response = Response.serverError().entity(ex.getMessage()).build();
        }

        return response;
    }

}
