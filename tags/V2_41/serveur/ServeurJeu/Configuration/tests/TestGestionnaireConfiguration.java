package ServeurJeu.Configuration.tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Before;
import org.junit.Test;

import ServeurJeu.Communications.tests.TestGestionnaireCommunication;
import ServeurJeu.Configuration.GestionnaireConfiguration;

public class TestGestionnaireConfiguration  extends TestCase
{
	private GestionnaireConfiguration config = null;
	
	@Before public void setUp() 
	{
		config = GestionnaireConfiguration.obtenirInstance();
	}
	
	@Test public void test_obtenirNombreEntier()
	{
		int i = config.obtenirNombreEntier( "controleurjeu.synchro.step" );
		assert i == 30;
		System.out.println( i );
	}
	
	@Test public void test_obtenirString( )
	{
		String s = config.obtenirString( "controleurjeu.info.fichier-sortie" );
		assert s.equals( "Espion.txt" );
		System.out.println( s );
	}
	
	@Test public void test_obtenirNombreDecimal()
	{
		float f = config.obtenirNombreDecimal( "controleurjeu.salles-initiales.regles.ratio-magasins" );
		assert f == 0.05f;
		System.out.println( f );
	}
	
	@Test public void test_obtenirValeurBooleenne()
	{
		boolean b = config.obtenirValeurBooleenne( "controleurjeu.salles-initiales.regles.chat" );
		assert b == true;
		System.out.println( b );
	}
	
	public static junit.framework.Test suite() 
	{
		return new TestSuite(TestGestionnaireConfiguration.class);
	}
	
	
	public static void main(String[] args) 
	{
		junit.textui.TestRunner.run(suite());
	}
}
