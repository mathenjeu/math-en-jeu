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

//il faut implanter connect et send

import mx.events.EventDispatcher;

class DispatchingTunneling //extends DispatchingXMLSocket
{
    // Déclaration d'une fonction qui va prendre en paramètre une fonction
    // et qui va la garder en mémoire et l'appeler lorsque nécessaire
    public var addEventListener:Function;

    // Déclaration d'une fonction qui va permettre d'enlever une fonction
    // qui écoute l'événement du DispatchingXMLSocket
    public var removeEventListener:Function;

    // Déclaration d'une fonction qui va permettre d'envoyer un événement
    // aux fonctions qui ont été ajoutées par addEventListener
    private var dispatchEvent:Function;
	
	// Objet XML qui servira a stocker la reponse du serveur
	private var responseXML:XML;
	
	private var Connected:Boolean;
	
	private var server:String;
	private var port:Number;

    /**
     * Constructeur du DispatchingXMLSocket.
     */
    function DispatchingTunneling(server, port)
    {
        // Initialiser le dispatcher d'événements (ajoute les fonctions
        // addEventListener, removeEventListener et dispatchEvent)
        EventDispatcher.initialize(this);

        // Définir les fonctions devant être appelées lorsque les différents
        // événements de connexion, déconnexion, ... vont survenir
       /* this.onConnect = dispatchOnConnect;
        this.onData = dispatchOnData;
        this.onClose = dispatchOnClose;
        this.onXML = dispatchOnXML;*/
		this.server = server;
		this.port = port;
		
		responseXML = new XML();
		responseXML.onLoad = MessageReceived;
		Connected = false;

	}
	
	
	private function MessageReceived (Message)
	{
		if(Message)
		{
			trace("Connected using HTTP Tunneling!");
			//dispatchOnXML
			
/*******************************************************************************

Next thing to do: dispatchOnXML
S'assurer qu'on arrete d'essayer de se connecter a moment donner...

*******************************************************************************/
			
			Connected = true;
			dispatchOnConnect(true);
		}
	}
    
	
	public function connect ()
	{
		var NextTry:Number = -1;
		var ConnectionXML = new XML('<MESSAGE="Test"/>');

		while(!Connected)
		{
			if (NextTry < getTimer())
			{
				trace("Attempting to connect with HTTP Tunneling");
				NextTry = getTimer()+1000;
				ConnectionXML.sendAndLoad("newton.mat.ulaval.ca:6100", responseXML);
				//ConnectionXML.sendAndLoad("http://" add server add ":" add port add "/cgi-bin/message=", responseXML );
			}
		}
			
	}
	
	public function send (MessageXML)
	{
		//MessageXML.sendAndLoad("http://" add server add ":" add port add "/cgi-bin/message=", responseXML );
	}
	
	


    /**
     * Cette fonction va être appelée lorsque la connexion aura été effectuée
     * (soit elle a échoué, soit elle a réussie).
     *
     * @param Boolean succes : Permet de savoir si la connexion a réussie
     *                         ou si elle a échoué
     */
    private function dispatchOnConnect(succes:Boolean)
    {
        // Envoyer l'événement indiquant l'état de la connexion
        dispatchEvent({target:this, type:"connect", succes:succes});
    }

    /**
     * Cette fonction va être appelée lorsque des données vont être reçues.
     *
     * @param String donnees : Le message reçu sous forme de String
     */
    private function dispatchOnData(donnees:String)
    {
        // Envoyer l'événement indiquant l'arrivée de données en String
        dispatchEvent({target:this, type:"data", donnees:donnees});

        // Appeler la méthode onData du XMLSocket parent (sinon la fonction
        // dispatchOnXML ne sera pas appelée)
        super.onData(donnees);
    }

    /**
     * Cette fonction va être appelée lorsque des données XML vont être reçues.
     *
     * @param XML donneesXML : Le message reçu sous forme d'objet XML
     */
    private function dispatchOnXML(donneesXML:XML)
    {
        // Envoyer l'événement indiquant l'arrivée de données en XML
        dispatchEvent({target:this, type:"xml", donnees:donneesXML});
    }

    /**
     * Cette fonction va être appelée lorsque la connexion va être perdue.
     */
    private function dispatchOnClose()
    {
        // Envoyer l'événement indiquant la fin de la connexion avec le serveur
        dispatchEvent({target:this, type:"close"});
    }
}