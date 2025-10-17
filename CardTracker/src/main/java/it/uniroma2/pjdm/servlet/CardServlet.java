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
import it.uniroma2.pjdm.entity.ListCards;
import it.uniroma2.pjdm.entity.User;


public class CardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	UserDAOJDBCImpl userDAO;
	
    public CardServlet() {
        super();
        
    }
    
    public void init() throws ServletException {
        ServletContext ctx = getServletContext();
        String ip = ctx.getInitParameter("db.ip");
        String port = ctx.getInitParameter("db.port");
        String dbName = ctx.getInitParameter("db.name");
        String user = ctx.getInitParameter("db.user");
        String password = ctx.getInitParameter("db.password");

        System.out.print("CardServlet. Opening DB connection...");

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
        System.out.print("CardServlet. Closing DB connection...");
        userDAO.closeConnection();
        System.out.println("DONE.");
    }
    
    /* Metodi Http
     * 
     * GET 		/card?formato=name				 	- Restituisce tutte le carte del formato
     * GET 		/card?idDeck=value				 	- Restituisce tutte le carte del Deck
     * GET 		/card?formato=name&name=name	  	- Restituisce tutte le carte del formato con nome simile a 'name'
     * POST 	/card							 	- Aggiunge una copia della carta richiesta dal mazzo
     * DELETE 	/card?idDeck=value&idCard=value		- Elimina una copia della carta richiesta dal mazzo
     *
     */
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if (userDAO == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Connessione al database non disponibile\"}");
            return;
        }

        String idDeckStr = request.getParameter("idDeck");
        String formato = request.getParameter("formato");
        String name = request.getParameter("name");

        try {
            if (idDeckStr != null) {
                int idDeck = Integer.parseInt(idDeckStr);
                Deck deck = userDAO.getDeck(idDeck);
                if (deck == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"error\": \"Mazzo non trovato\"}");
                    return;
                }
                handleGetCardsInDeck(idDeck, response, out);

            } else if (formato != null && name != null) {
                handleGetCardsByName(formato, name, response, out);

            } else if (formato != null) {
                handleGetCardsByFormat(formato, response, out);

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Richiesta non valida: specificare almeno un parametro\"}");
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

    private void handleGetCardsInDeck(int idDeck, HttpServletResponse response, PrintWriter out) throws IOException {
    	
    	ListCards cards = userDAO.getCardsInDeck(idDeck);

        if (cards == null || cards.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"error\": \"Nessuna carta trovata per il mazzo specificato\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.println(cards.toJSONString()); 
        return;
    }
    
    private void handleGetCardsByFormat(String formato, HttpServletResponse response, PrintWriter out) throws IOException {
    	ListCards cards = userDAO.getCards(formato);

        if (cards == null || cards.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"error\": \"Nessuna carta trovata per il formato '" + formato + "'\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.println(cards.toJSONString());
        return;
    }

    private void handleGetCardsByName(String formato, String name, HttpServletResponse response, PrintWriter out) throws IOException {
    	ListCards cards = userDAO.getCardsByName(name, formato);

        if (cards == null || cards.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("{\"error\": \"Nessuna carta trovata per '" + name + "' nel formato '" + formato + "'\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.println(cards.toJSONString());
        return;
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
        
        try {
            String idDeckStr = request.getParameter("idDeck");
            String idCardStr = request.getParameter("idCard");

            if (idDeckStr == null || idCardStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Parametri mancanti. Servono idDeck e idCard\"}");
                return;
            }

            int idDeck = Integer.parseInt(idDeckStr);
            int idCard = Integer.parseInt(idCardStr);

            Deck deck = userDAO.getDeck(idDeck);
            if (deck == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Mazzo non trovato\"}");
                return;
            }

            if (!deck.getEmail().equals(user.getEmail())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.println("{\"error\": \"Non puoi modificare un mazzo che non ti appartiene\"}");
                return;
            }
            
            if (!userDAO.checkCard(idCard)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Carta inesistente\"}");
                return;
            }

            if (!userDAO.canAddCardToDeck(idDeck, idCard)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.println("{\"error\": \"La Carta non può essere aggiunta al mazzo poiche hai superato il limite\"}");
                return;
            }
            

            
            boolean added = userDAO.addCardToDeck(idDeck,idCard);
            if (added) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println("{\"message\": \"Carta aggiunta con successo al mazzo\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"Errore durante l'aggiunta della carta\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"idDeck e idCard devono essere numerici\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Errore interno del server\"}");
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

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.equals("/delete")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Richiesta non valida. Usa /card/delete\"}");
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
            String idDeckStr = request.getParameter("idDeck");
            String idCardStr = request.getParameter("idCard");

            if (idDeckStr == null || idCardStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Parametri mancanti. Servono idDeck e idCard\"}");
                return;
            }

            int idDeck = Integer.parseInt(idDeckStr);
            int idCard = Integer.parseInt(idCardStr);

            Deck deck = userDAO.getDeck(idDeck);
            if (deck == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Mazzo non trovato\"}");
                return;
            }

            if (!deck.getEmail().equals(user.getEmail())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.println("{\"error\": \"Non puoi modificare un mazzo che non ti appartiene\"}");
                return;
            }
            
            if (!userDAO.checkCard(idCard)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Carta inesistente\"}");
                return;
            }

            if (!userDAO.checkCardInDeck(idDeck, idCard)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.println("{\"error\": \"La Carta non può essere cancellata da un mazzo se non è presente\"}");
                return;
            }
            

            
            boolean deleted = userDAO.removeCardFromMazzo(idDeck,idCard);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println("{\"message\": \"Carta cancellata con successo al mazzo\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"Errore durante la cancellazione della carta\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"idDeck e idCard devono essere numerici\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Errore interno del server\"}");
        }
    	
    }

}
