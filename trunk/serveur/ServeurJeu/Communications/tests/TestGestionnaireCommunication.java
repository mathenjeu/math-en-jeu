/*
 * Created on 2006-05-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.Communications.tests;

import ServeurJeu.Communications.GestionnaireCommunication;

/**
 * @author Marc
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestGestionnaireCommunication 
{

	private static void test_miseAJourInfo( GestionnaireCommunication gCom )
	{
		System.out.println( "test_miseAJourInfo" );
		gCom.miseAJourInfo();
	}
	
	public static void main(String[] args) 
	{
		System.out.println( "Debut des tests" );
		GestionnaireCommunication gCom = new GestionnaireCommunication( null, null, null, null, null );
		test_miseAJourInfo( gCom );
		System.out.println( "Fin des tests" );
	}
}
