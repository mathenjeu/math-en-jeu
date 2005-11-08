package Enumerations;

/**
 * @author Jean-François Brind'Amour
 */
public final class Filtre
{
	// Déclaration des membres de cette énumération
	public static final String Toutes = "Toutes";
	public static final String Completes = "Completes";
	public static final String Commencees = "Commencees";
	public static final String Incompletes = "Incompletes";
	
	/**
	 * Constructeur par défaut est privé pour empêcher de pourvoir créer des 
	 * instances de cette classe.
	 */
	private Filtre(){}
	
	/**
	 * Cette fonction statique permet de déterminer si la valeur passée en 
	 * paramètres est un membre de cette énumération.
	 * 
	 * @param String valeur : la valeur de filtre à vérifier
	 * @return boolean : true si la valeur est un membre de cette énumération
	 *                   false sinon
	 */
	public static boolean estUnMembre(String valeur)
	{
		// Si la valeur passée en paramètre n'est pas égale à aucune des
		// valeurs définies dans cette classe, alors la valeur n'est pas
		// un membre de cette énumération, sinon elle en est un
		return (valeur.equals(Toutes) || valeur.equals(Completes) || 
				valeur.equals(Commencees) || valeur.equals(Incompletes));
	}
}
