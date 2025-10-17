package it.uniroma2.pjdm.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import it.uniroma2.pjdm.DAO.UserDAOJDBCImpl;
import it.uniroma2.pjdm.entity.Comment;
import it.uniroma2.pjdm.entity.Deck;
import it.uniroma2.pjdm.entity.ListComments;
import it.uniroma2.pjdm.entity.User;

public class CommentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAOJDBCImpl userDAO;

    public CommentServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        String ip = ctx.getInitParameter("db.ip");
        String port = ctx.getInitParameter("db.port");
        String dbName = ctx.getInitParameter("db.name");
        String user = ctx.getInitParameter("db.user");
        String password = ctx.getInitParameter("db.password");

        System.out.print("CommentServlet. Opening DB connection...");

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
        System.out.print("CommentServlet. Closing DB connection...");
        if (userDAO != null)
            userDAO.closeConnection();
        System.out.println("DONE.");
    }

    /* 
     * Metodi HTTP:
     * GET     /comment?idDeck=value                  		- Restituisce i commenti del mazzo specificato
     * POST    /comment				                 	  	- Aggiunge un commento al mazzo specificato
     * DELETE  /comment?idDeck=value&idComment=value        - Elimina un commento specifico
     */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (userDAO == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Connessione al database non disponibile\"}");
            return;
        }

        String idDeckStr = request.getParameter("idDeck");
        if (idDeckStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Parametro idDeck mancante\"}");
            return;
        }

        try {
            int idDeck = Integer.parseInt(idDeckStr);
            
            Deck deck = userDAO.getDeck(idDeck);
            if (deck == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Mazzo non trovato\"}");
                return;
            }

            ListComments comments = userDAO.getCommentsForMazzo(idDeck);
            if (comments == null || comments.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.println("{\"error\": \"Nessun commento trovato per questo mazzo\"}");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.println(comments.toJSONString());

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"idDeck deve essere un numero\"}");
        }
    }
    
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
        
        String idDeckStr = request.getParameter("idDeck");
        if(idDeckStr==null) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Richiesta non valida: specificare il parametro idDeck\"}");
            return;
        }
        
        String token = request.getHeader("Authorization");
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"error\": \"Token mancante o non valido\"}");
            return;
        }

        User user = userDAO.loginToken(token);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"error\": \"Token non valido\"}");
            return;
        }

        try {
            int idMazzo = Integer.parseInt(idDeckStr);

            Deck deck = userDAO.getDeck(idMazzo);
            if (deck == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Mazzo non trovato\"}");
                return;
            }

            String text = request.getParameter("text");
            if (text == null || text.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Il testo del commento non può essere vuoto\"}");
                return;
            }

            boolean success = userDAO.leaveComment(user.getEmail(), text.trim(), idMazzo);
            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println("{\"message\": \"Commento aggiunto con successo\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"Impossibile aggiungere il commento\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"idMazzo deve essere un numero\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (userDAO == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Connessione al database non disponibile\"}");
            return;
        }

        String idMazzoStr = request.getParameter("idDeck");
        String idCommentStr = request.getParameter("idComment");

        if (idMazzoStr == null || idCommentStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Richiesta non valida: specificare idMazzo e idComment\"}");
            return;
        }

        String token = request.getHeader("Authorization");
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"error\": \"Token mancante o non valido\"}");
            return;
        }

        User user = userDAO.loginToken(token);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"error\": \"Token non valido\"}");
            return;
        }

        try {
            int idMazzo = Integer.parseInt(idMazzoStr);
            int idComment = Integer.parseInt(idCommentStr);

            Comment comment = userDAO.getComment(idComment);
            if (comment == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Commento non trovato\"}");
                return;
            }

            if (!user.getEmail().equals(comment.getEmail())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"error\": \"Non puoi eliminare questo commento\"}");
                return;
            }

            if (!userDAO.checkCommentInDeck(idComment, idMazzo)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Il commento non appartiene al mazzo specificato\"}");
                return;
            }

            boolean deleted = userDAO.deleteComment(idComment);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.println("{\"message\": \"Commento eliminato con successo\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"Errore durante l'eliminazione del commento\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Gli ID devono essere numerici\"}");
        }
    }
}
