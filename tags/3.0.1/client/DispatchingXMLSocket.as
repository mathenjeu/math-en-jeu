/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Generale Affero (AGPL), telle que publiee par
Affero Inc. ; soit la version 1 de la Licence, ou (a
votre discretion) une version ulterieure quelconque.

Ce programme est distribue dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans meme une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Generale Affero pour plus de details.

Vous devriez avoir recu un exemplaire de la Licence Publique
Generale Affero avec ce programme; si ce n'est pas le cas,
ecrivez a Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/



import mx.events.EventDispatcher;

class DispatchingXMLSocket extends XMLSocket
{
    // Declaration d'une fonction qui va prendre en parametre une fonction
    // et qui va la garder en memoire et l'appeler lorsque necessaire
    public var addEventListener:Function;

    // Declaration d'une fonction qui va permettre d'enlever une fonction
    // qui ecoute l'evenement du DispatchingXMLSocket
    public var removeEventListener:Function;

    // Declaration d'une fonction qui va permettre d'envoyer un evenement
    // aux fonctions qui ont ete ajoutees par addEventListener
    private var dispatchEvent:Function;

    /**
     * Constructeur du DispatchingXMLSocket.
     */
    function DispatchingXMLSocket()
    {
        // Initialiser le dispatcher d'evenements (ajoute les fonctions
        // addEventListener, removeEventListener et dispatchEvent)
        EventDispatcher.initialize(this);

        // Definir les fonctions devant etre appelees lorsque les differents
        // evenements de connexion, deconnexion, ... vont survenir
        this.onConnect = dispatchOnConnect;
        this.onData = dispatchOnData;
        this.onClose = dispatchOnClose;
        this.onXML = dispatchOnXML;
    }

    /**
     * Cette fonction va etre appelee lorsque la connexion aura ete effectuee
     * (soit elle a echoue, soit elle a reussie).
     *
     * @param Boolean succes : Permet de savoir si la connexion a reussie
     *                         ou si elle a echoue
     */
    private function dispatchOnConnect(succes:Boolean)
    {
        // Envoyer l'evenement indiquant l'etat de la connexion
        dispatchEvent({target:this, type:"connect", succes:succes});
    }

    /**
     * Cette fonction va etre appelee lorsque des donnees vont etre recues.
     *
     * @param String donnees : Le message recu sous forme de String
     */
    private function dispatchOnData(donnees:String)
    {
        // Envoyer l'evenement indiquant l'arrivee de donnees en String
        dispatchEvent({target:this, type:"data", donnees:donnees});

        // Appeler la methode onData du XMLSocket parent (sinon la fonction
        // dispatchOnXML ne sera pas appelee)
        super.onData(donnees);
    }

    /**
     * Cette fonction va etre appelee lorsque des donnees XML vont etre recues.
     *
     * @param XML donneesXML : Le message recu sous forme d'objet XML
     */
    private function dispatchOnXML(donneesXML:XML)
    {
        // Envoyer l'evenement indiquant l'arrivee de donnees en XML
        dispatchEvent({target:this, type:"xml", donnees:donneesXML});
        //super.onXML(donneesXML);
    }

    /**
     * Cette fonction va etre appelee lorsque la connexion va etre perdue.
     */
    private function dispatchOnClose()
    {
        // Envoyer l'evenement indiquant la fin de la connexion avec le serveur
        dispatchEvent({target:this, type:"close"});
    }
}