/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Générale Affero (AGPL), telle que publiée par
Affero Inc. ; soit la version 1 de la Licence, ou (à
votre discrétion) une version ultérieure quelconque.

Ce programme est distribué dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans même une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Générale Affero pour plus de détails.

Vous devriez avoir reçu un exemplaire de la Licence Publique
Générale Affero avec ce programme; si ce n'est pas le cas,
écrivez à Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/



class ExtendedString
{
    /**
     * Cette fonction permet de retourner si oui ou non la chaîne passée en
     * paramètres est un chiffre ou non.
     *
     * @param String stringNumber : La chaîne à vérifier
     * @return Boolean : true si stringNumber est un chiffre,
     *                   false sinon
     */
    public static function isNumber(stringNumber:String):Boolean
    {
        // Déclaration d'une variable booléenne qui va permettre de savoir si
        // la chaîne passe en paramètres est un chiffre ou non
        var bolEstChiffre:Boolean = true;

        // Déclaration d'un compteur
        var i:Number = 0;

        // Boucler tant qu'on n'a pas trouvé un caractère qui n'est pas un
        // chiffre
        while (i < stringNumber.length && bolEstChiffre == true)
        {
            // Si le code ascii du caractère courant est entre 48 et 57
            // (chiffres de 0 à 9), alors c'est un chiffre sinon ce n'en
            // n'est pas un
            bolEstChiffre = (stringNumber.charCodeAt(i) >= 48 &&
                             stringNumber.charCodeAt(i) <= 57);

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
     * @param Number numberToFormat : Le chiffre à formater
     * @param Number minLength : La longueur minimale que le chiffre doit avoir
     * @param String fillCharacter : Le chiffre à formater
     * @return String : La chaîne contenant le chiffre et les caractères de
     *                  remplissage
     */
    public static function formatNumber(numberToFormat:Number, minLength:Number, fillCharacter:String):String
    {
        // Déclaration d'une chaîne qui va contenir le chiffre avec le nombre
        // approprié de caractères de remplissage
        var strChiffre:String = String(numberToFormat);

        // Boucler tant qu'on n'a pas atteint la longueur minimum
        while (strChiffre.length < minLength)
        {
            // On ajoute un caractère de remplissage au début
            strChiffre = fillCharacter + strChiffre;
        }

        return strChiffre;
    }

    /**
     * Cette fonction permet d'encoder la chaîne passée en paramètres en
     * caractères UTF-8. Cet encodage n'accepte que les caractères de 0 à 127.
     * Tous les autres caractères seront remplacés par leur code &#ascii;
     *
     * @param String stringToEncode : La chaîne à encoder en UTF-8
     * @return String : La chaîne encodée
     */
    public static function encodeToUTF8(stringToEncode:String):String
    {
        // Déclaration d'une chaîne qui va contenir la version encodée en UTF-8
        // de la chaîne passée en paramètres
        var strChaineEncodee:String = "";

        // Si la chaîne contient au moins un caractère, alors on peut l'encoder
        if (stringToEncode != undefined && stringToEncode != null &&
            stringToEncode != "")
        {
            // Passer tous les caractères de la chaîne et transformer en
            // codes ascii ceux dont le code est supérieur à 127
            for (var i:Number = 0; i < stringToEncode.length; i++)
            {
                // Si le code ascii du caractère courant est plus grand que 127
                // alors on doit le transformer en code ascii, sinon on le
                // laisse tel quel
                if (stringToEncode.charCodeAt(i) > 127)
                {
                    strChaineEncodee += "&#" + formatNumber(stringToEncode.charCodeAt(i), 3, "0") + ";";
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
    public static function decodeFromUTF8(stringToDecode:String):String
    {
        // Déclaration d'une chaîne qui va contenir la version décodée
        // de la chaîne UTF-8 passée en paramètres
        var strChaineDecodee:String = "";

        // Si la chaîne contient au moins un caractère, alors on peut la
        // décoder
        if (stringToDecode != undefined && stringToDecode != null &&
            stringToDecode != "")
        {
            // Passer tous les caractères et transformer ceux qui sont
            // sous la forme &#xxx;
            for (var i:Number = 0; i < stringToDecode.length; i++)
            {
                // Si on a bien trouvé un code de caractère, alors on va
                // ajouter le caractère correspondant dans la chaîne
                if (i <= stringToDecode.length - 6 &&
                    stringToDecode.charAt(i) == "&" &&
                    stringToDecode.charAt(i + 1) == "#" &&
                    stringToDecode.charAt(i + 5) == ";" &&
                    isNumber(stringToDecode.substring(i + 2, i + 5)) == true)
                {
                    // Obtenir le caractère correspondant au code ASCII courant
                    strChaineDecodee += String.fromCharCode(Number(stringToDecode.substring(i + 2, i + 5)));

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

    /**
     * Cette fonction permet d'arranger un problème provoqué par l'objet XML.
     * Comme les objets XML sont encodés en UTF-8, les caractères accentués
     * français ne sont pas visibles et donnent des caractères bizarres
     * lorsqu'ils sont convertis automatiquement par l'objet XML. L'objet
     * XML change automatiquement les & en &amp;, mais ne change pas les
     * é en &#233;. Donc, pour éviter d'avoir des caractères bizarres, il
     * faut changer à bras les é en &#233;, mais le & de &#233; sera changé
     * automatiquement en &amp;#233;. Cette fonction va s'occuper de rechanger
     * les &amp;#233; en &#233;.
     *
     * @param String stringToCorrect : La chaîne UTF-8 à corriger
     * @return String : La chaîne corrigée
     */
    public static function correctAmperstand(stringToCorrect:String):String
    {
        // Si la chaîne contient au moins un caractère, alors on peut la
        // corriger
        if (stringToCorrect != undefined && stringToCorrect != null &&
            stringToCorrect != "")
        {
            // Déclaration d'une variable qui va contenir la position
            // du prochain &amp;
            var intPosition:Number = stringToCorrect.indexOf("&amp;");

            // Boucler tant qu'il reste des &amp; et remplacer ceux appropriés
            // (il ne faut pas tous les remplacer)
            while (intPosition > -1)
            {
                // Vérifier si c'est un des cas où il faudrait remplacer
                // le &amp; par &
                if (intPosition <= stringToCorrect.length - 10 &&
                    stringToCorrect.charAt(intPosition + 5) == "#" &&
                    stringToCorrect.charAt(intPosition + 9) == ";" &&
                    isNumber(stringToCorrect.substring(intPosition + 6, intPosition + 9)) == true)
                {
                    // Obtenir le caractère correspondant au code ASCII courant
                    stringToCorrect = stringToCorrect.substring(0, intPosition) + "&" + stringToCorrect.substring(intPosition + 5, stringToCorrect.length);

                    // Avancer d'un caractère
                    intPosition++;
                }
                else
                {
                    // Avancer de 5 caractères (pour &amp;)
                    intPosition += 5;
                }

                // Recalculer la position du prochain &amp;
                intPosition = stringToCorrect.indexOf("&amp;", intPosition);
            }
        }

        return stringToCorrect;
    }
}