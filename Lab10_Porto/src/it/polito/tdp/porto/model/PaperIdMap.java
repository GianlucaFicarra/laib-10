package it.polito.tdp.porto.model;

import java.util.HashMap;
import java.util.Map;

public class PaperIdMap {


	private Map<Integer, Paper> map;
	
	public PaperIdMap() {
		map = new HashMap<>();
	}
	
	public Paper get(int id) {//dato id torna oggetto della mappa corrispondente
		return map.get(id);
	}
	
	public Paper get(Paper articolo) { //passo oggetto e mappa controlla di averlo
		Paper old = map.get(articolo.getEprintid());
		if (old == null) {
			map.put(articolo.getEprintid(), articolo);
			return articolo; //se nuovo lo aggiunge e lo restituisce
		}
		return old; //se gia ce l'ha restituisce il vecchio
	}
	
	public void put(Paper articolo, int id) {//per inserire nuovo aereoporto
		map.put(id, articolo);
	}
}
