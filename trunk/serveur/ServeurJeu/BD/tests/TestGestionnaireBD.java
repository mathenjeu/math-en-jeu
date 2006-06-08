/*
 * Created on 2006-03-10
 *
 * 
 */
package ServeurJeu.BD.tests;

import java.util.TreeMap;
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
public class TestGestionnaireBD 
{
	private static void test_trouverProchaineQuestion( GestionnaireBD gBD )
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_trouverProchaineQuestion" );
		TreeMap listeQuestions = new TreeMap();
		Question q = gBD.trouverProchaineQuestion( 0, 0, listeQuestions );
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
			if( listeQuestions.get( new Integer( codeQuestion ) ) != null )
			{
				System.out.println( "La question existe deja." );
			}
			listeQuestions.put( new Integer( codeQuestion ), q );
			q = gBD.trouverProchaineQuestion( 0, 0, listeQuestions );
			if( q == null )
			{
				System.out.println( "Pas de questions disponibles" );
			}
		}
	}
	
	private static void test_joueurExiste( GestionnaireBD gBD )
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_joueurExiste" );
		boolean existe = gBD.joueurExiste( "test", "test" );
		if( existe )
		{
			System.out.println( "Joueur test existe" );
		}
		else
		{
			System.out.println( "Joueur test n'existe pas" );
		}
	}
	
	private static void test_remplirInformationsJoueur( GestionnaireBD gBD )
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_remplirInformationsJoueur" );
		JoueurHumain objJoueurHumain = new JoueurHumain(null, "test", 
				"127.0.0.1",
				"6100" );
		
		gBD.remplirInformationsJoueur( objJoueurHumain );
		
		System.out.println( objJoueurHumain.obtenirPrenom() + " " + objJoueurHumain.obtenirNomFamille() + " " + objJoueurHumain.obtenirPeutCreerSalle() );
	}
	
	private static void test_mettreAJourJoueur( GestionnaireBD gBD )
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
	}
	
	private static void test_remplirBoiteQuestions( GestionnaireBD gBD )
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_remplirBoiteQuestions" );
		BoiteQuestions boiteQuestions = new BoiteQuestions();
		gBD.remplirBoiteQuestions( boiteQuestions );
		Question q = boiteQuestions.pigerQuestion( 0, 0 );
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
			q = boiteQuestions.pigerQuestion( 0, 0 );
			if( q == null )
			{
				System.out.println( "Pas de questions disponibles" );
			}
		}
	}
	
	private static void test_arreterGestionnaireBD( GestionnaireBD gBD )
	{
		System.out.println( "_________________________________________" );
		System.out.println( "test_arreterGestionnaireBD" );
		gBD.arreterGestionnaireBD();
	}
	
	public static void main(String[] args) 
	{
		GestionnaireBD gBD = new GestionnaireBD( null );
		test_trouverProchaineQuestion( gBD );
		test_joueurExiste( gBD );
		test_remplirInformationsJoueur( gBD );
		test_mettreAJourJoueur( gBD );
		test_remplirBoiteQuestions( gBD );
		test_arreterGestionnaireBD( gBD );
	}
}
