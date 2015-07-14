package ca.serveurmej.importeur.procceseur;

/**
 * 
 *  Class used to read a csv file and create the list of csv records to be returned.
 *  It use the apache commons csv parser.
 *  
 *  @author Lilian Oloieri
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ProcesseurFichierCSV {
	
	private String nomFichier = "";
	private List<CSVRecord> listeJoueurs; 
	private String sortie;
	private static final Logger logger = LogManager.getLogger(ProcesseurFichierCSV.class);
	
	public ProcesseurFichierCSV(final String nomFichier) {
		this.nomFichier = nomFichier;
		setSortie("");
	}	
	
	public ProcesseurFichierCSV() {
		// exit
	}
	
	public List<CSVRecord> processe(){
		
		StringBuilder output = new StringBuilder();
		//output.append(" *** Parser fichier csv : \n");
		logger.info(" *** Parser fichier csv : \n");
		
		CSVParser parser = null;
		Reader csvData = null;
	 
		try {
			/*
			String workingDirectory = System.getProperty("user.dir");			 
			String absoluteFilePath = "";	 
			//absoluteFilePath = workingDirectory + System.getProperty("file.separator") + filename;
			absoluteFilePath = workingDirectory + File.separator + nomFichier;
			
			//System.out.println("Final filepath : " + absoluteFilePath);			
	 
			File file = new File(absoluteFilePath);
	 		
			csvData = new FileReader(file);
			*/
			csvData = new FileReader(nomFichier);
			parser = new CSVParser(csvData, CSVFormat.EXCEL);
			listeJoueurs = parser.getRecords();
			parser.close();
			csvData.close();
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			output.append(" *** Un erreur c'est produit au traitement du fichier csv : \n" + nomFichier);
		} catch (IOException e) {
			e.printStackTrace();
			output.append(" *** Un erreur c'est produit au traitement du fichier csv : \n" + nomFichier);
		} catch (Exception e) {
			e.printStackTrace();
			output.append(" *** Un erreur c'est produit au traitement du fichier csv : \n" + nomFichier);
		}		finally {
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
		
		//output.append("Un liste de " + listeJoueurs.size() + " utilisateurs a été créé \n");
		setSortie(output.toString());
		logger.info(" *** Fini de parser fichier csv : \n");
		
		return listeJoueurs;
	}

	public String getSortie() {
		return sortie;
	}

	public void setSortie(String sortie) {
		this.sortie = sortie;
	}

}
