package ServeurJeu.ComposantesJeu.Questions;

public class TrueFalseQuestion extends Question {

	public TrueFalseQuestion(int codeQuestion, int typeQuestion, int difficulte, String urlQuestion, 
			String reponse, String urlExplication)//, LinkedList<Integer> keywords)
	{
		super(codeQuestion, typeQuestion, difficulte, urlQuestion, reponse, urlExplication);
	}

	/**
	 * Cette fonction retourne une mauvaise réponse. Utilisé lorsqu'un
	 * joueur utilise l'objet "Livre" qui permet d'éliminer un choix
	 * de réponse. Dans le cas d'une question sans choix de réponse, la 
	 * fonction retourne "PasUnChoixDeReponse"
	 */
	public  String obtenirMauvaiseReponse()
	{
		return "PasUnChoixDeReponse";
	}
}
