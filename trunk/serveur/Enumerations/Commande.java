package Enumerations;

/**
 * @author Jean-François Brind'Amour
 */
public final class Commande
{
	// Déclaration des différentes commandes possibles traitées par le serveur
	public static final String Connexion = "Connexion";
	public static final String Deconnexion = "Deconnexion";
	public static final String ObtenirListeJoueurs = "ObtenirListeJoueurs";
	public static final String ObtenirListeSalles = "ObtenirListeSalles";
	public static final String EntrerSalle = "EntrerSalle";
	public static final String QuitterSalle = "QuitterSalle";
	public static final String ObtenirListeJoueursSalle = "ObtenirListeJoueursSalle";
	public static final String ObtenirListeTables = "ObtenirListeTables";
	public static final String CreerTable = "CreerTable";
	public static final String EntrerTable = "EntrerTable";
	public static final String QuitterTable = "QuitterTable";
	public static final String DemarrerMaintenant = "DemarrerMaintenant";
	public static final String DemarrerPartie = "DemarrerPartie";
	public static final String DeplacerPersonnage = "DeplacerPersonnage";
	public static final String RepondreQuestion = "RepondreQuestion";
	public static final String Pointage = "Pointage";
    public static final String RejoindrePartie = "RejoindrePartie";
    public static final String NePasRejoindrePartie = "NePasRejoindrePartie";
    public static final String UtiliserObjet = "UtiliserObjet";
    public static final String AcheterObjet = "AcheterObjet";
    
	/**
	 * Constructeur par défaut est privé pour empêcher de pourvoir créer des 
	 * instances de cette classe.
	 */
	private Commande(){}
	
	/**
	 * Cette fonction statique permet de déterminer si la valeur passée en 
	 * paramètres est un membre de cette énumération.
	 * 
	 * @param String valeur : La valeur à vérifier
	 * @return boolean : true si la valeur est un membre de cette énumération
	 *                   false sinon
	 */
	public static boolean estUnMembre(String valeur)
	{
		// Si la valeur passée en paramètre n'est pas égale à aucune des
		// valeurs définies dans cette classe, alors la valeur n'est pas
		// un membre de cette énumération, sinon elle en est un
		return (valeur.equals(Connexion) || valeur.equals(Deconnexion) || 
		        valeur.equals(ObtenirListeJoueurs) || valeur.equals(ObtenirListeSalles) || 
		        valeur.equals(EntrerSalle) || valeur.equals(QuitterSalle) || 
		        valeur.equals(ObtenirListeJoueursSalle) || valeur.equals(ObtenirListeTables) ||
		        valeur.equals(CreerTable) || valeur.equals(EntrerTable) ||
		        valeur.equals(QuitterTable) || valeur.equals(DemarrerPartie) ||
		        valeur.equals(DeplacerPersonnage) || valeur.equals(RepondreQuestion) ||
		        valeur.equals(DemarrerMaintenant) || valeur.equals(Pointage) ||
		        valeur.equals(RejoindrePartie) || valeur.equals(UtiliserObjet));

	}
}
