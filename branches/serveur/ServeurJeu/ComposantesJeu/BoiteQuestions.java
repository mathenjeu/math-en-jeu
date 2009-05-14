/*
 * Created on 2006-05-31
 *
 */
package ServeurJeu.ComposantesJeu;

import java.util.TreeMap;
import java.util.Vector;
import org.apache.log4j.Logger;
import ClassesUtilitaires.UtilitaireNombres;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Marc
 */
public class BoiteQuestions 
{
	static private Logger objLogger = Logger.getLogger( BoiteQuestions.class );
	private TreeMap<Integer, TreeMap<Integer, Vector<Question>>> lstQuestions;
	
	// Déclaration d'une référence vers un joueur humain correspondant à cet
	// objet d'information de partie
	private JoueurHumain objJoueurHumain;
	
	// Since there is a question box for each player, and all players might not want to play
	// in the same language, we set a language field for question boxes
	private Lang language;
	
	public BoiteQuestions(String language, String url, JoueurHumain joueur)
	{
		lstQuestions = new TreeMap<Integer, TreeMap<Integer, Vector<Question>>>();
        this.language = new Lang(language, url);
    	
        // Faire la référence vers le joueur humain courant
        objJoueurHumain = joueur;
	}// fin constructeur
	
	
	/**
	 *  This method adds a question to the question box
	 * 
	 * @param Question question : la question à ajouter
	 */
	public void ajouterQuestion( Question question )
	{
		// ajout acouet - tient en compte la categorie de la question
		int intCategorieQuestion = question.obtenirCategorie();
		int difficulte = question.obtenirDifficulte();
	
		TreeMap<Integer, Vector<Question>> difficultes = lstQuestions.get( intCategorieQuestion );
		if( difficultes == null )
		{
			difficultes = new TreeMap<Integer, Vector<Question>>();
			lstQuestions.put( intCategorieQuestion, difficultes );
		}
		
		Vector<Question> questions = difficultes.get( difficulte );
		if( questions == null )
		{
			questions = new Vector<Question>();
			difficultes.put( difficulte, questions);
		}
	
		questions.add( question );
	}
	
	/**
	 *  This method delete a used question from question box
	 * 
	 * @param Question question : question to delete 
	 */
	public void popQuestion( Question question )
	{
		// ajout acouet - tient en compte la categorie de la question
		int intCategorieQuestion = question.obtenirCategorie();
		int difficulte = question.obtenirDifficulte();
		
		TreeMap<Integer, Vector<Question>> difficultes = lstQuestions.get( intCategorieQuestion );
		Vector<Question> questions = difficultes.get( difficulte );
		System.out.println(question.obtenirCodeQuestion());
		questions.remove(question);		
	}
	
    /**
     * Cette fonction permet de sélectionner une question dans la
     * boite de questions selon sa catégorie et son niveau de difficulté
     *
     * @param int intDifficulte : la difficulte de la question
     * @param int intCategorieQuestion : la categorie de la question
     * @return Question : La question pigée
     */
	public Question pigerQuestion( int intCategorieQuestion, int intDifficulte )
	{
		// ajout acouet - tient en compte la categorie
		Question question = null;
	    Vector<Question> questions = obtenirQuestions( intCategorieQuestion, intDifficulte );
		

		// Let's choose a question among the possible ones
	    if( questions != null && questions.size() > 0 )
		{
	    	   int intRandom = UtilitaireNombres.genererNbAleatoire( questions.size() );
	    	   question = (Question)questions.elementAt( intRandom );
			   questions.remove( intRandom );
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
	 * @param int intCategorieQuestion : la categorie de la question
	 * @return boolean : si la boite est vide ou non
	 */
	public boolean estVide( int intCategorieQuestion, int intDifficulte )
	{
		boolean ret = true;
		Vector<Question> questions = obtenirQuestions( intCategorieQuestion, intDifficulte );
		
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
	 * @param int intCategorieQuestion : la categorie de la question
	 * @return Vector<Question> : un vecteur contenant les questions sélectionnées
	 */
	private Vector<Question> obtenirQuestions( int intCategorieQuestion, int intDifficulte )
	{
		Vector<Question> questions = null;
		TreeMap<Integer, Vector<Question>> difficultes = lstQuestions.get( intCategorieQuestion );	
		if( difficultes != null )
		{
			questions = difficultes.get( intDifficulte );
		}
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
