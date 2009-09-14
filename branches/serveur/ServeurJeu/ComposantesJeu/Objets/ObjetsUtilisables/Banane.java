package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;

import ClassesUtilitaires.BananaTask;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurVirtuel;
import java.util.Timer;

/**
 * @author Fran�ois Gingras
 */
public class Banane extends ObjetUtilisable 
{
	// Cette constante sp�cifie le prix de l'objet courant
	public static final int PRIX = 3;

	// Cette constante affirme que l'objet courant n'est pas limit� 
	// lorsqu'on l'ach�te (c'est-�-dire qu'un magasin n'�puise jamais 
	// son stock de cet objet)
	public static final boolean EST_LIMITE = false;

	// Cette constante affirme que l'objet courant ne peut �tre arm� 
	// et d�pos� sur une case pour qu'un autre joueur tombe dessus. Elle 
	// ne peut seulement �tre utilis�e imm�diatement par le joueur
	public static final boolean PEUT_ETRE_ARME = false;

	// Cette constante d�finit le nom de cet objet
	public static final String TYPE_OBJET = "Banane";
	
	private static final int Seconds = 90;

	/**
	 * Constructeur de la classe Banane qui permet de d�finir les propri�t�s 
	 * propres � l'objet courant.
	 *
	 * @param in id : Le num�ro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit �tre visible ou non
	 */
	public Banane(int id, boolean estVisible)
	{
		// Appeler le constructeur du parent
		super(id, estVisible, UID_OU_BANANE, PRIX, EST_LIMITE, PEUT_ETRE_ARME, TYPE_OBJET);
	}

	public static void utiliserBanane(JoueurHumain player, String nomJoueurChoisi, boolean estHumain)
	{
		// On pr�pare l'�v�nement � envoyer � tous
	    //joueur.obtenirPartieCourante().obtenirTable().preparerEvenementUtiliserObjet(joueur.obtenirNomUtilisateur(), nomJoueurChoisi, "Banane", "");///strCodeXML);
		
		if(estHumain)
		{
			// player under Banana
			JoueurHumain second = player.obtenirPartieCourante().obtenirTable().obtenirJoueurHumainParSonNom(nomJoueurChoisi); 
			
			// Create TimerTask and Timer.
			BananaTask bTask = new BananaTask(second);
			Timer bTimer = new Timer();
			//bkTimer.cancel();
			
			// effects of Banana
			second.obtenirPartieCourante().setIsUnderBananaEffect(player.obtenirNomUtilisateur());
			second.obtenirPartieCourante().setMoveVisibility(second.obtenirPartieCourante().getMoveVisibility() - 2);
						
			// used timer to take out effects of banana after the needed time
			bTimer.schedule(bTask, Seconds * 1000);
			
		}
		else
		{
			// player under Banana
			JoueurVirtuel vsecond = player.obtenirPartieCourante().obtenirTable().obtenirJoueurVirtuelParSonNom(nomJoueurChoisi); 
			vsecond.isUnderBananaEffect = player.obtenirNomUtilisateur();
			
			// Create TimerTask and Timer.
			BananaTask bTask = new BananaTask(vsecond);
			Timer bTimer = new Timer();
			//bkTimer.cancel();
			
			// used timer to take out effects of banana after the needed time
			bTimer.schedule(bTask, Seconds * 1000);
		}

		/*
		Document objDocumentXMLTemp = UtilitaireXML.obtenirDocumentXML();
		Element objNoeudCommandeTemp = objDocumentXMLTemp.createElement("Banane");

		Element objNoeudParametreNouvellePositionX = objDocumentXMLTemp.createElement("parametre");
		Element objNoeudParametreNouvellePositionY = objDocumentXMLTemp.createElement("parametre");
		objNoeudParametreNouvellePositionX.setAttribute("type", "NouvellePositionX");
		objNoeudParametreNouvellePositionY.setAttribute("type", "NouvellePositionY");
		Text objNoeudTexteNouvellePositionX = objDocumentXMLTemp.createTextNode(Integer.toString(pointOptimal.x));
		Text objNoeudTexteNouvellePositionY = objDocumentXMLTemp.createTextNode(Integer.toString(pointOptimal.y));
		objNoeudParametreNouvellePositionX.appendChild(objNoeudTexteNouvellePositionX);
		objNoeudParametreNouvellePositionY.appendChild(objNoeudTexteNouvellePositionY);
		objNoeudCommandeTemp.appendChild(objNoeudParametreNouvellePositionX);
		objNoeudCommandeTemp.appendChild(objNoeudParametreNouvellePositionY);

		objDocumentXMLTemp.appendChild(objNoeudCommandeTemp);
		String strCodeXML = "";
		try
		{
			strCodeXML = UtilitaireXML.transformerDocumentXMLEnString(objDocumentXMLTemp);
		}
		catch (TransformerConfigurationException tce)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_transformation"));
		}
		catch (TransformerException te)
		{
			System.out.println(GestionnaireMessages.message("evenement.XML_conversion"));
		}

         */
		
		
	}
}// end class