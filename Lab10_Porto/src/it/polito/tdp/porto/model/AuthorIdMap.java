package it.polito.tdp.porto.model;

import java.util.HashMap;
import java.util.Map;


public class AuthorIdMap {

	//schema valido per tutte e tre le mappe
	
		private Map<Integer, Author> map;
		
		public AuthorIdMap() {
			map = new HashMap<>();
		}
		
		public Author get(int id) {//dato id torna oggetto della mappa corrispondente
			return map.get(id);
		}
		
		public Author get(Author autore) { //passo oggetto e mappa controlla di averlo
			Author old = map.get(autore.getId());
			if (old == null) {
				map.put(autore.getId(), autore);
				return autore; //se nuovo lo aggiunge e lo restituisce
			}
			return old; //se gia ce l'ha restituisce il vecchio
		}
		
		public void put(Author autore, int id) {//per inserire nuovo aereoporto
			map.put(id, autore);
		}
}
