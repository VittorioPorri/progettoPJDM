package it.uniroma2.pjdm.servlet;


import java.io.IOException;
import java.io.PrintWriter;

import it.uniroma2.pjdm.DAO.UserDAOJDBCImpl;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAOJDBCImpl userDAO; 
    
    public RegisterServlet() {
        super();
    }
    
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        String ip = ctx.getInitParameter("db.ip");
        String port = ctx.getInitParameter("db.port");
        String dbName = ctx.getInitParameter("db.name");
        String user = ctx.getInitParameter("db.user");
        String password = ctx.getInitParameter("db.password");

        System.out.print("RegisterServlet. Opening DB connection...");

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
        System.out.print("RegisterServlet. Closing DB connection...");
        userDAO.closeConnection();
        System.out.println("DONE.");
    }

    /* Metodi Http
     * 
     * POST /register	- Registrazione dell'utente
     *
     */
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        if (userDAO == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Connessione al database non disponibile\"}");
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String name = request.getParameter("name");

       
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            name == null || name.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Email, password e nome sono obbligatori\"}");
            out.close();
            return;
        }
        
        if (!isValidEmail(email)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Formato email non valido\"}");
            out.close();
            return;
        }
        

        try {
        	String token = java.util.UUID.randomUUID().toString();
            boolean registered = userDAO.registerUser(email, password, name, token );

            if (registered) {
            	response.setHeader("Authorization", token);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println("{\"success\": \"Registrazione effettuata con successo\"}");
                    
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.println("{\"error\": \"Utente già registrato o errore nel salvataggio\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Errore del server durante la registrazione\"}");
        
        }
    }
    
    private boolean isValidEmail(String email) {
    	return email.contains("@") && email.length() > 4;
    }
}
    
