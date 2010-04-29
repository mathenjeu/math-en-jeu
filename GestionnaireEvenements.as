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

import mx.transitions.Tween;
import mx.transitions.easing.*;
import mx.utils.Delegate;
import FiltreTable;
import mx.controls.Alert;
import flash.utils.*;
import flash.geom.Transform;
import flash.geom.ColorTransform;
import NewsBox;

class GestionnaireEvenements
{
    private var roomDescription:String;  // short room description taked from DB
	private var nomUtilisateur:String;    // user name of our  user
	private var userRole:Number;  // if 1 - simple user, if 2 - is admin(master), if 3 - is  prof
	private var numeroDuPersonnage:Number; // sert a associer la bonne image pour le jeu d'ile au tresor
	
    public var  listeDesPersonnages:Array;   // liste associant les idPersonnage avec les nomUtilisateurs dans la table ou on est
	                                         // contient aussi autre informations sur les joueurs, comme couleur, role, pointage, argent ...
											 // un tableau complet avec toutes les informations sur les joueurs
    private var motDePasse:String;  // notre mot de passe pour pouvoir jouer
    private var nomSalle:String;  //  nom de la salle dans laquelle on est
	private var idRoom:Number;    // ID of our room in the server's list of rooms
	private var masterTime:Number; // masterTime of room, if masterTime != 0 is taked masterTime for the time of game 
	private var clientType:Number;  // if 1 is game, if 2 is prof's module
    private var numeroTable:Number;   //   numero de la table dans laquelle on est
	private var tablName:String;     // name of the created table
    private var tempsPartie:Number;   //  temps que va durer la partie, en minutes
    private var idPersonnage:Number;   //  
    private var listeDesJoueursDansSalle:Array;  // liste des joueurs dans la salle qu'on est. Un joueur contient un nom (nom) ???? 
    
	private var playersNumber:Number;   // ????????
	public var  listeDesTypesDeJeu:Array; //liste des TypesDeJeu de salles
	public var  listeDesSalles:Array;    //  liste de toutes les salles                !!!! Combiner ici tout dans un Objet
	private var listeNumeroJoueursSalles:Array;		//liste de numero de joueurs dans chaque salle
	private var maxPlayersInTable:Number = 0; // Number max of players by table in the room where we are  
	
	private var listeChansons:Array;    //  liste de toutes les chansons
    private var listeDesJoueursConnectes:Array;   // la premiere liste qu'on recoit, tous les joueurs dans toutes les salles. Un joueur contient un nom (nom)
    
	//liste de toutes les tables dans la salle ou on est
    //contient un numero (noTable), le temps (temps) et une liste de joueurs (listeJoueurs) un joueur de la liste contient un nom (nom)
    private var listeDesTables:Array;   // list of tables in our room with list of users in 
    private var objGestionnaireCommunication:GestionnaireCommunication;  //  pour caller les fonctions du serveur 
	private var tabPodiumOrdonneID:Array;			// id des personnages ordonnes par pointage une fois la partie terminee
	private var pointageMinimalWinTheGame:Number = -1 // pointage minimal a avoir droit d'atteindre le WinTheGame
	
	public  var typeDeJeu:String = "mathEnJeu";
	private var moveVisibility:Number;  // The number of cases that user can move. At the begining is 3. 
	                                    // With the 3 running correct answers the level increase by 1 
	private var langue;
	private var endGame:Boolean;   // used to ignore the movement of virtual players after the end of the game
	private var newsChat:NewsBox;  // all the messages to show in newsbox
	private var nbTracks:Number;
	private var finishPoints:Array;
	
	private var braniacState:String;
	private var bananaState:Boolean;
	
	//used to color clothes of our perso
	private var colorIt:String;
	
	function affichageChamps()
	{
		trace("------ debut affichage ------");
		trace("nomUtilisateur : " + nomUtilisateur);
		trace("numeroDuPersonnage : " + numeroDuPersonnage);	
		trace("listeDesPersonnages : " + listeDesPersonnages);	
		trace("motDePasse : " + motDePasse);	
		trace("nomSalle : " + nomSalle);	
		trace("numeroTable : " + numeroTable);	
		trace("tempsPartie : " + tempsPartie);	
		trace("idPersonnage : " + idPersonnage);	
		//trace("motDePasseSalle : " + motDePasseSalle);	
		trace("listeDesJoueursDansSalle : " + listeDesJoueursDansSalle);	
		trace("listeDesSalles : " + listeDesSalles);	
		trace("listeChansons : " + listeChansons);	
		trace("listeDesJoueursConnectes : " + listeDesJoueursConnectes);	
		trace("listeDesTables : " + listeDesTables);	
		trace("tabPodiumOrdonneID : " + tabPodiumOrdonneID);
		trace("------  fin affichage  ------");	
	}
		
	function obtenirNomUtilisateur()
	{
		return this.nomUtilisateur;
	}
	
	function obtenirMotDePasse()
	{
		return this.motDePasse;
	}

	function obtenirGestComm()
	{
		return this.objGestionnaireCommunication;
	}
	
    function obtenirPointageMinimalWinTheGame():Number
	{
		return this.pointageMinimalWinTheGame;
	}
	
	function setPointageMinimalWinTheGame(ptMin:Number)
    {
    	this.pointageMinimalWinTheGame = ptMin;
    }
	
	function getNbTracks():Number
	{
		return this.nbTracks;
	}
	
	function setNbTracks(nTracks:Number)
    {
    	this.nbTracks = nTracks;
    }
	
	function getListeFinishPoints():Array
	{
		return this.finishPoints;
	}
	
	function setListeFinishPoints(a:Array)
	{
		this.finishPoints = a;
	}
	
	function getColorIt():String
	{
		return this.colorIt;
	}
	
	function setColorIt(color:String)
    {
    	this.colorIt = color;
    }
	
	function getBraniacState():String
	{
		return this.braniacState;
	}
	
	function setBraniacState(stateBr:String)
    {
    	this.braniacState = stateBr;
    }


    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                  CONSTRUCTEUR
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function GestionnaireEvenements(nom:String, passe:String, langue:String, client:Number)
    {
        trace("*********************************************");
        trace("debut du constructeur de gesEve      " + nom + "      " + passe);
        this.nomUtilisateur = nom;
        this.listeDesPersonnages = new Array();
       	this.clientType = client;
	    this.motDePasse = passe;
		this.langue = langue;
        this.nomSalle = new String();
        this.listeDesSalles = new Array();
		this.listeNumeroJoueursSalles = new Array();
		this.listeDesTypesDeJeu = new Array();
        this.listeDesTables = new Array();
		this.listeChansons = new Array();
        this.listeDesJoueursConnectes = new Array();
        this.listeDesJoueursDansSalle = new Array();
		this.tabPodiumOrdonneID = new Array();
		this.moveVisibility = 3;
		this.endGame = false;
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port, 10);
		
		this.newsChat = new NewsBox();
		this.colorIt = "0";
		this.bananaState = false;
				
        this.objGestionnaireCommunication = new GestionnaireCommunication(Delegate.create(this, this.evenementConnexionPhysique), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
	
    	trace("fin du constructeur de gesEve");
    	trace("*********************************************\n");
    }
	
	function getTableName():String 
	{
		return this.tablName;
	}
    
	
	function obtenirNumeroJoueurs():Number 
	{
		return this.maxPlayersInTable;
	}
    
    
	function obtenirTabPodiumOrdonneID():Array
	{
		return this.tabPodiumOrdonneID;
	}
	
	function obtenirListeChansons():Array
	{
		return this.listeChansons;
	}
	
