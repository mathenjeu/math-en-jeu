package ServeurJeu.ComposantesJeu;

/**
 * @author Fran�ois Gingras
 * @date Juin 2007
 *
 * Chaque joueur poss�de une instance de cette classe, qui donne les param�tres
 * propres � chaque langue. Cela sert principalement pour la bo�te de questions
 * personnelle � chaque joueur.
 *
 */

public class Lang
{
	// Le nom de la langue
	private String lang;
	
	// L'URL qui donne o� se trouvent les fichiers Flash pour les questions et r�ponses
	private String URLQuestionsAnswers;
        
    
    // The constructor is called as soon as we know the player's language
    // The language is obtained in the method's arguments, and the rest is obtained from the server config file
	public Lang(String langue, String url)
	{
            this.lang = langue;
            URLQuestionsAnswers = url;
           
	}
        // getters....
        public String getLanguage()
        {
            return lang;
        }
        
        public String getURLQuestionsAnswers()
        {
            return URLQuestionsAnswers;
        }
        
             
}// end class
