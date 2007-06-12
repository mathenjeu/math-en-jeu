package ServeurJeu.ComposantesJeu;

import ServeurJeu.Configuration.GestionnaireConfiguration;

/**
 * @author Fran�ois Gingras
 * @date Juin 2007
 *
 * Chaque joueur poss�de une instance de cette classe, qui donne les param�tres
 * propres � chaque langue. Cela sert principalement pour la bo�te de questions
 * personnelle � chaque joueur.
 *
 */

public class Langue
{
	// Le nom de la langue
	private String langue;
	
	// L'URL qui donne o� se trouvent les fichiers Flash pour les questions et r�ponses
	private String URLQuestionsReponses;
        
        // Le nom de la table de questions dans la base de donn�es
        private String nomTableQuestionsBD;
        
        // Les valeurs de cl�s minimale et maximale pour les questions
        private int cleQuestionMin;
        private int cleQuestionMax;
        
        // Le constructeur (est appel� d�s que l'on sait la langue du joueur)
	public Langue(String langue, GestionnaireConfiguration config)
	{
            this.langue = langue;
            URLQuestionsReponses = config.obtenirString("langue." + langue + ".url-questions-reponses");
            nomTableQuestionsBD = config.obtenirString("langue." + langue + ".nom-table-questions-BD");
            cleQuestionMin = config.obtenirNombreEntier("langue." + langue + ".cle-question-min");
            cleQuestionMax = config.obtenirNombreEntier("langue." + langue + ".cle-question-max");
	}
        
        public String obtenirLangue()
        {
            return langue;
        }
        
        public String obtenirURLQuestionsReponses()
        {
            return URLQuestionsReponses;
        }
        
        public String obtenirNomTableQuestionsBD()
        {
            return nomTableQuestionsBD;
        }
        
        public int obtenirCleQuestionMin()
        {
            return cleQuestionMin;
        }
        
        public int obtenirCleQuestionMax()
        {
            return cleQuestionMax;
        }
}
