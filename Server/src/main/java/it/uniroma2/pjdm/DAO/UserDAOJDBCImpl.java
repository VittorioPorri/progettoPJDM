package it.uniroma2.pjdm.DAO;

import java.sql.*;
import it.uniroma2.pjdm.entity.*;

public class UserDAOJDBCImpl implements UserDAO {
    private Connection connection;

    public UserDAOJDBCImpl(String ip, String port, String dbName, String userName, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName +
                    "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            connection = DriverManager.getConnection(url, userName, password);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Errore connessione DB", e);
        }
    }

    @Override
    public boolean registerUser(String email, String password, String name, String token) {
        String insertUserSQL = "INSERT INTO user (email, password, name) VALUES (?, ?, ?)";
        String insertTokenSQL = "INSERT INTO token (email, token) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false); // Disabilita il commit automatico

            
            try (PreparedStatement userStmt = connection.prepareStatement(insertUserSQL)) {
                userStmt.setString(1, email);
                userStmt.setString(2, password);
                userStmt.setString(3, name);

                int userInserted = userStmt.executeUpdate();

                if (userInserted == 0) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement tokenStmt = connection.prepareStatement(insertTokenSQL)) {
                tokenStmt.setString(1, email);
                tokenStmt.setString(2, token);

                int tokenInserted = tokenStmt.executeUpdate();

                if (tokenInserted == 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback(); 
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public User loginUser(String email, String password) {
        String sql = "SELECT email, name FROM user WHERE email = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("email"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User loginToken(String token) {
        String sql = "SELECT u.email, u.name FROM user u JOIN token t ON u.email = t.email WHERE t.token = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("email"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    @Override
    public String getToken(String email) {
    	String sql = "SELECT token FROM token  WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("token");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	
    	return null;
    }
    

    @Override
    public Deck getDeck(int idMazzo) {
        String sql = "SELECT idMazzo, name, formato, email FROM deck WHERE idMazzo = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMazzo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Deck(
                    rs.getInt("idMazzo"),
                    rs.getString("name"),
                    rs.getString("formato"),
                    rs.getString("email")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public ListDecks getDecksByUser(String email) {
        String sql = "SELECT idMazzo, name, formato, email FROM deck WHERE email = ?";
        ListDecks decks = new ListDecks();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                decks.add(new Deck(
                    rs.getInt("idMazzo"),
                    rs.getString("name"),
                    rs.getString("formato"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return decks;
    }

    @Override
    public ListDecks getDecks(String formato) {
    	String sql = "SELECT idMazzo, name, formato, email FROM deck WHERE formato = ? ORDER BY RAND() LIMIT 20";
        ListDecks decks = new ListDecks();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, formato);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                decks.add(new Deck(
                    rs.getInt("idMazzo"),
                    rs.getString("name"),
                    rs.getString("formato"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return decks;
    }

    @Override
    public ListDecks getDecksbyName(String name, String formato) {
        String sql = "SELECT idMazzo, name, formato, email FROM deck WHERE formato = ? AND name LIKE ?";
        ListDecks decks = new ListDecks();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            pstmt.setString(2, formato);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                decks.add(new Deck(
                    rs.getInt("idMazzo"),
                    rs.getString("name"),
                    rs.getString("formato"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return decks;
    }

    @Override
    public boolean makeDeck(String email, String name, String formato) {
        String sql = "INSERT INTO deck (name, formato, email) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, formato);
            pstmt.setString(3, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteDeck(int idDeck) {
    	String sql = "DELETE FROM deck WHERE idDeck = ?";
    	try(PreparedStatement pstmt = connection.prepareStatement(sql)){
    		pstmt.setInt(1, idDeck);
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
    	} catch (SQLException e) {
            e.printStackTrace();
        }
    	return false;    	
    }

    @Override
    public boolean checkCard(int idCard) {
        String sql = "SELECT idCard FROM card WHERE idCard = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idCard);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean checkCardInDeck(int idMazzo, int idCard) {
        String sql = "SELECT idMazzo FROM deckCard WHERE idMazzo = ? AND idCard = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMazzo);
            pstmt.setInt(2, idCard);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ListCards getCards(String formato) {
        String sql = "SELECT idCard, name, image, num FROM card WHERE formato = ?";
        ListCards cards = new ListCards();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, formato);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cards.add(new Card(
                    rs.getInt("idCard"),
                    rs.getString("name"),
                    rs.getBytes("image"),
                    rs.getInt("num")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    @Override
    public ListCards getCardsByName(String name, String formato) {
        String sql = "SELECT idCard, name, image, num FROM card WHERE name LIKE ? AND formato = ?";
        ListCards cards = new ListCards();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            pstmt.setString(2, formato);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cards.add(new Card(
                    rs.getInt("idCard"),
                    rs.getString("name"),
                    rs.getBytes("image"),
                    rs.getInt("num")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }


    @Override
    public ListCards getCardsInDeck(int idMazzo) {
        String sql = """
            SELECT c.idCard, c.name, c.image, dc.quantita
            FROM card c
            JOIN deckCard dc ON c.idCard = dc.idCard
            WHERE dc.idMazzo = ?
        """;

        ListCards cards = new ListCards();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMazzo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                cards.add(new Card(
                    rs.getInt("idCard"),
                    rs.getString("name"),
                    rs.getBytes("image"),
                    rs.getInt("quantita")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }


    @Override
    public boolean canAddCardToDeck(int mazzoId, int cardId) {
        String sql =
            "SELECT COALESCE((" +
            "  (SELECT quantita FROM deckCard WHERE idMazzo = ? AND idCard = ?)" +
            "), 0) < " +
            "(SELECT num FROM card WHERE idCard = ?) AS canAdd";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, mazzoId);
            pstmt.setInt(2, cardId);
            pstmt.setInt(3, cardId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("canAdd");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; 
    }
    
    @Override
    public boolean addCardToDeck(int idMazzo, int idCard) {
        String sql = "INSERT INTO deckCard (idMazzo, idCard, quantita) VALUES (?, ?, 1) " +
                     "ON DUPLICATE KEY UPDATE quantita = quantita + 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMazzo);
            pstmt.setInt(2, idCard);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeCardFromMazzo(int idMazzo, int idCard) {
        try {
            String selectSql = "SELECT quantita FROM deckCard WHERE idMazzo = ? AND idCard = ?";
            int quantita = 0;
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
                selectStmt.setInt(1, idMazzo);
                selectStmt.setInt(2, idCard);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    quantita = rs.getInt("quantita");
                } 
            }

            if (quantita > 1) {
                String updateSql = "UPDATE deckCard SET quantita = quantita - 1 WHERE idMazzo = ? AND idCard = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, idMazzo);
                    updateStmt.setInt(2, idCard);
                    int rows = updateStmt.executeUpdate();
                    return rows > 0;
                }
            } else {
                String deleteSql = "DELETE FROM deckCard WHERE idMazzo = ? AND idCard = ?";
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, idMazzo);
                    deleteStmt.setInt(2, idCard);
                    int rows = deleteStmt.executeUpdate();
                    return rows > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Comment getComment(int idComment) {
        Comment comment = null;
        String query = "SELECT * FROM comment WHERE idComment = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idComment);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                comment = new Comment(
                    rs.getInt("idComment"),
                    rs.getString("text"),
                    rs.getString("email"),
                    rs.getInt("idMazzo")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return comment;
    }

    @Override
    public boolean checkCommentInDeck(int idComment, int idMazzo) {
        String sql = "SELECT idComment FROM comment WHERE idComment = ? AND idMazzo = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idComment);
            pstmt.setInt(2, idMazzo);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean leaveComment(String email, String testo, int idMazzo) {
        String sql = "INSERT INTO comment (text, email, idMazzo) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, testo);
            pstmt.setString(2, email);
            pstmt.setInt(3, idMazzo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteComment(int idComment) {
    	String sql = "DELETE FROM comment WHERE idComment = ?";
    	try(PreparedStatement pstmt = connection.prepareStatement(sql)){
    		pstmt.setInt(1, idComment);
            int rows = pstmt.executeUpdate();
            return rows > 0;
            
    	} catch (SQLException e) {
            e.printStackTrace();
        }
    	return false;    	
    }

    @Override
    public ListComments getCommentsForMazzo(int idMazzo) {
        String sql = "SELECT idComment, text, email, idMazzo FROM comment WHERE idMazzo = ?";
        ListComments comments = new ListComments();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMazzo);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                comments.add(new Comment(
                    rs.getInt("idComment"),
                    rs.getString("text"),
                    rs.getString("email"),
                    rs.getInt("idMazzo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
