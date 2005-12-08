package ClassesUtilitaires;

import java.util.Random;

/**
 * @author Jean-François Brind'Amour
 */
public final class UtilitaireNombres 
{
	/**
	 * Constructeur par défaut est privé pour empêcher de pourvoir créer des 
	 * instances de cette classe.
	 */
    private UtilitaireNombres() {}

    /**
     * Cette fonction permet de retourner si oui ou non la chaîne passée en
     * paramètres est un chiffre positif (>= 0) ou non.
     *
     * @param String stringNumber : La chaîne à vérifier
     * @return boolean : true si stringNumber est un chiffre,
     *                   false sinon
     */
    public static boolean isPositiveNumber(String stringNumber)
    {
        // Déclaration d'une variable booléenne qui va permettre de savoir si
        // la chaîne passe en paramètres est un chiffre ou non
        boolean bolEstChiffre = true;

        // Déclaration d'un compteur
        int i = 0;

        // Boucler tant qu'on n'a pas trouvé un caractère qui n'est pas un
        // chiffre
        while (i < stringNumber.length() && bolEstChiffre == true)
        {
            // Si le code ascii du caractère courant est entre 48 et 57
            // (chiffres de 0 à 9), alors c'est un chiffre sinon ce n'en
            // n'est pas un
            bolEstChiffre = (((int) stringNumber.charAt(i)) >= 48 &&
                             ((int) stringNumber.charAt(i)) <= 57);

            i++;
        }

        return bolEstChiffre;
    }
    
    /**
     * Cette fonction permet de formater un chiffre passé en paramètres
     * en lui ajoutant des caractères de remplissages à gauche tant que
     * la longueur du chiffre minimale en termes de caractères n'est pas
     * atteinte. Le champs fillCharacter peut contenir plus d'un caractère.
     *
     * @param int numberToFormat : Le chiffre à formater
     * @param int minLength : La longueur minimale que le chiffre doit avoir
     * @param String fillCharacter : Le chiffre à formater
     * @return String : La chaîne contenant le chiffre et les caractères de
     *                  remplissage
     */
    public static String formatNumber(int numberToFormat, int minLength, String fillCharacter)
    {
        // Déclaration d'une chaîne qui va contenir le chiffre avec le nombre
        // approprié de caractères de remplissage
        String strChiffre = Integer.toString(numberToFormat);

        // Boucler tant qu'on n'a pas atteint la longueur minimum
        while (strChiffre.length() < minLength)
        {
            // On ajoute un caractère de remplissage au début
            strChiffre = fillCharacter + strChiffre;
        }

        return strChiffre;
    }
    
    /**
     * Cette fonction permet de générer un entier à partir d'une loi normale.
     * La moyenne doit être plus grande que le double de la variance.
     * 
     * @param double moyenne : La moyenne
     * @param double variance : La variance
     * @return int : L'entier pris aléatoirement selon la loi normale
     */
    public static int genererNbAleatoireLoiNormale(double moyenne, double variance)
    {
    	// Déclaration d'une variable qui va permettre de savoir si on a 
    	// trouvé les valeurs recherchées
    	boolean bolTrouve = false;
    	
    	// Créer un objet permettant de générer des nombres aléatoires
    	Random objRandom = new Random();
    	
    	// Déclarations des variables utilisées pour l'algorithme
    	double u1 = 0.0d;
    	double u2 = 0.0d;
    	double v1 = 0.0d;
    	double v2 = 0.0d;
    	double s = 0.0d;
    	double stDev = 0.0d;
    	double z = 0.0d;
    	double rndNormal = 0.0d;
        
        // On boucle tant qu'on n'a pas trouvé les bonnes valeurs 
    	// de u1, u2, v1, v2 et s
        while (bolTrouve == false)
        {
        	// On génère les nombres aléatoires pour u1 et u2
            u1 = objRandom.nextFloat();
            u2 = objRandom.nextFloat();
        	
            // On calcule v1, v2 et s
            v1 = 2 * u1 - 1;
            v2 = 2 * u2 - 1;
            s = (v1 * v1) + (v2 * v2);
            
            // Si s est >= à 1, alors il faut obtenir d'autres valeurs 
            // aléatoires u1 et u2
            bolTrouve = (s < 1);
        }

        // Calculer la valeur normale
        z = Math.sqrt((-2 * Math.log(s)) / s) * v1;
        stDev = Math.sqrt(variance);
        rndNormal = (z * stDev) + moyenne;

     return (int) Math.round(rndNormal);   
    }
}