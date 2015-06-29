package ca.serveurmej.importeur.lanceur;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ca.serveurmej.importeur.dao.ProcesseurJoueurDAO;
import ca.serveurmej.importeur.procceseur.ProcesseurFichierCSV;


/**
 * Entry point to batch used to read csv file with players data and insert them in DB.
 *
 * 
 * @author Lilian Oloieri
 *
 */
public class Importeur {
	public static final String VERSION = "0.1";
	private static final Logger logger = LogManager.getLogger(Importeur.class);
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final ProcesseurFichierCSV processeur;
		String nomFichier = null;
		String fichierSortie = null;
		List<CSVRecord> listeJoueurs = null; 
		StringBuilder sortie = new StringBuilder();
		
		if( args.length > 1 )
		{
			nomFichier = args[0];
			fichierSortie = args[1];			
			
		} else {
			logger.error(" *** arguments non definies *** ");
		}		
		
		if( !nomFichier.isEmpty() ){
			processeur = new ProcesseurFichierCSV(nomFichier, fichierSortie);
			listeJoueurs = processeur.processe();
		}
		
		if( listeJoueurs != null && !listeJoueurs.isEmpty() ){
			
			ProcesseurJoueurDAO processeurDAO = new ProcesseurJoueurDAO();
			String sortieDao = processeurDAO.insererJoueurs(listeJoueurs);
			sortie.append(sortieDao);
			sortie.append("Toutes les profils ont été traité.");
		}
		
		
	}

}
