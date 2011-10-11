package ServeurJeu.ComposantesJeu.Joueurs;

import java.awt.Point;
import ServeurJeu.ComposantesJeu.Tables.Table;

/**
 * Master it's a creator and the observer of the game, 
 * but not participate actively. It must assure the synchronised 
 * observation of the game, but must be not observable by ordinary 
 * players.  
 * 
 * @author Oloieri Lilian
 *
 */

public class InformationPartieMaster extends InformationPartie {

	// Déclaration d'une référence vers le gestionnaire de bases de données
	//private final GestionnaireBDJoueur objGestionnaireBD;
	
	// Déclaration d'une référence vers un joueur correspondant à cet
	// objet d'information de partie
	private final Joueur objJoueur;
	
	private int idDessin;
	
	// Déclaration d'une position du joueur dans le plateau de jeu
	private Point objPositionJoueur;
	
	
	/**
	 * Constructeur de la classe InformationPartie qui permet d'initialiser
	 * les propriétés de la partie et de faire la référence vers la table.
	 * @param gestionnaireEv
	 * @param gestionnaireBD
	 * @param joueur
	 * @param tableCourante
	 */
	public InformationPartieMaster(Joueur joueur, Table tableCourante) {

		super(tableCourante, joueur);
		
		// Faire la référence vers le gestionnaire de base de données
		//objGestionnaireBD = new GestionnaireBDJoueur(joueur);

		// Faire la référence vers le gestionnaire d'evenements
		//objGestionnaireEv = gestionnaireEv;

		// Faire la référence vers le joueur humain courant
		objJoueur = joueur;
		
	}// fin constructeur
	
	
	/**
	 * @param idDessin the idDessin to set
	 */
	public void setIdDessin(int idDessin) {
		this.idDessin = idDessin;
	}

	/**
	 * @return the idDessin
	 */
	public int getIdDessin() {
		return idDessin;
	}


	/**
	 * @return the objGestionnaireBD
		public GestionnaireBDJoueur getObjGestionnaireBD() {
		return objGestionnaireBD;
	} */

	
}
