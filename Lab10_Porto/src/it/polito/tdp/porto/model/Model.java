package it.polito.tdp.porto.model;

import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;

import it.polito.tdp.porto.db.PortoDAO;

public class Model {
	
	/*UTILIZZO METODO ORM
	 * utilizzo le mappe per evitare doppioni
	 * idmap è una classe che al suo interno ha una mappa che mette in relazione idoggetto
	 * con l'oggetto stesso, e ridefinisce metodo get che restituisce l'oggetto stesso
	 * e put che permette oggetto con attenzione di non inserire duplicai */
	
	PortoDAO dao = null; //variabile globale per accedere al dao
	
	//creo due liste una per tutte le compagnie una per gli aereoporti e rotte
	List<Author> autori;
	List<Paper> articoli;
	

	//creo identity map --> e relative classi
	AuthorIdMap autoreIdMap;
	PaperIdMap articoliIdMap;
	
	/*Dopo aver creato il pattern ORM voglio crerae il mio grafo:
	 * lo voglio diretto perche rotte hanno partenza e destinazion, e lo voglio pesato,
	 * con l'info della distanza, ottenibile con la longitudine e latitudine di distanza 
	 * tra i due aereoporti, uso aposita libreria simplelatlng.(vedi su) */
	
	//--> scelgo grafico in base alle richieste  tramite schema delle slide
	SimpleGraph<Author, DefaultEdge> grafo;
                 //vertici   archi
	
	public Model() {
		dao = new PortoDAO();//inizzializzo variabile per accedere al dao

		//inizzializzo mappe prima delle lise
		autoreIdMap = new AuthorIdMap();
		articoliIdMap = new PaperIdMap();

		//aggiungo info sull IDmap
		//quando inserisco oggetto alla lista, viene anche inserito nella idmap 
		//sfrutto i metodi del dao implementati secondo pattern ORM
		autori = dao.getAllAuthors(autoreIdMap);
		System.out.println("Numero Autori: "+autori.size()); //stampo dimensione dei dati come debug

		articoli = dao.getAllPapers(articoliIdMap);
		System.out.println("Numero Articoli: "+articoli.size()); //stampo dimensione dei dati come debug

		//deve creare i riferimenti incrociati tra i due
		this.dao.getAllCreators(autoreIdMap, articoliIdMap);
		
	}
	
	//rende accessibile la lista autori dal controller
	public List<Author> getAuthors() {
		if (this.autori == null) {
			return new ArrayList<Author>();
		}
		return this.autori;
	}

	public void createGraph() {  
		
		//creo grafo.... dichiarazione standard
		grafo = new SimpleGraph<Author,DefaultEdge>(DefaultEdge.class);
		
		
		//creato grafo aggiungo i vertici dalla lista di autori
		Graphs.addAllVertices(grafo, this.autori);
		
		//aggiungo i collegameti cioè gli archi ed itero su articoli
		for (Author a : autori) {
			List<Author> coautori= dao.getListCoautori(a);
			//per ogni elemento della lista creatore
			for(Author a2: coautori) {
				if(!grafo.containsEdge(a, a2) && !a.equals(a2)) // !a.equals(a2) evita i loop
					grafo.addEdge(a, a2);
			}
		}
		
		//stampo di default vertici e archi
		System.out.println("\n Numero di vertici per il grafo: "+grafo.vertexSet().size());
		System.out.println("Numero di archi per il grafo: "+grafo.edgeSet().size());
	
		
	}
	

	public List<Author> coautori(Author autore) {
		//List<Author> lista= dao.getListCoautori(autore);
		
		//oppure sfrutto grafo
		 
		 if(this.grafo==null) //se ancora non h creato il grafo lo creo
			this.createGraph();
		
		List<Author> lista = Graphs.neighborListOf(this.grafo, autore) ;
		
		return lista;
	}
	
	
	/*1 modo per calcolare non coautori 
	 * public List<Author> nonCoautori(Author autore) {
		//dato autore del primo box prendo quelli che non gli sono coautori7
		List<Author> nocoautori= new ArrayList<>();
		List<Author> coautori=this.coautori(autore);
		
		for(Author a:autori) {
			if(!coautori.contains(a))
				nocoautori.add(a);
		}
		return nocoautori;
		
	}*/
	
	
	//Trova una sequenza di articoli che legano l'autore {@code a1} all'autore
	public List<Paper> findShortestPath(Author a1, Author a2) {

		List<Paper> result = new ArrayList<>() ; 

		if (a1 == null || a2 == null) {
			throw new RuntimeException("Gli autori selezionati non sono presenti in memoria\n");
		}
		
		// trovo un cammino minimo tra a1 ed a2
		DijkstraShortestPath<Author, DefaultEdge> minPath = 
				new DijkstraShortestPath<Author, DefaultEdge>(this.grafo);
		
		GraphPath<Author, DefaultEdge> edges = minPath.getPath(a1, a2);
		// ciascun edge corrisponderà ad un paper
		
		if(edges!=null) {
					
		for(DefaultEdge e: edges.getEdgeList()) {
			// autori che corrispondono all'edge
			Author as = grafo.getEdgeSource(e) ;
			Author at = grafo.getEdgeTarget(e) ;
			
			
	// trovo l'articolo, posso farlo in due modi:
		//1)  consultare il DB per trovare almeno un articolo in cui hanno collaborato i vertici adiacenti
			Paper p = dao.articoloComune(as, at) ;

       //2)   attraverso ORM trovare l'intersezione tra i due insiemi di articoli di ogni vertice
			// Paper p = this.intersezioneInsiemi (as, at);
			
			if(p == null)
				throw new InternalError("Paper not found...") ;
			
			result.add(p) ;
		}
		}
		return result;
	
	}
	
	//OSS
	private Paper intersezioneInsiemi(Author aPartenza, Author aDestinazione) {
		List <Paper> list1 = aPartenza.getArticoli();
		List <Paper> list2 = aDestinazione.getArticoli();
		
		System.out.println("\n Numero di giornali autore ap: "+list1.size());
		System.out.println("\n Numero di giornali autore ad: "+list2.size());
		// è sufficiente almeno un articolo
		for (Paper p1 : list1)
			for (Paper p2 : list2) {
				if (p1.equals(p2))
					return p1;
			}
		
		return null;
	}

	
	
	
}
