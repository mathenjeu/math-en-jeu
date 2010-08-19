package ServeurJeu.ComposantesJeu;

import java.util.LinkedList;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class Question
{
	// D�claration d'une variable qui va contenir le code de la question
	private final int intCodeQuestion;
	
	// D�claration d'une variable qui va contenir l'URL de la question
	private final String strURLQuestion;
	
	// D�claration d'une variable qui va garder le type de la question
	private final int objTypeQuestion;
	
	// D�claration d'une variable qui va contenir la r�ponse � la question
	private final String strReponse;
	
	// D�claration d'une variable qui va contenir l'url de l'explication de 
	// la r�ponse
	private final String strURLExplication;
	
    /**
     *  D�claration d'une variable qui va contenir les keywords de la question
     */
	// we don't need it for the moment
	//private final LinkedList<Integer> intKeywords;
		
	/**
	 *  D�claration d'une variable qui va garder la difficult� de la question.
	 *	Peut avoir une valeur entre 1 et 6, que d�pend de niveau scolaire du 
	 *  joueur pour cette categorie, si 0 est pas applicable pour joueur
     */
	private final int intDifficulte;
	 
	
	
  /**
	 * Constructeur de la classe Question qui initialise les propri�t�s de 
	 * la question.
	 * 
	 * @param int codeQuestion : Le code de la question
	 * @param String typeQuestion : Le type de la question
	 * @param int difficulte : La difficulte de la question - entre 0 et 6
	 * @param String urlQuestion : Le URL de la question
	 * @param String reponse : La r�ponse � la question
	 * @param String urlExplication : Le URL de l'explication de la r�ponse
	 */
	public Question(int codeQuestion, int typeQuestion, int difficulte, String urlQuestion, 
			String reponse, String urlExplication)//, LinkedList<Integer> keywords)
	{
		// D�finir les propri�t�s des questions
		intCodeQuestion = codeQuestion;
		objTypeQuestion = typeQuestion;
		intDifficulte = difficulte;
		strURLQuestion = urlQuestion;
		strReponse = reponse.toLowerCase().replace(",",".");
		strURLExplication = urlExplication;
		//intKeywords = keywords;
	}
	
	/**
	 * Cette fonction retourne la reponse  de la question.
	 * 
	 * @return string : La reponse de la question
	 */
	public String getStringAnswer()
	{
		return strReponse;
	}
	
	/**
	 * Cette fonction retourne le code de la question.
	 * 
	 * @return int : Le code de la question
	 */
	public int obtenirCodeQuestion()
	{
		return intCodeQuestion;
	}

	
	/**
	 * Cette fonction retourne le type de la question.
	 * 
	 * @return String : Le type de la question
	 */
	public int obtenirTypeQuestion()
	{
		return objTypeQuestion;
	}
	
	/**
	 * Cette fonction retourne la difficult� de la question.
	 * 
	 * @return String : La difficult� de la question
	 */
	public int obtenirDifficulte()
	{
		return intDifficulte;
	}
	
	/**
	 * Cette fonction retourne le URL de la question courante.
	 * 
	 * @return String : Le URL de la question courante
	 */
	public String obtenirURLQuestion()
	{
		return strURLQuestion;
	}
	
	/**
	 * Cette fonction retourne si oui ou non la r�ponse est valide.
	 * @param reponseCorrect 
	 * 
	 * @return boolean : true si la r�ponse est valide
	 * 					 false sinon
	 */
	public static boolean reponseEstValide(String reponse, String reponseCorrect)
	{
		// standartisation des r�ponses
		String tempStr = reponse.trim().toLowerCase();
		tempStr = tempStr.replace("�","c").replace("�","u");
		tempStr = tempStr.replace("�", "u").replace("�","o").replace("�","o");
		tempStr = tempStr.replace("�", "e").replace("�","e").replace("�","e");
		tempStr = tempStr.replace("�", "a").replace("�","a").replace(".",",");
		tempStr = tempStr.replace("�", "i").replace("�","i");
		tempStr = tempStr.replace(",",".");
		//System.out.println("La reponse : " + reponse + " Est la rep dans BD : " + strReponse);
		return reponseCorrect.equals(tempStr);
	}
		 
	/**
	 * Cette fonction retourne le URL de l'explication de la r�ponse � la 
	 * question courante.
	 * 
	 * @return String : Le URL de l'explication de la r�ponse
	 */
	public String obtenirURLExplication()
	{
		return strURLExplication;
	}
	

	/**
	 * 
	 * @return
	 
	public LinkedList<Integer> getKeyword() 
	{
		return intKeywords;
	}*/
			
} // fin classe
