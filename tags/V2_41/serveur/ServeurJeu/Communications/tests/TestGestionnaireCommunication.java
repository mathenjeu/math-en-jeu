/*
 * Created on 2006-05-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ServeurJeu.Communications.tests;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.BD.tests.TestGestionnaireBD;
import ServeurJeu.Communications.GestionnaireCommunication;

/**
 * @author Marc
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestGestionnaireCommunication  extends TestCase
{
	private GestionnaireCommunication gCom = null;
	
	@Before public void setUp() 
	{
		gCom = new GestionnaireCommunication( null, null, null, null, null );
	}

	@Test public void  test_miseAJourInfo()
	{
		System.out.println( "test_miseAJourInfo" );
		gCom.miseAJourInfo();
	}
	
	public static junit.framework.Test suite() 
	{
		return new TestSuite(TestGestionnaireCommunication.class);
	}
	
	
	public static void main(String[] args) 
	{
		junit.textui.TestRunner.run(suite());
	}
}
