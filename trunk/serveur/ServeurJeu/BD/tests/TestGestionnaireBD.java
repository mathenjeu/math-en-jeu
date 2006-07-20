/*
 * Created on 2006-03-10
 *
 * 
 */
package ServeurJeu.BD.tests;

import java.util.TreeMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.BoiteQuestions;
import ServeurJeu.ComposantesJeu.Question;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.InformationPartie;

/**
 * @author Marc
 *
 * 
 */
public class TestGestionnaireBD extends TestCase 
{
	private GestionnaireBD gBD = null;
	
	@Before public void setUp() 
	{
		gBD = new GestionnaireBD( null );
	}

	@Test public void test_joueurExiste()
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_joueurExiste" );
		boolean existe = gBD.joueurExiste( "test", "test" );
		Assert.assertTrue( existe );
	}
	
	@Test public void test_remplirInformationsJoueur()
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_remplirInformationsJoueur" );
		JoueurHumain objJoueurHumain = new JoueurHumain(null, "test", 
				"127.0.0.1",
				"6100" );
		
		gBD.remplirInformationsJoueur( objJoueurHumain );
		
		System.out.println( objJoueurHumain.obtenirPrenom() + " " + objJoueurHumain.obtenirNomFamille() + " " + objJoueurHumain.obtenirPeutCreerSalle() );
	}
	
	@Test public void test_mettreAJourJoueur()
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_mettreAJourJoueur" );
		
		JoueurHumain objJoueurHumain = new JoueurHumain(null, "test", 
				"127.0.0.1",
				"6100" );
		
		InformationPartie partie = new InformationPartie( null, gBD, objJoueurHumain, null );
		objJoueurHumain.definirPartieCourante( partie );
		
		objJoueurHumain.obtenirPartieCourante().definirPointage( 100 );
		gBD.mettreAJourJoueur( objJoueurHumain, 60 );
		
		objJoueurHumain.obtenirPartieCourante().definirPointage( 10 );
		gBD.mettreAJourJoueur( objJoueurHumain, 60 );
		
		objJoueurHumain.obtenirPartieCourante().definirPointage( 500 );
		gBD.mettreAJourJoueur( objJoueurHumain, 60 );
		
		Assert.assertTrue( true );
	}
	
	@Test public void test_remplirBoiteQuestions()
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_remplirBoiteQuestions" );
		BoiteQuestions boiteQuestions = new BoiteQuestions();
		gBD.remplirBoiteQuestions( boiteQuestions );
		Question q = boiteQuestions.pigerQuestion( 1, 1 );
		int nbQuestion = 0;
		while( q != null )
		{
			nbQuestion++;
			int codeQuestion = q.obtenirCodeQuestion();
			String question = q.obtenirURLQuestion();
			String explication = q.obtenirURLExplication();
			System.out.println( "Code question " + nbQuestion +  " : " + codeQuestion );
			System.out.println( "Url question : " + question );
			System.out.println( "Url explication : " + explication );
			q = boiteQuestions.pigerQuestion( 1, 1 );
			if( q == null )
			{
				System.out.println( "Pas de questions disponibles" );
			}
		}
		Assert.assertTrue( true );
	}
	
	@Test public void test_arreterGestionnaireBD()
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_arreterGestionnaireBD" );
		gBD.arreterGestionnaireBD();
		
		Assert.assertTrue( true );
	}
	
	public static junit.framework.Test suite() 
	{
		return new TestSuite(TestGestionnaireBD.class);
	}
	
	
	public static void main(String[] args) 
	{
		junit.textui.TestRunner.run(suite());
	}
}
