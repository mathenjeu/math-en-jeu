/*
 * Created on 2006-05-31
 *
 * Last change 06.05.2010 Oloieri Lilian
 */
package ServeurJeu.ComposantesJeu;

import java.util.LinkedList;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import ClassesUtilitaires.UtilitaireNombres;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Marc
 */
public class BoiteQuestions 
{
	static private Logger objLogger = Logger.getLogger( BoiteQuestions.class );
	private TreeMap<Integer, LinkedList<Question>> lstQuestions;
		
	// Since there is a question box for each player, and all players might not want to play
	// in the same language, we set a language field for question boxes
	private final Lang language;
	
	public BoiteQuestions(String language, String url)
	{
		lstQuestions = new TreeMap<Integer, LinkedList<Question>>();
        this.language = new Lang(language, url);
    	       
	}// fin constructeur
	
	
	/**
	 *  This method adds a question to the question box
	 * 
	 * @param Question question : la question à ajouter
	 */
	public void ajouterQuestion( Question question )
	{
		int difficulte = question.obtenirDifficulte();
					
		LinkedList<Question> questions = lstQuestions.get( difficulte );
		if( questions == null )
		{
			questions = new LinkedList<Question>();
			lstQuestions.put( difficulte, questions);
		}
	
		System.out.println("Boite question : " + question.obtenirCodeQuestion() + " diff: " + question.obtenirDifficulte());
		questions.add( question );
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
		
		Question question = null;
		
		LinkedList<Question> questions = lstQuestions.get(intDifficulte);
		
		// Let's choose a question among the possible ones
	    if( questions != null && questions.size() > 0 )
		{
	    	   int intRandom = UtilitaireNombres.genererNbAleatoire( questions.size() );
	    	   question = (Question)questions.get( intRandom );
			   
		}
		else
		{
			objLogger.error(GestionnaireMessages.message("boite.pas_de_question"));
		}
		//System.out.println("\nquestion : " + question.obtenirCodeQuestion()+"\n");
		return question;
	}
	
	/**
     * Cette fonction permet de sélectionner une question dans la
     * boite de questions selon son niveau de difficulté
     *
     * @param int intDifficulte : la difficulte de la question
     * @param int intCategorieQuestion : la categorie de la question
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
	    		questions.remove( intRandom );
	    		limit++;

	    	}while(question.obtenirCodeQuestion() == oldQuestionId || limit > 10);
		}
		else
		{
			objLogger.error(GestionnaireMessages.message("boite.pas_de_question"));
		}
		
	   return question;
	}
	

	/**
	 * Cette fonction permet de determiner si la boite a question
	 * est vide pour une certaine difficulte et catégorie
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
	 * Cette fonction permet de retourner toutes les questions 
	 * correspondant aux paramètres (difficulte, categorie)
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
    public Lang obtenirLangue()
    {
        return language;
    }
}
