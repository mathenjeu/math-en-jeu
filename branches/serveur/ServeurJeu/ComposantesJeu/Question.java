package ServeurJeu.ComposantesJeu;

import ClassesUtilitaires.UtilitaireNombres;

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
     *  D�claration d'une variable qui va contenir la cat�gorie de la question
     * 
     */
	private final int intCategorie;
	
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
	public Question(int codeQuestion, int typeQuestion, int difficulte, String urlQuestion, String reponse, String urlExplication, int categorie )
	{
		// D�finir les propri�t�s des questions
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
	 * 
	 * @return boolean : true si la r�ponse est valide
	 * 					 false sinon
	 */
	public boolean reponseEstValide(String reponse)
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
		return strReponse.equals(tempStr);
	}
	
	
	/**
	 * Cette fonction retourne une mauvaise r�ponse. Utilis� lorsqu'un
	 * joueur utilise l'objet "Livre" qui permet d'�liminer un choix
	 * de r�ponse. Dans le cas d'une question sans choix de r�ponse, la 
	 * fonction retourne "PasUnChoixDeReponse"
	
	 public  String obtenirMauvaiseReponse()
	 {
		// Choisir al�atoirement une mauvaise r�ponse
	 	
		int nbChoix = 0;
	 	if(objTypeQuestion == 1)
	 			nbChoix = 4;
	 	else if(objTypeQuestion == 5)
	 			nbChoix = 3;
	 	else if(objTypeQuestion == 4)
	 			nbChoix = 5; 
	 	
	 	// V�rifier si la r�ponse est un choix de r�ponse
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
	 * Cette fonction retourne le URL de l'explication de la r�ponse � la 
	 * question courante.
	 * 
	 * @return String : Le URL de l'explication de la r�ponse
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
