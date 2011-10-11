package ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables;

import java.util.HashMap;

import org.w3c.dom.Element;

import ServeurJeu.ComposantesJeu.Joueurs.InformationPartieHumain;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;

/**
 * @author François Gingras
 * changed Oloieri Lilian
 * last change August 2011
 */
public class Banane extends ObjetUtilisable 
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
	public static final String TYPE_OBJET = "Banane";
	
	//private static final long Seconds = 90;

	/**
	 * Constructeur de la classe Banane qui permet de définir les propriétés 
	 * propres à l'objet courant.
	 *
	 * @param in id : Le numéro d'identification de l'objet
	 * @param boolean estVisible : Permet de savoir si l'objet doit être visible ou non
	 */
	public Banane(int id, boolean estVisible)
	{
		// Appeler le constructeur du parent
		super(id, estVisible, UID_OU_BANANE, PRIX, EST_LIMITE, PEUT_ETRE_ARME, TYPE_OBJET);
	}
	
	public void useObject(Element noeudCommande, String playerName, JoueurHumain objJoueurHumain)
	{
		 System.out.println("we are in objet banana");
		 InformationPartieHumain infoPartie = objJoueurHumain.obtenirPartieCourante();
		 
		 // La partie ici ne fait que sélectionner le joueur qui sera affecté
         // Le reste se fait dans Banane.java
         noeudCommande.setAttribute("type", "Banane");
         boolean estHumain = false; //Le joueur choisi est'il humain?

         // On obtient la liste des joueurs humains, puis la liste des joueurs virtuels
         HashMap<String, JoueurHumain> listeJoueursHumains = infoPartie.obtenirTable().obtenirListeJoueurs();
         for (JoueurHumain objJoueur: listeJoueursHumains.values()) {
             if (objJoueur.obtenirNom().equals(playerName)) {
                 estHumain = true;
                 break;
             }
         }

         infoPartie.obtenirTable().preparerEvenementUtiliserObjet(
                 objJoueurHumain.obtenirNom(),
                 playerName,
                 "Banane",
                 "");

         //System.out.println("Protocole joueur 4189 Banane " + objJoueurHumain.obtenirNomUtilisateur() + " " + playerName);
         if (estHumain) {
             JoueurHumain joueur = infoPartie.obtenirTable().obtenirJoueurHumainParSonNom(playerName);
             if (joueur != null) {
                 joueur.obtenirPartieCourante().getBananaState().startBanana();
             }
             
         }else{
         	infoPartie.obtenirTable().obtenirJoueurVirtuelParSonNom(playerName).obtenirPartieCourante().getBananaState().startBanana();
         }


	}
	/*
	public static void utiliserBanane(Joueur player)
	{   
		if(player != null)
		   player.getPlayerGameInfo().bananaIsUsed();		
	}*/

	/*
	public static BananaHumainTask utiliserBanane(JoueurHumain player, long delay)
	{   
		//first cancel the Brainiac
		player.obtenirPartieCourante().getBrainiacState().setOffBrainiac();

		// Create TimerTask and Timer.
		BananaHumainTask bTask = new BananaHumainTask(player);
		
		// effects of Banana
		player.obtenirPartieCourante().setMoveVisibility(player.obtenirPartieCourante().getMoveVisibility() - 2);

		// used timer to take out effects of banana after the needed time
		player.obtenirProtocoleJoueur().getObjControleurJeu().obtenirGestionnaireTemps().schedule(bTask, delay);
		
		return bTask;

	}
	
	// if VitualPlayer use the Banana
	public static BananaVirtualTask utiliserBanane(JoueurVirtuel player, long delay)
	{

		//first cancel the Brainiac
		player.obtenirPartieCourante().getBrainiacState().setOffBrainiac();

		// Create TimerTask and Timer.
		BananaVirtualTask bTask = new BananaVirtualTask(player);
		Timer bTimer = new Timer();

		// effects of Banana
		player.obtenirPartieCourante().getBananaState().setisUnderBananaEffects(true);

		// used timer to take out effects of banana after the needed time
		bTimer.schedule(bTask, delay);
		return bTask;

	}*/
}// end class