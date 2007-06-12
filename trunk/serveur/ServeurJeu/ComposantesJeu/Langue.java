package ServeurJeu.ComposantesJeu;

import ServeurJeu.Configuration.GestionnaireConfiguration;

/**
 * @author François Gingras
 * @date Juin 2007
 *
 * Chaque joueur possède une instance de cette classe, qui donne les paramètres
 * propres à chaque langue. Cela sert principalement pour la boîte de questions
 * personnelle à chaque joueur.
 *
 */

public class Langue
{
	// Le nom de la langue
	private String langue;
	
	// L'URL qui donne où se trouvent les fichiers Flash pour les questions et réponses
	private String URLQuestionsReponses;
        
        // Le nom de la table de questions dans la base de données
        private String nomTableQuestionsBD;
        
        // Les valeurs de clés minimale et maximale pour les questions
        private int cleQuestionMin;
        private int cleQuestionMax;
        
        // Le constructeur (est appelé dès que l'on sait la langue du joueur)
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
