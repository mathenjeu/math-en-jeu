package ServeurJeu.ComposantesJeu.Questions;

import java.text.Collator;
import java.util.Locale;

/**
 * @author Jean-François Brind'Amour
 */
public class Question
{
    private static final Collator collator = Collator.getInstance(Locale.CANADA_FRENCH);
	// Déclaration d'une variable qui va contenir le code de la question
	private final int intCodeQuestion;
	
	// Déclaration d'une variable qui va contenir l'URL de la question
	private final String strURLQuestion;
	
	// Déclaration d'une variable qui va garder le type de la question
	private final int objTypeQuestion;
	
	// Déclaration d'une variable qui va contenir la réponse à la question
	private final String strReponse;
	
	// Déclaration d'une variable qui va contenir l'url de l'explication de
	// la réponse
	private final String strURLExplication;
	
    /**
     *  Déclaration d'une variable qui va contenir les keywords de la question
     */
	// we don't need it for the moment
	//private final LinkedList<Integer> intKeywords;
		
	/**
	 *  Déclaration d'une variable qui va garder la difficulté de la question.
	 *	Peut avoir une valeur entre 1 et 6, que dépend de niveau scolaire du
	 *  joueur pour cette categorie, si 0 est pas applicable pour joueur
     */
	private final int intDifficulte;
	 
	
	
  /**
	 * Constructeur de la classe Question qui initialise les propriétés de
	 * la question.
	 * 
	 * @param codeQuestion Le code de la question
	 * @param typeQuestion Le type de la question
	 * @param difficulte La difficulte de la question - entre 0 et 6
	 * @param urlQuestion Le URL de la question
	 * @param reponse La réponse à la question
	 * @param urlExplication Le URL de l'explication de la réponse
	 */
	public Question(int codeQuestion, int typeQuestion, int difficulte, String urlQuestion, 
			String reponse, String urlExplication)//, LinkedList<Integer> keywords)
	{
		// Définir les propriétés des questions
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
	 * Cette fonction retourne la difficulté de la question.
	 * 
	 * @return String : La difficulté de la question
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
	 * Cette fonction retourne si oui ou non la réponse est valide.
         * @param reponse Réponse donnée par le joueur
         * @param reponseCorrect Réponse dans la BD
	 * 
	 * @return true si la réponse est valide
	 *         false sinon
	 */
	public static boolean reponseEstValide(String reponse, String reponseCorrect)
	{
            //En utilisant Collator.PRIMARY, les réponses sont considérées
            //équivalentes en ignorant la différence dû aux accents, au nombre
            //d'espaces blancs (espace, tab) et à la capitalisation. Par example
            // "éTé, \t   hiver" est equivalent à "Éte,hïvèr"
            // Cependant les erreures de ponctuation sont importantes, par example
            // "1,2,3" n'est pas équivalent à "1;2;3" ni à "1-2-3"
            collator.setStrength(Collator.PRIMARY);
            int cmp = collator.compare(reponse, reponseCorrect);
            //System.out.println("Reponse du joueur: '" + reponse + "', reponse correcte: '" + reponseCorrect + "', cmp == " + cmp);
            return cmp == 0;
	}
		 
	/**
	 * Cette fonction retourne le URL de l'explication de la réponse à la
	 * question courante.
	 * 
	 * @return String : Le URL de l'explication de la réponse
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
