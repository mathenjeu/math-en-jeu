package ca.serveurmej.importeur.procceseur;

/**
 * 
 *  Class used to read a csv file and create the list of csv records to be returned.
 *  It use the apache commons csv parser.
 *  
 *  @author Lilian Oloieri
 */
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ProcesseurFichierCSV {
	
	private String nomFichier = "";
	private List<CSVRecord> listeJoueurs; 
	private String sortie;
	
	public ProcesseurFichierCSV(final String nomFichier) {
		this.nomFichier = nomFichier;
		setSortie("");
	}	
	
	public ProcesseurFichierCSV() {
		// exit
	}
	
	public List<CSVRecord> processe(){
		
		StringBuilder output = new StringBuilder();
		output.append(" *** Parser fichier csv : \n");
		
		CSVParser parser = null;
		Reader csvData = null;
	 
		try {
	 
			csvData = new FileReader(nomFichier);
			parser = new CSVParser(csvData, CSVFormat.EXCEL);
			listeJoueurs = parser.getRecords();
			parser.close();
			csvData.close();
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (parser != null) {
				try {
					parser.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (csvData != null) {
				try {
					csvData.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		for (CSVRecord joueur : listeJoueurs){
			output.append(joueur.toString() + "\n");
		}
		setSortie(output.toString());
		return listeJoueurs;
	}

	public String getSortie() {
		return sortie;
	}

	public void setSortie(String sortie) {
		this.sortie = sortie;
	}

}
