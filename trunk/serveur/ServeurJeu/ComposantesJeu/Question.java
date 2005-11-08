package ServeurJeu.ComposantesJeu;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class Question
{
	// D�claration d'une variable qui va contenir le code de la question
	private int intCodeQuestion;
	
	// D�claration d'une variable qui va contenir l'URL de la question
	private String strURLQuestion;
	
	// D�claration d'une variable qui va contenir la r�ponse � la question
	private String strReponse;
	
	/**
	 * Constructeur de la classe Question qui initialise les propri�t�s de 
	 * la question.
	 * 
	 * @param int codeQuestion : Le code de la question
	 * @param String urlQuestion : Le URL de la question
	 * @param String reponse : La r�ponse � la question
	 */
	public Question(int codeQuestion, String urlQuestion, String reponse)
	{
		// D�finir les propri�t�s des questions
		intCodeQuestion = codeQuestion;
		strURLQuestion = urlQuestion;
		strReponse = reponse;
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
	 * 
	 * @return boolean : true si la r�ponse est valide
	 * 					 false sinon
	 */
	public boolean reponseEstValide(String reponse)
	{
		return strReponse.equals(reponse);
	}
}
