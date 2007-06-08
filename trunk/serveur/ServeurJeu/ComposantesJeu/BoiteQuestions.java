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
import ServeurJeu.Configuration.GestionnaireMessages;

/**
 * @author Marc
 *
 * 
 */
public class BoiteQuestions 
{
	static private Logger objLogger = Logger.getLogger( BoiteQuestions.class );
	private TreeMap<Integer, TreeMap<Integer, Vector<Question>>> lstQuestions;
	
	public BoiteQuestions()
	{
		lstQuestions = new TreeMap<Integer, TreeMap<Integer, Vector<Question>>>();
	}
	
	public void ajouterQuestion( Question question )
	{
		int intCategorieQuestion = 1;// = question.obtenirCategorie();
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
	
	public Question pigerQuestion( int intCategorieQuestion, int intDifficulte )
	{
		int intPointageQuestion = intDifficulte;
		intCategorieQuestion = 1;
		
		Question question = null;
	    Vector<Question> questions = obtenirQuestions( intCategorieQuestion, intDifficulte );
		

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
		
		return question;
	}
	
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
