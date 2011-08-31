package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.Communications.ProtocoleJoueur;

/**
 * @author Jean-François Brind'Amour
 */
public abstract class Joueur 
{
	// Cette variable va contenir le nom du joueur 
	protected final String strNom;
	
	// Used to distinguish between simple user and administrator
	// 1 - simple user - virtuels too ... at least for the moment
	// 2 - admin
	// 3 - prof
	protected int role;
	
	
	
	public Joueur(String strNom)
	{
		this.strNom = strNom;
	}
	
	/* Cette fonction permet d'obtenir le nom du joueur 
	 */
	public String obtenirNom()
	{
		return strNom;
	}
	
	/**
	 * @return the role
	 */
	public int getRole() {
		return role;
	}

	/**
	 * @param role le role de ce joueur.  1 = normal(virtual aussi), 2 = admin, 3 = prof
	 */
	public void setRole(int role) {
		this.role = role;
	}  
	
	public abstract InformationPartie getPlayerGameInfo();	

}