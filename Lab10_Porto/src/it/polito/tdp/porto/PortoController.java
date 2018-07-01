package it.polito.tdp.porto;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.porto.model.Author;
import it.polito.tdp.porto.model.Model;
import it.polito.tdp.porto.model.Paper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class PortoController {

	private Model model;
	
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ComboBox<Author> boxPrimo;

    @FXML
    private ComboBox<Author> boxSecondo;

    @FXML
    private TextArea txtResult;
    

    public void setModel(Model model) {
		this.model=model;
		
		//iizializzo i menu
		boxPrimo.getItems().addAll(model.getAuthors());
		boxSecondo.getItems().clear(); //dato che cambia lo pulisco sempre
	}
    
    @FXML
    void handleCoautori(ActionEvent event) {

    	txtResult.clear();
    	
    	//salvo autore passato dall'utente
		Author autore = boxPrimo.getValue();
		
		if(autore==null) {
			txtResult.setText("Seleziona un autore.\n");
			return;
		}


		//creo un grafo 
		try {
			model.createGraph();
			
		} catch (RuntimeException e) {
			e.printStackTrace();
			txtResult.appendText("Errore nella creazione grafo\n");
			return;
		}
		
		//calcolo coautori
		List<Author> coautori= model.coautori(autore);
		if(!coautori.isEmpty()) {
		txtResult.appendText("Stampo coautori di "+autore.toString()+":\n");
		for(Author a: coautori) {
			txtResult.appendText(String.format("%s\n", a.toString()));
		}
		} else {
			txtResult.appendText(autore.toString()+" non ha coautori.\n");
		}
		
		//calcolo nocoautori
		//1 modo: List<Author> nocoautori=model.nonCoautori(autore);
		List<Author> noncoautori = new ArrayList<>(model.getAuthors()) ;
    	noncoautori.removeAll(coautori) ;
    	noncoautori.remove(autore);
    	
		// riempi ed abilita la seconda tendina
    	boxSecondo.getItems().clear();		
		boxSecondo.getItems().addAll(noncoautori);
		boxSecondo.setDisable(false);
    }

    @FXML
	void handleSequenza(ActionEvent event) {
    	txtResult.clear();

    	Author a1 = boxPrimo.getValue();
    	Author a2 = boxSecondo.getValue();
    	
    	if( a1==null || a2==null ) {
    		txtResult.appendText("Errore: selezionare due autori\n");
    		return ;
    	}
    	
    	// trovo il cammino minimo tra i due
    	List<Paper> minPath = model.findShortestPath(a1, a2);

    	if(!minPath.isEmpty()) {
    	txtResult.appendText("\nArticoli che collegano "+a1.toString()+" e "+a2.toString()+":\n");
    	String result = "";
    	for(Paper p : minPath) {
    		txtResult.appendText(p.getTitle()+"\n");
    	}
    	} else {
    		txtResult.appendText("\nNon esistono articoli che collegano "+a1.toString()+" e "+a2.toString()+"\n");
    	}
    	
    }


    @FXML
    void initialize() {
        assert boxPrimo != null : "fx:id=\"boxPrimo\" was not injected: check your FXML file 'Porto.fxml'.";
        assert boxSecondo != null : "fx:id=\"boxSecondo\" was not injected: check your FXML file 'Porto.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Porto.fxml'.";

        boxSecondo.setDisable(true);
    }

	
}
