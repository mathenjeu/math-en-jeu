package ClassesRetourFonctions;

import ServeurJeu.ComposantesJeu.Objets.ObjetsUtilisables.ObjetUtilisable;
import ServeurJeu.ComposantesJeu.Objets.Magasins.Magasin;
import java.awt.Point;

/**
 * @author Jean-François Brind'Amour
 */
public class RetourVerifierReponseEtMettreAJourPlateauJeu
{
	// Déclaration d'une variable qui va contenir si oui ou non le déplacement
	// a été accepté à cause que la réponse était bonne
	private boolean bolDeplacementAccepte;
	
	// Déclaration d'une variable qui va contenir l'explication du résultat
	private String strExplications;
	
	// Déclaration d'une variable qui va contenir la référence vers l'objet
	// ramassé
	private ObjetUtilisable objObjetRamasse;
	
	// Déclaration d'une variable qui va contenir la référence vers l'objet
	// que le joueur a subi
	private ObjetUtilisable objObjetSubi;
	
	// Déclaration d'une référence vers un magasin que le joueur rencontre
	private Magasin objMagasin;
	
	//TODO: Il ne serait pas nécessaire de retourner la nouvelle position
	// 		mais on doit la retourner à cause du client 
	private Point objNouvellePosition;
	
	// Déclaration d'une variable qui va contenir le nouveau pointage
	private final int intNouveauPointage;
        
    private final int intNouvelArgent;
	
	private String strCollision;
	
	private final int playerBonus;
	
	/**
	 * Constructeur de la classe RetourVerifierReponseEtMettreAJourPlateauJeu.
	 * @param bonus 
	 * 
	 * @param boolean deplacementAccepte : Permet de savoir si le déplacement a été accepté ou non
	 * @param int nouveauPointage : Le nouveau pointage du joueur
	 */
	public RetourVerifierReponseEtMettreAJourPlateauJeu(boolean deplacementAccepte, int nouveauPointage, int nouvelArgent, int bonus)
	{
		// Initialiser les membres de la classe de retour
		bolDeplacementAccepte = deplacementAccepte;
		//strExplications = "";
		//objObjetRamasse = null;
		//objObjetSubi = null;
		//objNouvellePosition = null;
		intNouveauPointage = nouveauPointage;
        intNouvelArgent = nouvelArgent;
		//strCollision = "vide";
		playerBonus = bonus;
	}
	
	/**
	 * @return the playerBonus
	 */
	public int getPlayerBonus() {
		return playerBonus;
	}

	

	/**
	 * Cette fonction retourne si oui ou non le déplacement est accepté.
	 * 
	 * @return boolean : true si la réponse était bonne et que le déplacement
	 * 						est accepté
	 * 					 false sinon
	 */
	public boolean deplacementEstAccepte()
	{
		return bolDeplacementAccepte;
	}
	
	/**
	 * Cette fonction retourne l'url de l'explication de la réponse.
	 * 
	 * @return String : L'Url de l'explication de la réponse
	 */
	public String obtenirExplications()
	{
		return strExplications;
	}
	
	/**
	 * Cette méthode définit l'url de l'explication de la réponse.
	 * 
	 * @param String explications : L'url de l'explication de la réponse
	 */
	public void definirExplications(String explications)
	{
		strExplications = explications;
	}
	
	/**
	 * Cette fonction retourne l'objet ramassé sur la case
	 * 
	 * @return ObjetUtilisable : L'objet ramassé sur la case
	 */
	public ObjetUtilisable obtenirObjetRamasse()
	{
		return objObjetRamasse;
	}
	
	/**
	 * Cette méthode définit l'objet ramassé sur la case.
	 * 
	 * @param ObjetUtilisable objetRamasse : L'objet ramassé
	 */
	public void definirObjetRamasse(ObjetUtilisable objetRamasse)
	{
		objObjetRamasse = objetRamasse;
	}
	
	/**
	 * Cette fonction retourne l'objet subi sur la case.
	 * 
	 * @return ObjetUtilisable : L'objet subi sur la case
	 */
	public ObjetUtilisable obtenirObjetSubi()
	{
		return objObjetSubi;
	}
	
	/**
	 * Cette méthode définit l'objet subi sur la case.
	 * 
	 * @param ObjetUtilisable objetSubi : L'objet subi
	 */
	public void definirObjetSubi(ObjetUtilisable objetSubi)
	{
		objObjetSubi = objetSubi;
	}
	
	/**
	 * Cette fonction retourne la nouvelle position du joueur.
	 * 
	 * @return Point : La nouvelle position du joueur
	 */
	public Point obtenirNouvellePosition()
	{
		return objNouvellePosition;
	}
	
	/**
	 * Cette méthode définit la nouvelle position du joueur.
	 * 
	 * @param Point nouvellePosition : La nouvelle position du joueur
	 */
	public void definirNouvellePosition(Point nouvellePosition)
	{
		objNouvellePosition = nouvellePosition;
	}
	
	/**
	 * Cette fonction retourne le nouveau pointage du joueur.
	 * 
	 * @return int : Le nouveau pointage du joueur
	 */
	public int obtenirNouveauPointage()
	{
		return intNouveauPointage;
	}
	
	

    public int obtenirNouvelArgent()
    {
        return intNouvelArgent;
    }
	
	public String obtenirCollision()
	{
		return strCollision;
	}
	
	public void definirCollision( String collision )
	{
		strCollision = collision;
	}
	
	public Magasin obtenirMagasin()
	{
		return objMagasin;
	}
	
	public void definirMagasin(Magasin mag)
    {
    	objMagasin = mag;
    }	
}
