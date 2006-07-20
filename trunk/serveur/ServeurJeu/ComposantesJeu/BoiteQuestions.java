/*
 * Created on 2006-05-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.ComposantesJeu;


import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import ClassesUtilitaires.UtilitaireNombres;

/**
 * @author Marc
 *
 * TODO Tenir compte du niveau de difficulté des questions, et de la catégorie
 */
public class BoiteQuestions 
{
	private TreeMap<Integer, TreeMap<Integer, Vector<Question>>> lstQuestions;
	
	public BoiteQuestions()
	{
		lstQuestions = new TreeMap<Integer, TreeMap<Integer, Vector<Question>>>();
	}
	
	public void ajouterQuestion( Question question )
	{
		int intCategorieQuestion = 1;
		int difficulte = question.obtenirDifficulte();
		
		TreeMap<Integer, Vector<Question>> difficultes = lstQuestions.get( intCategorieQuestion );
		
		if( difficultes == null )
		{
			difficultes = new TreeMap<Integer, Vector<Question>>();
			lstQuestions.put( intCategorieQuestion, difficultes );
			difficultes.put( difficulte, new Vector<Question>());
		}
		Vector<Question> questions = difficultes.get( difficulte );
		questions.add( question );
	}
	
	public Question pigerQuestion( int intCategorieQuestion, int intDifficulte )
	{
		intCategorieQuestion = 1;
		Question question = null;
		Vector<Question> questions = obtenirQuestions( intCategorieQuestion, intDifficulte );
		
		if( questions != null )
		{
			if( questions.size() > 0 )
			{
				int intRandom = UtilitaireNombres.genererNbAleatoire( questions.size() );
				question = (Question)questions.elementAt( intRandom );
				questions.remove( intRandom );
			}
		}
		else
		{
					
		}
		
		return question;
	}
	
	public boolean estVide( int intCategorieQuestion, int intDifficulte )
	{
		boolean ret = true;
		Vector<Question> questions = obtenirQuestions( intCategorieQuestion, intDifficulte );
		
		if( questions != null )
		{
			ret = questions.size() == 0;
		}
		else
		{
			
		}
		
		return ret;
	}
	
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
}
