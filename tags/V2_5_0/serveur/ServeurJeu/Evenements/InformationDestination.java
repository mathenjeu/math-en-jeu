package ServeurJeu.Evenements;

import ServeurJeu.Communications.ProtocoleJoueur;

/**
 * @author Jean-François Brind'Amour
 */
public class InformationDestination
{
	// Déclaration d'une variable qui va contenir le numéro de commande généré
	private int intNoCommande;
	
	// Déclaration d'une référence vers le ProtocoleJoueur à qui envoyer le message
	private ProtocoleJoueur objProtocoleJoueur;
	
	/**
	 * Constructeur de la classe InformationDestination qui permet d'initialiser
	 * le numéro de la commande ainsi que le protocole.
	 */
	public InformationDestination(int noCommande, ProtocoleJoueur protocole)
	{
	    // Définir les propriétés de l'objet InformationDestination
	    intNoCommande = noCommande;
	    objProtocoleJoueur = protocole;
	}
	
	/**
	 * Cette fonction permet de retourner le numéro de la commande pour le joueur
	 * de destination suivant.
	 * 
	 * @return int : Le numéro de commande du InformationDestination courant
	 */
	public int obtenirNoCommande()
	{
	   return intNoCommande; 
	}
	
	/**
	 * Cette fonction permet de retourner le protocole du joueur à qui envoyer
	 * l'événement.
	 * 
	 * @return ProtocoleJoueur : Le protocole du joueur à qui envoyer l'événement
	 */
	public ProtocoleJoueur obtenirProtocoleJoueur()
	{
	   return objProtocoleJoueur;
	}
}
