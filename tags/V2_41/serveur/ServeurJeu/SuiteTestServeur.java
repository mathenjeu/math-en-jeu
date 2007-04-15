package ServeurJeu;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import ServeurJeu.BD.tests.TestGestionnaireBD;
import ServeurJeu.Communications.tests.TestGestionnaireCommunication;
import ServeurJeu.Configuration.tests.TestGestionnaireConfiguration;

public class SuiteTestServeur extends TestCase
{
	public static junit.framework.Test suite() 
	{
		TestSuite suite= new TestSuite("Tous les tests unitaires");
		suite.addTest(TestGestionnaireCommunication.suite());
		suite.addTest(TestGestionnaireBD.suite());
		suite.addTest(TestGestionnaireConfiguration.suite());
		return suite;
	}

	public static void main(String[] args)
	{
		junit.textui.TestRunner.run (suite());
	}

}
