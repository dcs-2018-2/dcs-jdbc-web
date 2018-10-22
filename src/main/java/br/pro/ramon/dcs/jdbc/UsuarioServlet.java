package br.pro.ramon.dcs.jdbc;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;

@WebServlet("/usuarios")
@MultipartConfig
public class UsuarioServlet extends HttpServlet {

    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL = "jdbc:sqlserver://facenac.database.windows.net;database=facenac";
    private static final String USER = "";
    private static final String PASS = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int statusCode;
        String mensagem;

        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");
        Part foto = request.getPart("foto");

        try {
            Class.forName(DRIVER);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                    PreparedStatement stmt = conn.prepareStatement("insert into usuario (nome, email, senha, foto) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nome);
                stmt.setString(2, email);
                stmt.setString(3, senha);
                stmt.setBytes(4, IOUtils.toByteArray(foto.getInputStream()));

                int n = stmt.executeUpdate();
                if (n == 0) {
                    statusCode = SC_INTERNAL_SERVER_ERROR;
                    mensagem = "Usuário não foi cadastrado!";
                } else {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            Long id = rs.getLong(1);
                            statusCode = SC_CREATED;
                            mensagem = "usuarios/" + id;
                        } else {
                            statusCode = SC_INTERNAL_SERVER_ERROR;
                            mensagem = "Id do usuário não foi criado!";
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            statusCode = SC_INTERNAL_SERVER_ERROR;
            mensagem = ex.getMessage();
        }

        response.setStatus(statusCode);
        if (statusCode == SC_CREATED) {
            String location = request.getScheme() + "://"
                    + request.getServerName() + ":" + request.getServerPort()
                    + request.getServletContext().getContextPath()
                    + "/webresources/" + mensagem;
            response.addHeader("Location", location);
        }
        PrintWriter out = response.getWriter();
        out.print(mensagem);
    }

}
