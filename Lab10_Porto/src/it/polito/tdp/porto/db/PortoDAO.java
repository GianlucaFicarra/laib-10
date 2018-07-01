package it.polito.tdp.porto.db;

import java.sql.Connection;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.porto.model.Author;
import it.polito.tdp.porto.model.AuthorIdMap;
import it.polito.tdp.porto.model.Paper;
import it.polito.tdp.porto.model.PaperIdMap;

public class PortoDAO {

	/*
	 * Dato l'id ottengo l'autore.
	 */
	public Author getAutore(int id) {

		final String sql = "SELECT * FROM author where id=?";

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, id);

			ResultSet rs = st.executeQuery();

			if (rs.next()) {

				Author autore = new Author(rs.getInt("id"), rs.getString("lastname"), rs.getString("firstname"));
				return autore;
			}

			return null;

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new RuntimeException("Errore Db");
		}
	}

	/*
	 * Dato l'id ottengo l'articolo.
	 */
	public Paper getArticolo(int eprintid) {

		final String sql = "SELECT * FROM paper where eprintid=?";

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, eprintid);

			ResultSet rs = st.executeQuery();

			if (rs.next()) {
				Paper paper = new Paper(rs.getInt("eprintid"), rs.getString("title"), rs.getString("issn"),
						rs.getString("publication"), rs.getString("type"), rs.getString("types"));
				return paper;
			}

			return null;

		} catch (SQLException e) {
			 e.printStackTrace();
			throw new RuntimeException("Errore Db");
		}
	}

	//metodi totali per estrarre i dati dal DB:
	
	public List<Author> getAllAuthors(AuthorIdMap autoreIdMap) {
		String sql = "SELECT * FROM author ORDER BY lastname ASC, firstname ASC";
		List<Author> list = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				Author autore = new Author(res.getInt("id"), res.getString("lastname"), res.getString("firstname"));
				list.add(autoreIdMap.get(autore));//alla lista inserisco oggetto già presente o appena aggiunta nell'idmap
			}
			conn.close();
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public List<Paper> getAllPapers(PaperIdMap articoliIdMap) {
		String sql = "SELECT * FROM paper";
		List<Paper> list = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();

			while (res.next()) {
				
				Paper articolo = new Paper(res.getInt("eprintid"), res.getString("title"), res.getString("issn"),
						res.getString("publication"), res.getString("type"),res.getString("types"));
				list.add(articoliIdMap.get(articolo));//alla lista inserisco oggetto già presente o appena aggiunta nell'idmap
			}
			conn.close();
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	//Definisce le relazioni tra autori e articoli dal DB, no ritorna
	public void getAllCreators(AuthorIdMap autoreIdMap, PaperIdMap articoliIdMap) {
		String sql = "SELECT * FROM creator";
		
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			while (res.next()) {
				
				Author autore = autoreIdMap.get(res.getInt("authorid"));
				Paper articolo = articoliIdMap.get(res.getInt("eprintid"));
				
				// popolazione delle liste relative a ogni oggetto creato
				autore.getArticoli().add(articolo);
				articolo.getAutori().add(autore);
				
			}	
			conn.close();
	
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public List<Author> getListCoautori(Author a) {
		/*String sql = "SELECT DISTINCT a.* " + 
				"FROM creator AS c, creator AS c2, author AS a " + 
				"WHERE (a.id= c.authorid OR a.id= c2.authorid) " + 
				"AND c.authorid<c2.authorid AND c.eprintid=c2.eprintid AND (c.authorid=? OR c2.authorid=?) ";*/
		
		String sql = "SELECT DISTINCT a2.id, a2.lastname, a2.firstname " + 
				"FROM creator c1, creator c2, author a2 " + 
				"WHERE c1.eprintid=c2.eprintid " + 
				"AND c2.authorid=a2.id " + 
				"AND c1.authorid= ? " + 
				"AND a2.id <> c1.authorid " + 
				"ORDER BY a2.lastname ASC, a2.firstname ASC" ;
		
		List<Author> list = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, a.getId());
			//st.setInt(2, a.getId());
			ResultSet res = st.executeQuery();

			/*torna già autori filtrati senza ripetizione
			 join creatore co se stessa, perche voglio una coppia di autori, fatto
			 con i codici di libri, seleziono l'autore passato come parametro, e gli
			 autori collegati dal join devono avere appunto lo stesso libro ma non essere quell'autore
			 di ciò seleziono soloi dati del secondo autore
			*/
			
			while (res.next()) {
				Author autore = new Author(res.getInt("id"), res.getString("lastname"), res.getString("firstname"));
				list.add(autore);
			}
			conn.close();
			return list;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/*
	 * Restituisce un articolo in comune tra i due autori.
	 * Se esistono più articoli comuni, ne restituisce comunque solamente uno (LIMIT 1 nella query).
	 * Se invece non esistono articoli comuni restituisce {@code null}
	 */
	public Paper articoloComune(Author as, Author at) {
		String sql = "SELECT paper.eprintid, title, issn, publication, type, types " + 
				"FROM paper, creator c1, creator c2 " + 
				"WHERE paper.eprintid=c1.eprintid " + 
				"AND paper.eprintid=c2.eprintid " + 
				"AND c1.authorid=? " + 
				"AND c2.authorid=? " + 
				"LIMIT 1" ;
		
		Connection conn = DBConnect.getConnection() ;
		
		try {
			PreparedStatement st = conn.prepareStatement(sql) ;
			st.setInt(1, as.getId());
			st.setInt(2, at.getId());
			
			ResultSet res = st.executeQuery() ;
			
			Paper p = null ;
			if(res.next()) {
				// c'è almeno un articolo: ritornalo!
				p = new Paper(res.getInt("eprintid"), res.getString("title" ), res.getString("issn"),
						res.getString("publication"), res.getString("type"), res.getString("types")) ;
			}
			
			conn.close();
			return p ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}

}