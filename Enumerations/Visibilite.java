package Enumerations;

/**
 * @author Jean-François Brind'Amour
 */
public final class Visibilite
{
	// Déclaration des membres de cette énumération
	public static final String ToujoursVisible = "ToujoursVisible";
	public static final String JamaisVisible = "JamaisVisible";
	public static final String Aleatoire = "Aleatoire";
	
	/**
	 * Constructeur par défaut est privé pour empêcher de pourvoir créer des 
	 * instances de cette classe.
	 */
	private Visibilite(){}
	
	/**
	 * Cette fonction statique permet de déterminer si la valeur passée en 
	 * paramètres est un membre de cette énumération.
	 * 
	 * @param String valeur : la valeur de visibilité à vérifier
	 * @return boolean : true si la valeur est un membre de cette énumération
	 *                   false sinon
	 */
	public static boolean estUnMembre(String valeur)
	{
		// Si la valeur passée en paramètre n'est pas égale à aucune des
		// valeurs définies dans cette classe, alors la valeur n'est pas
		// un membre de cette énumération, sinon elle en est un
		return (valeur.equals(ToujoursVisible) || valeur.equals(JamaisVisible) || 
				valeur.equals(Aleatoire));
	}
}
