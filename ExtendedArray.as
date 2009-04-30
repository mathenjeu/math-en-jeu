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



class ExtendedArray extends Array
{
    /**
     * Cette fonction permet de créer un ExtendedArray selon un Array.
     *
     * @param Array array : Le tableau à transformer
     * @return ExtendedArray : Le nouveau tableau étendu créé
     */
    public static function fromArray(array:Array):ExtendedArray
    {
        // Créer un nouveau tableau étendu
        var lstTableauEtendu:ExtendedArray = new ExtendedArray();

        // Passer tous les éléments du tableau passé en paramètre et créer
        // le ExtendedArray
        for (var i:Number = 0; i < array.length; i++)
        {
            // Ajouter l'élément courant du tableau
            lstTableauEtendu.push(array[i]);
        }

        return lstTableauEtendu;
    }

    /**
     * Cette fonction permet de déterminer si un élément passé en paramètre
     * se trouve dans le tableau.
     *
     * @param Object element : L'élément à chercher
     * @return Boolean : true si l'objet est dans le tableau
     *                   false sinon
     */
    public function contains(element:Object):Boolean
    {
        // Déclaration d'une variable qui va permettre de savoir si l'élément
        // est contenu dans le tableau
        var bolEstContenu:Boolean = false;

        // Déclaration d'un compteur
        var i:Number = 0;

        // Boucler tant qu'on n'a pas atteint la fin du tableau et qu'on n'a
        // pas trouvé l'élément
        while (i < this.length && bolEstContenu == false)
        {
            // Garder en mémoire si oui ou non l'élément est présent
            bolEstContenu = (this[i] === element);

            i++;
        }

        return bolEstContenu;
    }

    /**
     * Cette fonction permet de déterminer si un élément passé en paramètre
     * se trouve dans le tableau en utilisant la propriété property comme
     * façon de vérifier si l'élément est présent.
     *
     * @param Object element : L'élément à chercher
     * @param String property : Le nom de la propriété (champ) à vérifier
     * @return Boolean : true si l'objet est dans le tableau
     *                   false sinon
     */
    public function containsByProperty(element:Object, property:String):Boolean
    {
        // Déclaration d'une variable qui va permettre de savoir si l'élément
        // est contenu dans le tableau
        var bolEstContenu:Boolean = false;

        // Déclaration d'un compteur
        var i:Number = 0;

        // Boucler tant qu'on n'a pas atteint la fin du tableau et qu'on n'a
        // pas trouvé l'élément
        while (i < this.length && bolEstContenu == false)
        {
            // Garder en mémoire si oui ou non l'élément est présent
            bolEstContenu = (this[i][property] === element);

            i++;
        }

        return bolEstContenu;
    }

    /**
     * Cette fonction permet de retourner l'index dans le tableau de la
     * première occurence rencontrée de l'élément passé en paramètres.
     *
     * @param Object element : L'élément à chercher
     * @return Number : L'index de l'élément dans le tableau (la première
     *                  occurence). Si l'élément n'est pas présent dans le
     *                  tableau, alors on retourne -1
     */
    public function indexOf(element:Object):Number
    {
        // Déclaration d'une variable qui va garder l'index où a été trouvé
        // l'élément dans le tableau
        var intIndex:Number = -1;

        // Déclaration d'un compteur
        var i:Number = 0;

        // Boucler tant qu'on n'a pas atteint la fin du tableau et qu'on n'a
        // pas trouvé l'élément
        while (i < this.length && intIndex == -1)
        {
            // Garder en mémoire l'index de l'élément dans le tableau si on
            // le rencontre
            if (this[i] === element)
            {
                intIndex = i;
            }

            i++;
        }

        return intIndex;
    }

    /**
     * Cette fonction permet d'enlever l'élément passé en paramètres. Toutes
     * les occurences de cet objet sont enlevées du tableau.
     *
     * @param Object element : L'élément à enlever de la liste
     */
    public function remove(element:Object)
    {
        // Déclaration d'un compteur
        var i:Number = this.length - 1;

        // Boucler tant qu'on n'a pas atteint le début du tableau
        while(i >= 0)
        {
            // Si l'élément courant dans le tableau est l'élément recherché,
            // alors on va le retirer du tableau
            if (this[i] === element)
            {
                // Enlever l'élément courant
                this.splice(i, 1);
            }

            i--;
        }
    }

    /**
     * Cette fonction permet d'enlever tous les éléments du tableau.
     */
    public function clear()
    {
        // Enlever tous les éléments du tableau
        this.splice(0, this.length);
    }
}