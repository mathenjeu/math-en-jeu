package ServeurJeu.Configuration.tests;

import ServeurJeu.Configuration.GestionnaireConfiguration;

public class TestGestionnaireConfiguration 
{
	static private void test_obtenirNombreEntier( GestionnaireConfiguration config )
	{
		int i = config.obtenirNombreEntier( "controleurjeu.synchro.step" );
		assert i == 30;
		System.out.println( i );
	}
	
	static private void test_obtenirString( GestionnaireConfiguration config )
	{
		String s = config.obtenirString( "controleurjeu.info.fichier-sortie" );
		assert s.equals( "Espion.txt" );
		System.out.println( s );
	}
	
	public static void main(String[] args)
	{
		GestionnaireConfiguration config = GestionnaireConfiguration.obtenirInstance();
		test_obtenirNombreEntier( config );
		test_obtenirString( config );
	}
}
