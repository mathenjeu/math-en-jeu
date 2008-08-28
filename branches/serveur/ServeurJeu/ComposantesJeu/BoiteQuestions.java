/*
 * Created on 2006-05-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.ComposantesJeu;


import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import ClassesUtilitaires.UtilitaireNombres;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.Configuration.GestionnaireMessages;
import org.w3c.dom.Node;

/**
 * @author Marc
 *
 * 
 */
public class BoiteQuestions 
{
	static private Logger objLogger = Logger.getLogger( BoiteQuestions.class );
	private TreeMap<Integer, TreeMap<Integer, Vector<Question>>> lstQuestions;
	
	// DÈclaration d'une rÈfÈrence vers un joueur humain correspondant ‡ cet
	// objet d'information de partie
	private JoueurHumain objJoueurHumain;
	

	    // Since there is a question box for each player, and all players might not want to play
	    // in the same language, we set a language field for question boxes
	    private Langue langue;
	
	public BoiteQuestions(String langue, Node noeudLangue, String nomSalle, JoueurHumain joueur)
	{
		lstQuestions = new TreeMap<Integer, TreeMap<Integer, Vector<Question>>>();
                this.langue = new Langue(langue, noeudLangue, nomSalle);
    	
        // Faire la rÈfÈrence vers le joueur humain courant
        objJoueurHumain = joueur;
	}
	
	/**
	 *  This method adds a question to the question box
	 * 
	 * @param Question question : la question à ajouter
	 */
	public void ajouterQuestion( Question question )
	{
		//int intCategorieQuestion = 1;
		// ajout acouet - tient en compte la categorie de la question
		int intCategorieQuestion = question.obtenirCategorie();
		int intSujetQuestion = question.obtenirSujet();
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
     * Cette fonction permet de sélectionner une question dans la
     * boite de questions selon sa catégorie et son niveau de
     * difficulté
     *
     * @param int intDifficulte : la difficulte de la question
     * @param int intCategorieQuestion : la categorie de la question
     * @return Question : La question pigée
     */
	public Question pigerQuestion( int intCategorieQuestion, int intDifficulte )
	{
		int intPointageQuestion = intDifficulte;
		int i = 0;
		boolean questionOK = false;
		
		// ajout acouet - tient en compte la categorie
		//intCategorieQuestion = 1;
		
		Question question = null;
	    Vector<Question> questions = obtenirQuestions( intCategorieQuestion, intDifficulte );
		
		
//			int intRandom;
//			do
//			{
//				intRandom = UtilitaireNombres.genererNbAleatoire( questions.size() );
//				question = (Question)questions.elementAt( intRandom );
//				
//				System.out.println("sujet : " + question.obtenirSujet());
//				
//				// le sujet de l'histoire a bien 6 comme ID ?
//				if(question.obtenirSujet() != 6 || objJoueurHumain.peutPoserQuestionHistoire())
//				{
//					questionOK = true;
//				}
//				
//				i++;
//				
//			} while(i < 10 && questionOK != true);
			
			// Let's choose a question among the possible ones
	    	
	    if( questions != null && questions.size() > 0 )
		{
	    	int intRandom = UtilitaireNombres.genererNbAleatoire( questions.size() );
	    	question = (Question)questions.elementAt( intRandom );
			questions.remove( intRandom );
                        
			question.definirDifficulte(intPointageQuestion);
		}
		else
		{
			objLogger.error(GestionnaireMessages.message("boite.pas_de_question"));
		}
		
		//System.out.println("ds piger question - categorie : " + question.obtenirCategorie());
		
		return question;
	}
	

	/**
	 * Cette fonction permet de determiner si la boite a question
	 * est vide pour une certaine difficulte et catégorie
	 * -----  Ne semble pas être appelée pour l'instant  -----
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
    public Langue obtenirLangue()
    {
        return langue;
    }
}
