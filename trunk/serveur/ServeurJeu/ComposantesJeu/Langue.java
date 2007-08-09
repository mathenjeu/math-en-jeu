package ServeurJeu.ComposantesJeu;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        
        // The constructor is called as soon as we know the player's language
        // The language is obtained in the method's arguments, and the rest is obtained from the server config file
	public Langue(String langue, Node noeudLangue, String nomSalle)
	{
            this.langue = langue;
            NodeList listeDeLangues = noeudLangue.getChildNodes();
            for(int i=0; i<listeDeLangues.getLength(); i++)
            {
                // If it's the kind of node we want and it's the right language...
                if(listeDeLangues.item(i).getNodeType()==1 && listeDeLangues.item(i).getNodeName().equals(langue))
                {
                    NodeList listeDeParametres = listeDeLangues.item(i).getChildNodes();
                    for(int j=0; j<listeDeParametres.getLength(); j++)
                    {
                        if(listeDeParametres.item(j).getNodeType()==1)
                        {
                            if(listeDeParametres.item(j).getNodeName().equals("url-questions-reponses")) URLQuestionsReponses = listeDeParametres.item(j).getTextContent();
                            else if(listeDeParametres.item(j).getNodeName().equals("nom-table-questions-BD")) nomTableQuestionsBD = listeDeParametres.item(j).getTextContent();
                            else if(listeDeParametres.item(j).getNodeName().equals("cle-question-min")) cleQuestionMin = Integer.parseInt(listeDeParametres.item(j).getTextContent());
                            else if(listeDeParametres.item(j).getNodeName().equals("cle-question-max")) cleQuestionMax = Integer.parseInt(listeDeParametres.item(j).getTextContent());
                        }
                    }
                }
            }
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
