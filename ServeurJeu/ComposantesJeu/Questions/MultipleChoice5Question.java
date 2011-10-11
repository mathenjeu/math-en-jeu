package ServeurJeu.ComposantesJeu.Questions;

import ClassesUtilitaires.UtilitaireNombres;

public class MultipleChoice5Question extends Question {
	public static final int CHOICES = 5;

	public MultipleChoice5Question(int codeQuestion, int typeQuestion, int difficulte, String urlQuestion, 
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
		// Choisir aléatoirement une mauvaise réponse
		int arrShuffle[] = new int[CHOICES];
		for(int i = 0; i < CHOICES; i++)
			arrShuffle[i] = i + 1;

		for (int x = 1; x < 10; x++)
		{
			int a = UtilitaireNombres.genererNbAleatoire(CHOICES);
			int b = UtilitaireNombres.genererNbAleatoire(CHOICES);

			int temp = arrShuffle[a];
			arrShuffle[a] = arrShuffle[b];
			arrShuffle[b] = temp;
		}
		for (int x = 1; x < CHOICES; x++)
		{
			//Character c = new Character((char)(arrShuffle[x] + 48));  // 65 for the letters 48 for the numbers
			//String strMauvaiseReponse = c.toString();

			String strMauvaiseReponse = ((Integer)(arrShuffle[x])).toString();
			if (!strMauvaiseReponse.equals(getStringAnswer().toUpperCase()))
			{
				//System.out.println("ICI mauvaise rep : "  + strMauvaiseReponse);
				return strMauvaiseReponse;
			}
		}	 

		return "Erreur";		

	}

}
