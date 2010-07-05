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

import ExtendedArray;

// Chaque objet état contient un numéro, une liste des commandes possibles (les
// commandes pouvant être acceptées lorsque le client Flash se trouve dans cet
// état) et une liste des événements acceptables (les événements pouvant être
// acceptés lorsque le client Flash se trouve dans cet état)
class EtatProfModule
{
    // Constante objet indiquant que le joueur est déconnecté (non connecté au
    // serveur physiquement)
    public static var NON_CONNECTE:Object = {no:0, listeCommandesPossibles:new Array(), listeEvenementsAcceptables:new Array()};

    // Constante objet indiquant que le joueur est connecté au serveur
    // physiquement, mais qu'on n'est pas connecté (authentifié) au serveur
    // de jeu
    public static var DECONNECTE:Object = {no:1, listeCommandesPossibles:new Array({nom:"ConnexionProf", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()}), listeEvenementsAcceptables:new Array()};

    // Constante objet indiquant que le joueur est connecté au serveur
    // physiquement et authentifié
    public static var CONNECTE:Object = {no:2, listeCommandesPossibles:new Array({nom:"Musique", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()},
	{nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()},
	{nom:"ObtenirListeSallesProf", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()}), 
    listeEvenementsAcceptables:new Array()};

    // Constante objet indiquant que le joueur a obtenu la liste des salles
    public static var LISTE_SALLES_OBTENUE:Object = {no:3, listeCommandesPossibles:new Array({nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()},
    {nom:"CreateRoom", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()},
	{nom:"getReport", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()},
	{nom:"ObtenirListeSallesProf", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()}),
	listeEvenementsAcceptables:new Array()};

      
    // Déclaration d'un tableau qui va contenir pour chaque numéro d'état l'objet
    // représentant l'état
    private static var lstEtats:Array = new Array(NON_CONNECTE, DECONNECTE, CONNECTE, LISTE_SALLES_OBTENUE);

    /**
     * Cette fonction permet de retourner un tableau contenant toutes les
     * commandes possibles à effectuer à partir de l'état dont le numéro est
     * passé en paramètres.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @return Array : Un tableau contenant les commandes pouvant être
     *                 effectuées
     */
    public static function obtenirCommandesPossibles(numeroEtat:Number):Array
    {
        // Si l'état existe bien, alors on retourne la liste des commandes
        // possibles pour l'état ayant le numéro passé en paramètres
        if (lstEtats[numeroEtat] != undefined)
        {
            return lstEtats[numeroEtat].listeCommandesPossibles;
        }
        else
        {
            // Sinon on retourne un tableau vide
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner un tableau contenant tous les
     * événements pouvant être acceptés immédiatement selon l'état courant
     * dont le numéro est passé en paramètres.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @return Array : Un tableau contenant les événements pouvant être
     *                 acceptés
     */
    public static function obtenirEvenementsAcceptablesEtat(numeroEtat:Number):Array
    {
        // Si l'état existe bien, alors on retourne la liste des événements
        // acceptables pour l'état ayant le numéro passé en paramètres
        if (lstEtats[numeroEtat] != undefined)
        {
            return lstEtats[numeroEtat].listeEvenementsAcceptables;
        }
        else
        {
            // Sinon on retourne un tableau vide
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner un tableau contenant tous les
     * événements pouvant être acceptés immédiatement selon l'état courant
     * dont le numéro est passé en paramètres et la commande.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @param String commande : Le nom de la commande en traitement
     * @return Array : Un tableau contenant les événements pouvant être
     *                 acceptés immédiatements
     */
    public static function obtenirEvenementsAcceptablesCommande(numeroEtat:Number, commande:String):Array
    {
        // Déclaration d'une référence vers la commande désirée
        var objCommandePossible:Object = obtenirCommande(numeroEtat, commande);

        // Si l'objet commande a été trouvé, alors on va retourner la liste
        // des événements acceptables, sinon on retourne une liste vide
        if (objCommandePossible != null)
        {
            return objCommandePossible.listeEvenementsAcceptables;
        }
        else
        {
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner un tableau contenant tous les
     * événements pouvant être acceptés seulement avant une commande selon
     * l'état courant dont le numéro est passé en paramètres et la commande.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @param String commande : Le nom de la commande en traitement
     * @return Array : Un tableau contenant les événements pouvant être
     *                 acceptés avant la commande passée en paramètres
     */
    public static function obtenirEvenementsAcceptablesAvant(numeroEtat:Number, commande:String):Array
    {
        // Déclaration d'une référence vers la commande désirée
        var objCommandePossible:Object = obtenirCommande(numeroEtat, commande);

        // Si l'objet commande a été trouvé, alors on va retourner la liste
        // des événements acceptables, sinon on retourne une liste vide
        if (objCommandePossible != null)
        {
            return objCommandePossible.listeEvenementsAcceptablesAvant;
        }
        else
        {
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner un tableau contenant tous les
     * événements pouvant être acceptés seulement après une commande selon
     * l'état courant dont le numéro est passé en paramètres et la commande.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @param String commande : Le nom de la commande en traitement
     * @return Array : Un tableau contenant les événements pouvant être
     *                 acceptés après la commande passée en paramètres
     */
    public static function obtenirEvenementsAcceptablesApres(numeroEtat:Number, commande:String):Array
    {
        // Déclaration d'une référence vers la commande désirée
        var objCommandePossible:Object = obtenirCommande(numeroEtat, commande);

        // Si l'objet commande a été trouvé, alors on va retourner la liste
        // des événements acceptables, sinon on retourne une liste vide
        if (objCommandePossible != null)
        {
            return objCommandePossible.listeEvenementsAcceptablesApres;
        }
        else
        {
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner l'objet de commande selon l'état
     * courant dont le numéro est passé en paramètres et selon la commande.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @param String commande : Le nom de la commande en traitement
     * @return Object : Un objet de commande correspondant à la commande passée
     *                  en paramètres
     */
    private static function obtenirCommande(numeroEtat:Number, commande:String):Object
    {
        // Déclaration d'une référence vers la commande désirée
        var objCommandePossible:Object = null;

        // Si l'état existe bien et que la commande est possible pour cet
        // état, alors on retourne l'objet de commande pour l'état ayant
        // le numéro passé en paramètres et pour la commande
        if (lstEtats[numeroEtat] != undefined)
        {
            // Déclaration d'un compteur
            var i:Number = 0;

            // Garder la référence vers l'objet état courant
            var objEtat:Object = lstEtats[numeroEtat];

            // Boucler tant qu'on n'a pas atteint la fin du tableau et qu'on
            // n'a pas trouvé la commande
            while (i < objEtat.listeCommandesPossibles.length && objCommandePossible == null)
            {
                // Si la commande courante dans la liste des commandes
                // possibles est la commande passée en paramètres, alors
                // on va garder la référence vers l'objet de commande
                if (objEtat.listeCommandesPossibles[i].nom == commande)
                {
                    objCommandePossible = objEtat.listeCommandesPossibles[i];
                }

                i++;
            }
        }

        return objCommandePossible;
    }
}
