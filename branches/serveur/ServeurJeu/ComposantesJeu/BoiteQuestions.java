/*
 * Created on 2006-05-31
 *
 * Last change 06.05.2010 Oloieri Lilian
 */
package ServeurJeu.ComposantesJeu;

import java.util.HashMap;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import ClassesUtilitaires.UtilitaireNombres;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Marc changed Oloieri Lilian
 * 
 */
public class BoiteQuestions 
{
	private static Logger objLogger = Logger.getLogger( BoiteQuestions.class );
	private HashMap<Integer, LinkedList<Question>> lstQuestions;
	
		
	// Since there is a question box for each player, and all players might not want to play
	// in the same language, we set a language field for question boxes
	private final Language language;
	
	public BoiteQuestions(String language, String url)
	{
		lstQuestions = new HashMap<Integer, LinkedList<Question>>();
        this.language = new Language(language, url);
    	       
	}// fin constructeur
	
	
	/**
	 *  This method adds a question to the question box
	 * 
	 * @param Question question : la question à ajouter
	 */
	public void ajouterQuestion( Question question)
	{
		int difficulte = question.obtenirDifficulte();
							
		LinkedList<Question> questions = lstQuestions.get( difficulte );
		if( questions == null )
		{
			questions = new LinkedList<Question>();
			lstQuestions.put( difficulte, questions);
		}
	
		
		//System.out.println("Boite question : " + question.obtenirCodeQuestion() + " diff: " + question.obtenirDifficulte());
		questions.addLast(question );
	}
	
	/**
	 *  This method delete a used question from question box
	 * 
	 * @param Question question : question to delete 
	 */
	public void popQuestion( Question question )
	{
		int difficulte = question.obtenirDifficulte();
		LinkedList<Question> questions = lstQuestions.get( difficulte );
		//System.out.println(question.obtenirCodeQuestion());
		questions.remove(question);		
	}
	
		
	/**
     * Cette fonction permet de sélectionner une question dans la
     * boite de questions selon son niveau de difficulté
     *
     * @param int intDifficulte : la difficulte de la question
     * @return Question : La question pigée
     */
	public Question pigerQuestion(int intDifficulte)
	{
		//System.out.println("Question1: " + System.currentTimeMillis());
		Question question = null;
		
		LinkedList<Question> questions = lstQuestions.get(intDifficulte);
						
		// Let's choose a question among the possible ones
	    if( questions != null && questions.size() > 0 )
		{
	    	   question = (Question)(questions.get(UtilitaireNombres.genererNbAleatoire(questions.size())));
	    	   
    	}
		else
		{
			objLogger.error(GestionnaireMessages.message("boite.pas_de_question"));
		}
		
	    questions.remove(question);
		//System.out.println("Question2: " + System.currentTimeMillis());
	    //System.out.println("\nquestion : " + question.obtenirCodeQuestion()+ "  " + lstQuestions.containsValue(question) +  " " + questions.indexOf(question) + "\n");
		return question;
	}
	
	/**
     * Cette fonction permet de sélectionner une question dans la
     * boite de questions selon son niveau de difficulté
     *
     * @param int intDifficulte : la difficulte de la question
     * @return Question : La question pigée
     */
	public Question pigerQuestionCristall( int intDifficulte, int oldQuestionId )
	{
		
		Question question = null;
		
		LinkedList<Question> questions = lstQuestions.get(intDifficulte);
		
		// Let's choose a question among the possible ones
	    if( questions != null && questions.size() > 0 )
		{
	    	int limit = 0;
	    	do{   
	    		int intRandom = UtilitaireNombres.genererNbAleatoire( questions.size() );
	    		question = (Question)questions.get( intRandom );
	    		//to not take the same question twice
	    		questions.remove(question);	//questions.remove( intRandom );
	    		limit++;

	    	}while(question.obtenirCodeQuestion() == oldQuestionId || limit > 10);
		}
		else
		{
			objLogger.error(GestionnaireMessages.message("boite.pas_de_question"));
		}
	
	   //popQuestion(question); 
	   return question;
	}
	

	/**
	 * Cette fonction permet de determiner si la boite a question
	 * est vide pour une certaine difficulte 
	 * -----  Ne semble pas ètre appelée pour l'instant  -----
	 *
	 * @param int intDifficulte : la difficulte de la question
	 * @return boolean : si la boite est vide ou non
	 */
	public boolean estVide( int intDifficulte )
	{
		boolean ret = true;
		LinkedList<Question> questions = obtenirQuestions( intDifficulte );
		
		if( questions != null )
		{
			ret = ( questions.size() == 0 );
		}
		else
		{
			objLogger.error(GestionnaireMessages.message("boite.pas_de_question"));
		}
		
		return ret;
	}
	
	/**
	 * Cette fonction permet de determiner si la boite a question
	 * est vide en general
	 * -----  Ne semble pas ètre appelée pour l'instant  -----
	 *
	 * @param int intDifficulte : la difficulte de la question
	 * @return boolean : si la boite est vide ou non
	 */
	public boolean estVide()
	{
		boolean ret = false;
		//LinkedList<Question> questions = obtenirQuestions( intDifficulte );
		
		
		if(lstQuestions.isEmpty())
		{
			ret = true;
			objLogger.error(GestionnaireMessages.message("boite.pas_de_question"));
		}
				
		return ret;
	}
	
	
	/**
	 * Cette fonction retourne une mauvaise réponse. Utilisé lorsqu'un
	 * joueur utilise l'objet "Livre" qui permet d'éliminer un choix
	 * de réponse. Dans le cas d'une question sans choix de réponse, la 
	 * fonction retourne "PasUnChoixDeReponse"
	 */
	 public  String obtenirMauvaiseReponse(Question questo)
	 {
		// Choisir aléatoirement une mauvaise réponse
		int objTypeQuestion = questo.obtenirTypeQuestion();
		
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
	 	    	if (!strMauvaiseReponse.equals(questo.getStringAnswer().toUpperCase()))
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
	 }


	/**
	 * Cette fonction permet de retourner toutes les questions 
	 * correspondant aux paramètres (difficulte)
	 *
	 * @param int intDifficulte : la difficulte de la question
	 * @return LinkedList<Question> : un vecteur contenant les questions sélectionnées
	 */
	private LinkedList<Question> obtenirQuestions( int intDifficulte )
	{
		LinkedList<Question> questions = lstQuestions.get(intDifficulte);
				
		return questions;
	}
     

	/**
	 * Cette fonction retourne la langue
	 *
	 * @return Langue : la langue
	 */
    public Language obtenirLangue()
    {
        return language;
    }
}
