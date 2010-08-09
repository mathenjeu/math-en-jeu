package ServeurJeu.ComposantesJeu;

import ClassesUtilitaires.UtilitaireNombres;

/**
 * @author Jean-François Brind'Amour
 */
public class Question
{
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
     *  Déclaration d'une variable qui va contenir la catégorie de la question
     * 
     */
	private final int intCategorie;
	
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
	 * @param int codeQuestion : Le code de la question
	 * @param String typeQuestion : Le type de la question
	 * @param int difficulte : La difficulte de la question - entre 0 et 6
	 * @param String urlQuestion : Le URL de la question
	 * @param String reponse : La réponse à la question
	 * @param String urlExplication : Le URL de l'explication de la réponse
	 */
	public Question(int codeQuestion, int typeQuestion, int difficulte, String urlQuestion, String reponse, String urlExplication, int categorie )
	{
		// Définir les propriétés des questions
		intCodeQuestion = codeQuestion;
		objTypeQuestion = typeQuestion;
		intDifficulte = difficulte;
		strURLQuestion = urlQuestion;
		strReponse = reponse.toLowerCase().replace(",",".");
		strURLExplication = urlExplication;
		intCategorie = categorie;
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
	 * 
	 * @return boolean : true si la réponse est valide
	 * 					 false sinon
	 */
	public boolean reponseEstValide(String reponse)
	{
		// standartisation des réponses
		String tempStr = reponse.trim().toLowerCase();
		tempStr = tempStr.replace("ç","c").replace("ù","u");
		tempStr = tempStr.replace("û", "u").replace("ô","o").replace("ò","o");
		tempStr = tempStr.replace("é", "e").replace("ê","e").replace("è","e");
		tempStr = tempStr.replace("à", "a").replace("â","a").replace(".",",");
		tempStr = tempStr.replace("ï", "i").replace("î","i");
		tempStr = tempStr.replace(",",".");
		//System.out.println("La reponse : " + reponse + " Est la rep dans BD : " + strReponse);
		return strReponse.equals(tempStr);
	}
	
	
	/**
	 * Cette fonction retourne une mauvaise réponse. Utilisé lorsqu'un
	 * joueur utilise l'objet "Livre" qui permet d'éliminer un choix
	 * de réponse. Dans le cas d'une question sans choix de réponse, la 
	 * fonction retourne "PasUnChoixDeReponse"
	
	 public  String obtenirMauvaiseReponse()
	 {
		// Choisir aléatoirement une mauvaise réponse
	 	
		int nbChoix = 0;
	 	if(objTypeQuestion == 1)
	 			nbChoix = 4;
	 	else if(objTypeQuestion == 5)
	 			nbChoix = 3;
	 	else if(objTypeQuestion == 4)
	 			nbChoix = 5; 
	 	
	 	// Vérifier si la réponse est un choix de réponse
	 	if (nbChoix > 2 && nbChoix < 6 )
	 	{ 		
	 		
	 	    int arrShuffle[] = new int[nbChoix];
	 	    for(int i = 0; i < nbChoix; i++)
	 	    	arrShuffle[i] = i + 1;
	 	    
	 	    for (int x = 1; x < 10; x++)
	 	    {
	 	    	int a = UtilitaireNombres.genererNbAleatoire(nbChoix);
	 	    	int b = UtilitaireNombres.genererNbAleatoire(nbChoix);
	 	    	
	 	    	int temp = arrShuffle[a];
	 	    	arrShuffle[a] = arrShuffle[b];
	 	    	arrShuffle[b] = temp;
	 	    }
	 	    for (int x = 1; x < nbChoix; x++)
	 	    {
	 	    	//Character c = new Character((char)(arrShuffle[x] + 48));  // 65 for the letters 48 for the numbers
	 	    	//String strMauvaiseReponse = c.toString();
	 	    	
	 	    	String strMauvaiseReponse = ((Integer)(arrShuffle[x])).toString();
	 	    	if (!strMauvaiseReponse.equals(strReponse.toUpperCase()))
	 	    	{
	 	    		//System.out.println("ICI mauvaise rep : "  + strMauvaiseReponse);
	 	    		return strMauvaiseReponse;
	 	    	}
	 	    }	 
	 	    
	 	    return "Erreur";		
	 	}
	 	else
	 	{
	 		return "PasUnChoixDeReponse";
	 	}
	 } */
	 
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
	

	public int obtenirCategorie() 
	{
		return intCategorie;
	}

		
} // fin classe
