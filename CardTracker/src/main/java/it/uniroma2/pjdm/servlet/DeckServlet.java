package it.uniroma2.pjdm.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import it.uniroma2.pjdm.DAO.UserDAOJDBCImpl;
import it.uniroma2.pjdm.entity.Deck;
import it.uniroma2.pjdm.entity.ListDecks;
import it.uniroma2.pjdm.entity.User;


public class DeckServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private UserDAOJDBCImpl userDAO;
    
    public DeckServlet() {
        super();
    }
    
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        String ip = ctx.getInitParameter("db.ip");
        String port = ctx.getInitParameter("db.port");
        String dbName = ctx.getInitParameter("db.name");
        String user = ctx.getInitParameter("db.user");
        String password = ctx.getInitParameter("db.password");

        System.out.print("DeckServlet. Opening DB connection...");

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
        System.out.print("DeckServlet. Closing DB connection...");
        userDAO.closeConnection();
        System.out.println("DONE.");
    }
    
    /* 
     * Metodi HTTP:
     * GET     	/deck?idDeck=value               			- Restituisce il mazzo specificato
     * GET 	   	/deck?formato=name							- Restituisce 20 deck del formato
     * GET     	/deck?name=name&formato=name               	- Restituisce il mazzo con nome simile 
     * GET		/deck?my									- Restituisce i mazzi dell'utente
     * POST    	/deck				                 		- Crea un mazzo 
     * DELETE  	/deck?idDeck=value       					- Elimina un mazzo specifico
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
        String name = request.getParameter("name");
        String formato = request.getParameter("formato");
        String my = request.getParameter("my");

        try {
            if (idDeckStr != null) {
                int idDeck = Integer.parseInt(idDeckStr);
                handleGetDeck(idDeck, response, out);
                return;
            }

            if (name != null && formato != null) {
                handleGetDeckByName(name, formato, response, out);
                return;
            }
            
            if (formato != null) {
                handleGetDecksByFormato(formato, response, out);
                return;
            }

         
            if (my != null) {
            	
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
                
                handleGetMyDecks(user.getEmail(), response, out);
                return;
            }

            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Richiesta non valida: specificare almeno un parametro tra idDeck, name+formato, formato o my\"}");

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"idDeck deve essere numerico\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Errore interno del server\"}");
        }
    }


    private void handleGetDeck(int idDeck, HttpServletResponse response, PrintWriter out) {
        Deck deck = userDAO.getDeck(idDeck);

        if (deck == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"error\": \"Mazzo non trovato\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.println(deck.toJSONString());
    }


    private void handleGetDeckByName(String name, String formato, HttpServletResponse response, PrintWriter out) {
        ListDecks decks = userDAO.getDecksbyName(name, formato);

        if (decks == null || decks.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"error\": \"Nessun mazzo trovato\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.println(decks.toJSONString());
    }


    private void handleGetDecksByFormato(String formato, HttpServletResponse response, PrintWriter out) {
        ListDecks decks = userDAO.getDecks(formato);

        if (decks == null || decks.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"error\": \"Nessun mazzo trovato\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.println(decks.toJSONString());
    }


    private void handleGetMyDecks(String email, HttpServletResponse response, PrintWriter out) {
        ListDecks decks = userDAO.getDecksByUser(email);

        if (decks == null || decks.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"error\": \"Nessun mazzo trovato\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.println(decks.toJSONString());
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

        String formato = request.getParameter("formato");
        String name = request.getParameter("name");

        if (formato == null || formato.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Richiesta non valida: specificare i parametri name e formato\"}");
            return;
        }

        boolean success = userDAO.makeDeck(user.getEmail(), name.trim(), formato.trim());
        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.println("{\"message\": \"Mazzo creato con successo\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Impossibile creare il mazzo\"}");
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

        String idDeckStr = request.getParameter("idDeck");
        if (idDeckStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Richiesta non valida: specificare il parametro idDeck\"}");
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

            if (!deck.getEmail().equals(user.getEmail())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("{\"error\": \"Non puoi eliminare questo mazzo\"}");
                return;
            }

            boolean success = userDAO.deleteDeck(idDeck);
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.println("{\"message\": \"Mazzo eliminato con successo\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"Impossibile eliminare il mazzo\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"idDeck deve essere numerico\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Errore interno del server\"}");
        }
    }

    
}
