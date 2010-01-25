package Enumerations;

/**
 * @author Jean-François Brind'Amour
 */
public final class TypeQuestion
{
	// Déclaration des membres de cette énumération
	public static final String ChoixReponse = "MULTIPLE_CHOICE";  // "ChoixReponse";
	public static final String ChoixReponse3 = "MULTIPLE_CHOICE_3";
	public static final String ChoixReponse5 = "MULTIPLE_CHOICE_5"; 
	public static final String VraiFaux =  "TRUE_OR_FALSE";        //  "VraiFaux";
	public static final String ReponseCourte =   "SHORT_ANSWER"; // "ReponseCourte";
	
	/**
	 * Constructeur par défaut est privé pour empêcher de pourvoir créer des 
	 * instances de cette classe.
	 */
	private TypeQuestion(){}
	
	/**
	 * Cette fonction statique permet de déterminer si la valeur passée en 
	 * paramètres est un membre de cette énumération.
	 * 
	 * @param String valeur : la valeur du type de question à vérifier
	 * @return boolean : true si la valeur est un membre de cette énumération
	 *                   false sinon
	 */
	public static boolean estUnMembre(String valeur)
	{
		// Si la valeur passée en paramètre n'est pas égale à aucune des
		// valeurs définies dans cette classe, alors la valeur n'est pas
		// un membre de cette énumération, sinon elle en est un
		return (valeur.equals(ChoixReponse) || valeur.equals(ChoixReponse3) || valeur.equals(ChoixReponse5) || valeur.equals(VraiFaux) || 
				valeur.equals(ReponseCourte));
	}
}
