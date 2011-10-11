package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ServeurJeu.ComposantesJeu.Joueurs.InformationPartieHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;

/**
 * @author Jean-François Brind'Amour
 */
public class Livre extends ObjetUtilisable 
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
	public static final String TYPE_OBJET = "Livre";

	/**
	 * Constructeur de la classe Livre qui permet de définir les propriétés 
	 * propres à l'objet courant.
	 *
	 * @param in id : Le numéro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit être 
	 * 							   visible ou non
	 */
	public Livre(int id, boolean estVisible)
	{
		// Appeler le constructeur du parent
		super(id, estVisible, UID_OU_LIVRE, PRIX, EST_LIMITE, PEUT_ETRE_ARME, TYPE_OBJET);
	}

	public void useObject(Element noeudCommande, String playerName, JoueurHumain objJoueurHumain)	
	{
		InformationPartieHumain infoPartie = objJoueurHumain.obtenirPartieCourante();
		System.out.println("we are in livre");
		// Le livre est utilisé lorsqu'un joueur se fait poser une question
		// à choix de réponse. Le serveur renvoie alors une mauvaise réponse
		// à la question, et le client fera disparaître ce choix de réponse
		// parmi les choix possibles pour le joueur.
		// On obtient une mauvaise réponse à la dernière question posée
		String mauvaiseReponse = infoPartie.getObjBoiteQuestions().obtenirMauvaiseReponse(infoPartie.obtenirQuestionCourante());

		Document docSortie = noeudCommande.getOwnerDocument();
		// Créer le noeud contenant le choix de réponse si c'était une question à choix de réponse
		Element objNoeudParametreMauvaiseReponse = docSortie.createElement("parametre");
		objNoeudParametreMauvaiseReponse.setAttribute("type", "MauvaiseReponse");
		objNoeudParametreMauvaiseReponse.appendChild(docSortie.createTextNode(mauvaiseReponse));
		noeudCommande.setAttribute("type", "Livre");
		noeudCommande.appendChild(objNoeudParametreMauvaiseReponse);
	}

}