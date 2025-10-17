package it.uniroma2.pjdm.DAO;

import it.uniroma2.pjdm.entity.*;

public interface UserDAO {
    /**
     * Registra un nuovo utente nel sistema
     * @param email indirizzo email identificativo dell'utente
     * @param password password di accesso dell'utente
     * @param name nome dell'utente
     * @param token token di sessione generato precedentemente
     * @return true se la registrazione è avvenuta con successo, false in caso di errore
     */
    public boolean registerUser(String email, String password, String name, String token);
    
    /**
     * Effettua l'autenticazione di un utente tramite credenziali
     * @param email indirizzo email dell'utente
     * @param password password di accesso dell'utente
     * @return oggetto User corrispondente se l'autenticazione ha successo, null altrimenti
     */
    public User loginUser(String email, String password);
    
    /**
     * Effettua l'autenticazione di un utente tramite token di sessione
     * @param token token di sessione generato precedentemente
     * @return oggetto User corrispondente se l'autenticazione ha successo, null altrimenti
     */
    public User loginToken(String token);
    
    /**
     * Recupera il token di sessione di un utente
     * @param email indirizzo email dell'utente
     * @return token token di sessione dell'utente
     */
    public String getToken(String email);
    
    /**
     * Ritorna il mazzo nel sistema
     * @param idMazzo identificativo univoco del mazzo
     * @return oggetto Deck corrispondente se ha successo, null altrimenti
     */
    public Deck getDeck(int idMazzo);
    
    /**
     * Seleziona tutti i mazzi creati da un utente
     * @param email indirizzo email dell'utente
     * @return lista dei mazzi associati all'utente specificato
     */
    public ListDecks getDecksByUser(String email);
    
    /**
     * Recupera una selezione di mazzi compatibili con un formato specifico
     * @param formato tipo di formato di gioco (es: "Standard", "Modern")
     * @return lista mazzi conformi al formato
     */
    public ListDecks getDecks(String formato);
    
    /**
     * Ricerca mazzi in base al nome
     * @param name stringa (parziale o completa) del nome da ricercare
     * @return lista dei mazzi con nome corrispondente alla ricerca
     */
    public ListDecks getDecksbyName(String name, String formato);
    
    /**
     * Crea un nuovo mazzo associato a un utente
     * @param email indirizzo email dell'utente creatore
     * @param name nome assegnato al mazzo
     * @param formato formato di gioco del mazzo
     * @return true se la creazione è avvenuta con successo, false altrimenti
     */
    public boolean makeDeck(String email, String name, String formato);
    
    /**
     * Elimina un Mazzo
     * @param idDeck identificativo dell mazzo.
     * @return true se la cancellazione è avvenuta, false altrimenti
     */
    public boolean deleteDeck(int idDeck);
    
    /**
     * Verifica l'esistenza di una carta nel sistema
     * @param idCard identificativo univoco della carta
     * @return true se la carta esiste, false altrimenti
     */
    public boolean checkCard(int idCard);
    
    /**
     * Verifica la presenza di una carta in un specifico mazzo
     * @param idMazzo identificativo del mazzo
     * @param idCard identificativo della carta
     * @return true se la carta è presente nel mazzo, false altrimenti
     */
    public boolean checkCardInDeck(int idMazzo, int idCard);
    
    /**
     * Seleziona tutte le carte appartenenti a un formato specifico
     * @param formato tipo di formato di gioco
     * @return lista delle carte compatibili con il formato
     */
    public ListCards getCards(String formato);
    
    /**
     * Ricerca carte per nome all'interno di un formato
     * @param name nome (parziale o completo) della carta
     * @param formato formato di riferimento per la ricerca
     * @return lista delle carte corrispondenti ai criteri
     */
    public ListCards getCardsByName(String name, String formato);
    
    /**
     * Recupera tutte le carte contenute in un mazzo
     * @param idMazzo identificativo del mazzo
     * @return lista delle carte appartenenti al mazzo
     */
    public ListCards getCardsInDeck(int idMazzo);
    
    /**
     * Verifica la possibilità di aggiungere una carta a un mazzo
     * @param mazzoId identificativo del mazzo
     * @param cardId identificativo della carta
     * @return true se l'aggiunta è consentita, false altrimenti
     */
    public boolean canAddCardToDeck(int mazzoId, int cardId);
    
    /**
     * Aggiunge una carta a un mazzo esistente
     * @param idMazzo identificativo del mazzo
     * @param idCard identificativo della carta da aggiungere
     * @param email indirizzo email dell'utente autorizzato
     * @return true se l'operazione è riuscita, false altrimenti
     */
    public boolean addCardToDeck(int idMazzo, int idCard);
    
    
    /**
     * Rimuove una carta da un mazzo esistente
     * @param idMazzo identificativo del mazzo
     * @param IdCard identificativo della carta da rimuovere
     * @param email indirizzo email dell'utente autorizzato
     * @return true se la rimozione è riuscita, false altrimenti
     */
    public boolean removeCardFromMazzo(int idMazzo, int IdCard);
    
    /**
     * Ritorna il mazzo nel sistema
     * @param idComment identificativo univoco del mazzo
     * @return oggetto Comment corrispondente se ha successo, null altrimenti
     */
    public Comment getComment(int idComment);
    
    /**
     * Verifica che il commento appartenga al mazzo
     * @param idComment identificativo univoco della carta
     * @param idMazzo identificativo del mazzo target
     * @return true se la carta esiste, false altrimenti
     */
    public boolean checkCommentInDeck(int idComment, int idMazzo);
    
    /**
     * Aggiunge un commento a un mazzo
     * @param email indirizzo email dell'utente che commenta
     * @param testo testo del commento
     * @param idMazzo identificativo del mazzo target
     * @return true se l'inserimento è riuscito, false altrimenti
     */
    public boolean leaveComment(String email, String testo, int idMazzo);
    
    /**
     * Elimina un commento 
     * @param idComment identificativo del commento.
     * @return true se la cancellazione è avvenuta, false altrimenti
     */
    public boolean deleteComment(int idComment);
    
    /**
     * Recupera tutti i commenti associati a un mazzo
     * @param idMazzo identificativo del mazzo
     * @return lista dei commenti relativi al mazzo
     */
    public ListComments getCommentsForMazzo(int idMazzo);
    
    /**
     * Chiude la connessione con il database
     */
    public void closeConnection();
}