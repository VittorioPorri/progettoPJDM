package it.uniroma2.pjdm.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import it.uniroma2.pjdm.DAO.UserDAOJDBCImpl;
import it.uniroma2.pjdm.entity.User;

public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAOJDBCImpl userDAO;
    
    public LoginServlet() {
        super();        
    }
    
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        String ip = ctx.getInitParameter("db.ip");
        String port = ctx.getInitParameter("db.port");
        String dbName = ctx.getInitParameter("db.name");
        String user = ctx.getInitParameter("db.user");
        String password = ctx.getInitParameter("db.password");

        System.out.print("LoginServlet. Opening DB connection...");

        try {
            userDAO = new UserDAOJDBCImpl(ip, port, dbName, user, password);
            System.out.println("DONE.");
        } catch (Exception e) {
            System.out.println("FAIL");
            userDAO = null; 
        }
    }

    @Override
    public void destroy() {
        System.out.print("LoginServlet. Closing DB connection...");
        userDAO.closeConnection();
        System.out.println("DONE.");
    }

    /* Metodi Http
     * 
     * POST /login	- Login dell'utente
     *
     */
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	PrintWriter out = response.getWriter();
    	
    	if (userDAO == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Connessione al database non disponibile\"}");
            return;
        }
    	
        String token = request.getHeader("Authorization");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        if (token != null && !token.trim().isEmpty()) {
            handleToken(token, out, request, response);
        } else {
            handleLogin(out, request, response);
        }
    }
    
    private void handleToken(String token, PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws IOException {
      
        try {
            User user = userDAO.loginToken(token); 
            if (user != null) {
            	
                out.println(user.toJSONString()); 
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"error\": \"Token non valido\"}"); 
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Errore del server durante la validazione del token\"}"); 
        }
    }
    
    private void handleLogin(PrintWriter out, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Email e/o password sono necessari\"}"); 
            return;
        }
        
        try {
            User user = userDAO.loginUser(email, password);
            if (user != null) {
            	String token = userDAO.getToken(email);
            	response.setHeader("Authorization", token);
                out.println(user.toJSONString()); 
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"error\": \"Email e/o password non validi\"}"); 
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Errore del server durante il login\"}"); 
        }
    }
        
}