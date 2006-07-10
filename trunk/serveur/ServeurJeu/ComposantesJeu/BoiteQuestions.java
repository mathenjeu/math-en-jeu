/*
 * Created on 2006-05-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.ComposantesJeu;


import java.util.Vector;
import ClassesUtilitaires.UtilitaireNombres;

/**
 * @author Marc
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BoiteQuestions 
{
	private Vector <Question>lstQuestions;
	
	public BoiteQuestions()
	{
		lstQuestions = new Vector();
	}
	
	public void ajouterQuestion( Question question )
	{
		lstQuestions.add( question );
	}
	
	public Question pigerQuestion( int intCategorieQuestion, int intDifficulte )
	{
		Question question = null;
		if( lstQuestions.size() > 0  )
		{
			int intRandom = UtilitaireNombres.genererNbAleatoire( lstQuestions.size() );
			question = (Question)lstQuestions.elementAt( intRandom );
			lstQuestions.remove( intRandom );
			
			// Fix temporaire pour la difficulté de la question
		    question.definirDifficulte(intDifficulte);
		}
		
		return question;
	}
	
	public boolean estVide()
	{
		return lstQuestions.size() == 0;
	}
}
