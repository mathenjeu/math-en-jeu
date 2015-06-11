package ca.serveurmej.importeur.lanceur;

import ca.serveurmej.importeur.procceseur.ProcesseurFichierCSV;

/**
 * 
 */

/**
 * @author JohnI
 *
 */
public class Importeur {
	private static ProcesseurFichierCSV processeur;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String nomFichier = null;
		if( args.length > 0 )
		{
			nomFichier = args[0];
		}
		
		System.out.println(nomFichier);
		
		if(!nomFichier.isEmpty()){
			processeur = new ProcesseurFichierCSV(nomFichier);
			processeur.processe();
		}
		
	}

}
