package ServeurJeu.ComposantesJeu;

/**
 * @author Jean-François Brind'Amour
 */
public class Question
{
	// Déclaration d'une variable qui va contenir le code de la question
	private int intCodeQuestion;
	
	// Déclaration d'une variable qui va contenir l'URL de la question
	private String strURLQuestion;
	
	// Déclaration d'une variable qui va contenir la réponse à la question
	private String strReponse;
	
	/**
	 * Constructeur de la classe Question qui initialise les propriétés de 
	 * la question.
	 * 
	 * @param int codeQuestion : Le code de la question
	 * @param String urlQuestion : Le URL de la question
	 * @param String reponse : La réponse à la question
	 */
	public Question(int codeQuestion, String urlQuestion, String reponse)
	{
		// Définir les propriétés des questions
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
	 * Cette fonction retourne si oui ou non la réponse est valide.
	 * 
	 * @return boolean : true si la réponse est valide
	 * 					 false sinon
	 */
	public boolean reponseEstValide(String reponse)
	{
		return strReponse.equals(reponse);
	}
}
