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




import mx.events.EventDispatcher;
import mx.utils.Delegate;

class Timer
{
    // Déclaration d'une variable qui va garder le ID de l'intervalle
    private var intIDIntervalle:Number;

    // Déclaration d'une variable qui va garder le temps total du timer
    private var intTempsTotal:Number;

    // Déclaration d'une variable qui va permettre de savoir si le timer
    // est en activité ou non
    private var bolEstActif:Boolean;

    // Déclaration d'une fonction qui va prendre en paramètre une fonction
    // et qui va la garder en mémoire et l'appeler lorsque le Timer finit
    public var addEventListener:Function;

    // Déclaration d'une fonction qui va permettre d'enlever une fonction
    // qui écoute l'événement du Timer
    public var removeEventListener:Function;

    // Déclaration d'une fonction qui va permettre d'envoyer un événement
    // aux fonctions qui ont été ajoutées par addEventListener
    public var dispatchEvent:Function;

    /**
     * Constructeur du Timer qui prend en paramètre un temps total. L'événement
     * sera déclenché à la fin de ce temps.
     *
     * @param Number temps : Le temps total du Timer
     */
    function Timer(temps:Number)
    {
        // Initialiser le dispatcher d'événements (ajoute les fonctions
        // addEventListener, removeEventListener et dispatchEvent)
        EventDispatcher.initialize(this);

        // Garder en mémoire le temps total du timer
        intTempsTotal = temps;

        // Au début le timer n'est pas actif
        bolEstActif = false;
    }

    /**
     * Cette événement est appelé lorsque le temps a été écoulé entièrement
     */
    private function this_tempsEcoule()
    {
        // Appeler la fonction qui va s'occuper d'envoyer l'événement de fin du timer
        dispatchEvent({type:'timeout', target:this});
    }

    /**
     * Cette méthode permet de démarrer le timer selon le temps passé en
     * paramètres lors de sa création. Le timer est démarré seulement s'il
     * n'est pas actif.
     */
    public function demarrer()
    {
        // On démarre le timer seulement s'il n'est pas actif
        if (bolEstActif == false)
        {
            // Définir l'intervalle de temps et ajouter une référence vers la
            // fonction tempsEcoule
            intIDIntervalle = setInterval(Delegate.create(this, this_tempsEcoule), intTempsTotal);

            // Le timer est en activité
            bolEstActif = true;
        }
    }

    /**
     * Cette méthode permet d'arrêter le timer. Si le timer n'est pas en
     * activité, alors il n'est pas arrêté.
     */
    public function arreter()
    {
        // On arrête le timer seulement s'il est actif
        if (bolEstActif == true)
        {
            // Arrêter et libérer l'intervalle
            clearInterval(intIDIntervalle);

            // Le timer n'est plus en activité
            bolEstActif = false;
        }
    }


    /**
     * Cette fonction permet de savoir si le timer est présentement actif.
     *
     * @return Boolean : true s'il est actif
     *                   false sinon
     */
    public function estActif():Boolean
    {
        return bolEstActif;
    }

	
}