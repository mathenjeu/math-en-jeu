package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Enumerations.TypeQuestion;
import ServeurJeu.ComposantesJeu.Joueurs.InformationPartieHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Questions.Question;

/**
 * @author Jean-François Brind'Amour
 */
public class Boule extends ObjetUtilisable 
{
	// Cette constante spécifie le prix de l'objet courant
	public static final int PRIX = 1;
	
	// Cette constante affirme que l'objet courant n'est pas limité 
	// lorsqu'on l'achète (c'est-à-dire qu'un magasin n'épuise jamais 
	// son stock de cet objet)
	public static final boolean EST_LIMITE = false;
	
	// Cette constante affirme que l'objet courant ne peut être armé 
	// et déposé sur une case pour qu'un autre joueur tombe dessus. Elle 
	// ne peut seulement être utilisée immédiatement par le joueur
	public static final boolean PEUT_ETRE_ARME = false;
	
	// Cette constante définit le nom de cet objet
	public static final String TYPE_OBJET = "Boule";
	
	/**
	 * Constructeur de la classe qui permet de définir les propriétés 
	 * propres à l'objet courant.
	 *
	 * @param in id : Le numéro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit être 
	 * 							   visible ou non
	 */
	public Boule(int id, boolean estVisible)
	{
		// Appeler le constructeur du parent
		super(id, estVisible, UID_OU_BOULE, PRIX, EST_LIMITE, PEUT_ETRE_ARME, TYPE_OBJET);
	}
	
	public void useObject(Element noeudCommande, String playerName, JoueurHumain objJoueurHumain)
	{
		InformationPartieHumain infoPartie = objJoueurHumain.obtenirPartieCourante();
		 System.out.println("we are in objet boule");
		 Document docSortie = noeudCommande.getOwnerDocument();
		 
		 // La boule permettra à un joueur de changer de question si celle
         // qu'il s'est fait envoyer ne lui tente pas
         Question nouvelleQuestion = infoPartie.trouverQuestionAPoserCristall(true);

         // On prépare l'envoi des informations sur la nouvelle question
         Element objNoeudParametreNouvelleQuestion = docSortie.createElement("parametre");
         objNoeudParametreNouvelleQuestion.setAttribute("type", "nouvelleQuestion");
         Element objNoeudParametreQuestion = docSortie.createElement("question");
         objNoeudParametreQuestion.setAttribute("id", Integer.toString(nouvelleQuestion.obtenirCodeQuestion()));
         objNoeudParametreQuestion.setAttribute("type", TypeQuestion.getValue(nouvelleQuestion.obtenirTypeQuestion()));
         objNoeudParametreQuestion.setAttribute("url", nouvelleQuestion.obtenirURLQuestion());
         objNoeudParametreNouvelleQuestion.appendChild(objNoeudParametreQuestion);
         noeudCommande.setAttribute("type", "Boule");
         noeudCommande.appendChild(objNoeudParametreNouvelleQuestion);

	}
	
}