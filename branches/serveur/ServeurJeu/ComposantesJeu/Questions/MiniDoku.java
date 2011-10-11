package ServeurJeu.ComposantesJeu.Questions;


public class MiniDoku extends Question {

	public MiniDoku(int codeQuestion, int typeQuestion, int difficulte, String urlQuestion, 
			String reponse, String urlExplication)
	{
		super(codeQuestion, typeQuestion, difficulte, urlQuestion, reponse, urlExplication);
	}

	/**
	 * Cette fonction retourne si oui ou non la réponse est valide.
   	 * @return true si la réponse est valide
	 *         false sinon
	 */
	public boolean reponseEstValide(String reponse)
	{
            System.out.println("Reponse du joueur: " + reponse);
            return Boolean.parseBoolean(reponse);
	}
}