	function setListeChansons(a:Array)
	{
		this.listeChansons = a;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirNumeroDuPersonnage():Number
	{
		return this.numeroDuPersonnage;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirNumeroJoueursDansSalle():Number
	{
		return this.maxPlayersInTable;
	}
	////////////////////////////////////////////////////////////
	function definirNumeroDuPersonnage(n:Number)
	{
		this.numeroDuPersonnage = n;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	function utiliserPortSecondaire()
	{
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server_secondaire;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port_secondaire, 10);
		
        this.objGestionnaireCommunication = new GestionnaireCommunication(Delegate.create(this, this.evenementConnexionPhysique2), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	function tryTunneling()
	{
		//var url_serveur:String = _level0.configxml_mainnode.attributes.url_server_tunneling;
		//var port:Number = parseInt(_level0.configxml_mainnode.attributes.port_tunneling, 10);
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server_secondaire;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port_secondaire, 10);
		
        //this.objGestionnaireCommunication = new GestionnaireCommunicationTunneling(Delegate.create(this, this.evenementConnexionPhysiqueTunneling), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
		this.objGestionnaireCommunication = new GestionnaireCommunication(Delegate.create(this, this.evenementConnexionPhysiqueTunneling), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
	}
	
	 ///////////////////////////////////////////////////////////////////////////////////////////////////
    function beginNewGame()
    {
        trace("*********************************************");
        trace("begin New Game");
        this.objGestionnaireCommunication.beginNewGame(Delegate.create(this, this.feedbackBeginNewGame));
        trace("*********************************************\n");
    }
	
	 ///////////////////////////////////////////////////////////////////////////////////////////////////
    function restartOldGame()
    {
	    trace("*********************************************");
        trace("restart Old Game");
        this.objGestionnaireCommunication.restartOldGame(Delegate.create(this, this.feedbackRestartOldGame), Delegate.create(this, this.evenementJoueurEntreTable), Delegate.create(this, this.evenementJoueurQuitteTable), Delegate.create(this, this.evenementPartieDemarree), Delegate.create(this, this.evenementJoueurDeplacePersonnage), Delegate.create(this, this.evenementSynchroniserTemps), Delegate.create(this, this.evenementUtiliserObjet), Delegate.create(this, this.evenementPartieTerminee), Delegate.create(this, this.evenementJoueurRejoindrePartie));  // , Delegate.create(this, this.feedbackRestartListePlayers)
        trace("end restart Old Game");
        trace("*********************************************\n");
    }
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
    function createRoom(nameRoom:String, description:String, pass:String, fromDate:String, toDate:String, defaultTime:String, roomCategories:String)
    {
        trace("*********************************************");
        trace("debut de createRoom     :" + nameRoom + " " + toDate + " " + defaultTime);
        this.objGestionnaireCommunication.createRoom(Delegate.create(this, this.retourCreateRoom), nameRoom, description, pass, fromDate, toDate, defaultTime, roomCategories);
        trace("fin de createRoom");
        trace("*********************************************\n");
    }

	///////////////////////////////////////////////////////////////////////////////////////////////////
    function getReport(idRoom:Number)
    {
        trace("*********************************************");
        trace("begin of getReport     :" + idRoom);
        this.objGestionnaireCommunication.getReport(Delegate.create(this, this.retourGetReport), idRoom);
        trace("end getReport");
        trace("*********************************************\n");
    }
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
    function reportBugQuestion(description:String)
    {
        trace("*********************************************");
        trace("reportBugQuestion     : ");
        this.objGestionnaireCommunication.reportBugQuestion(Delegate.create(this, this.retourReportBugQuestion), description);
        trace("end reportBugQuestion");
        trace("*********************************************\n");
    }
	
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function entrerSalle(roomId:Number)
    {
        trace("*********************************************");
        trace("debut de entrerSalle      " + roomId);
        var guiPWD:MovieClip;  
		
		var motDePasseSalle:String = "";
		var finded:Boolean = false;
		var count:Number = listeDesSalles.length;
        
        for(var i:Number = 0; i < count; i++)
        {
            if(listeDesSalles[i].idRoom == roomId)
            {
	            maxPlayersInTable = listeDesSalles[i].maxnbplayers;
	            typeDeJeu = listeDesSalles[i].typeDeJeu;
	            this.idRoom = listeDesSalles[i].idRoom;
				this.nomSalle = listeDesSalles[i].nom;
                if(listeDesSalles[i].possedeMotDePasse == true)
                {
					
	               guiPWD = _level0.loader.contentHolder.attachMovie("GUI_pwd", "guiPWD", 2003);//_level0.loader.contentHolder.getNextHighestDepth());
                   guiPWD.textGUI_PWD.text = _root.texteSource_xml.firstChild.attributes.textGUI_PWD;
					
                   
			     }else{
					   this.objGestionnaireCommunication.entrerSalle(Delegate.create(this, this.retourEntrerSalle), this.idRoom, motDePasseSalle);
				 }
                
                break;
            }
        }
		
        
        trace("fin de entrerSalle");
        trace("*********************************************\n");
    }
	
	 ///////////////////////////////////////////////////////////////////////////////////////////////////
    function entrerSallePWD(pwd:String)
    {
        trace("*********************************************");
        trace("debut de entrerSallePWD      " + pwd);
        
		
		   this.objGestionnaireCommunication.entrerSalle(Delegate.create(this, this.retourEntrerSalle), this.idRoom, pwd);
				
		
        
        trace("fin de entrerSallePWD");
        trace("*********************************************\n");
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function entrerTable(nTable:Number)
    {
        trace("*********************************************");
        trace("debut de entrerTable     :" + nTable);
        this.numeroTable = nTable;
        this.objGestionnaireCommunication.entrerTable(Delegate.create(this, this.retourEntrerTable), Delegate.create(this, this.evenementJoueurDemarrePartie), this.numeroTable);
        trace("fin de entrerTable");
        trace("*********************************************\n");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function sortirSalle()
    {
		trace("*********************************************");
        trace("debut de sortirSalle");
        this.objGestionnaireCommunication.quitterSalle(Delegate.create(this, this.retourQuitterSalle));
        trace("fin de sortirSalle");
        trace("*********************************************\n");
    }
	

	///////////////////////////////////////////////////////////////////////////////////////////////////
	/*public */
	function deconnexion()
	{
		trace("*********************************************");
        trace("debut de deconnexion");
		this.objGestionnaireCommunication.deconnexion(Delegate.create(this, this.retourDeconnexion));
		trace("fin de deconnexion");
        trace("*********************************************\n");
	}
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function sortirTable()
    {
        trace("*********************************************");
        trace("debut de sortirTable");
        this.objGestionnaireCommunication.quitterTable(Delegate.create(this, this.retourQuitterTable));
        trace("fin de sortirTable");
        trace("*********************************************\n");
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function creerTable(temps:Number, nb_lines:Number, nb_columns:Number, nameTable:String)
    {
        trace("*********************************************");
        trace("debut de creerTable     " + temps);
        this.objGestionnaireCommunication.creerTable(Delegate.create(this, this.retourCreerTable), Delegate.create(this, this.evenementJoueurDemarrePartie), temps, nb_lines, nb_columns, nameTable);
        trace("fin de creerTable");
        trace("*********************************************\n");
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function demarrerPartie(idDessin:Number) 
    {
        trace("*********************************************");
        trace("debut de demarrerPartie     " + idDessin); 
        this.listeDesPersonnages.push(new Object());
        this.listeDesPersonnages[this.listeDesPersonnages.length - 1].id = 0;
        this.listeDesPersonnages[this.listeDesPersonnages.length - 1].nom = this.nomUtilisateur;
		this.listeDesPersonnages[this.listeDesPersonnages.length - 1].role = this.userRole;
		this.listeDesPersonnages[this.listeDesPersonnages.length - 1].pointage = 0;
		this.listeDesPersonnages[this.listeDesPersonnages.length - 1].idessin = idDessin;
		this.listeDesPersonnages[this.listeDesPersonnages.length - 1].win = 0;
		this.listeDesPersonnages[this.listeDesPersonnages.length - 1].clocolor = this.colorIt;
		//this.listeDesPersonnages[numeroJoueursDansSalle-1].lastPoints = 0;
		
        this.objGestionnaireCommunication.demarrerPartie(Delegate.create(this, this.retourDemarrerPartie), Delegate.create(this, this.evenementPartieDemarree), Delegate.create(this, this.evenementJoueurDeplacePersonnage), Delegate.create(this, this.evenementSynchroniserTemps), Delegate.create(this, this.evenementUtiliserObjet), Delegate.create(this, this.evenementPartieTerminee), Delegate.create(this, this.evenementJoueurRejoindrePartie),  idDessin, colorIt);//this.idPersonnage);//  
	
		trace("fin de demarrerPartie");
        trace("*********************************************\n");
    }
	
	/////////////////////////////////////////////////////////////////////////////////////////////////// 
    function demarrerMaintenant(niveau:String)
    {
        trace("*********************************************");
        trace("debut de demarrerMaintenant");
		trace("idPersonnage: " + this.idPersonnage);
		//trace("niveau des personnages virtuels : " + niveau);
        this.objGestionnaireCommunication.demarrerMaintenant(Delegate.create(this, this.retourDemarrerMaintenant), niveau);
        trace("fin de demarrerMaintenant");
        trace("*********************************************\n");
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function deplacerPersonnage(pt:Point)
    {
        trace("*********************************************");
        trace("debut de deplacerPersonnage     " + pt.obtenirX() + "     " + pt.obtenirY());
        this.objGestionnaireCommunication.deplacerPersonnage(Delegate.create(this, this.retourDeplacerPersonnage), _level0.loader.contentHolder.planche.calculerPositionOriginale(pt.obtenirX(), pt.obtenirY()));  
        // to correct the state 
		_level0.loader.contentHolder.planche.obtenirPerso().minigameLoade = false;
	    trace("fin de deplacerPersonnage");
        trace("*********************************************\n");
    }
   
    ///////////////////////////////////////////////////////////////////////////////////////////////////   
	function acheterObjet(id:Number)
    {
        trace("*********************************************");
        trace("debut de acheterObjet : " + id);
        this.objGestionnaireCommunication.acheterObjet(Delegate.create(this, this.retourAcheterObjet), id);  
        trace("fin de acheterObjet");
        trace("*********************************************\n");
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////    
	function utiliserObjet(id:Number, objectName:String)
    {
        trace("*********************************************");
        trace("debut de utiliserObjet : " + id + " name : " + objectName );
		
			this.objGestionnaireCommunication.utiliserObjet(Delegate.create(this, this.retourUtiliserObjet), id, objectName);  
	   
        trace("fin de utiliserObjet");
        trace("*********************************************\n");
    }
	
	///////////////////////////////////////////////////////////////////////////////////////////////////   
	public function definirPointageApresMinigame(points:Number)
	{
		trace("*********************************************");
		trace("debut de definirPointageApresMinigame  ds gestEve   " + points);
		this.objGestionnaireCommunication.definirPointageApresMinigame(Delegate.create(this, this.retourDefinirPointageApresMinigame), points);  
		trace("fin de definirPointageApresMinigame");
		trace("*********************************************\n");
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////   
	public function definirArgentApresMinigame(argent:Number)
	{
		trace("*********************************************");
		trace("debut de definirArgentApresMinigame  ds gestEve   " + argent);
		this.objGestionnaireCommunication.definirArgentApresMinigame(Delegate.create(this, this.retourDefinirArgentApresMinigame), argent);  
		trace("fin de definirPointageApresMinigame");
		trace("*********************************************\n");
	}
	   
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public function repondreQuestion(str:String)
    {
        trace("*********************************************");
        trace("debut de repondreQuestion     " + str);
        this.objGestionnaireCommunication.repondreQuestion(Delegate.create(this, this.retourRepondreQuestion), str);  
        trace("fin de repondreQuestion");
        trace("*********************************************\n");
    }
	
	 ///////////////////////////////////////////////////////////////////////////////////////////////////
    function cancelQuestion()
    {
        trace("*********************************************");
        trace("CancelQuestion");
        this.objGestionnaireCommunication.cancelQuestion(Delegate.create(this, this.returnCancelQuestion));
        trace("end CancelQuestion");
        trace("*********************************************\n");
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                  fonctions retour
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // c'est bon ?????  ca va pas dans le html ??
    public function retourObtenirChoixLangage(objetEvenement:Object)
    {
        trace("*********************************************");
        trace("debut de retourObtenirChoixLanguage     "+objetEvenement.resultat);
        //   objetEvenement.resultat = ChoixLangages, CommandeNonReconnue, ParametrePasBon
        switch(objetEvenement.resultat)
        {
            case "ChoixLangues":
                // choix de la langue
                trace("Choix de la langue obtenue");
            break;
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
            case "ParamettrePasBon":
                trace("ParamettrePasBon");
            break;
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourObtenirChoixLanguage");
        trace("*********************************************\n");
    }
    //    etiquettes de langue
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirNomsInterface(objetEvenement:Object)
    {
        trace("*********************************************");
        trace("debut de retourObtenirNomsInterface     "+objetEvenement.resultat);

		switch(objetEvenement.resultat)
        {
            case "ListeNomsInterfaces":
            break;
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
            case "LangageNonConnu":
                trace("Langage non connu");
            break;
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourObtenirNomsInterface");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourConnexion(objetEvenement:Object)
    {
    	// c'est la fonction qui va etre appellee lorsque le GestionnaireCommunication aura
        // recu la reponse du serveur
        // objetEvenement est un objet qui est propre a chaque fonction comme retourConnexion
        // (en termes plus informatiques, on appelle ca un eventHandler -> fonction qui gere
        // les evenements). Selon la fonction que vous appelerez, il y aura differentes valeurs
        // dedans. Ici, il y a juste une valeur qui est succes qui est de type booleen
    	// objetEvenement.resultat = Ok, JoueurNonConnu, JoueurDejaConnecte
    	trace("*********************************************");
    	trace("debut de retourConnexion     " + objetEvenement.resultat);
        // param:  userRoleMaster == 1 if simple user or 2 if master
    	var dejaConnecte:MovieClip;
		var isOldGame:MovieClip;
    
        switch(objetEvenement.resultat)
        {
			case "OkEtPartieDejaCommencee":
			
			trace("Q musique " + objetEvenement.listeChansons.length);
				
				var count:Number = objetEvenement.listeChansons.length;
				for(var k:Number = 0;  k < count; k++)
				{
					this.listeChansons.push(objetEvenement.listeChansons[k]);
					trace(objetEvenement.listeChansons[k]);
				}
				
				this.userRole = objetEvenement.userRoleMaster; 
				//musique();
				
				isOldGame = _level0.loader.contentHolder.attachMovie("GUI_oldGame", "restartGame", 9999);
				//isOldGame.textGUI_erreur.text = _root.texteSource_xml.firstChild.attributes.GUIdejaConnecte;
				
				
				trace("La connexion a marche");
			break;

			case "Musique":
				this.objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), Delegate.create(this, this.evenementJoueurConnecte), Delegate.create(this, this.evenementJoueurDeconnecte));

				//trace("objEvenement");
				trace("Q musique " + objetEvenement.listeChansons.length);
				var count:Number = objetEvenement.listeChansons.length;
				for(var k:Number = 0;  k < count; k++)
				{
					this.listeChansons.push(objetEvenement.listeChansons[k]);
					trace(objetEvenement.listeChansons[k]);
				}
				
				this.userRole = objetEvenement.userRoleMaster; 
				//musique();
				
				trace("La connexion a marche");
			break;
			 
            case "JoueurNonConnu":
                trace("Joueur non connu");
            break;
             
			case "JoueurDejaConnecte":
	  			_level0.loader._visible = true;
				_level0.bar._visible = false;

				//_root.dejaConnecte_txt._visible = true;
				_root.texteSalle._visible = false;
						
	     		dejaConnecte = _level0.loader.contentHolder.attachMovie("GUI_erreur", "DejaConnecte", 9999);
				dejaConnecte.linkGUI_erreur._visible = false;
				dejaConnecte.btn_ok._visible = false;
			
				dejaConnecte.textGUI_erreur.text = _root.texteSource_xml.firstChild.attributes.GUIdejaConnecte;
			
                trace("Joueur deja connecte");
            break;
	     
            default:
            	trace("Erreur Inconnue");
        }
		objetEvenement = null;
     	trace("fin de retourConnexion");
     	trace("*********************************************\n");
    }
	//********** new code *************************************************
	
	 public function feedbackBeginNewGame(objetEvenement:Object)
    {
    	// c'est la fonction qui va etre appellee lorsque le GestionnaireCommunication aura
        // recu la reponse du serveur
        // objetEvenement est un objet qui est propre a chaque fonction comme retourConnexion
        // (en termes plus informatiques, on appelle ca un eventHandler -> fonction qui gere
        // les evenements). Selon la fonction que vous appelerez, il y aura differentes valeurs
        // dedans. Ici, il y a juste une valeur qui est succes qui est de type booleen
    	// objetEvenement.resultat = Ok
    	trace("*********************************************");
    	trace("debut de feedbackBeginNewGame    " + objetEvenement.resultat);
      
        switch(objetEvenement.resultat)
        {
			case "Ok":
			
			trace("<<<<<<<<<<<<<<<<  reconnexion with new game >>>>>>>>>>>>>>>>>>>");
			this.objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), Delegate.create(this, this.evenementJoueurConnecte), Delegate.create(this, this.evenementJoueurDeconnecte));
			_level0.loader.contentHolder["restartGame"].removeMovieClip();
			
			break;
	     
            default:
            	trace("Erreur Inconnue");
        }
		objetEvenement = null;
     	trace("fin de feedbackBeginNewGame");
     	trace("*********************************************\n");
    }
	
	public function feedbackRestartOldGame(objetEvenement:Object)
    {
    	// c'est la fonction qui va etre appellee lorsque le GestionnaireCommunication aura
        // recu la reponse du serveur
        // objetEvenement est un objet qui est propre a chaque fonction comme retourConnexion
        // (en termes plus informatiques, on appelle ca un eventHandler -> fonction qui gere
        // les evenements). Selon la fonction que vous appelerez, il y aura differentes valeurs
        // dedans. Ici, il y a juste une valeur qui est succes qui est de type booleen
    	// objetEvenement.resultat = Ok, ListeJoueurs, pointage, Argent, ListeObjets
    	trace("*********************************************");
    	trace("debut de feedbackRestartOldGame    " + objetEvenement.resultat + " " );
        var i:Number;
        switch(objetEvenement.resultat)
        {
			case "ListeJoueurs":
			   this.listeDesPersonnages.removeAll();
			   _level0.loader.contentHolder["restartGame"].removeMovieClip();
			   _level0.loader.contentHolder.gestionBoutons(false);
			   		 
			   this.playersNumber = objetEvenement.playersListe.length;
			   this.typeDeJeu = "mathEnJeu";
			   this.endGame = false;
			   var count:Number = objetEvenement.playersListe.length;
			  
        	   for(i = 0; i < count; i++)
               {
                  this.listeDesJoueursConnectes.push(objetEvenement.playersListe[i]);
                  this.listeDesPersonnages.push(new Object());
			      this.listeDesPersonnages[i].nom = objetEvenement.playersListe[i].nom;
                  this.listeDesPersonnages[i].id = objetEvenement.playersListe[i].idPersonnage;
			      this.listeDesPersonnages[i].role = objetEvenement.playersListe[i].userRole;
			      this.listeDesPersonnages[i].pointage = objetEvenement.playersListe[i].pointage;
				  //this.listeDesPersonnages[i].clocolor = objetEvenement.playersListe[i].clocolor;
			      this.listeDesPersonnages[i].win = 0;
				  					
		          var idDessin:Number = calculatePicture(this.listeDesPersonnages[i].id);
				  
				    if(idDessin != 0)
					{
					   this.listeDesPersonnages[i].idessin = idDessin;
                     
					}
			   }// end for
			   
			   var count:Number = this.listeDesPersonnages.length;
			   for(i = 0; i < count; i++)
               {
                  if(this.listeDesPersonnages[i].nom == this.nomUtilisateur)
                  {
						this.idPersonnage = this.listeDesPersonnages[i].id;
						this.numeroDuPersonnage = this.listeDesPersonnages[i].idessin;
						
				  }
			   }
			break;
			
			// realy speacking can be removed - we have "pointage" in the up one case 
			case "Pointage":
			    
			   _level0.loader.contentHolder.planche.obtenirPerso().modifierPointage(objetEvenement.pointage);
			   
			   var count:Number = this.listeDesPersonnages.length;
			   for(i = 0; i < count; i++)
               {
                  if(this.listeDesPersonnages[i].nom == this.nomUtilisateur)
                  {
						this.listeDesPersonnages[i].pointage = objetEvenement.pointage;
						
				  }
			   }
			
			   remplirMenuPointage();
			
			break;
			
			case "Argent":
						
			   _level0.loader.contentHolder.planche.obtenirPerso().modifierArgent(objetEvenement.argent);
			
			break;
			
			case "Table":
						
			   this.numeroTable = objetEvenement.noTable;
			   //trace("rejoindre table : " +  this.numeroTable);
			
			break;
			
			case "ListeObjets":
				var count:Number = objetEvenement.objectsListe.length;
			   for(i = 0; i < count; i++)
               {
                   _level0.loader.contentHolder.planche.obtenirPerso().ajouterObjet(objetEvenement.objectsListe[i].idObject, objetEvenement.objectsListe[i].typeObject);
				   //trace(objetEvenement.objectsListe[i].idObject + "  " + objetEvenement.objectsListe[i].typeObject);
			   }// end for
			   
			break;
			
			case "Ok":
			
			    // newsbox
		        //_level0.loader.contentHolder.newsbox_mc.newsone = this.newsArray[this.newsArray.length - 1];
		        var messageInfo:String = objetEvenement.nomUtilisateur + _root.texteSource_xml.firstChild.attributes.restartMess; 
				this.newsChat.addMessage(messageInfo);
		        //this.newsArray[newsArray.length] = messageInfo;
		        //_level0.loader.contentHolder.newsbox_mc.newstwo = this.newsArray[this.newsArray.length - 1];
		        //_level0.loader.contentHolder.orderId = 0;
			   trace("<<<<<<<<<<<<<<<<  feedbackRestartOldGame  finish restart >>>>>>>>>>>>>>>>>>>" + this.numeroTable);
			
			break;
	     
            default:
            	trace("resultat Inconnue");
        }
		objetEvenement = null;
     	trace("fin de feedbackRestartOldGame");
     	trace("*********************************************\n");
    }// end methode
	
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirListeJoueurs(objetEvenement:Object)
    {
        //  objetEvenement.resultat = ListeJoueurs, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourObtenirListeJoueurs   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {  
	        
            case "ListeJoueurs":
            	this.playersNumber = objetEvenement.listeNomUtilisateurs.length;
        		        		
                for(var i:Number = 0; i < this.playersNumber ; i++)
                {
                    this.listeDesJoueursConnectes.push(objetEvenement.listeNomUtilisateurs[i]);
                }
                this.objGestionnaireCommunication.obtenirListeSalles(Delegate.create(this, this.retourObtenirListeSalles), Delegate.create(this, this.evenementNouvelleSalle), this.clientType);
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
             
			default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourObtenirListeJoueur");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirListeSalles(objetEvenement:Object)
    {
        //   objetEvenement.resultat = ListeSalles, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourObtenirListeSalles   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "ListeSalles":
			    this.listeDesSalles.removeAll();
				var count:Number = objetEvenement.listeNomSalles.length;
                for (var i:Number = 0; i < count; i++)
                {
					
					this.listeDesSalles.push(objetEvenement.listeNomSalles[i]);
					_level0.loader.contentHolder.listeSalle.addItem({label: this.listeDesSalles[i].nom, data:  this.listeDesSalles[i].idRoom});
					trace("salle " + i + " : " + this.listeDesSalles[i].nom);
					//_level0.listeRooms.addItem(this.listeDesSalles[i].nom );
															
					this.listeNumeroJoueursSalles.push(objetEvenement.listeNumberoJSalles[i]);
					_level0.loader.contentHolder.listeNumeroJSalles.push(this.listeNumeroJoueursSalles[i].maxnbplayers );
					trace("salle " + i + " : " + this.listeDesSalles[i].maxnbplayers + " " + objetEvenement.listeNomSalles[i].maxnbplayers);
					
					this.listeDesTypesDeJeu.push(objetEvenement.typeDeJeuAll[i]);
					_level0.loader.contentHolder.listeDesTypesDeJeu.push(this.listeDesTypesDeJeu[i].typeDeJeu );
					trace("salle " + i + " : " + objetEvenement.typeDeJeuAll[i].typeDeJeu + " ~ " +this.listeDesTypesDeJeu[i].typeDeJeu);
					

				}
								
				_level0.loader.contentHolder.bt_continuer1._visible = true;
				_level0.loader.contentHolder.txtChargementSalles._visible = false;
				
				//for the profModule;
				_level0.gotoAndStop(3);
            break;
			 
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			 
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			 
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourObtenirListeSalles" + " " + objetEvenement.resultat);
        trace("*********************************************\n");
    }
	
	//*****************************************************************************************
	 
    public function retourCreateRoom(objetEvenement:Object)
    {
        //   objetEvenement.resultat = , CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourCreateRoom   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "OK":
               
			trace("room created  ");
			this.objGestionnaireCommunication.obtenirListeSalles(Delegate.create(this, this.retourObtenirListeSalles), Delegate.create(this, this.evenementNouvelleSalle), this.clientType);
			

            break;
			 
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			 
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			 
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourCreateRoom");
        trace("*********************************************\n");
    }
	
	//*****************************************************************************************
	 
    public function retourReportBugQuestion(objetEvenement:Object)
    {
        //   objetEvenement.resultat = , CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourReportBugQuestion   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "OK":
            
			   // newsbox
		       //_level0.loader.contentHolder.newsbox_mc.newsone = this.newsArray[this.newsArray.length - 1];
		       var messageInfo:String = _root.texteSource_xml.firstChild.attributes.bugReportMess; 
			   this.newsChat.addMessage(messageInfo);
		       //this.newsArray[newsArray.length] = messageInfo;
		       //_level0.loader.contentHolder.newsbox_mc.newstwo = this.newsArray[this.newsArray.length - 1];
		       //_level0.loader.contentHolder.orderId = 0;
			trace("bug reported  ");
			
            break;
			 
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			 
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			 
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourReportBugQuestion");
        trace("*********************************************\n");
    }
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourGetReport(objetEvenement:Object)
    {
        //   objetEvenement.resultat = OK, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourGetReport   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "OK":
            trace("report created  ");
			_level0.roomReportText_txt.text = objetEvenement.report;
			_level0.roomReportText_txt.setTextFormat(_level0.reportFormat);
            break;
			 
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			 
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			 
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourGetReport");
        trace("*********************************************\n");
    }
	//*****************************************************************************************
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourEntrerSalle(objetEvenement:Object)
    {
        //objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, MauvaisMotDePasseSalle, SalleNonExistante, JoueurDansSalle
        trace("*********************************************");
        trace("debut de retourEntrerSalle   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "Ok":
			    _level0.loader.contentHolder["guiPWD"].removeMovieClip();
				
				var count:Number = this.listeDesSalles.length;
				for (var i:Number = 0; i < count; i++)
                {
                    if(this.listeDesSalles[i].idRoom == this.idRoom)
                    {
                        this.masterTime = this.listeDesSalles[i].masterTime;
						this.nbTracks = Number(this.listeDesSalles[i].nbTracks);
						break;
                    }
                }
                this.objGestionnaireCommunication.obtenirListeJoueursSalle(Delegate.create(this, this.retourObtenirListeJoueursSalle), Delegate.create(this, this.evenementJoueurEntreSalle), Delegate.create(this, this.evenementJoueurQuitteSalle));
                _level0.loader.contentHolder.gotoAndPlay(2);
			break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "MauvaisMotDePasseSalle":
                trace("Mauvais mot de passe");
									
				_level0.loader.contentHolder["guiPWD"].removeMovieClip();
				var erreur:String = _root.texteSource_xml.firstChild.attributes.errorPWD;
	            var pwdAlert:String = _root.texteSource_xml.firstChild.attributes.pwdAlert;
	            //var myAlert:Alert = new Alert();//createClassObject(Alert,"myAlert", getNextHighestDepth()); 
				//Alert.setStyle("themeColor", "haloBlue");
				_global.styles.Alert.setStyle("themeColor", "haloBlue");
				_global.styles.Alert.setStyle("color", 0x000099);

				Alert.show(erreur, pwdAlert); 
				
            break;
			
            case "SalleNonExistante":
                trace("Salle non existante");
            break;
			
            case "JoueurDansSalle":
                trace("Joueur dans salle");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourEntrerSalle");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourQuitterSalle(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurDansTable
        trace("*********************************************");
        trace("debut de retourQuitterSalle   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "Ok":
                delete this.listeDesJoueursDansSalle;
                delete this.listeDesSalles;
                delete this.listeDesJoueursConnectes;
                this.listeDesJoueursDansSalle = new Array();
                this.nomSalle = "";
                //this.motDePasseSalle = "";
                this.listeDesSalles = new Array();
                this.listeDesJoueursConnectes = new Array();
                objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), Delegate.create(this, this.evenementJoueurConnecte), Delegate.create(this, this.evenementJoueurDeconnecte));
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			
            case "JoueurDansTable":
                trace("Joueur dans table");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourQuitterSalle");
        trace("*********************************************\n");
    } 
	 
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirListeJoueursSalle(objetEvenement:Object)
    {
        //   objetEvenement.resultat = ListeJoueursSalle, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle
        trace("*********************************************");
        trace("debut de retourObtenirListeJoueursSalle   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "ListeJoueursSalle":
			    var count:Number = objetEvenement.listeNomUtilisateurs.length;
                for(var i:Number=0; i < count; i++)
                {
                    this.listeDesJoueursDansSalle.push(objetEvenement.listeNomUtilisateurs[i]);
                }
                this.objGestionnaireCommunication.obtenirListeTables(Delegate.create(this, this.retourObtenirListeTables), Delegate.create(this, this.evenementJoueurEntreTable), Delegate.create(this, this.evenementJoueurQuitteTable), Delegate.create(this, this.evenementNouvelleTable), Delegate.create(this, this.evenementTableDetruite), FiltreTable.INCOMPLETES_NON_COMMENCEES);
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourObtenirListeJoueursSalle");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirListeTables(objetEvenement:Object)
    {
        //   objetEvenement.resultat = ListeTables, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, FiltreNonConnu
        trace("*********************************************");
        trace("debut de retourObtenirListeTables   "+objetEvenement.resultat);
        var str:String = new String();
        switch(objetEvenement.resultat)
        {
            case "ListeTables":
			     
				 _level0.loader.contentHolder.listeTable.removeAll();
				 delete this.listeDesTables;
				 this.listeDesTables = new Array();
				 var count:Number = objetEvenement.listeTables.length;
                for (var i:Number = 0; i < count; i++)
                {
                    this.listeDesTables.push(objetEvenement.listeTables[i]);
					
                    str = objetEvenement.listeTables[i].no + ".  *" +  objetEvenement.listeTables[i].tablName + "*  " + objetEvenement.listeTables[i].temps + " min. " ;
					
					_level0.loader.contentHolder.listeTable.addItem({label : str, data : objetEvenement.listeTables[i].no});
                }
				
				if ( objetEvenement.listeTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2)
				{
					trace("longeur liste table : " +  objetEvenement.listeTables.length);
					_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
					_level0.loader.contentHolder.txtChargementTables._visible = true;
				}
				else
				{
					_level0.loader.contentHolder.chargementTables = "";
					_level0.loader.contentHolder.chargementTables._visible = false;
				}
				_level0.loader.contentHolder.bt_continuer2._visible = true;
            break;
			 
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			 
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			 
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			 
            case "FiltreNonConnu":
                trace("Filtre non connu");
            break;
			 
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourObtenirListeTables");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourCreerTable(objetEvenement:Object)
    {
        //   objetEvenement.resultat = "NoTable", CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle,  JoueurDansTable
        // parametre : noTable, name
        trace("*********************************************");
        trace("debut de retourCreerTable   " + objetEvenement.resultat + "    " + objetEvenement.noTable + "  " + objetEvenement.nameTable);
        var movClip:MovieClip;

        switch(objetEvenement.resultat)
        {
            case "NoTable":
                this.numeroTable = objetEvenement.noTable;
				this.tablName =  objetEvenement.nameTable;
                _level0.loader.contentHolder.gotoAndPlay(3);
               
                _level0.loader.contentHolder.nomJ4 = this.nomUtilisateur;
              
			 				
            break;
			 
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			 
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			 
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			 
            case "JoueurDansTable":
                trace("Joueur dans table");
            break;
			 
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
    	trace("fin de retourCreerTable");
    	trace("*********************************************\n");
    }
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
    function obtenirListeTables()
    {
        trace("*********************************************");
        trace("debut de sortirTable");
        this.objGestionnaireCommunication.obtenirListeTablesApres(Delegate.create(this, this.retourObtenirListeTables), FiltreTable.INCOMPLETES_NON_COMMENCEES);
        trace("fin de sortirTable");
        trace("*********************************************\n");
    } 
	
    //  on ne s'ajoute pas a la liste des joueur dans cette table, c grave ??  c correct pour quand on veut sortir....
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourEntrerTable(objetEvenement:Object)
    {
        //   objetEvenement.resultat = ListePersonnageJoueurs, CommandeNonReconnue,  ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, TableNonExistante, TableComplete
        trace("*********************************************");
        trace("debut de retourEntrerTable   " + objetEvenement.resultat);
        var movClip:MovieClip;
        switch(objetEvenement.resultat)
        {
            case "ListePersonnageJoueurs":
			    var count:Number = this.listeDesTables.length;
				for (var i:Number = 0; i < count; i++)
                {
                    if(this.listeDesTables[i].no == numeroTable)
                    {
                        tempsPartie = this.listeDesTables[i].temps;
						tablName = this.listeDesTables[i].tablName;
                        break;
                    }
                }
				
                _level0.loader.contentHolder.gotoAndPlay(3);
				
                _level0.loader.contentHolder.nomJ4 = nomUtilisateur;								
                
				// put the players in the liste
				var count:Number = objetEvenement.listePersonnageJoueurs.length;
               	for(var i:Number = 0; i < count; i++)
                {
	                
											
				   if(!(objetEvenement.listePersonnageJoueurs[i].userRoles == 2 && this.typeDeJeu == "Tournament"))
				   {   
					  this.listeDesPersonnages.push(new Object());
					  this.listeDesPersonnages[this.listeDesPersonnages.length -1].nom = objetEvenement.listePersonnageJoueurs[i].nom;
                      this.listeDesPersonnages[this.listeDesPersonnages.length -1].id = objetEvenement.listePersonnageJoueurs[i].idPersonnage;
					  this.listeDesPersonnages[this.listeDesPersonnages.length -1].role = objetEvenement.listePersonnageJoueurs[i].userRoles;
					  this.listeDesPersonnages[this.listeDesPersonnages.length -1].clocolor = objetEvenement.listePersonnageJoueurs[i].clothesColor;
					  this.listeDesPersonnages[this.listeDesPersonnages.length -1].pointage = 0;
					  this.listeDesPersonnages[this.listeDesPersonnages.length -1].win = 0;
					  //this.listeDesPersonnages[i].argent = 0;
									
					  trace("control demarrepartie " + objetEvenement.listePersonnageJoueurs[i].nom + " " + this.listeDesPersonnages[this.listeDesPersonnages.length -1].nom + " "  + this.listeDesPersonnages[this.listeDesPersonnages.length -1].id);
				   }
                }// end for
				
				
				var j:Number = 0;
				var count:Number = this.listeDesPersonnages.length; 
				for(var i:Number = 0; i < count; i++)
                {
	                if(i>3) {j=1;}
					if(i>7) {j=2;}
					if(i>11) {j=3;}
					
					
                    var idDessin:Number = calculatePicture(this.listeDesPersonnages[i].id);
										    
                    if(idDessin != 0)
					{
						
					   this.listeDesPersonnages[i].idessin = idDessin;
					   
					   var idPers =  calculateIDPers(this.listeDesPersonnages[i].id, idDessin);
					   var cloColor:String = this.listeDesPersonnages[i].clocolor;
					 
					   // change back if not used perso load
	                  movClip = _level0.loader.contentHolder.refLayer.createEmptyMovieClip("Personnage" + idPers,i);
					   
					  this.drawUserFrame3(i, cloColor, idDessin, movClip);
					  					  
				       _level0.loader.contentHolder["joueur"+(i+1)] = this.listeDesPersonnages[i].nom;
                                              
					   movClip._x = 485 - j*60;
                       movClip._y = 150 + i*60-j*240;
					   movClip._xscale -= 70;
					   movClip._yscale -= 70;
					} // if
				 }// for
				
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			
            case "TableNonExistante":
                trace("table non existante");
				obtenirListeTables();
			break;
			
            case "TableComplete":
                trace("Table complete!!!!!");
				obtenirListeTables();
				
			break;
			
            default:
                trace("Erreur Inconnue");
				obtenirListeTables();
        }
		objetEvenement = null;
    	trace("fin de retourEntrerTable");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourQuitterTable(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable
        trace("*********************************************");
        trace("debut de retourQuitterTable   "+objetEvenement.resultat);
        var str:String = new String();
		var indice:Number;
		
        switch(objetEvenement.resultat)
        {
            case "Ok":     
			    var count:Number = this.listeDesTables.length;
				for(var i = 0; i < count; i++)
				{
					if(this.listeDesTables[i].no == this.numeroTable)
					{
						indice = i;
						break;
					}
				}

				 _level0.loader.contentHolder.listeTable.removeAll();
				// on s'enleve de la liste des joueurs 
				var count:Number = this.listeDesTables[indice].listeJoueurs.length;
            	for(var j=0; j <  count; j++)
            	{
                	if(this.listeDesTables[indice].listeJoueurs[j].nom == this.nomUtilisateur)
                	{
                    	this.listeDesTables[indice].listeJoueurs.splice(j,1);
                   	}
					if(this.listeDesTables[indice].listeJoueurs.length == 0)
					{
            	      //_level0.loader.contentHolder.listeTable.removeItemAt(indice);
					  this.listeDesTables.splice(indice,1);
					}
            	}
				
				if (this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2)
		        {
			      
			       _level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
				   _level0.loader.contentHolder.txtChargementTables._visible = true;
		        }
				
			 	
                this.objGestionnaireCommunication.obtenirListeJoueursSalle(Delegate.create(this, this.retourObtenirListeJoueursSalle), Delegate.create(this, this.evenementJoueurEntreSalle), Delegate.create(this, this.evenementJoueurQuitteSalle));						
			 	var count:Number = this.listeDesTables.length;
			 	for (var i:Number = 0; i < count; i++)
			 	{
					str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
					
					_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
			 	}
				this.listeDesPersonnages.removeAll();
								
            break;

			case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
             
			case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
             
			case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
             
			case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
             
			case "JoueurPasDansTable":
                trace("Joueur pas dans table");
            break;
             
			default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourQuitterTable");
        trace("*********************************************\n");
    }
    // chat
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
	public function retourDemarrerMaintenant(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable, JoueursPasDansMemeTable
        trace("*********************************************");
        trace("debut de retourDemarrerMaintenant   "+objetEvenement.resultat+" "+objetEvenement);

		switch(objetEvenement.resultat)
        {
            case "DemarrerMaintenant":
                trace("Commande DemarrerMaintenant acceptee par le serveur");
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			
            case "JoueurPasDansTable":
                trace("Joueur pas dans table");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourDemarrerMaintenant");
        trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourEnvoyerMessage(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable, JoueursPasDansMemeTable
        trace("*********************************************");
        trace("debut de retourEnvoyerMessage   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "Ok":
                trace("Message envoye");
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			
            case "JoueurPasDansTable":
                trace("Joueur pas dans table");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourEnvoyerMessage");
        trace("*********************************************\n");
    }
	 
    //  pour kicker out un joueur
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourSortirJoueurTable(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable, JoueurPasMaitreTable
        trace("*********************************************");
        trace("debut de retourSortirJoueurTable   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "Ok":
			var count:Number = this.listeDesTables.length;
            for(var i = 0; i <  count; i++)
    	    {
			    var countS:Number = this.listeDesTables[i].listeJoueurs.length;
        	   for(var j = 0; j < countS; j++)
               {
                   if(this.listeDesTables[i].listeJoueurs[j].nom == objetEvenement.nomUtilisateur)
                  {
                    this.listeDesTables[i].listeJoueurs.splice(j,1);
                    break;
                  }
               }
			
			   if(this.listeDesTables[i].listeJoueurs.length == 0)
			   {
            	  // _level0.loader.contentHolder.listeTable.removeItemAt(i);
				   this.listeDesTables.splice(i,1);
			   }
			   
			   
       	    }//end for
			
			var str:String = new String();
            _level0.loader.contentHolder.listeTable.removeAll();
			count = this.listeDesTables.length;
			for (var i:Number = 0; i < count; i++)
			 {
				str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
				_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
			 }
					
		     if (this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2)
		     {
			   _level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
			   _level0.loader.contentHolder.txtChargementTables._visible = true;
		     }
			 
			 count = this.listeDesPersonnages.length;
			 for(var j = 0; j < count; j++)
             {
                   if(this.listeDesPersonnages[j].nom == objetEvenement.nom)
                  {
                    this.listeDesPersonnages.removeItemAt(j);
                    break;
                  }
             }
				
			break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			
            case "JoueurPasMaitreTable":
                trace("Joueur pas maitre de la table");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourSortirJoueurTable");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourDemarrerPartie(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable, TableNonComplete
        trace("*********************************************");
        trace("debut de retourDemarrerPartie   "+objetEvenement.resultat + " " + objetEvenement.idP);
        switch(objetEvenement.resultat)
        {
            case "Ok":
			    this.idPersonnage = objetEvenement.idP;
				this.colorIt =  objetEvenement.clocolor;
				_level0.loader.contentHolder.mc_perso.clothesCol = objetEvenement.clocolor;
				var count:Number =  this.listeDesPersonnages.length; 
			    for(var i:Number = 0; i < count; i++)
				{
				   if(this.listeDesPersonnages[i].nom == this.nomUtilisateur)
				   {
				      this.listeDesPersonnages[i].id = objetEvenement.idP;
					  trace("nom and idP = " + this.listeDesPersonnages[i].nom +  " " + this.listeDesPersonnages[i].id );
				   }
				
				}
                trace("retourDemarrerPartie ok");
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			
            case "JoueurPasDansTable":
                trace("Joueur pas dans table");
            break;
			
            case "TableNonComplete":
                trace("Table non complete");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de retourDemarrerPartie");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Ca c'est la fonction qui va etre appelee lorsque le GestionnaireCommunication aura
    // recu la reponse du serveur
    public function retourDeconnexion(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
    	trace("*********************************************");
    	trace("debut de retourDeconnexion   "+objetEvenement.resultat);
    	switch(objetEvenement.resultat)
        {
            case "Ok":
                trace("deconnexion");
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
    	trace("fin de retourDeconnexion");
    	trace("*********************************************\n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourDeplacerPersonnage(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Question, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, DeplacementNonAutorise
		var question:MovieClip;
	
      	trace("*********************************************");
      	trace("debut de retourDeplacerpersonnage   " + objetEvenement.resultat);
      	switch(objetEvenement.resultat)
        {
            case "Question":
		     	_level0.loader.contentHolder.url_question = objetEvenement.question.url;
		     	_level0.loader.contentHolder.type_question = objetEvenement.question.type;
				_level0.loader.contentHolder.box_question.gotoAndPlay(2);

                /*
				switch(objetEvenement.question.type)
			 	{
		     		case "MULTIPLE_CHOICE_5":
						trace("type = ChoixReponse : MULTIPLE_CHOICE_5");
		     		break;
					
					case "MULTIPLE_CHOICE_3":
						trace("type = ChoixReponse : MULTIPLE_CHOICE_3");
		     		break;
	
	                case "MULTIPLE_CHOICE":
						trace("type = ChoixReponse : MULTIPLE_CHOICE");
		     		break;
					
		     		case "TRUE_OR_FALSE":
						trace("type = VraiFaux : TRUE_OR_FALSE");
		     		break;
	
		     		case "ReponseCourte":
						trace("type = ReponseCourte");
		     		break;
		     		
		     		case "SHORT_ANSWER":
						trace("type = SHORT_ANSWER");
		     		break;
	
		     		default:
						trace("Pas bon type de question   "+objetEvenement.question.type);
					break;
		 		}*/
				_root.objGestionnaireInterface.effacerBoutons(1);
            break;

			case "Banane":
                trace("todo si necessaire");
            break;	
				
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;

            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
			case "DeplacementNonAutorise":
                trace("DeplacementNonAutorise");
            break;
			 		 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break; 
			 
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
    	trace("fin de retourDeplacerpersonnage");
    	trace("*********************************************\n");
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
	public function retourAcheterObjet(objetEvenement:Object)
    {
		//   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
    	trace("*********************************************");
    	trace("debut de retourAcheterObjet   "+objetEvenement.resultat);
    	switch(objetEvenement.resultat)
        {
			case "Ok":
				
					trace("nom de l'objet : " + objetEvenement.argent.type + " " + objetEvenement.argent.id);
					_level0.loader.contentHolder.planche.obtenirPerso().ajouterObjet(objetEvenement.argent.id, objetEvenement.argent.type); /// id???
					break;
			
			case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
			default:
				trace("Erreur Inconnue. Message du serveur: "+objetEvenement.resultat);
        }
		objetEvenement = null;
    	trace("fin de retourAcheterObjet");
    	trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	public function retourUtiliserObjet(objetEvenement:Object)
    {
	  	//   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
      	trace("*********************************************");
      	trace("debut de retourUtiliserObjet   "+objetEvenement.resultat);
	  
      	switch(objetEvenement.resultat)
        {
			case "RetourUtiliserObjet":
				///////////////////////////////////////////
				//trace("c'est ici ds retourUtiliserObjet");
				
				switch(objetEvenement.objetUtilise.typeObjet)
				{
					// lorsqu'on utilise un livre
					// le serveur envoie une mauvaise reponse
					// on efface alors un choix
					case "Livre":
						trace("mauvaise reponse (livre) : "  + objetEvenement.objetUtilise.mauvaiseReponse);
					
						switch((String)(objetEvenement.objetUtilise.mauvaiseReponse))
						{
							case "1":
								_level0.loader.contentHolder.box_question.btn_a._visible = false;
							break;
							
							case "2":
								_level0.loader.contentHolder.box_question.btn_b._visible = false;
							break;
							
							case "3":
								_level0.loader.contentHolder.box_question.btn_c._visible = false;
							break;
							
							case "4":
								_level0.loader.contentHolder.box_question.btn_d._visible = false;
							break;
							
							case "5":
								_level0.loader.contentHolder.box_question.btn_e._visible = false;
							break;
							default:
								trace("erreur choix reponse ds retourUtiliser : Livre");
						}
					break;
				
					// lorsqu'on utilise la boule
					// le serveur nous retourne une autre question
					case "Boule":
						trace("on utilise la boule ici !!!");
						var question:MovieClip = new MovieClip();
					
						_level0.loader.contentHolder.url_question = objetEvenement.objetUtilise.url;
						_level0.loader.contentHolder.type_question = objetEvenement.objetUtilise.type;
						_level0.loader.contentHolder.box_question.gotoAndPlay(7);
					break;
				
					case "Banane":
						trace("banane");
						//_level0.loader.contentHolder.toss.removeMovieClip();
						//_level0.loader.contentHolder.planche.obtenirPerso().tossBanana();
					break;
				
					default:
						trace("erreur choix d'objet ds typeObjet a utiliser");
					break;
				}
			break;

          	case "CommandeNonReconnue":
           		trace("CommandeNonReconnue");
           	break;
			
           	case "ParametrePasBon":
               	trace("ParamettrePasBon");
           	break;
			
           	case "JoueurNonConnecte":
               	trace("Joueur non connecte");
           	break;
			
			default:
				trace("Erreur Inconnue. Message du serveur: "+objetEvenement.resultat);
			break;
        }
		objetEvenement = null;
    	trace("fin de retourUtiliserObjet");
    	trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////    
    public function retourDefinirPointageApresMinigame(objetEvenement:Object)
    {
	    //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
    	trace("*********************************************");
    	trace("debut de retourDefinirPointageApresMinigame   "+objetEvenement.resultat);
    	switch(objetEvenement.resultat)
        {
            case "Pointage":
                trace("on a le pointage total: " + objetEvenement.pointage + " Il reste a l'utiliser...");
				// modifier le pointage
				
				_level0.loader.contentHolder.planche.obtenirPerso().modifierPointage(objetEvenement.pointage);
				 
				// il faut mettre a jour le pointage
				// qu'arrive-t-il s'il y a des delais et que le perso c'est deja deplace?
				var count:Number = this.listeDesPersonnages[i].length;
				for(var i:Number = 0; i < count; i++)
    	        {
        	       if(this.listeDesPersonnages[i].nom == this.nomUtilisateur)
        	       {
            	     this.listeDesPersonnages[i].pointage = objetEvenement.pointage;
        	       }
        	   	}
				
				remplirMenuPointage();
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            default:
                trace("Erreur Inconnue. Message du serveur: "+objetEvenement.resultat);
        }
		objetEvenement = null;
    	trace("fin de retourDefinirPointageApresMinigame");
    	trace("*********************************************\n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////    
    public function retourDefinirArgentApresMinigame(objetEvenement:Object)
    {
	    //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
    	trace("*********************************************");
    	trace("debut de retourDefinirArgentApresMinigame   " + objetEvenement.resultat);
    	switch(objetEvenement.resultat)
        {
            case "Argent":
                
				trace("on a l'argent total: " + objetEvenement.argent + " Il reste a l'utiliser...");
				// modifier l'argent
				_level0.loader.contentHolder.planche.obtenirPerso().modifierArgent(objetEvenement.argent);
				
				// not in use for the moment
				/*
				for(var i:Number = 0; i < this.listeDesJoueursDansSalle.length; i++)
    	        {
        	       if(this.listeDesPersonnages[i].nom == this.nomUtilisateur)
        	       {
            	     this.listeDesPersonnages[i].argent = objetEvenement.argent;
        	       }
        	   	}*/
				
				// il faut mettre a jour l'argent
				// qu'arrive-t-il s'il y a des delais et que le perso s'est deja deplace?
            break;
			 
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			 
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			 
            default:
                trace("Erreur Inconnue. Message du serveur: "+objetEvenement.resultat);
        }
		objetEvenement = null;
    	trace("fin de retourDefinirArgentApresMinigame");
    	trace("*********************************************\n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourRepondreQuestion(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
		var retro:MovieClip;
		var pt:Point = new Point(0,0);
	
    	trace("*********************************************");
    	trace("debut de retourRepondreQuestion   " + objetEvenement.resultat);
    	trace("deplacement accepte oui ou non  :  " + objetEvenement.deplacementAccepte);   
    	trace("url explication  :  " + objetEvenement.explication);
    	trace("nouveau pointage  :  " + objetEvenement.pointage);
    	trace("nouvel argent  :  " + objetEvenement.argent);
    	trace("collision  :" + objetEvenement.collision);
		trace("bonus  : " + objetEvenement.bonus);
      
    	switch(objetEvenement.resultat)
        { 
            case "Deplacement":
		     	if(objetEvenement.deplacementAccepte)
		     	{
					trace("deplacement accepte");
			
					_level0.loader.contentHolder.box_question.gotoAndPlay(9);
			
					_root.objGestionnaireInterface.afficherBoutons(1);
			    	pt.definirX(objetEvenement.nouvellePosition.x);
			     	pt.definirY(objetEvenement.nouvellePosition.y);
					
					trace("juste avant la teleportation param  " +  objetEvenement.nouvellePosition.x + " " +  objetEvenement.nouvellePosition.y);
					var collision:String = objetEvenement.collision;
					
					_level0.loader.contentHolder.planche.obtenirPerso().definirProchainePosition(_level0.loader.contentHolder.planche.calculerPositionTourne(pt.obtenirX(), pt.obtenirY()), collision);
					
					// modifier le pointage
					_level0.loader.contentHolder.planche.obtenirPerso().modifierPointage(objetEvenement.pointage);
					_level0.loader.contentHolder.planche.obtenirPerso().modifierArgent(objetEvenement.argent);
					_level0.loader.contentHolder.sortieDunMinigame = false; 
					this.moveVisibility = objetEvenement.moveVisibility;
					
					var count:Number = this.listeDesPersonnages.length;
					for(var i:Number = 0; i < count;  i++)
    	            {
						
        	           if(this.listeDesPersonnages[i].nom == this.nomUtilisateur)
        	           {
            	          this.listeDesPersonnages[i].pointage = objetEvenement.pointage;
						  this.listeDesPersonnages[i].argent = objetEvenement.argent;
						  
						  // if we have bonus in the tournament game  we must treat this
						  if(objetEvenement.bonus > 0){
						     	this.listeDesPersonnages[i].win = 1;
								//this.listeDesPersonnages[i].pointage += objetEvenement.bonus;
								//_level0.loader.contentHolder.bonusBox.bonus = objetEvenement.bonus;
						  }
						  
						  // newsbox
		                 var messageInfo:String;
						 
						 if(objetEvenement.collision == "Livre")
						 {
						    messageInfo = _root.texteSource_xml.firstChild.attributes.bookCollectMess;
							this.newsChat.addMessage(messageInfo);
						 }
						 else if(objetEvenement.collision == "Banane")
						 {
							 messageInfo = _root.texteSource_xml.firstChild.attributes.bananaCollectMess;
							 this.newsChat.addMessage(messageInfo);
						 }
						 else if(objetEvenement.collision == "Piece")
						 {
							 messageInfo = _root.texteSource_xml.firstChild.attributes.moneyCollectMess;
							 this.newsChat.addMessage(messageInfo);
						 }
						 else if(objetEvenement.collision == "Boule")
						 {
							 messageInfo = _root.texteSource_xml.firstChild.attributes.cristallCollectMess;
		                     this.newsChat.addMessage(messageInfo);
						 }
        	           }
        	   	    }
					
					remplirMenuPointage();
		     	}
		     	else
		     	{
					if(_level0.loader.contentHolder.erreurConnexion)
					{
						// Dans le cas d'une erreur de connexion, nous envoyons une reponse
						// assurement mauvaise au serveur. Il ne faut pas afficher de retro dans ce cas
						_level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_root.objGestionnaireInterface.afficherBoutons(1);
						_level0.loader.contentHolder.erreurConnexion = false;
					}
					else
					{
			     		trace("deplacement refuse  ");
						_level0.loader.contentHolder.url_retro = objetEvenement.explication;

                        _level0.loader.contentHolder.box_question.monScroll._visible = false;
						var ptX:Number = _level0.loader.contentHolder.box_question.monScroll._x;
						var ptY:Number = _level0.loader.contentHolder.box_question.monScroll._y;
						_level0.loader.contentHolder.box_question.attachMovie("GUI_retro","GUI_retro", 100, {_x:ptX, _y:ptY});
						
						//define the time of penality
						_level0.loader.contentHolder.box_question.GUI_retro.timeX = 15;
						// define new visibility
						this.moveVisibility = objetEvenement.moveVisibility;

						_root.objGestionnaireInterface.effacerBoutons(1);
					}
		     	}
			
            break;
	
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			 
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			 
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
     	trace("fin de retourRepondreQuestion");
    	trace("*********************************************\n");
    }
	
	 ////////////////////////////////////////////////////////////////////////////////////////////////////
	public function returnCancelQuestion(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable
        trace("*********************************************");
        trace("debut de returnCancelQuestion   " + objetEvenement.resultat + " " + objetEvenement);

		switch(objetEvenement.resultat)
        {
            case "OK":
                trace("Commande CancelQuestion acceptee par le serveur");
            break;
			
            case "CommandeNonReconnue":
                trace("CommandeNonReconnue");
            break;
			
            case "ParametrePasBon":
                trace("ParamettrePasBon");
            break;
			
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break;
			
            case "JoueurPasDansSalle":
                trace("Joueur pas dans salle");
            break;
			
            case "JoueurPasDansTable":
                trace("Joueur pas dans table");
            break;
			
            default:
                trace("Erreur Inconnue");
        }
		objetEvenement = null;
        trace("fin de returnCancelQuestion");
        trace("*********************************************\n");
    }

	
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                       EVENEMENTS                                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementConnexionPhysique(objetEvenement:Object)
    {
        trace("*********************************************");
        trace("debut de evenementConnexionPhysique GEv");
        if(objetEvenement.resultat == true)
        {
            this.objGestionnaireCommunication.connexion(Delegate.create(this, this.retourConnexion), this.nomUtilisateur, this.motDePasse, this.langue);
		}
        else
        {
			this.utiliserPortSecondaire();
        }
		objetEvenement = null;
        trace("fin de evenementConnexionPhysique");
        trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementConnexionPhysique2(objetEvenement:Object)
    {
        trace("*********************************************");
        trace("debut de evenementConnexionPhysique2");
        if(objetEvenement.resultat == true)
        {
            this.objGestionnaireCommunication.connexion(Delegate.create(this, this.retourConnexion), this.nomUtilisateur, this.motDePasse);
		}
        else
        {
			this.tryTunneling();
        }
		objetEvenement = null;
        trace("fin de evenementConnexionPhysique2");
        trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementConnexionPhysiqueTunneling(objetEvenement:Object)
    {
        trace("*********************************************");
        trace("debut de evenementConnexionPhysiqueTunneling");
        if(objetEvenement.resultat == true)
        {
            this.objGestionnaireCommunication.connexion(Delegate.create(this, this.retourConnexion), this.nomUtilisateur, this.motDePasse);
		}
        else
        {
            trace("pas de connexion physique");
			
	    	_level0.loader._visible = true;
	    	_level0.bar._visible = false;
			
			_root.texteSalle._visible = false;
			
			var horsService:MovieClip;
			
			horsService = _level0.loader.contentHolder.attachMovie("GUI_erreur", "HorsService", 9999);
			
			horsService.textGUI_erreur.text = _root.texteSource_xml.firstChild.attributes.GUIhorsService;
			
			horsService.linkGUI_erreur.text = _root.texteSource_xml.firstChild.attributes.GUIhorsService2;
			horsService.linkGUI_erreur.html = true;
			horsService.btn_ok._visible = false;
			
			var formatLink = new TextFormat();
			formatLink.url = _root.texteSource_xml.firstChild.attributes.GUIhorsServiceURL;
			formatLink.target = "_blank";
			formatLink.font = "Arial";
			formatLink.size = 12;
			formatLink.color = 0xFFFFFF;
			formatLink.bold = true;
			formatLink.underline = true;
			formatLink.align = "Center";
			
			horsService.linkGUI_erreur.setTextFormat(formatLink);
        }
		objetEvenement = null;
        trace("fin de evenementConnexionPhysiqueTunneling");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementDeconnexionPhysique(objetEvenement:Object)
    {
        trace("*********************************************");
    	trace("debut de evenementDeconnexionPhysique   ");
		objetEvenement = null;
    	trace("fin de evenementDeconnexionPhysique");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurConnecte(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurConnecte   "+objetEvenement.nomUtilisateur);
		objetEvenement = null;
    	trace("fin de evenementJoueurConnecte");
    	trace("*********************************************\n");
    }
	
	 ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementNouvelleSalle(objetEvenement:Object)
    {
        // parametre: NoSalle, NomSalle, ProtegeeSalle, CreatorUserName, GameType, MasterTime, MaxNbPlayers, RoomDescriptions
    	trace("*********************************************");
    	trace("debut de evenementNouvelleSalle   " + objetEvenement.NoSalle + "  " + objetEvenement.RoomDescriptions + " " + objetEvenement.MaxNbPlayers);
    	  
		    this.listeDesSalles.push(new Object());
			this.listeDesSalles[this.listeDesSalles.length - 1].nom = objetEvenement.NomSalle;
			this.listeDesSalles[this.listeDesSalles.length - 1].idRoom = objetEvenement.NoSalle;
			this.listeDesSalles[this.listeDesSalles.length - 1].possedeMotDePasse = Boolean(objetEvenement.ProtegeeSalle);
			this.listeDesSalles[this.listeDesSalles.length - 1].descriptions = objetEvenement.RoomDescriptions;
			this.listeDesSalles[this.listeDesSalles.length - 1].maxnbplayers = objetEvenement.MaxNbPlayers;
			this.listeDesSalles[this.listeDesSalles.length - 1].typeDeJeu = objetEvenement.GameType;
			this.listeDesSalles[this.listeDesSalles.length - 1].userCreator = objetEvenement.CreatorUserName;
			this.listeDesSalles[this.listeDesSalles.length - 1].masterTime = objetEvenement.MasterTime;
			trace(" GE : " + this.listeDesSalles[this.listeDesSalles.length - 1].nom + " * " +  this.listeDesSalles[this.listeDesSalles.length - 1].possedeMotDePasse);
			
			_level0.loader.contentHolder.listeSalle.addItem({label: objetEvenement.NomSalle, data:  objetEvenement.NoSalle});
			_level0.loader.contentHolder.listeSalle.redraw();
																
			this.listeNumeroJoueursSalles.push(objetEvenement.MaxNbPlayers);
			_level0.loader.contentHolder.listeNumeroJSalles.push(objetEvenement.MaxNbPlayers );
					
			this.listeDesTypesDeJeu.push(objetEvenement.GameType);
			_level0.loader.contentHolder.listeDesTypesDeJeu.push(objetEvenement.GameType);
						
		objetEvenement = null;
    	trace("fin de evenementNouvelleSalle");
    	trace("*********************************************\n");
    }// end event
	
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDeconnecte(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurDeconnecte   "+objetEvenement.nomUtilisateur);
		
		var count:Number = this.listeDesPersonnages.length;
		for(var i:Number = 0; i < count; i++)
    	{
        	if(this.listeDesPersonnages[i].nom == objetEvenement.nomUtilisateur)
        	{
            	this.listeDesPersonnages.removeItemAt(i);
           		trace("un joueur enlever de la liste evenementJoueurDeconnecte:   " + objetEvenement.nomUtilisateur);
            	break;
        	}
        	
    	}
		objetEvenement = null;
    	trace("fin de evenementJoueurDeconnecte");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurEntreSalle(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurEntreSalle   " + objetEvenement.nomUtilisateur);
    	this.listeDesJoueursDansSalle.push(objetEvenement.nomUtilisateur);
		objetEvenement = null;
    	trace("fin de evenementJoueurEntreSalle");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurQuitteSalle(objetEvenement:Object)  
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurQuitteSalle   " + objetEvenement.nomUtilisateur);
    	
		var count:Number = this.listeDesJoueursDansSalle.length;
		for(var i:Number = 0; i < count; i++)
    	{
        	if(this.listeDesJoueursDansSalle[i] == objetEvenement.nomUtilisateur)
        	{
            	this.listeDesJoueursDansSalle.splice(i,1);
           		trace("un joueur enlever de la liste :   "+objetEvenement.nomUtilisateur);
            	break;
        	}
        	
    	}
		count = this.listeDesTables.length; 
		for(i = 0; i < count; i++)
    	{
			var countS:Number =  this.listeDesTables[i].listeJoueurs.length;
        	for(var j=0; j < countS; j++)
            {
                if(this.listeDesTables[i].listeJoueurs[j].nom == objetEvenement.nomUtilisateur)
                {
                    this.listeDesTables[i].listeJoueurs.splice(j,1);
                    break;
                }
            }
			
			if(this.listeDesTables[i].listeJoueurs.length == 0){
				this.listeDesTables.splice(i,1);
            	//_level0.loader.contentHolder.listeTable.removeItemAt(i);
			}
		}
		
		var str:String = new String();
		_level0.loader.contentHolder.listeTable.removeAll();
		count = this.listeDesTables.length;
		for (var i:Number = 0; i < count; i++)
	    {
			str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
			_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
		}
    	
		
		if (this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2 && _level0.loader.contentHolder["p0"]._visible == false)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
		objetEvenement = null;
		trace("fin de evenementJoueurQuitteSalle");
    	trace("*********************************************\n");
    }
    
	//  temps de la partie : est-ce que ca marche?
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementNouvelleTable(objetEvenement:Object)
    {
        // parametre: noTable, tempsPartie , tablName
    	trace("*********************************************");
    	trace("debut de evenementNouvelleTable   " + objetEvenement.noTable + "  " + objetEvenement.tempsPartie + " " + objetEvenement.nameTable);
    	var str:String = new String();
    	// on ajoute une liste pour pouvoir inserer les joueurs quand ils vont entrer
        objetEvenement.listeJoueurs = new Array();
        objetEvenement.no = objetEvenement.noTable;
		objetEvenement.temps = objetEvenement.tempsPartie;
		objetEvenement.tablName = objetEvenement.nameTable;
        
		this.listeDesTables.push(objetEvenement);
        
		str = this.listeDesTables[this.listeDesTables.length-1].no + ".  *" + this.listeDesTables[this.listeDesTables.length-1].tablName + "*  "+ this.listeDesTables[this.listeDesTables.length-1].temps + " min. \n    "; ;
       
				
		  _level0.loader.contentHolder.listeTable.removeAll();
		     var count:Number = this.listeDesTables.length;
			 for (var i:Number = 0; i < count; i++)
			 {
				str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
				_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
			 }
		
        
		_level0.loader.contentHolder.chargementTables = "";
		/*
		for(var i:Number = 0; i < maxPlayersInTable; i++)
        {
			trace(i + ": "+this.listeDesPersonnages[i].nom+" id:"+this.listeDesPersonnages[i].id);
		}
		*/
		//objetEvenement = null;
    	trace("fin de evenementNouvelleTable");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementTableDetruite(objetEvenement:Object)
    {
		// param :  NoTable
    	trace("*********************************************");
    	trace("debut de evenementTableDetruite   "  + objetEvenement.noTable);
    	
		var i:Number;
		var count:Number = this.listeDesTables.length;
    	for(i = 0; i < count; i++)
    	{
        	if(this.listeDesTables[i].no == objetEvenement.noTable)
        	{
            	this.listeDesTables.splice(i,1);
				//_level0.loader.contentHolder.listeTable.removeItemAt(i);
            	break;
        	}
    	}
		
		var str:String = new String();
		_level0.loader.contentHolder.listeTable.removeAll();
		
		for (var i:Number = 0; i < count; i++)
		{
			str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
			_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
		}
				
		if (this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2 && _level0.loader.contentHolder["p0"]._visible == false)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
		
		if(!endGame && objetEvenement.noTable == this.numeroTable){
		   sortirSalle();
		   //_level0.loader.contentHolder.gestionBoutons(true);
		   _level0.loader.contentHolder["att"].removeMovieClip();
		   _level0.loader.contentHolder.gotoAndPlay(1);
		   this.listeDesPersonnages.removeAll();  
		  		  
		}
		
		objetEvenement = null;
    	trace("fin de evenementTableDetruite");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurEntreTable(objetEvenement:Object)
    {
        // parametre: noTable, nomUtilisateur, userRole
    	trace("*********************************************");
    	trace("debut de evenementJoueurEntreTable   "+objetEvenement.noTable + "    " + objetEvenement.nomUtilisateur);
    	var i:Number;
    	var j:Number;
    	var indice:Number;
    	var str:String = new String();
    	indice = -1;
	
		//	musique();
		var count:Number = this.listeDesTables.length;
	
    	for(i = 0; i < count; i++)
    	{
        	if(this.listeDesTables[i].no == objetEvenement.noTable)
        	{
            	this.listeDesTables[i].listeJoueurs.push(new Object());
            	this.listeDesTables[i].listeJoueurs[this.listeDesTables[i].listeJoueurs.length - 1].nom = objetEvenement.nomUtilisateur;
            	indice = i;
            	break;
        	}
    	}
    	/*
		for(var i:Number = 0; i < maxPlayersInTable; i++)
        {
					trace(i + " avant this.listeDesPersonnages[i].nom: " + this.listeDesPersonnages[i].nom + " id:" + this.listeDesPersonnages[i].id);
	    }
		*/
		if(objetEvenement.noTable == this.numeroTable)
    	{
			
	       if(!(objetEvenement.userRole == 2 && this.typeDeJeu == "Tournament"))
		   {
			   //trace(" evenementJoueurEntreTable length = " + listeDesPersonnages.length + " "  + objetEvenement.nomUtilisateur + " " + objetEvenement.userRole);
			  listeDesPersonnages.push(new Object());
			  listeDesPersonnages[listeDesPersonnages.length - 1].nom = objetEvenement.nomUtilisateur;
			  listeDesPersonnages[listeDesPersonnages.length - 1].role = objetEvenement.userRole;
			  _level0.loader.contentHolder["joueur" + listeDesPersonnages.length] = listeDesPersonnages[listeDesPersonnages.length - 1].nom;
		   }
    	}// if
		
    	if(indice != -1)
    	{
			   	
			// enlever la table de la liste si elle est pleine
			if(this.listeDesTables[indice].listeJoueurs.length == maxPlayersInTable)
        	{
            	for(i = 0; i < count; i++)
            	{
                	if(_level0.loader.contentHolder.listeTable.getItemAt(i).data == objetEvenement.noTable)
                	{
                    	_level0.loader.contentHolder.listeTable.removeItemAt(i);
		    			break;
                	}
            	}
        	}
    	}
		  _level0.loader.contentHolder.listeTable.removeAll();
			 for (var i:Number = 0; i < count; i++)
			 {
				str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
				_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
			 }
		
		if (this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2 && _level0.loader.contentHolder["p0"]._visible == false)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
    	/*
		for(var i:Number = 0; i < maxPlayersInTable; i++)
        {
			trace(i+"this.listeDesPersonnages[i].nom: "+this.listeDesPersonnages[i].nom+" id:"+this.listeDesPersonnages[i].id);
	    }*/
		
		objetEvenement = null;
    	trace("fin de evenementJoueurEntreTable");
    	//trace("*********************************************\n");
    }
	
    //  est-ce qu'on recoit cet eve si on quitte notre table ?????????  NON NON NON
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurQuitteTable(objetEvenement:Object)
    {
        // parametre: noTable, nomUtilisateur
    	//trace("*********************************************");
    	trace("debut de evenementJoueurQuitteTable   "+objetEvenement.noTable+"    "+objetEvenement.nomUtilisateur);
    	var indice:Number = -1;
    	var i:Number;
    	var j:Number;
    	var tableAffichee:Boolean = false;
    	var str:String = new String();
		
		var count:Number =  this.listeDesTables.length;
    	
		for(i = 0; i < count; i++)
    	{
        	if(this.listeDesTables[i].no == objetEvenement.noTable)
        	{
            	indice = i;
            	break;
        	}
    	}
    	// si la table est la notre (on choisit nos perso frame 3)
    	if(objetEvenement.noTable == this.numeroTable)
    	{
	    	for(i = 0; i < maxPlayersInTable; i++)
        	{
	        	//  on enleve le nom du joueur dans la liste et a l'ecran
            	if(listeDesPersonnages[i].nom == objetEvenement.nomUtilisateur)
            	{
					//trace("un joueur enlever de la liste var1!!!:   " + objetEvenement.nomUtilisateur + " " + this.listeDesPersonnages[i].nom);
					var idDessin = listeDesPersonnages[i].idessin;
					var idPers:Number = calculateIDPers(this.listeDesPersonnages[i].id, idDessin);
					 _level0.loader.contentHolder.refLayer["Personnage" + idPers].removeMovieClip();
                	this.listeDesPersonnages.removeItemAt(i);
				   
                	_level0.loader.contentHolder["joueur"+(i+1)] = " ";
                	                	
            	}
            	
            		
        	}
        	
    	}
    	// si ce n'est pas notre table
    	else
    	{
        	// si la table existe
        	if(indice != -1)
        	{
            	// on enleve le joueur de la liste des joueurs de la table en question
				var countS:Number = this.listeDesTables[indice].listeJoueurs.length;
            	for(j = 0; j < countS;  j++)
            	{
                	if(this.listeDesTables[indice].listeJoueurs[j].nom == objetEvenement.nomUtilisateur)
                	{
                    	this.listeDesTables[indice].listeJoueurs.splice(j,1);
                    	break;
                	}
            	}
            	
				
        	}
    	}
		_level0.loader.contentHolder.listeTable.removeAll();
		for (var i:Number = 0; i < count; i++)
	    {
			str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
			_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
		}
		
		count = this.listeDesPersonnages.length;
		for(var i:Number = 0; i < count; i++)
    	{
        	if(this.listeDesPersonnages[i].nom == objetEvenement.nomUtilisateur)
        	{
				//trace("un joueur enlever de la liste var2:   " + objetEvenement.nomUtilisateur + " " + this.listeDesPersonnages[i].nom);
            	this.listeDesPersonnages.removeItemAt(i);
				
        	}
        	
    	}
		
    	for(var i:Number = 0; i < count; i++)
        {
			trace(i + ": " + this.listeDesPersonnages[i].nom + " id:" + this.listeDesPersonnages[i].id);
		}
    				
		
		if(this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2 && _level0.loader.contentHolder["p0"]._visible == false)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
		
		if(!endGame && _level0.loader.contentHolder._currentframe > 3 && objetEvenement.noTable == this.numeroTable){
		   _level0.loader.contentHolder.planche.getPersonnageByName(objetEvenement.nomUtilisateur).cachePersonnage();
		   dessinerMenu();
		   remplirMenuPointage();
		     
		   // newsbox
		   var messageInfo:String = objetEvenement.nomUtilisateur + _root.texteSource_xml.firstChild.attributes.outMess; 
		   this.newsChat.addMessage(messageInfo);
		 }
		
		objetEvenement = null;
		trace("fin de evenementJoueurQuitteTable");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementMessage(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, message
    	trace("*********************************************");
    	trace("debut de evenementMessage   "+objetEvenement.message+"    "+objetEvenement.nomUtilisateur);
		objetEvenement = null;
    	trace("fin de evenementMessage");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementSynchroniserTemps(objetEvenement:Object)
    {
        // parametre: tempsRestant
    	trace("*********************************************");
    	trace("debut de evenementSynchroniserTemps   "+objetEvenement.tempsRestant);
    	_level0.loader.contentHolder.horlogeNum = objetEvenement.tempsRestant;
		objetEvenement = null;
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
	public function evenementPartieDemarree(objetEvenement:Object)
    {
		
        // parametre: plateauJeu, positionJoueurs (nom, x, y), tempsPartie
        trace("*********************************************");
        trace("debut de evenementPartieDemarree   " + objetEvenement.tempsPartie + "   " + getTimer());
        var i:Number;
        var j:Number;
        
		 _level0.loader.contentHolder["att"].removeMovieClip();
		 
		 _level0.loader.contentHolder.gotoAndPlay(4);
		 
		 var count:Number;// = objetEvenement.plateauJeu[0].length;
	    /*
        for(i = 0; i < count; i++)
        {
            _level0.loader.contentHolder.tab.push(new Array());
			var count1:Number = objetEvenement.plateauJeu.length;
            for(j = 0; j < count1; j++)
            {
                _level0.loader.contentHolder.tab[i][j] = objetEvenement.plateauJeu[i][j];
            }
        }*/

		// ici on initie les noms et pointages des adversaire (dans le panneau qui descend)
		// et on met la face de leur avatar a cote de leur nom
		// Initialise our opponents' name and score
		// and put the face of our opponents' avatar in the panel (next to their name)
 		
		/*
		j = 0;
		for(i = 0; i < maxPlayersInTable; i++)
		{
       				
			if((undefined != this.listeDesPersonnages[i].nom) && !(this.listeDesPersonnages[i].role == 2 && this.typeDeJeu == "Tournament")){
				
			  this["tete"+j] = new MovieClip();
			  j++;
			}
		}*/
		
		var maTete:MovieClip;
		
		count = this.listeDesPersonnages.length;
        for(i = 0; i < count; i++)
        {
            if(this.listeDesPersonnages[i].nom == this.nomUtilisateur)
            {
	            var idDessin:Number = calculatePicture(this.listeDesPersonnages[i].id);
				var idPers:Number = calculateIDPers(this.listeDesPersonnages[i].id, idDessin);
				this.listeDesPersonnages[i].clocolor = this.colorIt;
				
				// put the face of my avatar in the panel (next to my name)
		
		       maTete = _level0.loader.contentHolder.maTete.attachMovie("tete" + idDessin, "maTete", -10099);
		       maTete._x = -7;
		       maTete._y = -6;
		       // V3 head size
		       maTete._xscale = 180;
		       maTete._yscale = 180;
				
                _level0.loader.contentHolder.planche = new PlancheDeJeu(objetEvenement.plateauJeu, this.listeDesPersonnages[i].id, _level0.loader.contentHolder.gestionnaireInterface);
				//_level0.loader.contentHolder.planche = new PlancheDeJeu(_level0.loader.contentHolder.tab, this.listeDesPersonnages[i].id, _level0.loader.contentHolder.gestionnaireInterface);
			}
        }
       
	    _level0.loader.contentHolder.planche.afficher();
        
		trace("longueur de la liste des noms envoyes par serveur    :" + objetEvenement.positionJoueurs.length);
		var count1 = objetEvenement.positionJoueurs.length;
        for(i = 0; i < count1; i++)
        {
			
            for(j = 0; j < count; j++)
            {
	            //trace(this.listeDesPersonnages[j].nom+" : "+objetEvenement.positionJoueurs[i].nom);
                if(this.listeDesPersonnages[j].nom == objetEvenement.positionJoueurs[i].nom)
                {
	                var idDessin:Number = calculatePicture(this.listeDesPersonnages[j].id);
					var idPers:Number = calculateIDPers(this.listeDesPersonnages[j].id, idDessin);
					this.listeDesPersonnages[i].idPers = idPers;
										
					// to update clothes color
					this.listeDesPersonnages[j].clocolor = objetEvenement.positionJoueurs[i].clocolor;
					trace("test color : " + this.listeDesPersonnages[j].clocolor);
					
					// after we create the perso's
					_level0.loader.contentHolder.planche.ajouterPersonnage(this.listeDesPersonnages[j].nom, objetEvenement.positionJoueurs[i].x, objetEvenement.positionJoueurs[i].y, idPers, idDessin, this.listeDesPersonnages[j].role, this.listeDesPersonnages[j].clocolor);
		    		//trace("Construction du personnage : " + this.listeDesPersonnages[j].clocolor + " " + objetEvenement.positionJoueurs[i].x + " " + objetEvenement.positionJoueurs[i].y + " idDessin:" + idDessin + " idPers:" + idPers);
					_level0.loader.contentHolder.referenceLayer["Personnage" + idPers].nom = this.listeDesPersonnages[j].nom;
				}
            }
        }
		
		
        //_level0.loader.contentHolder.planche.afficher();
				
		
        _level0.loader.contentHolder.horlogeNum = 60*objetEvenement.tempsPartie;
		
		_level0.loader.contentHolder.objectMenu.Boule.countTxt = "0";
		_level0.loader.contentHolder.objectMenu.Banane.countTxt = "0";
		_level0.loader.contentHolder.objectMenu.Livre.countTxt = 0;
		_level0.loader.contentHolder.objectMenu.piece.countTxt = 0;
		
		//newsbox
		var messageInfo:String = _root.texteSource_xml.firstChild.attributes.welcomeMess +  "         " + _root.texteSource_xml.firstChild.attributes.moveMess;
		this.newsChat.addMessage(messageInfo);

        remplirMenuPointage();
		
		objetEvenement = null;
        trace("fin de evenementPartieDemarree    " + getTimer());
        trace("*********************************************\n");
    } 
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	//   "JoueurRejoindrePartie"
	public function evenementJoueurRejoindrePartie(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, idPersonnage, Pointage
     	trace("*********************************************");
     	trace("debut de evenementJoueurRejoindrePartie   " + objetEvenement.nomUtilisateur + "    " + objetEvenement.idPersonnage + " " + objetEvenement.Pointage);
		
		if(this.listeDesPersonnages.length < maxPlayersInTable)
		{
		   this.listeDesPersonnages.push(new Object());		  
		}
		
		
					this.listeDesPersonnages[this.listeDesPersonnages -1].nom = objetEvenement.nomUtilisateur;
                    this.listeDesPersonnages[this.listeDesPersonnages -1].id = objetEvenement.idPersonnage;
		            this.listeDesPersonnages[this.listeDesPersonnages -1].clocolor = objetEvenement.clocolor;
		            this.listeDesPersonnages[this.listeDesPersonnages -1].role = 1;//objetEvenement.userRole;   !!!!!!!!!!!!!!!!!!!!!!
		            this.listeDesPersonnages[this.listeDesPersonnages -1].pointage = objetEvenement.Pointage;
		            this.listeDesPersonnages[this.listeDesPersonnages -1].win = 0;
		            this.listeDesPersonnages[this.listeDesPersonnages -1].idessin = calculatePicture(this.listeDesPersonnages[this.listeDesPersonnages -1].id);
					trace("test Rejoindre: "  + this.listeDesPersonnages[this.listeDesPersonnages -1].nom + objetEvenement.nomUtilisateur);
                	
		
	
		_level0.loader.contentHolder.planche.getPersonnageByName(objetEvenement.nomUtilisateur).afficher();
		dessinerMenu();
		remplirMenuPointage();
		   
		 //complete the message box  - newsbox
		 var messageInfo:String = objetEvenement.nomUtilisateur + _root.texteSource_xml.firstChild.attributes.InMess; 
		 this.newsChat.addMessage(messageInfo);
		
		objetEvenement = null;
		trace("fin de evenement evenementJoueurRejoindrePartie ");
        trace("*********************************************\n");
				
	}
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDemarrePartie(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, idPersonnage
     	trace("*********************************************");
     	trace("debut de evenementJoueurDemarrePartie   "+objetEvenement.nomUtilisateur+"     "+objetEvenement.idPersonnage + "   " + objetEvenement.clothesColor);
        var movClip:MovieClip;
        var j:Number=0;
        for (var i:Number = 0; i < maxPlayersInTable; i++)
        {
	        
			if(i>3) j=1;
			if(i>7) j=2;
			if(i>11) j=3;
        	if(listeDesPersonnages[i].nom == objetEvenement.nomUtilisateur)
        	{
            	this.listeDesPersonnages[i].id = objetEvenement.idPersonnage;
				this.listeDesPersonnages[i].pointage = 0;
				this.listeDesPersonnages[i].win = 0;
            	_level0.loader.contentHolder.refLayer["b" + i].removeMovieClip();
            	var idDessin:Number = calculatePicture(this.listeDesPersonnages[i].id);
				this.listeDesPersonnages[i].idessin = idDessin;
				var idPers:Number = calculateIDPers(this.listeDesPersonnages[i].id, idDessin);
            	var cloCol:String = objetEvenement.clothesColor;
				
				movClip = _level0.loader.contentHolder.refLayer.createEmptyMovieClip("Personnage" + idPers,i);
				
			    this.drawUserFrame3(i, cloCol, idDessin, movClip);
				
				//movClip = _level0.loader.contentHolder.refLayer.loadMovie("perso1.swf","b" + i,i);
				//movClip = _level0.loader.contentHolder.refLayer.attachMovie("Personnage" + idDessin,"b" + i, 100*i );
            	
				movClip._x = 485 - j*60;
                movClip._y = 150 + i*60 - j*240;
				movClip._xscale -= 70;
				movClip._yscale -= 70;
				//trace("idPers : " + idPers + "\n" + "idDessin");
		    
            	break;
        	}
        	
        }
		/*
        for(var i:Number = 0; i < maxPlayersInTable; i++)
        {
					trace( i + ": " + this.listeDesPersonnages[i].nom + " id:" + this.listeDesPersonnages[i].id);
		}
		*/
    	
		objetEvenement = null;
		trace("fin de evenementJoueurDemarrePartie");
    	trace("*********************************************\n");
    }
	////////////////////////////////////////////////////////////////////////////////////////////////////
	function calculateMenu():Number
	{
		// we make an array to sort the players regarding theirs points 
		var jouersStarted:Array = new Array();
		var count:Number = this.listeDesPersonnages.length;
		for (var i:Number = 0; i < count; i++) {
					jouersStarted[i] = new Object();
			        jouersStarted[i].nomUtilisateur = this.listeDesPersonnages[i].nom;
			        jouersStarted[i].pointage = this.listeDesPersonnages[i].pointage;
			        jouersStarted[i].role = this.listeDesPersonnages[i].role;
					jouersStarted[i].idessin = this.listeDesPersonnages[i].idessin;					
					//trace("Dans menuointage : " + jouersStarted[i].pointage + " " + jouersStarted[i].idessin + " " + this.listeDesPersonnages[i].idessin );
		}// end for
		
		// to cut the holes ...
		count = jouersStarted.length;
		for(i = 0; i < count; i++){
		      if(jouersStarted[i].role == 2 && this.typeDeJeu == "Tournament")   
		         jouersStarted.removeItemAt(i);
		}
		   		
		return jouersStarted.length;
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////	
	function remplirMenuPointage()
    {
		trace("*********************************************");
    	trace("debut de remplirMenuPointage   ");
		trace("type: " + this.typeDeJeu);
				
		// we make an array to sort the players regarding theirs points 
		var jouersStarted:Array = new Array();
		var count:Number = this.listeDesPersonnages.length;
		for (var i:Number = 0; i < count; i++) {
					jouersStarted[i] = new Object();
			        jouersStarted[i].nomUtilisateur = this.listeDesPersonnages[i].nom;
			        jouersStarted[i].pointage = this.listeDesPersonnages[i].pointage;
			        jouersStarted[i].role = this.listeDesPersonnages[i].role;
					jouersStarted[i].idessin = this.listeDesPersonnages[i].idessin;//this.listeDesPersonnages[i].idessin;
					jouersStarted[i].win = this.listeDesPersonnages[i].win;
					
					//trace("Dans menuointage : " + jouersStarted[i].nomUtilisateur + " " + this.listeDesPersonnages[i].nom );
		}// end for
		
		//sort the elements using a compare function
		jouersStarted.sort(compareByPointsDescending);
		//jouersStarted.reverse();
				
		// mettre les id en ordre : tabOrdonne.id contient les id des personnages en ordre de pointage
		// il suffit de mettre les MC correspondants sur le podium
		
		// to control the holes ...
		count = jouersStarted.length;
		for(i = 0; i < count; i++){
		      if((jouersStarted[i].role == 2 && this.typeDeJeu == "Tournament"))   
		         jouersStarted.removeItemAt(i);
		   }
		for(i = 0; i < count; i++){
		      if((jouersStarted[i].role == 2 && this.typeDeJeu == "Tournament"))   
		         jouersStarted.removeItemAt(i);
		   } 
		
		
		var fondClip:MovieClip = new MovieClip();
		var colorTrans:ColorTransform = new ColorTransform();
		
		// NOTE HUGO : Voici comment placer des variables dans des champs de texte dynamique
		// demonstrate the result
		for(i = 0; i < count; i++){
			
			
		    fondClip = _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + (i + 1)]["fondBleu" + (i+1)].fondIntern;
			var trans:Transform = new Transform(fondClip);
		   
			
			_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(i+1)]["nomJoueur"+(i+1)] = jouersStarted[i].nomUtilisateur;	
			_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(i+1)]["pointageJoueur"+(i+1)] = jouersStarted[i].pointage;
			
			// to color our player points
			if(jouersStarted[i].nomUtilisateur ==  this.nomUtilisateur)
			{
				colorTrans.rgb = 0x2A57F6;
			    trans.colorTransform = colorTrans;
			}
			else
			{
			    colorTrans.rgb = 0x000033;
			    trans.colorTransform = colorTrans;
			}
		  
			if(jouersStarted[i].win == 1){
			   this["Flag" + (i + 1)] = new MovieClip();
			   this["Flag" + (i + 1)] = _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(i+1)].attachMovie("checkFlag_mc", "flag" + i, 220 + i, {_x:-20, _y:0});
			   
			}
			//trace("Pointage: !!!!!!!!!! " + i + " " + jouersStarted[i].win);
			//this["tete"+j]=new MovieClip();
			this["tete" + (i + 1)] = _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+ (i + 1)]["tete"+ (i + 1)].attachMovie("tete" + jouersStarted[i].idessin, "Tete" + i, -10100 + i);
			this["tete" + (i + 1)]._x = -6;
			this["tete" + (i + 1)]._y = -6;
			this["tete" + (i + 1)]._xscale = 60;
			this["tete" + (i + 1)]._yscale = 60;
	
    	} 
		delete jouersStarted;
    }// end methode
	
	
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementPartieTerminee(objetEvenement:Object)
    {
        // parametre: 
    	trace("*********************************************");
    	trace("debut de evenementPartieTerminee   " + objetEvenement.statistiqueJoueur);
   
        var i,j:Number; 
		var k:Number = 0;
				
		//for(i = 0; i < objetEvenement.statistiqueJoueur.length; i++)
			//trace(i+" joueur objetEvenement "+objetEvenement.statistiqueJoueur[i].nomUtilisateur+"   "+objetEvenement.statistiqueJoueur[i].pointage);
		    	
    	    	
    	var taille:Number = objetEvenement.statistiqueJoueur.length;
		       
		// trouver une facon de faire fonctionner ces lignes :
		_root.vrai_txt.removeTextField();
		_root.faux_txt.removeTextField();
		_root.reponse_txt.removeTextField();
		_root.penalite_txt.removeTextField();
		_root.secondes_txt.removeTextField();
			
		// jouersStarted est liste de nom de joueurs et leurs pointage et IDs 
		//var jouersStarted:Array = new Array();
		
		for (i = 0; i < taille; i++) {
					
			this.tabPodiumOrdonneID[i] = new Object();
			this.tabPodiumOrdonneID[i].nomUtilisateur = objetEvenement.statistiqueJoueur[i].nomUtilisateur;
			this.tabPodiumOrdonneID[i].pointage = objetEvenement.statistiqueJoueur[i].pointage;
			this.tabPodiumOrdonneID[i].role = objetEvenement.statistiqueJoueur[i].userRole;
			this.tabPodiumOrdonneID[i].idessin = 0; 
						
		}// end for
						
		//this.tabPodiumOrdonneID.sortOn("pointage");
		//sort the elements using a compare function
		this.tabPodiumOrdonneID.sort(compareByPointsDescending);
		//this.tabPodiumOrdonneID.reverse();
		
		taille = this.tabPodiumOrdonneID.length;
		// to find the picture
		for (i = 0; i < 12; i++) {
												
			for(k = 0; k < taille; k++){
				
				if(listeDesPersonnages[i].nom == this.tabPodiumOrdonneID[k].nomUtilisateur)
					this.tabPodiumOrdonneID[k].idessin = this.listeDesPersonnages[i].idessin;
			      
			}	
		} // end find the picture
		
		
    	    	
  		_level0.loader.contentHolder.miniGameLayer["magasin"].removeMovieClip();
		_level0.loader.contentHolder["aide"].removeMovieClip();
    	_level0.loader.contentHolder["boutonFermer"].removeMovieClip();
		_level0.loader.contentHolder["banane"].removeMovieClip();
		_level0.loader.contentHolder["bananeUser"].removeMovieClip();
		_level0.loader.contentHolder["GUI_utiliserObjet"].removeMovieClip();
		_level0.loader.contentHolder["box_question"].removeMovieClip();
		_level0.loader.contentHolder.toss.removeMovieClip();
		_level0.loader.contentHolder["fond_MiniGame"]._y += 400;
		_level0.loader.contentHolder.branBox.removeMovieClip();
		_level0.loader.contentHolder.bananaBox.removeMovieClip();
		_level0.loader.contentHolder.toolTip.removeMovieClip();
		
		
		//s'assurer que la musique s'arrete en fin de partie
		_level0.loader.contentHolder.musique.stop();
		_level0.loader.contentHolder.musiqueDefault.stop();
	
    	_level0.loader.contentHolder.gotoAndStop(5);
    
    	Mouse.show();
 
  	
		// mettre les id en ordre : tabOrdonne.id contient les id des personnages en ordre de pointage
		// il suffit de mettre les MC correspondants sur le podium
		
		// to cut the holes ...
		
		   for(i = 0; i <= taille; i++){
		      if((this.tabPodiumOrdonneID[i].role == 2 && this.typeDeJeu == "Tournament") )   
		         this.tabPodiumOrdonneID.removeItemAt(i);
		   }  
		
		
		
		// NOTE HUGO : Voici comment placer des variables dans des champs de texte dynamique
		// demonstrate the result
		for(i = 1; i <= taille; i++){
					
			_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["nomJoueur" + i] = this.tabPodiumOrdonneID[i - 1].nomUtilisateur;	
			_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["pointageJoueur" + i] = this.tabPodiumOrdonneID[i - 1].pointage;
			//_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["tete" + i].attachMovie("tete" + this.tabPodiumOrdonneID[i].idessin,  "Tete" + i, -10100 + i);
            this["tete" + i] = _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["tete" + i].attachMovie("tete" + this.tabPodiumOrdonneID[i - 1].idessin,  "Tete" + i, -10100 + i - 1);
			this["tete" + i]._x = -6;
			this["tete" + i]._y = -6;
			this["tete" + i]._xscale = 60;
			this["tete" + i]._yscale = 60;		
    	}
    	this.endGame = true;
		objetEvenement = null;
    	trace("fin de evenementPartieTerminee    ");
    	trace("*********************************************\n");
    }// end methode
	
	// methode used as compare function to sort our players by their points
	public function compareByPointsAscending(element1, element2)
	{
		return element1.pointage - element2.pointage;
	}
	
	// methode used as compare function to sort our players by their points
	public function compareByPointsDescending(element1, element2)
	{
		return element2.pointage - element1.pointage;
	}
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDeplacePersonnage(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, anciennePosition et nouvellePosition, pointage, bonus
    	trace("*********************************************");
    	trace("debut de evenementJoueurDeplacePersonnage (sans compter les rotations)  " + objetEvenement.nomUtilisateur + "   " + objetEvenement.anciennePosition.x+"   "+objetEvenement.anciennePosition.y+"   "+objetEvenement.nouvellePosition.x+"   "+objetEvenement.nouvellePosition.y+"   "+objetEvenement.collision+"   "+objetEvenement.pointage+"   "+objetEvenement.argent);
   
    	var pt_initial:Point = new Point();
    	var pt_final:Point = new Point();
     
    	pt_initial = _level0.loader.contentHolder.planche.calculerPositionTourne(objetEvenement.anciennePosition.x, objetEvenement.anciennePosition.y);
     
    	pt_final = _level0.loader.contentHolder.planche.calculerPositionTourne(objetEvenement.nouvellePosition.x, objetEvenement.nouvellePosition.y);
   
		trace("juste avant la teleportation nom du perso et param  " + objetEvenement.anciennePosition.x + " " +  objetEvenement.anciennePosition.y + " " + objetEvenement.nouvellePosition.x + " " +  objetEvenement.nouvellePosition.y);
		//to cancel after end game virtual players move's
		if(!endGame){
			_level0.loader.contentHolder.planche.teleporterPersonnage(objetEvenement.nomUtilisateur, pt_initial.obtenirX(), pt_initial.obtenirY(), pt_final.obtenirX(), pt_final.obtenirY(), objetEvenement.collision);
	
	        // update players array
			var count:Number = this.listeDesPersonnages.length;
		   for(var i:Number=0; i < count; i++){
					
			   if(this.listeDesPersonnages[i].nom == objetEvenement.nomUtilisateur){
			      this.listeDesPersonnages[i].pointage = objetEvenement.pointage;
				  
				  // to put the flag
				  if(objetEvenement.bonus > 0)
		          {
			         this.listeDesPersonnages[i].win = 1;
		          }
			   }
		   }
		
		   // show the results
		   remplirMenuPointage();
		   // newsbox
		   var messageInfo:String = objetEvenement.nomUtilisateur + _root.texteSource_xml.firstChild.attributes.moveMessage; 
		   this.newsChat.addMessage(messageInfo);
		}
		objetEvenement = null;
     	trace("fin de evenementJoueurDeplacePersonnage");
     	trace("*********************************************\n");
    }   
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementUtiliserObjet(objetEvenement:Object)
    {
        // parametre: joueurQuiUtilise, joueurAffecte, objetUtilise, autresInformations
    	trace("*********************************************");
    	trace("debut de evenementUtiliserObjet  " + objetEvenement.joueurQuiUtilise + "   " + objetEvenement.joueurAffecte + "   " + objetEvenement.objetUtilise );
        var playerThat:String = objetEvenement.joueurQuiUtilise;
		var playerUnder:String = objetEvenement.joueurAffecte;
		
		// info for newsbox
		if(objetEvenement.objetUtilise == "Banane"){
		   var messageInfo:String = playerThat + _root.texteSource_xml.firstChild.attributes.bananaMess + playerUnder; 
		   this.newsChat.addMessage(messageInfo);
		 		
		}else if(objetEvenement.objetUtilise == "Livre")
		{
		   var messageInfo:String = objetEvenement.joueurQuiUtilise + _root.texteSource_xml.firstChild.attributes.bookUsedMess; 
		   this.newsChat.addMessage(messageInfo);
		}else if(objetEvenement.objetUtilise == "Braniac")
		{
		   var messageInfo:String = objetEvenement.joueurQuiUtilise + _root.texteSource_xml.firstChild.attributes.braniacUsedMess; 
		   this.newsChat.addMessage(messageInfo);
		}else if(objetEvenement.objetUtilise == "Boule")
		{
		   var messageInfo:String = objetEvenement.joueurQuiUtilise + _root.texteSource_xml.firstChild.attributes.cristallUsedMess; 
		   this.newsChat.addMessage(messageInfo);
		}
		
		
		
		// here we treat the Banana
		if(objetEvenement.objetUtilise == "Banane" && objetEvenement.joueurAffecte == this.nomUtilisateur )
		{
    	   //this.moveVisibility = this.moveVisibility - 2;
		   //if(this.moveVisibility < 1)
		    //  this.moveVisibility = 1;
			
			this.bananaState = true;
			trace("in the GE " + bananaState);
			setBananaTimer(playerUnder);
			
		   //if the player is in the minigame 
		   if(_level0.loader.contentHolder.planche.obtenirPerso().minigameLoade)
		   {
		      
			  if(_level0.loader.contentHolder.miniGameLayer["Minigame"])
			  {
                  _level0.loader.contentHolder.miniGameLayer["Minigame"].loader.contentHolder.quitter(true);
			  }else  if(_level0.loader.contentHolder.miniGameLayer["magasin"])
			  {
                  _level0.loader.contentHolder.miniGameLayer["magasin"].loader.contentHolder.quitter();
			  }
			 			  
			  _level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
			   this.moveVisibility = this.moveVisibility - 2;
		       if(this.moveVisibility < 1)
		       this.moveVisibility = 1;
		      _level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
			
			_global.timerIntervalBanana = setInterval(this, "waitBanana", 4500, playerUnder);
			  
			
			
			// if the player read at the moment a question
		   }else if(_level0.loader.contentHolder.box_question.monScroll._visible)
		   {
    		   
			   this.cancelQuestion();
			   _level0.loader.contentHolder.box_question.gotoAndPlay(9);
			   _root.objGestionnaireInterface.afficherBoutons(1);
			   _level0.loader.contentHolder.sortieDunMinigame = false;
			   
			   _level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
			    this.moveVisibility = this.moveVisibility - 2;
		        if(this.moveVisibility < 1)
		          this.moveVisibility = 1;
		       _level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
			  
			  _global.timerIntervalBanana = setInterval(this, "waitBanana", 4500, playerUnder);
			  
			   //_global.timerIntervalBananaShell = setInterval(this, "bananaShell", 8000);	
			
			// if the player read a feedback of a question 
		   }else if(_level0.loader.contentHolder.box_question.GUI_retro.texteTemps._visible) 
		   {
			   // catch the rested time to be used after banana show
			   	var tempsRested:Number = _level0.loader.contentHolder.box_question.GUI_retro.tempsPenalite;
								
				_level0.loader.contentHolder.box_question.monScroll._visible = false;
				_level0.loader.contentHolder.box_question._visible = false;
				_level0.loader.contentHolder.box_question.GUI_retro.removeMovieClip();
				_global.timerIntervalBanana = setInterval(this, "waitBanana", 4500, playerUnder);
				
			    // here show banana in action
			    // setTimeout( Function, delay in miliseconds, arguments)
               _global.timerInterval = setInterval(this,"funcToRecallFeedback", 7000, tempsRested);
			   
			  	   
				//_root.objGestionnaireInterface.effacerBoutons(1);
			  
		   }else{
		      
                _level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
				
				 this.moveVisibility = this.moveVisibility - 2;
		         if(this.moveVisibility < 1)
		            this.moveVisibility = 1;
			    _level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
				_global.timerIntervalBanana = setInterval(this, "waitBanana", 4500, playerUnder);
						
		   
		   }//end else if
		   		   
		   _global.timerIntervalMessage = setInterval(this,"funcToCallMessage", 6000, playerThat);
		   
		}else if(objetEvenement.objetUtilise == "Banane" && objetEvenement.joueurAffecte != this.nomUtilisateur)
		{
			
			
		    _global.timerIntervalBananaAutre = setInterval(this, "waitBananaAutre", 4500, playerUnder);
					
		}// end if
		
		if(objetEvenement.objetUtilise == "Banane" && playerThat != this.nomUtilisateur)
		{
			_level0.loader.contentHolder.planche.tossBananaShell(playerThat, playerUnder);//getPersonnageByName(playerThat).tossBanana();
			
		}
		//***********  END treat the Banana **************************
		
		// we put our perso in Braniac...  
		if(objetEvenement.objetUtilise == "Braniac" && objetEvenement.joueurAffecte == this.nomUtilisateur)
		{
			//this.moveVisibility = +1;
			_level0.loader.contentHolder.planche.obtenirPerso().setBraniac(true);
			this.setBraniacTimer(playerUnder);
			
		}else if (objetEvenement.objetUtilise == "Braniac" && objetEvenement.joueurAffecte != this.nomUtilisateur)
		{
			_level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).setOutBraniac();
		}
		
		objetEvenement = null;
     	trace("fin de evenementUtiliserObjet");
     	trace("*********************************************\n");
    } // end methode
	
	// this function is used to put on the Sprite the Timer of the Braniac
	// after the time finished it must disapear
	function setBraniacTimer(playerUnder)
	{
		
		
		
		//first on put on the sprite the box for the timer if is our perso
		if(playerUnder == this.nomUtilisateur)
		{
		   _level0.loader.contentHolder.attachMovie("timeBox", "branBox",6);//_level0.loader.contentHolder.getNextHigesthDepth());
		   _level0.loader.contentHolder.branBox._x = 470;
		   _level0.loader.contentHolder.branBox._xscale = 90;
		   _level0.loader.contentHolder.branBox._y = 320;
		
		   //create text field to put info in
		   _level0.loader.contentHolder.branBox.createTextField("braniacTime", _level0.loader.contentHolder.branBox.getNextHigesthDepth(), 20, 5, 40, 20);
		
		   // Make the field dynamic text field
           _level0.loader.contentHolder.branBox.braniacTime.type = "dynamic";
           //_level0.loader.contentHolder.branBox.braniacTime.variable = "timeRest";
           with(_level0.loader.contentHolder.branBox.braniacTime)
           {
	          multiline = false;
	          background = false;
	          //text = "5";
	          textColor = 0x65FF00;
	          border = false;
	          _visible = true;
	          //autoSize = true;
           }
   
           var formatTimer:TextFormat = new TextFormat();
           formatTimer.bold = true;
           formatTimer.size = 16;
           formatTimer.font = "Impact";
           formatTimer.align = "Center";
           _level0.loader.contentHolder.branBox.braniacTime.setNewTextFormat(formatTimer);
		}//end if
		
		
		if(_global.restedTime > 0) 
		{ 
		    _global.restedTime += 89;
		}else
		{
		    _global.restedTime = 89; 
		}
		  
		
		if(_global.intervalIdBran != null) {
		
             // trace("clearInterval************************************    " + tempTime);
			 clearInterval(_global.intervalIdBran);
        }

		   // _global.restedTime:Number = 89;
	      _global.intervalIdBran = setInterval(branTimerSet, 1000, playerUnder);	// sert pour attendre la jusqu'a la fin de action de Braniac
	   
	      function branTimerSet(playerUnder:String){
	        _global.restedTime--;	  
		   _level0.loader.contentHolder.branBox.braniacTime.text =  _global.restedTime; 
		   if( _global.restedTime == 2)
		   {
			  _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).obtenirImage().braniacState = "end";
		   }
		   else if( _global.restedTime == 0)
	       {
			  _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).setBraniac(false);
			 
			  if(playerUnder == _level0.loader.contentHolder.objGestionnaireEvenements.nomUtilisateur)
			  {
				  if(_level0.loader.contentHolder.planche.obtenirPerso().boardCentre == false || _level0.loader.contentHolder.box_question.GUI_retro.texteTemps._visible)
		          {
				     _level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility--;
			  			
		          }else if(_level0.loader.contentHolder.box_question.monScroll._visible || _level0.loader.contentHolder.planche.obtenirPerso().minigameLoade)
		          {
			         _level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility--;
		      			
		          }else{
				
                     _level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
				     _level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility--;
			         _level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
		          }  
			  }
					
		   } // 
		   
		   // to end the Braniac and remove the timer box
		   if( _global.restedTime < 0)
		   { 
		      _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).obtenirImage().braniacState = "out";
              _level0.loader.contentHolder.branBox.removeMovieClip();
		      clearInterval(_global.intervalIdBran);
		   }
			
	   } // end function branTimerSet
		
	}// end function  setBraniacTimer
	
	//*****************************************************************************************
	// this function is used to put on the Sprite the Timer of the Banana
	// after the time finished it must disapear
	function setBananaTimer(playerUnder:String)
	{
		//first on put on the sprite the box for the timer
		var banBox:MovieClip = 	_level0.loader.contentHolder.attachMovie("timeBox", "bananaBox", 7);//_level0.loader.contentHolder.getNextHigesthDepth());
		_level0.loader.contentHolder.bananaBox._x = 390;
		_level0.loader.contentHolder.bananaBox._xscale = 90;
		_level0.loader.contentHolder.bananaBox._y = 320;
		
		//create text field to put info in
		_level0.loader.contentHolder.bananaBox.createTextField("bananaTime", _level0.loader.contentHolder.branBox.getNextHigesthDepth(), 20, 5, 40, 20);
		
		 // Make the field dynamic text field
        _level0.loader.contentHolder.bananaBox.bananaTime.type = "dynamic";
        //_level0.loader.contentHolder.branBox.braniacTime.variable = "timeRest";
        with(_level0.loader.contentHolder.bananaBox.bananaTime)
        {
	       multiline = false;
	       background = false;
	       //text = "5";
	       textColor = 0x65FF00;
	       border = false;
	       _visible = true;
	       //autoSize = true;
        }
   
        var formatTimer:TextFormat = new TextFormat();
        formatTimer.bold = true;
        formatTimer.size = 16;
        formatTimer.font = "Impact";
        formatTimer.align = "Center";
        _level0.loader.contentHolder.bananaBox.bananaTime.setNewTextFormat(formatTimer);
		
		if(_global.restedTimeBanana > 0) 
		{ 
		    _global.restedTimeBanana += 90;
		}else
		{
		    _global.restedTimeBanana = 90; 
		}
		
		if(_global.intervalIdBanana != null) {
		
             // trace("clearInterval************************************    " + tempTime);
			 clearInterval(_global.intervalIdBanana);
        }

		//_global.intervalIdBanana:Number;
	    _global.intervalIdBanana = setInterval(bananaTimerSet, 1000, playerUnder);	
	   
	   function bananaTimerSet(playerUnder){
	       
		   _global.restedTimeBanana--;	  
		   
		   _level0.loader.contentHolder.bananaBox.bananaTime.text = _global.restedTimeBanana; 
		   	var repost:Boolean = false;
			
			if(_global.restedTimeBanana < 3)
			   repost = true;
		   // to remove the timer box
		   if(_global.restedTimeBanana < 0)
		   {  
		      _level0.loader.contentHolder.bananaBox.removeMovieClip();
		      
			  if(repost)
		      {
				 _level0.loader.contentHolder.objGestionnaireEvenements.bananaState = false;
			     _level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility += 2;
		         if(_level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility > 6)
		            _level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility = 6;
					
					
			  }else
			  {
		         _level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
			     _root.objGestionnaireInterface.effacerBoutons(1);
			     _level0.loader.contentHolder.objGestionnaireEvenements.bananaState = false;
			     _level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility += 2;
		         if(_level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility > 6)
		            _level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility = 6;
					//_level0.loader.contentHolder.planche.obtenirPerso().setBoardCentre(false);
				 _level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
			  }
		      clearInterval(_global.intervalIdBanana);
		   }
			
	   } // end function bananaTimerSet
		
	}// end function  setBananaTimer
	
	//****************************************************************************
	// cette fonction attend jusqu'au signal du compteur
	// et appelle le fonction d'action de la Banane
    function waitBanana(playerUnder:String):Void
    {
        _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).slippingBanana();
	    clearInterval(_global.timerIntervalBanana);
						
    }
	
	//****************************************************************************
	// cette fonction attend jusqu'au signal du compteur
	// et appelle le fonction d'action de la Banane
    function waitBananaAutre(playerUnder:String):Void
    {
        _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).slippingBanana();
	    clearInterval(_global.timerIntervalBananaAutre);
				
    }
	
	
	function funcToCallMessage(playerThat:String)
	{
		var twMove:Tween;
        var guiBanane:MovieClip
		guiBanane = _level0.loader.contentHolder.attachMovie("GUI_banane", "banane", 9998);
		guiBanane._y = 200;
        guiBanane._x = 275;
		_level0.loader.contentHolder["banane"].nomCible = " ";
	    _level0.loader.contentHolder["banane"].nomJoueurUtilisateur = playerThat;
	    twMove = new Tween(guiBanane, "_alpha", Strong.easeOut, 40, 100, 1, true);
		 clearInterval(_global.timerIntervalMessage);
	}
    
	function funcToRecallFeedback(tempsRested:Number):Void
    {
		  
		  trace("callback: " + getTimer() + " ms.");

          //and now continue to show the feedback 
		  _level0.loader.contentHolder.box_question._visible = true;
		  _level0.loader.contentHolder.box_question.monScroll._visible = true;
		  var ptX:Number = _level0.loader.contentHolder.box_question.monScroll._x;
		  var ptY:Number = _level0.loader.contentHolder.box_question.monScroll._y;
		  _level0.loader.contentHolder.box_question.attachMovie("GUI_retro","GUI_retro", 100, {_x:ptX, _y:ptY});
	      _level0.loader.contentHolder.box_question.GUI_retro.timeX = tempsRested;
			   
			   clearInterval(_global.timerInterval);
     
    } // end methode
	
	
 /*   ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function EvenementDeplacementWinTheGame(objetEvenement:Object)
    {
    	// parametre: x, y 
		trace("*********************************************");
		trace("debut de EvenementDeplacementWinTheGame  " + objetEvenement.x+"   "+objetEvenement.y);
	
		var pt_initial:Point = new Point();
		var pt_final:Point = new Point();

		trace("juste avant la teleportation");

    	trace("fin de EvenementDeplacementWinTheGame");
    	trace("*********************************************\n");
    }*/
	
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      Autres
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    public function obtenirNumeroTable():Number
    {
        return this.numeroTable;
    }
	
    //////////////////////////////////////////////////////////////////////////////////////////
    public function obtenirTempsPartie():Number
    {
        return this.tempsPartie;
    }
	
    //////////////////////////////////////////////////////////////////////////////////////////
    public function definirTempsPartie(t:Number)
    {
        trace("*********************************************");
        trace("debut de definirTempsPartie   "+t);
        this.tempsPartie = t;
        trace("fin de tempsPartie");
        trace("*********************************************\n");
    }
	
	public function obtenirGestionnaireCommunication():GestionnaireCommunication
	{
		return objGestionnaireCommunication;
	}
	/*
	// here we control if username contain the  'master'
	// this function is not used for the moment
	// is not in use now
	public function controlForMaster(nom:String):Boolean
    {
		// Bloc of code to treat the username
        var firstDel = nom.indexOf(".");                 // find first delimiter
        var secondDel = nom.indexOf(".",firstDel + 1);   // find second delimiter
        var master;

        //Now extract the 'master' from username
        if (firstDel != -1 && secondDel != -1)
           master = nom.substring(0, firstDel);
        else
           master = "";
		   //trace(" controlForMaster : " + master);
        return (master == "game-master" || master == "maitre-du-jeu");
    }// end method  */
	
	// used to calculate the id of the players picture to show on the game board
	private function calculatePicture(perso:Number):Number
	{
		return((perso-10000)-(perso-10000)%100)/100;
	}
	
	private function calculateIDPers(perso:Number, idDessin:Number):Number
	{
		return (perso - 10000 - idDessin * 100);
	}
    
	//// function used to draw the points menu	
	////////////////////////////////////////////////////
	public function dessinerMenu()
	{
		// used to know the size of menu
       var playersNumber:Number = calculateMenu();
      // Create the base shape with blue color
      _level0.loader.contentHolder.createEmptyMovieClip("menuPointages", 5);
      _level0.loader.contentHolder.menuPointages._x = 450;
      _level0.loader.contentHolder.menuPointages._y = 60;
      drawRoundedRectangle(_level0.loader.contentHolder.menuPointages, 100, 31 + 8 + playersNumber*21, 10, 0x2A57F6, 100);

      // add the intern black box there put the players lines
      _level0.loader.contentHolder.menuPointages.createEmptyMovieClip("mc_autresJoueurs", 11);
      _level0.loader.contentHolder.menuPointages.mc_autresJoueurs._x = 7;
      _level0.loader.contentHolder.menuPointages.mc_autresJoueurs._y = 31;
      drawRoundedRectangle(_level0.loader.contentHolder.menuPointages.mc_autresJoueurs, 86, 2 + playersNumber*21, 3, 0x000000, 100);

      //add the shines
     if( playersNumber > 3)
     {
      _level0.loader.contentHolder.menuPointages.attachMovie("shine1", "shine11", 14, {_x:3, _y:40});
      _level0.loader.contentHolder.menuPointages.attachMovie("shine1", "shine12", 15, {_x:96, _y:55});
     }
     _level0.loader.contentHolder.menuPointages.attachMovie("shine2", "shine22", 16, {_x:1, _y:(26 + playersNumber * 21) });

     // add the title
     if(langue == "fr")
     {
	    _level0.loader.contentHolder.menuPointages.attachMovie("pointages titre", "mc_pointages_title", 12, {_x:7, _y:3});
	
     }
     else
     {
	    _level0.loader.contentHolder.menuPointages.attachMovie("score title", "mc_score_title", 12, {_x:7, _y:3});
	
     }
	
     // add the players mc
     for (var i:Number = 1; i <= playersNumber; i++){

        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs.createEmptyMovieClip("mc_joueur" + i, i );
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]._x = 0;
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]._y = 2 + 21 * (i - 1);
   
        var playerNumberBox:String = "";
        if( i == 1){
           //playerNumberBox == "golden_box";
	       _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].attachMovie("gold_box","number_box" + i, i, {_x:0, _y:0});	
        }else if( i == 2){
           //playerNumberBox == "silver_box";
	       _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].attachMovie("silver_box","number_box" + i, i, {_x:0, _y:0});	
        }else if( i == 3){
           //playerNumberBox == "bronze_box";
	       _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].attachMovie("bronze_box","number_box" + i, i, {_x:0, _y:0});	
        }else{ 
           //playerNumberBox == "green_box";
	       _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].attachMovie("green_box","number_box" + i, i, {_x:0, _y:0});
		}
        
		if(i < 10)
	       _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["number_box" + i].listNumber = "  " + i;
	    else
	     _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["number_box" + i].listNumber = i;
        // end else
   
        //background
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].attachMovie("fondBleu_mc","fondBleu" + i, 20 + i, {_x:12, _y:0});
        // players face
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].attachMovie("faceHolder2","tete" + i, 40 + i, {_x:22, _y:7});
        // players name
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].createTextField("dtNamePlayer" + i, 60 + i, 30, -4, 55, 14);
      
        // Make the field an label text field
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["dtNamePlayer" + i].type = "dynamic";
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["dtNamePlayer" + i].variable = "nomJoueur" + i;
        with(_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["dtNamePlayer" + i])
        {
	       multiline = false;
	       background = false;
	       text = "";
	       textColor = 0xFFFFFF;
	       border = false;
	       _visible = true;
	       //autoSize = true;
        }
   
        var formatName:TextFormat = new TextFormat();
        formatName.bold = true;
        formatName.size = 8;
        formatName.font = "Times New Roman";
        formatName.align = "Right";
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["dtNamePlayer" + i].setNewTextFormat(formatName);
        //******************************************************************************************
   
        // players points
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].createTextField("dtPoints" + i, 200 + i, 50, 5, 35, 15);
      
        // Make the field an label text field
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["dtPoints" + i].type = "dynamic";
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["dtPoints" + i].variable = "pointageJoueur" + i;
        with(_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["dtPoints" + i])
        {
	       multiline = false;
	       background = false;
	       text = "";
	       textColor = 0xFF9933;
	       border = false;
	       _visible = true;
	       //autoSize = true;
        }
   
        var formatPoints:TextFormat = new TextFormat();
        formatPoints.bold = true;
        formatPoints.size = 8;
        formatPoints.font = "Arial";
        formatPoints.align = "Right";
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i]["dtPoints" + i].setNewTextFormat(formatPoints);
		   
       //menuPointages.mc_autresJoueurs["mc_joueur" + i].attachMovie("checkFlag_mc","flag" + i, 220 + i, {_x:-20, _y:0});
    
     
   }// end for
  
} //end fonction 


// modified code from source - www.adobe.com
function drawRoundedRectangle(target_mc:MovieClip, boxWidth:Number, boxHeight:Number, cornerRadius:Number, fillColor:Number, fillAlpha:Number):Void {
    with (target_mc) {
		
		lineStyle(2, 0x000000, 100);

        beginFill(fillColor, fillAlpha);
        moveTo(cornerRadius, 0);
        lineTo(boxWidth - cornerRadius, 0);
        curveTo(boxWidth, 0, boxWidth, cornerRadius);
        lineTo(boxWidth, cornerRadius);
        lineTo(boxWidth, boxHeight - cornerRadius);
        curveTo(boxWidth, boxHeight, boxWidth - cornerRadius, boxHeight);
        lineTo(boxWidth - cornerRadius, boxHeight);
        lineTo(cornerRadius, boxHeight);
        curveTo(0, boxHeight, 0, boxHeight - cornerRadius);
        lineTo(0, boxHeight - cornerRadius);
        lineTo(0, cornerRadius);
        curveTo(0, 0, cornerRadius, 0);
        lineTo(cornerRadius, 0);
        endFill();
    }
}//end function

function drawToolTip(messInfo:String, mcMovie:MovieClip)
{
	var stringLength:Number = messInfo.length;
	var wid:Number = Math.floor(stringLength / 20 * 16);
	_level0.loader.contentHolder.createEmptyMovieClip("toolTip", _level0.loader.contentHolder.getNextHigesthDepth());
	_level0.loader.contentHolder.toolTip.swapDepths(mcMovie);
	drawRoundedRectangle(_level0.loader.contentHolder.toolTip, 120, wid + 10, 15, 0xFFEB5B, 100);
	_level0.loader.contentHolder.toolTip.createTextField("toolTipMessage", 60, 5, 3, 110, wid);
	
	 // Make the field an label text field
       _level0.loader.contentHolder.toolTip.toolTipMessage.type = "dynamic";
       
       with(_level0.loader.contentHolder.toolTip.toolTipMessage)
       {
	       multiline = true;
	       background = false;
	       text = messInfo;
	       textColor = 0x330000;
	       border = false;
	       _visible = true;
	       //autoSize = true;
		   wordWrap = true;
	       autoSize = "left";
		   maxChars = 70;
       }
	  _level0.loader.contentHolder.toolTip.toolTipMessage.setStyle("fontSize", "7");
	
	_level0.loader.contentHolder.toolTip._visible = false;
	//_level0.loader.contentHolder.

}

function drawUserFrame3(i:Number, colorC:String, idDessin:Number, movClip:MovieClip)
{
	 
	//***********************************************
	// to load the perso .. use ClipLoader to know the moment of complet load
    // create them dinamicaly
	   var mcLoaderString = "myLoader" + i;
	   this["mcLoaderString"] = new MovieClipLoader();
	   var mclListenerString = "myListener" + i;
	   this["mclListenerString"] = new Object();
       this["mclListenerString"].onLoadComplete = function(target_mc:MovieClip) {
            		    			  
		   target_mc.clothesCol = colorC;
			             
		};
		this["mcLoaderString"].addListener(this["mclListenerString"]);
					
	   
	   this["mcLoaderString"].loadClip("persox" + idDessin + ".swf", movClip);
}

// to take a good Id for our perso 
/*
function haveThisId(idPers:Number):Boolean
{
   	var i:Number;
	
	for(i = 0; i < _level0.loader.contentHolder.tableauDesPersoChoisis.length; i++)
	{
		if(_level0.loader.contentHolder.tableauDesPersoChoisis[i] == idPers)
		   return true;
	}
	return false;
}*/
	
	
}// end class
