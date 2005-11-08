package ClassesUtilitaires;

/**
 * @author Jean-François Brind'Amour
 */
public final class UtilitaireEncodeurDecodeur 
{
	/**
	 * Constructeur par défaut est privé pour empêcher de pourvoir créer des 
	 * instances de cette classe.
	 */
    private UtilitaireEncodeurDecodeur() {}

    /**
     * Cette fonction permet d'encoder la chaîne passée en paramètres en
     * caractères UTF-8. Cet encodage n'accepte que les caractères de 0 à 127.
     * Tous les autres caractères seront remplacés par leur code &#ascii;
     *
     * @param String stringToEncode : La chaîne à encoder en UTF-8
     * @return String : La chaîne encodée
     */
    public static String encodeToUTF8(String stringToEncode)
    {
        // Déclaration d'une chaîne qui va contenir la version encodée en UTF-8
        // de la chaîne passée en paramètres
        String strChaineEncodee = "";

        // Si la chaîne contient au moins un caractère, alors on peut l'encoder
        if (stringToEncode != null && stringToEncode.equals("") == false)
        {
            // Passer tous les caractères de la chaîne et transformer en
            // codes ascii ceux dont le code est supérieur à 127
            for (int i = 0; i < stringToEncode.length(); i++)
            {
                // Si le code ascii du caractère courant est plus grand que 127
                // alors on doit le transformer en code ascii, sinon on le
                // laisse tel quel
                if (((int) stringToEncode.charAt(i)) > 127)
                {
                    strChaineEncodee += "&#" + UtilitaireNombres.formatNumber(((int) stringToEncode.charAt(i)), 3, "0") + ";";
                }
                else
                {
                    strChaineEncodee += stringToEncode.charAt(i);
                }
            }
        }

        return strChaineEncodee;
    }

    /**
     * Cette fonction permet de décoder la chaîne passée en paramètres. On
     * va remplacer tous les caraètres écris en ascii &#ascii; en un véritable
     * caractère.
     *
     * @param String stringToDecode : La chaîne UTF-8 à décoder
     * @return String : La chaîne décodée
     */
    public static String decodeFromUTF8(String stringToDecode)
    {
        // Déclaration d'une chaîne qui va contenir la version décodée
        // de la chaîne UTF-8 passée en paramètres
        String strChaineDecodee = "";

        // Si la chaîne contient au moins un caractère, alors on peut la
        // décoder
        if (stringToDecode != null && stringToDecode.equals("") == false)
        {
            // Passer tous les caractères et transformer ceux qui sont
            // sous la forme &#xxx;
            for (int i = 0; i < stringToDecode.length(); i++)
            {
                // Si on a bien trouvé un code de caractère, alors on va
                // ajouter le caractère correspondant dans la chaîne
                if (i <= stringToDecode.length() - 6 &&
                    stringToDecode.charAt(i) == '&' &&
                    stringToDecode.charAt(i + 1) == '#' &&
                    stringToDecode.charAt(i + 5) == ';' &&
                    UtilitaireNombres.isPositiveNumber(stringToDecode.substring(i + 2, i + 5)) == true)
                {
                    // Obtenir le caractère correspondant au code ASCII courant
                    strChaineDecodee += ((char) Integer.parseInt(stringToDecode.substring(i + 2, i + 5)));

                    // Augmenter le i pour ne pas retraiter le même caractère
                    i += 5;
                }
                else
                {
                    strChaineDecodee += stringToDecode.charAt(i);
                }
            }
        }

        return strChaineDecodee;
    }
}
