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
import mx.utils.*;
import flash.geom.Transform;
import flash.geom.ColorTransform;
import flash.filters.ColorMatrixFilter;
import NewsBox;
//import Personnage;

class GestionnaireEvenements
{
    // subs this by ouPerso ? encapsulation we must have only reference to ourPerso
	private var nomUtilisateur:String;    // user name of our  user
	private var userRole:Number;  // if 1 - simple user, if 2 - is admin(master), if 3 - is  prof
	private var motDePasse:String;  // notre mot de passe pour pouvoir jouer
	private var langue;
	
	private var roomDescription:String;  // short room description taked from DB
	private var numeroDuDessin:Number; // sert a associer la bonne image pour le jeu d'ile au tresor
	
    private var nomSalle:String;  //  nom de la salle dans laquelle on est
	private var idRoom:Number;    // ID of our room in the server's list of rooms
	private var masterTime:Number; // masterTime of room, if masterTime != 0 is taked masterTime for the time of game 
	
	// subs this 5 by GameTable object
	//private var numeroTable:Number;   //   numero de la table dans laquelle on est
	//private var tablName:String;     // name of the created table
    //private var tempsPartie:Number;   //  temps que va durer la partie, en minutes
	//private var typeDeJeu:String;        // gameType in our table
	private var nbTracks:Number;   // usually is 4, 
    
	private var listeDesJoueursDansSalle:Array;  // liste des joueurs dans la salle qu'on est. Un joueur contient un nom (nom) ???? 
    private var playersNumber:Number;   // ???????? we really need it?
	public var  listeDesSalles:Array;    //  liste de toutes les salles                !!!! Combiner ici tout dans un Objet
	private var maxPlayers:Number; // Number max of players by table in the room where we are  
	private var listeChansons:Array;    //  liste de toutes les chansons
    private var listeDesJoueursConnectes:Array;   // la premiere liste qu'on recoit, tous les joueurs dans toutes les salles. Un joueur contient un nom (nom)
    //liste de toutes les tables dans la salle ou on est
    //contient un numero (noTable), le temps (temps) et une liste de joueurs (listeJoueurs) un joueur de la liste contient un nom (nom)
    private var listeDesTables:Array;   // list of tables in our room with list of users in 
    private var objGestionnaireCommunication:GestionnaireCommunication;  //  pour caller les fonctions du serveur 
	private var tabPodiumOrdonneID:Array;			// id des personnages ordonnes par pointage une fois la partie terminee
		
	
	private var endGame:Boolean;   // used to indicate the end of game
	private var newsChat:NewsBox;  // all the messages to show in newsbox	
	private var finishPoints:Array;
	
	// used to take bonus
	private var winIt:Number;
		
	private var clothesColorID:Number;   // color ID - number of the color set of pictures. Given from server.	encapsulate too ...
	// sets of persos colors
	private var colorsSource_xml:XML;
	
	private var allowedTypes:Array;   // allowed types of game in our room - course, tournament ...
	
	private var allowedTypesMap:Array;
	private var allowedTypesTracks:Array;			
	
    // is used in selectPlayer in the cases the player 
    // change his first selected picture
    public var wasHereOnce:Boolean = false;
	private var isOldGame:Boolean = false;
	// indicate if we are in the process to select our perso
	private var inSelectPlayer:Boolean;
	
	// move to the table object ... maybe later
	private var tableauDesPersonnages:Array;
	private var ourPerso:MyPersonnage;  // reference to our personnage on the table(planche) ..
	private var ourTable:GameTable;
		
///////////////////////////////////////////////////////////////////////
    public function setInSelectPlayer(select:Boolean)
	{
		this.inSelectPlayer = select;
	}
    public function getOurTable():GameTable
	{
		return this.ourTable;
	}
	
	public function setOurTable(table:GameTable)
    {
    	this.ourTable = table;
    }

	public function getOurPerso():MyPersonnage
	{
		return this.ourPerso;
	}
	
	public function setOurPerso(perso:MyPersonnage)
    {
    	this.ourPerso = perso;
    }
	
	public function getPersonnageByName(playerName:String):IPersonnage
	{
		for(var i in tableauDesPersonnages)
		{
		   if(tableauDesPersonnages[i].obtenirNom() == playerName){
			 
			  return tableauDesPersonnages[i];
		   }
		}
	}
	
	public function removePersoFromListe(nameP:String)
    {
		for(var i in tableauDesPersonnages)
		{
		   if(tableauDesPersonnages[i].obtenirNom() == nameP){
			 
			  tableauDesPersonnages.splice(i,1);
		   }
		}
	}

    public function obtenirTableauDesPersonnages():Array
    {
        return this.tableauDesPersonnages;
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
	
	public function obtenirGestionnaireCommunication():GestionnaireCommunication
	{
		return objGestionnaireCommunication;
	}
	    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                  CONSTRUCTEUR
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function GestionnaireEvenements(nom:String, passe:String, langue:String)
    {
        trace("*********************************************");
        trace("debut du constructeur de gesEve      " + nom + "      " + passe);
        this.nomUtilisateur = nom;
        this.motDePasse = passe;
		this.langue = langue;
        this.nomSalle = new String();
        this.listeDesSalles = new Array();
		this.allowedTypes = new Array();
		this.listeDesTables = new Array();
		this.listeChansons = new Array();
        this.listeDesJoueursConnectes = new Array();
        this.listeDesJoueursDansSalle = new Array();
		this.tabPodiumOrdonneID = new Array();
		tableauDesPersonnages =  new Array();
		this.endGame = false;
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port, 10);
		
		this.newsChat = new NewsBox();
		this.clothesColorID = 0;
		this.colorsSource_xml = new XML();
		// get the xml for perso's colors
		this.treatTheColorsXML();
		
		this.isOldGame = false;
		
		this.winIt = 0;
		this.maxPlayers = 0;
		this.allowedTypesMap = new Array("mathEnJeu", "Tournament", "Course");
		this.allowedTypesTracks = new Array();
				
        this.objGestionnaireCommunication = new GestionnaireCommunication(Delegate.create(this, this.evenementConnexionPhysique), 
																		  Delegate.create(this, this.evenementDeconnexionPhysique),
																		  url_serveur, port);	
    	trace("fin du constructeur de gesEve");
    	trace("*********************************************\n");
    } // end cons
	
	function treatTheColorsXML()
	{
		// Create new XML Object and set ignoreWhite true
       colorsSource_xml.ignoreWhite = true;
       // Setup load handler which just invokes another function which will do the parsing of our XML
       colorsSource_xml.onLoad = function(success) {
	      if (success) {
		     processTexteSource();
	      }
       };
	   var path:String = _level0.loader.contentHolder.path;
	   if(path == undefined) path = "";
	   colorsSource_xml.load(path + 'colors.xml');
	   function processTexteSource()
	   {
		   //trace("colors loaded.......******************");
	   }
		  
	}
	
	 
	
	// we treat persos colors xml and return the string of color of our perso
	public function getColorByID(colorID:Number, idDessin:Number):String
	{
		var ourColor:String;
		
		var lstNoeudsPersos:Array = colorsSource_xml.firstChild.childNodes;
		//trace("lstNoeudsPersos : "  + lstNoeudsPersos.length);
		// Passer tous les parametres et trouver le notre
        for (var i:Number = 0; i < lstNoeudsPersos.length; i++)
        {
          // Faire la reference vers le noeud courant
          var objNoeudPerso:XMLNode = lstNoeudsPersos[i];
		  var nb:Number = Number(objNoeudPerso.attributes.ID);
		  //trace("color nb  = " + nb);
		  if(nb == idDessin)
		  {
			  var lstNoeudsColors:Array = objNoeudPerso.childNodes;
		      //trace("lstNoeudsColors : "  + lstNoeudsColors.length);
			  for (var k:Number = 0; k < lstNoeudsColors.length; k++)
              {
                 // Faire la reference vers le noeud courant
                 var objNoeudColor:XMLNode = lstNoeudsColors[k];
		         var nmb:Number = Number(objNoeudColor.attributes.ID);
		         //trace("color nmb  = " + nmb);
		         if(nmb == colorID)
		         {
					 //trace("color value " +  objNoeudColor.toString());
			        return String(objNoeudColor.firstChild.nodeValue);
		         }
			  } // end 2 for
		  } // endd if
		 
		}// end 1 for
	}// end method
	
	// we treat persos colors xml and return the set of 12 string's of colors 
	function getColorsByID(colorID:Number):Array
	{
		trace("color number  = " + colorID);
		var ourColors:Array;
		
		var lstNoeudsPersos:Array = colorsSource_xml.firstChild.childNodes;
		trace("lstNoeudsPersos : "  + lstNoeudsPersos.length);
		// Passer tous les parametres et trouver le notre
        for (var i:Number = 0; i < lstNoeudsPersos.length; i++)
        {
           // Faire la reference vers le noeud courant
           var objNoeudPerso:XMLNode = lstNoeudsPersos[i];
		   var lstNoeudsColors:Array = objNoeudPerso.childNodes;
		   for (var k:Number = 0; k < lstNoeudsColors.length; k++)
           {
              // Faire la reference vers le noeud courant
              var objNoeudColor:XMLNode = lstNoeudsColors[k];
		      var nmb:Number = Number(objNoeudColor.attributes.ID);
		      //trace("color nmb  = " + nmb);
		      if(nmb == colorID)
		      {
					trace("color value " +  String(objNoeudColor.nodeValue));
			        ourColors.push(String(objNoeudColor.nodeValue));					
		      }
		   } // end 2 for
		  		 
		}// end 1 for
		return ourColors;
	}// end method
		    
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
	function getMaxPlayers():Number
	{
		return this.maxPlayers;
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
	
	//////////////////////////////////////////////////////////////////////////////////////////
	public function backToFrameOneGetRooms()
	{
		trace("*********************************************");
		// roomsType - general ou profs rooms
        trace("back to frame1 " + _level0.roomsType);
		this.objGestionnaireCommunication.obtenirListeSallesRetour(Delegate.create(this, this.retourObtenirListeSalles), _level0.roomsType);
		
	}// end method  
	
	
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
        this.objGestionnaireCommunication.restartOldGame(Delegate.create(this, this.feedbackRestartOldGame), 
											             Delegate.create(this, this.evenementJoueurEntreTable), 
														 Delegate.create(this, this.evenementJoueurQuitteTable), 
														 Delegate.create(this, this.evenementPartieDemarree), 
														 Delegate.create(this, this.evenementJoueurDeplacePersonnage), 
														 Delegate.create(this, this.evenementSynchroniserTemps), 
														 Delegate.create(this, this.evenementUtiliserObjet), 
														 Delegate.create(this, this.evenementPartieTerminee),
														 Delegate.create(this, this.eventServerWillStop),
														 Delegate.create(this, this.evenementJoueurRejoindrePartie));  // , Delegate.create(this, this.feedbackRestartListePlayers)
        trace("end restart Old Game");
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
	            //maxPlayersInTable = listeDesSalles[i].maxnbplayers;
	            //typeDeJeu = listeDesSalles[i].typeDeJeu;
	            this.idRoom = listeDesSalles[i].idRoom;
				this.nomSalle = listeDesSalles[i].nom;
                if(listeDesSalles[i].possedeMotDePasse == true)
                {
					
	               guiPWD = _level0.loader.contentHolder.attachMovie("GUI_pwd", "guiPWD", 2003);//_level0.loader.contentHolder.getNextHighestDepth());
                   //guiPWD.textGUI_PWD.text = _root.texteSource_xml.firstChild.attributes.textGUI_PWD;
					
                   
			     }else{
					   this.objGestionnaireCommunication.entrerSalle(Delegate.create(this, this.retourEntrerSalle), this.idRoom, motDePasseSalle);
					   //_level0.loader.contentHolder.gotoAndPlay(2);
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
        //this.numeroTable = nTable;
		this.ourTable = new GameTable(nTable, 0, "", "", 0, 0);
			
        this.objGestionnaireCommunication.entrerTable(Delegate.create(this, this.retourEntrerTable), 
													  Delegate.create(this, this.evenementJoueurDemarrePartie), nTable);
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
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
    function obtenirListeTables()
    {
        trace("*********************************************");
        trace("debut de sortirTable");
		this.ourTable = null;
        this.objGestionnaireCommunication.obtenirListeTablesApres(Delegate.create(this, this.retourObtenirListeTables), FiltreTable.INCOMPLETES_NON_COMMENCEES);
        trace("fin de sortirTable");
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
    function creerTable(temps:Number, nb_lines:Number, nb_columns:Number, nameTable:String, gameType:String)
    {
        trace("*********************************************");
        trace("debut de creerTable     " + temps);
		this.ourTable = new GameTable(0, temps, nameTable, gameType, nb_lines, nb_columns);
		this.objGestionnaireCommunication.creerTable(Delegate.create(this, this.retourCreerTable), 
													 Delegate.create(this, this.evenementJoueurDemarrePartie),
													 temps, nb_lines, nb_columns, nameTable, gameType);
		trace("fin de creerTable");
        trace("*********************************************\n");
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function demarrerPartie(idDessin:Number) 
    {
        trace("*********************************************");
        trace("debut de demarrerPartie     " + idDessin); 
       		
		//this.colorIt = getColorByID(this.clothesColorID, idDessin);
		setOurPerso(new MyPersonnage(0, this.nomUtilisateur, this.userRole, idDessin, this.clothesColorID, getColorByID(this.clothesColorID, idDessin)));
		tableauDesPersonnages.push(getOurPerso());

        this.objGestionnaireCommunication.demarrerPartie(Delegate.create(this, this.retourDemarrerPartie), 
														 Delegate.create(this, this.evenementPartieDemarree), 
			                                             Delegate.create(this, this.evenementJoueurDeplacePersonnage), 
														 Delegate.create(this, this.evenementSynchroniserTemps), 
														 Delegate.create(this, this.evenementUtiliserObjet), 
			                                             Delegate.create(this, this.evenementPartieTerminee), 
														 Delegate.create(this, this.evenementJoueurRejoindrePartie), 
														 Delegate.create(this, this.eventPlayerPictureCanceled), 
			                                             Delegate.create(this, this.eventPlayerSelectedPicture),  idDessin); 
		trace("our perso - " + tableauDesPersonnages[0].obtenirNom());
	
		trace("fin de demarrerPartie");
        trace("*********************************************\n");
    }
	
	/*
	   User decided to cancel picture and he is back to the frame 3 selectPicture
	*/
	function playerCanceledPicture()
	{
	  trace("*********************************************");
      trace("debut de cancelPicture "); 
	  this.objGestionnaireCommunication.playerCanceledPicture(Delegate.create(this, this.returnPlayerCanceledPicture)); 
	  trace("fin de cancelPicture ");
      trace("*********************************************\n");
	}
	
	/*
	   User selected another picture ....
	*/
	function playerSelectedNewPicture(idDessin:Number)
	{
	  trace("*********************************************");
      trace("debut de selectedNewPicture "); 
	  this.objGestionnaireCommunication.playerSelectedNewPicture(Delegate.create(this, this.returnPlayerSelectedNewPicture), idDessin); 
	  trace("fin de selectedNewPicture ");
      trace("*********************************************\n");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////// 
    function demarrerMaintenant(niveau:String)
    {
        trace("*********************************************");
        trace("debut de demarrerMaintenant");
		//trace("idPersonnage: " + this.idPersonnage);
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
		//_level0.loader.contentHolder.planche.obtenirPerso().minigameLoade = false;
		ourPerso.setMinigameLoade(false);
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
	function utiliserObjet(id:Number, playerName:String)
    {
        trace("*********************************************");
        trace("debut de utiliserObjet : " + id + " name : " + playerName );
		getOurPerso().prepareBananaToUse(id, playerName);		
		this.objGestionnaireCommunication.utiliserObjet(Delegate.create(this, this.retourUtiliserObjet), id, playerName);  
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
	
    ////////////////////////////////////////////////////////////////////////////////////
    //                                  fonctions retour                             ///
    ////////////////////////////////////////////////////////////////////////////////////
    
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
    	var messageMovie:MovieClip;
		//var isOldGame:MovieClip;
    
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
				
				messageMovie = _level0.loader.contentHolder.attachMovie("GUI_oldGame", "restartGame", 9999);
							
				trace("La connexion a marche");
			break;

			case "Musique":
				this.objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), 
																	  Delegate.create(this, this.evenementJoueurConnecte), 
																	  Delegate.create(this, this.evenementJoueurDeconnecte),
																	  Delegate.create(this, this.eventServerWillStop));
                 //this.objGestionnaireCommunication.obtenirListeSalles(Delegate.create(this, this.retourObtenirListeSalles), Delegate.create(this, this.evenementNouvelleSalle), this.clientType);
               
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
			   
			    //messageMovie = _level0.loader.contentHolder.attachMovie("GUI_erreur", "DejaConnecte", 9999);
				//messageMovie.thisMessage = "JoueurNonConnu2";//_level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUInonConnu;		
				// this is in main_menu, so...
				messageMovie = _level0.attachMovie("GUI_erreurRole", "DejaConnecte", 9999);
								
				
				messageMovie.linkGUI_erreur._visible = false;
				messageMovie.btn_ok._visible = false;
				messageMovie.textReturn._visible = false;
				trace("Joueur non connu ");
				messageMovie.messageAlerte = _level0.texteSource_xml.firstChild.attributes.GUInonConnu;				
				
                
            break;
             
			case "JoueurDejaConnecte":
	  			_level0.loader._visible = true;
				_level0.bar._visible = false;

				//_root.dejaConnecte_txt._visible = true;
				_root.texteSalle._visible = false;
						
	     		messageMovie = _level0.loader.contentHolder.attachMovie("GUI_erreur", "DejaConnecte", 9999);
				messageMovie.linkGUI_erreur._visible = false;
				messageMovie.btn_ok._visible = false;
			
				messageMovie.thisMessage = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUIdejaConnecte;
			
                trace("Joueur deja connecte");
            break;
	     
            default:
            	trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
        }
		objetEvenement = null;
     	trace("fin de retourConnexion");
     	trace("*********************************************\n");
    }
		
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
			this.objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), 
																  Delegate.create(this, this.evenementJoueurConnecte), 
																  Delegate.create(this, this.evenementJoueurDeconnecte),
																  Delegate.create(this, this.eventServerWillStop));
			// this.objGestionnaireCommunication.obtenirListeSalles(Delegate.create(this, this.retourObtenirListeSalles), Delegate.create(this, this.evenementNouvelleSalle), this.clientType);
            
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
			   _level0.loader.contentHolder["restartGame"].removeMovieClip();
			   _level0.loader.contentHolder.gestionBoutons(false);
			 		 
			   this.playersNumber = objetEvenement.playersListe.length;
			   this.endGame = false;
			   this.isOldGame = true;
			   //var count:Number = .length;
			   for(i in objetEvenement.playersListe)
               {				   
                  this.listeDesJoueursConnectes.push(objetEvenement.playersListe[i]);
				  
				 trace("control demarrepartie " + i);
				
				
				  if(objetEvenement.playersListe[i].nom == this.nomUtilisateur)
				  {
					  var idDessin:Number = UtilsBox.calculatePicture(objetEvenement.playersListe[i].idPersonnage);
				      var cloColor:String = getColorByID(objetEvenement.playersListe[i].clocolor, idDessin);
					 					 
		              setOurPerso(new MyPersonnage( objetEvenement.playersListe[i].idPersonnage, objetEvenement.playersListe[i].nom, 
											  objetEvenement.playersListe[i].userRole, idDessin, objetEvenement.playersListe[i].clocolor, cloColor));
		              tableauDesPersonnages.push(getOurPerso());
					 
					  
				  }else
				  {
					  var idDessin:Number = UtilsBox.calculatePicture(objetEvenement.playersListe[i].idPersonnage);
				      var cloColor:String = getColorByID(objetEvenement.playersListe[i].clocolor, idDessin);
					 
				      this.tableauDesPersonnages.push(new AdversaryPersonnage( objetEvenement.playersListe[i].idPersonnage, objetEvenement.playersListe[i].nom, 
											  objetEvenement.playersListe[i].userRole, idDessin, objetEvenement.playersListe[i].clocolor, cloColor));										
					  
				  }
                  
			   }// end for
			   

			break;
			
			// realy speacking can be removed - we have "pointage" in the up one case 
			case "Pointage":
			    
			   //_level0.loader.contentHolder.planche.obtenirPerso().modifierPointage(objetEvenement.pointage);
			   ourPerso.modifierPointage(objetEvenement.pointage);			 
			   remplirMenuPointage();
    		break;
			
			case "Argent":
						
			   _level0.loader.contentHolder.planche.obtenirPerso().modifierArgent(objetEvenement.argent);
			
			break;
			
			case "Table":
			
			   this.ourTable = new GameTable(objetEvenement.noTable, 0, "", objetEvenement.gameType, 0, 0);
			   this.maxPlayers = objetEvenement.maxPlayers;
			   _level0.loader.contentHolder.planche.zoomer("out", 4);
			   
			   var xState:Boolean;
			   if(objetEvenement.brainiacState == "true") 
			   {
				  xState = Boolean(true);
				  ourPerso.setBrainiac(xState);
			      ourPerso.getReconnectionBrainiacAnimaton(objetEvenement.brainiacTime);
				  trace("Dans Brainiac" + xState);
			   }
			   
			   ourPerso.setMoveSight(objetEvenement.moveVisibility);
			   var yState:Boolean; 
			   if(objetEvenement.bananaState == "true")
			   {
				   yState = Boolean(true);
				  ourPerso.setBananaTime(0);
				  ourPerso.setBananaState(true);
			      ourPerso.setBananaTimer(objetEvenement.bananaTime);
  				  trace("Dans Banana " + yState);

			   }
     		break;
			
			case "ListeObjets":
			   var count:Number = objetEvenement.objectsListe.length;
			   for(i = 0; i < count; i++)
               {
                   ourPerso.ajouterObjet(objetEvenement.objectsListe[i].idObject, objetEvenement.objectsListe[i].typeObject);
				   //trace(objetEvenement.objectsListe[i].idObject + "  " + objetEvenement.objectsListe[i].typeObject);
			   }// end for
			   
			break;
			
			case "Ok":
			
			    // newsbox
		        var messageInfo:String = "Rejoindre partie!!!";//objetEvenement.nomUtilisateur + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.restartMess; 
				this.newsChat.addMessage(messageInfo);
				
			   //trace("<<<<<<<<<<<<<<<<  feedbackRestartOldGame  finish restart >>>>>>>>>>>>>>>>>>>" + this.numeroTable);
			
			break;
	     
            default:
            	trace("resultat Inconnue - feedbackRestartOldGame");
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
            	this.playersNumber = objetEvenement.listeNomUtilisateurs.length;  //???
        		        		
                for(var i:Number = 0; i < this.playersNumber ; i++)
                {
                    this.listeDesJoueursConnectes.push(objetEvenement.listeNomUtilisateurs[i]);
                }
                this.objGestionnaireCommunication.obtenirListeSalles(Delegate.create(this, this.retourObtenirListeSalles), Delegate.create(this, this.evenementNouvelleSalle), _level0.roomsType);
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
                trace("Erreur Inconnue - retourObtenirListeJoueurs");
        }
		objetEvenement = null;
        trace("fin de retourObtenirListeJoueurs");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirListeSalles(objetEvenement:Object)
    {
        //   objetEvenement.resultat = ListeSalles, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
		// nom, possedeMotDePasse, descriptions, idRoom, gameTypes, userCreator, masterTime
        trace("*********************************************");
        trace("debut de retourObtenirListeSalles   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "ListeSalles":
			    this.listeDesSalles.removeAll();
				var count:Number = objetEvenement.listeNomSalles.length;
				var roomLabel:String;
				var i:Number;
				
                for ( i= 0; i < count; i++)
                {					
					this.listeDesSalles.push(objetEvenement.listeNomSalles[i]);
					if(_level0.loader.contentHolder.roomsType == "General")
					   roomLabel = " * " +  this.listeDesSalles[i].nom;
					else if (_level0.loader.contentHolder.roomsType == "profsType")
					   roomLabel = this.listeDesSalles[i].userCreator + " - " +  this.listeDesSalles[i].nom;
					trace("salle " + i + " : " + this.listeDesSalles[i].nom);
					_level0.loader.contentHolder.listeSalle.addItem({label: (roomLabel), data:  this.listeDesSalles[i].idRoom});
				}
								
				_level0.loader.contentHolder.bt_continuer1._visible = true;
				_level0.loader.contentHolder.txtChargementSalles._visible = false;
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
                trace("Erreur Inconnue - retourObtenirListeSalles");
        }
        //		objetEvenement = null;
        //trace("fin de retourObtenirListeSalles" + " " + objetEvenement.resultat);
        //trace("*********************************************\n");
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
		       var messageInfo:String = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.bugReportMess; 
			   this.newsChat.addMessage(messageInfo);
		      
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
                trace("Erreur Inconnue in retourReportBugQuestion");
        }
		objetEvenement = null;
        trace("fin de retourReportBugQuestion");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourEntrerSalle(objetEvenement:Object)
    {
        //objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, MauvaisMotDePasseSalle, SalleNonExistante, JoueurDansSalle
        trace("*********************************************");
        trace("debut de retourEntrerSalle   " + objetEvenement.resultat);
		var listeTypes:String;
        switch(objetEvenement.resultat)
        {
            case "Ok":
			    _level0.loader.contentHolder["guiPWD"].removeMovieClip();
				var count:Number = this.listeDesSalles.length;
				var i:Number;
				for (i = 0; i < count; i++)
                {
                    if(this.listeDesSalles[i].idRoom == this.idRoom)
                    {
                        this.masterTime = this.listeDesSalles[i].masterTime;
						// to treat allowed types of game in this room
						listeTypes = this.listeDesSalles[i].gameTypes;
						listeTypes = listeTypes.slice(1, listeTypes.length - 1);
						this.allowedTypes = listeTypes.split(", ");
						if(this.allowedTypes.length == 1 && this.allowedTypes[0] == "")
						   this.allowedTypes = new Array("1", "3");
						trace(this.allowedTypes);						
						break;
                    }
                }
				this.allowedTypesTracks = objetEvenement.listeTracks;
				/*
				count = this.allowedTypesTracks.length;
				for(i = 0; i < count; i++)
				{ 
				   trace("verify " + this.allowedTypesTracks[i].ids + " t: " + this.allowedTypesTracks[i].tracks);
				} */
                this.objGestionnaireCommunication.obtenirListeJoueursSalle(Delegate.create(this, this.retourObtenirListeJoueursSalle), Delegate.create(this, this.evenementJoueurEntreSalle), Delegate.create(this, this.evenementJoueurQuitteSalle));
               
				
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
	           //	_global.styles.Alert.setStyle("themeColor", "haloBlue");
				//_global.styles.Alert.setStyle("color", 0x000099);

				Alert.show(erreur, pwdAlert); 
				
            break;
			
            case "SalleNonExistante":
                trace("Salle non existante");
            break;
			
            case "JoueurDansSalle":
                trace("Joueur dans salle");
            break;
			
            default:
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
        }
		//objetEvenement = null;
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
                objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), 
																 Delegate.create(this, this.evenementJoueurConnecte), 
																 Delegate.create(this, this.evenementJoueurDeconnecte),
																 Delegate.create(this, this.eventServerWillStop));
				//this.objGestionnaireCommunication.obtenirListeSalles(Delegate.create(this, this.retourObtenirListeSalles), Delegate.create(this, this.evenementNouvelleSalle), this.clientType);
            
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
		//   objetEvenement.listeTables[i].  --- tablName 
        trace("*********************************************");
        trace("debut de retourObtenirListeTables   " + objetEvenement.resultat);
        var str:String = new String();
        switch(objetEvenement.resultat)
        {
            case "ListeTables":
			 
			 _level0.loader.contentHolder.listeTable.removeAll();
			 this.listeDesTables = new Array();
			 var count:Number = objetEvenement.listeTables.length;
             for (var i:Number = 0; i < count; i++)
             {
                 this.listeDesTables.push(objetEvenement.listeTables[i]);
			 }
			 _global.intervalIdX = setInterval(xTimerSet, 300);
				
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
                trace("Erreur Inconnue in  retourObtenirListeTables");
        }
		//objetEvenement = null;
        trace("fin de retourObtenirListeTables");
        trace("*********************************************\n");
    }
	
	// used to renew the list of tables... 
	function xTimerSet(objetEvenement:Object){
		
		       var str:String = new String();
				
				var count:Number = _level0.loader.contentHolder.objGestionnaireEvenements.listeDesTables.length;
                for (var i:Number = 0; i < count; i++)
                {                    
					var iconName:String;
					if(_level0.loader.contentHolder.objGestionnaireEvenements.listeDesTables[i].gameType == "mathEnJeu")
					{   
					   iconName = "maze";
					}
					else if (_level0.loader.contentHolder.objGestionnaireEvenements.listeDesTables[i].gameType == "Course" || _level0.loader.contentHolder.objGestionnaireEvenements.listeDesTables[i].gameType == "Tournament")
					{
						iconName = "flags";
					}
					//trace("game type liste table : " +  objetEvenement.listeTables[i].gameType + " " + iconName);
                    str =  "   << " +  _level0.loader.contentHolder.objGestionnaireEvenements.listeDesTables[i].tablName + " >>   - " + _level0.loader.contentHolder.objGestionnaireEvenements.listeDesTables[i].temps + " min. " ;
					
					_level0.loader.contentHolder.listeTable.addItem({label : str, data : _level0.loader.contentHolder.objGestionnaireEvenements.listeDesTables[i].no, icon: iconName});
					
                }
								
				 clearInterval(_global.intervalIdX);
	}// end func
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourCreerTable(objetEvenement:Object)
    {
        //   objetEvenement.resultat = "NoTable", CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle,  JoueurDansTable
        // parametre : noTable, name
        trace("*********************************************");
        trace("debut de retourCreerTable   " + objetEvenement.resultat + "    " + objetEvenement.clocolor + "  " + objetEvenement.nameTable);
        var movClip:MovieClip;
		var i:Number;

        switch(objetEvenement.resultat)
        {
            case "NoTable":
			    this.ourTable.setTableId(objetEvenement.noTable);
    			this.ourTable.setTableName(objetEvenement.nameTable);
				this.clothesColorID = objetEvenement.clocolor;				
				this.maxPlayers = objetEvenement.maxNbPlayers;
				
                _level0.loader.contentHolder.gotoAndPlay(3);
				              
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
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
        }
		objetEvenement = null;
    	trace("fin de retourCreerTable");
    	trace("*********************************************\n");
    }
	
	// used to draw the hiddens void circles on frame 3
    function drawUsersVoid()
    {
	   var i:Number;
	   for(i = 0; i < 12; i++)
	   {
          // to load the perso .. use ClipLoader to know the moment of complet load
	      // we are in for so load it dynamically
          var mclListenerString:String = "mclListener" + i;
		  this["mclListenerString"] = new Object();
		  this["mclListenerString"].onLoadComplete = function(target_mc:MovieClip) {
    
	        target_mc._xscale = 94;
			target_mc._yscale = 94;
			target_mc._visible = false;
									
          };
		  var myLoaderString:String = "myLoader" + i;
		  this["myLoaderString"] = new MovieClipLoader();
		  this["myLoaderString"].addListener(this["mclListenerString"]);
		
		  this["myLoaderString"].loadClip("Perso/persosVoid.swf", _level0.loader.contentHolder["player" + i]); 	
	   }
     }
	
	// used to draw the hiddens void circles on frame 3
    function drawUserVoidHidden(i:Number)
    {
      // to load the perso .. use ClipLoader to know the moment of complet load
	  // we are in for so load it dynamically
       var mclListenerString:String = "mclListener" + i;
		this["mclListenerString"] = new Object();
		this["mclListenerString"].onLoadComplete = function(target_mc:MovieClip) {
    
	        target_mc._xscale = 94;
			target_mc._yscale = 94;
			target_mc._visible = false;
									
        };
		var myLoaderString:String = "myLoader" + i;
		this["myLoaderString"] = new MovieClipLoader();
		this["myLoaderString"].addListener(this["mclListenerString"]);
		
		this["myLoaderString"].loadClip("Perso/persosVoid.swf", _level0.loader.contentHolder["player" + i]); 	
     }
	 
	 // used to draw the void circles on frame 3
    function drawUserVoidVisible(i:Number)
    {
      // to load the perso .. use ClipLoader to know the moment of complet load
	  // we are in for so load it dynamically
       var mclListenerString:String = "mclListener" + i;
		this["mclListenerString"] = new Object();
		this["mclListenerString"].onLoadComplete = function(target_mc:MovieClip) {
    
	        target_mc._xscale = 94;
			target_mc._yscale = 94;
			target_mc.gotoAndPlay(1);
        };
		var myLoaderString:String = "myLoader" + i;
		this["myLoaderString"] = new MovieClipLoader();
		this["myLoaderString"].addListener(this["mclListenerString"]);
		
		this["myLoaderString"].loadClip("Perso/persosVoid.swf", _level0.loader.contentHolder["player" + i]); 	
     }
	
	
    //  on ne s'ajoute pas a la liste des joueur dans cette table, c grave ??  c correct pour quand on veut sortir....
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourEntrerTable(objetEvenement:Object)
    {
        //   objetEvenement.resultat = ListePersonnageJoueurs, CommandeNonReconnue,  ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, TableNonExistante, TableComplete
        trace("*********************************************");
        trace("debut de retourEntrerTable   " + objetEvenement.resultat);
        var i:Number;
        switch(objetEvenement.resultat)
        {
            case "ListePersonnageJoueurs":
			    var count:Number = this.listeDesTables.length;
				for (i = 0; i < count; i++)
                {
                    if(this.listeDesTables[i].no == this.ourTable.getTableId())
                    {
                        this.ourTable.setTableTime(Number(this.listeDesTables[i].temps));
						this.ourTable.setTableName(this.listeDesTables[i].tablName);
						this.ourTable.setGameType(this.listeDesTables[i].gameType);
						this.maxPlayers = Number(this.listeDesTables[i].maxNbPlayers);						
						//trace("Verifie - retour entrer tableX: " + this.tempsPartie + " " + this.tablName + " " + this.maxPlayers + " " + this.typeDeJeu);
                        break;
                    }
                }
				//trace("Verifie - retour entrer table: " + this.tempsPartie + " " + this.tablName + " " + this. maxPlayers + " " + this.typeDeJeu);
				this.clothesColorID =  objetEvenement.clocolor;
                
				_level0.loader.contentHolder.gotoAndPlay(3);
				                               						
				// put the players in the liste
                count = objetEvenement.listePersonnageJoueurs.length;
               	for(i = 0; i < count; i++)
                {											
				   //if(!(objetEvenement.listePersonnageJoueurs[i].userRoles > 1 && this.typeDeJeu == "Tournament"))
				   //{   
					 trace("control demarrepartie " + i);
					 var idDessin:Number = UtilsBox.calculatePicture(objetEvenement.listePersonnageJoueurs[i].idPersonnage);
					 var cloColor:String = getColorByID(objetEvenement.listePersonnageJoueurs[i].clothesColor, idDessin);
					 
					 this.tableauDesPersonnages.push(new AdversaryPersonnage( objetEvenement.listePersonnageJoueurs[i].idPersonnage, objetEvenement.listePersonnageJoueurs[i].nom, 
											  objetEvenement.listePersonnageJoueurs[i].userRoles, idDessin, objetEvenement.listePersonnageJoueurs[i].clothesColor, cloColor));					
				   //}
					
                }// end for			
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
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
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
			    //if(_level0.loader.contentHolder._currentframe != 2)
				 //  _level0.loader.contentHolder.gotoAndStop(2);
				
            case "Ok":     
			    var count:Number = this.listeDesTables.length;
				for(var i = 0; i < count; i++)
				{
					if(this.listeDesTables[i].no == this.ourTable.getTableId())
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
            	      this.listeDesTables.splice(indice,1);
					}
            	}
				
				if (this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2)
		        {
			      
			       _level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
				   _level0.loader.contentHolder.txtChargementTables._visible = true;
		        }

               this.tableauDesPersonnages = new Array();
			   this.ourTable = null;

               this.objGestionnaireCommunication.obtenirListeJoueursSalle(Delegate.create(this, this.retourObtenirListeJoueursSalle), Delegate.create(this, this.evenementJoueurEntreSalle), Delegate.create(this, this.evenementJoueurQuitteSalle));						
			 	var count:Number = this.listeDesTables.length;
			 	for (var i:Number = 0; i < count; i++)
			 	{
					var iconName:String;
					if(this.listeDesTables[i].gameType == "mathEnJeu")
					{   
					   iconName = "maze";
					}
					else if (this.listeDesTables[i].gameType == "Course" ||this.listeDesTables[i].gameType == "Tournament")
					{
						iconName = "flags";
					}
					str = "  << " +  this.listeDesTables[i].tablName + " >>  " + this.listeDesTables[i].temps + " min. " ;
					
					_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no, icon: iconName});
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
             
			case "JoueurPasDansTable":
                trace("Joueur pas dans table");
            break;
             
			default:
                trace("Erreur Inconnue + retourQuitterTable");
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
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
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
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
        }
		objetEvenement = null;
        trace("fin de retourEnvoyerMessage");
        trace("*********************************************\n");
    }
	 
    //  pour kicker out un joueur  --- it is used??? OL .. is yes must be updated
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
            removePersoFromListe(objetEvenement.nom);
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
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
        }
		objetEvenement = null;
        trace("fin de retourSortirJoueurTable");
        trace("*********************************************\n");
    }//end 
	
	////////////////////////////////////////////////////////////////
	public function returnPlayerCanceledPicture(objetEvenement)
	{
		// objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable
        trace("*********************************************");
        trace("debut de retourPlayerCanceledPicture   " + objetEvenement.resultat);
		switch(objetEvenement.resultat)
        {
            case "Ok":
    		   ourPerso.setIDessin(0);
			   trace("retourCanceledPicture ok");
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
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
        }
		objetEvenement = null;
        trace("fin de retourCanceledPicture");
        trace("*********************************************\n");
		
	}
	
	////////////////////////////////////////////////////////////////
	public function returnPlayerSelectedNewPicture(objetEvenement)
	{
		// objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable
        trace("*********************************************");
        trace("debut de retourPlayerSelectedNewPicture   " + objetEvenement.idP);
		switch(objetEvenement.resultat)
        {
            case "Ok":
			    ourPerso.setIdPersonnage(objetEvenement.idP);
			    //ourPerso.setIDessin(UtilsBox.calculatePicture(objetEvenement.idP));
				// redraw of frame 3 the list of users that is on our table
				if(!inSelectPlayer)
				   showTableUsers();				
                trace("retourCanceledPicture ok");
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
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
        }
		objetEvenement = null;
        trace("fin de retourCanceledPicture");
        trace("*********************************************\n");
		
	}
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourDemarrerPartie(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable, TableNonComplete
        trace("*********************************************");
        trace("debut de retourDemarrerPartie   "+objetEvenement.resultat + " " + objetEvenement.idP);
        var i:Number;
		switch(objetEvenement.resultat)
        {
            case "Ok":
			    ourPerso.setIdPersonnage(objetEvenement.idP);
				//this.colorIt =  objetEvenement.clocolor;
				trace("nom and idP = " + " " + objetEvenement.idP);
				var cloColor:String  = getColorByID(this.clothesColorID, UtilsBox.calculatePicture(objetEvenement.idP));
				ourPerso.setColor(cloColor);
			    showTableUsers();				
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
                trace("Erreur Inconnue Message du serveur: " + objetEvenement.resultat);
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
    	trace("debut de retourDeconnexion   " + objetEvenement.resultat);
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
                trace("Erreur Inconnue = retourDeconnexion");
        }
		objetEvenement = null;
    	trace("fin de retourDeconnexion - GestEven");
    	trace("*********************************************\n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourDeplacerPersonnage(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Question, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, DeplacementNonAutorise
		var question:MovieClip;
	
      	trace("*********************************************");
      	trace("debut de retourDeplacerpersonnage   " + objetEvenement.resultat + " " + objetEvenement.question.url );
      	switch(objetEvenement.resultat)
        {
            case "Question":
		     	_level0.loader.contentHolder.url_question = objetEvenement.question.url;//"Q-1-en.swf"
		     	_level0.loader.contentHolder.type_question = objetEvenement.question.type;
				_level0.loader.contentHolder.box_question.gotoAndPlay(2);
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
                trace("Erreur Inconnue - retourDeplacerpersonnage");
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
    	trace("debut de retourAcheterObjet   " + objetEvenement.resultat);
    	switch(objetEvenement.resultat)
        {
			case "Ok":				
				    //var idObjet:Number =  objetEvenement.objet.id;
					//trace("nom de l'objet : " + objetEvenement.objet.type + " " + objetEvenement.objet.id);
					ourPerso.buyObject(objetEvenement.objet.id, objetEvenement.objet.type, objetEvenement.objet.newId);					
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
      	trace("debut de retourUtiliserObjet   " + objetEvenement.resultat + " " + objetEvenement.objetUtilise.typeObjet);
	  
      	switch(objetEvenement.resultat)
        {
			case "RetourUtiliserObjet":
								
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
						//var question:MovieClip = new MovieClip();
					
						_level0.loader.contentHolder.url_question = objetEvenement.objetUtilise.url;
						_level0.loader.contentHolder.type_question = objetEvenement.objetUtilise.type;
						_level0.loader.contentHolder.box_question.gotoAndPlay(7);
					break;
				
					case "Banane":
						trace("banane");
						//getOurPerso().tossBanana();
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
				trace("Erreur Inconnue. Message du serveur: " + objetEvenement.resultat);
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
                trace("Erreur Inconnue. Message du serveur: " + objetEvenement.resultat);
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
                trace("Erreur Inconnue. Message du serveur: " + objetEvenement.resultat);
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
			
			       _level0.loader.contentHolder.planche.effacerCasesPossibles();
					_level0.loader.contentHolder.box_question.gotoAndPlay(9);
			
					pt.definirX(objetEvenement.nouvellePosition.x);
			     	pt.definirY(objetEvenement.nouvellePosition.y);
					
					trace("juste avant la teleportation param  " +  objetEvenement.nouvellePosition.x + " " +  objetEvenement.nouvellePosition.y);
					var collision:String = objetEvenement.collision;
					
					_level0.loader.contentHolder.planche.obtenirPerso().definirProchainePosition(_level0.loader.contentHolder.planche.calculerPositionTourne(pt.obtenirX(), pt.obtenirY()), collision);
					
					// modifier le pointage
					_level0.loader.contentHolder.planche.obtenirPerso().modifierPointage(objetEvenement.pointage);
					_level0.loader.contentHolder.planche.obtenirPerso().modifierArgent(objetEvenement.argent);
					_level0.loader.contentHolder.sortieDunMinigame = false; 
					//_level0.loader.contentHolder.planche.
					
					// the same in evenementUtiliseObjet for all players
					// we need here that because to have a correct visibility
					if(objetEvenement.collision == "Brainiac")
					   _level0.loader.contentHolder.planche.obtenirPerso().setBrainiac(true);
					ourPerso.setMoveSight( objetEvenement.moveVisibility );
					// if we have bonus in the tournament game  we must treat this
					//	  if(objetEvenement.bonus > 0){
					//	     	this.listeDesPersonnages[i].win = 1;
					//			 this.winIt = 1;
					//	  }
						  
				    // newsbox
		            var messageInfo:String;
						 
					if(objetEvenement.collision == "Livre")
					{
						messageInfo = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.bookCollectMess;
						this.newsChat.addMessage(messageInfo);
					}
				    else if(objetEvenement.collision == "Banane")
					{
							 messageInfo = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.bananaCollectMess;
							 this.newsChat.addMessage(messageInfo);
						 }
						 else if(objetEvenement.collision == "Piece")
						 {
							 messageInfo = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.moneyCollectMess;
							 this.newsChat.addMessage(messageInfo);
						 }
						 else if(objetEvenement.collision == "Boule")
						 {
							 messageInfo = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.cristallCollectMess;
		                     this.newsChat.addMessage(messageInfo);
				   }
        	         
        	   	   
					
				   remplirMenuPointage();
		     	}
		     	else
		     	{
					if(_level0.loader.contentHolder.erreurConnexion)
					{
						// Dans le cas d'une erreur de connexion, nous envoyons une reponse
						// assurement mauvaise au serveur. Il ne faut pas afficher de retro dans ce cas
						//_level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.planche.setRepostCases(true);
						_level0.loader.contentHolder.erreurConnexion = false;
					}
					else
					{
			     		trace("deplacement refuse  ");
						_level0.loader.contentHolder.url_retro =objetEvenement.explication;// "Q-1-F-en.swf";
						_level0.loader.contentHolder.planche.effacerCasesPossibles();

                        _level0.loader.contentHolder.box_question.monScroll._visible = false;
						var ptX:Number = _level0.loader.contentHolder.box_question.monScroll._x;
						var ptY:Number = _level0.loader.contentHolder.box_question.monScroll._y;
						_level0.loader.contentHolder.box_question.attachMovie("GUI_retro","GUI_retro", 100, {_x:ptX, _y:ptY});
						
						//define the time of penality
						_level0.loader.contentHolder.box_question.GUI_retro.timeX = 15;
						// define new visibility
						ourPerso.setMoveSight( objetEvenement.moveVisibility );

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
                trace("Erreur Inconnue - retourRepondreQuestion");
        }
		objetEvenement = null;
     	trace("fin de ");
    	trace("*********************************************\n");
    }// end method
	
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
    //                                       EVENEMENTS                                               //
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
    }// end method
	
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
			
			horsService.textGUI_erreur.text = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUIhorsService;
			
			horsService.linkGUI_erreur.text = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUIhorsService2;
			horsService.linkGUI_erreur.html = true;
			horsService.btn_ok._visible = false;
			
			var formatLink = new TextFormat();
			formatLink.url = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUIhorsServiceURL;
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
    	trace("debut de evenementDeconnexionPhysique  GestEven ");
		var errorDeconnexion:MovieClip;
		
		if(!this.endGame){

		   errorDeconnexion = _level0.loader.contentHolder.attachMovie("GUI_erreur", "deconnexion", 9999);//, {x: 20, y: 20});
		   errorDeconnexion.linkGUI_erreur._visible = false;
		   errorDeconnexion.btn_ok._visible = false;
			
		   errorDeconnexion.textGUI_erreur.text = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUIerrorConnection;
		}
		trace("fin de evenementDeconnexionPhysique");
    	trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
    public function eventServerWillStop(objetEvenement:Object)
    {
        trace("*********************************************");
    	trace("debut de evenement ServerWillStop  GestEven ");
		var serverDeconnexion:MovieClip;
		
		serverDeconnexion = _level0.loader.contentHolder.attachMovie("GUI_serveur", "deconnexion", 9999);
		serverDeconnexion.linkGUI_erreur._visible = false;
		serverDeconnexion.btn_ok._visible = false;
		serverDeconnexion._x = 100;
		serverDeconnexion._y = 100;
		
			
		serverDeconnexion.textGUI_erreur.text = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.GUIerrorConnection;
		
		objetEvenement = null;
    	trace("fin de evenement ServerWillStop");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurConnecte(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurConnecte   " + objetEvenement.nomUtilisateur);
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
			this.listeDesSalles[this.listeDesSalles.length - 1].userCreator = objetEvenement.CreatorUserName;
			this.listeDesSalles[this.listeDesSalles.length - 1].masterTime = objetEvenement.MasterTime;
			this.listeDesSalles[this.listeDesSalles.length - 1].roomType = objetEvenement.RoomType;
			this.listeDesSalles[this.listeDesSalles.length - 1].gameTypes = objetEvenement.GameTypes;
			trace(" GE : " + this.listeDesSalles[this.listeDesSalles.length - 1].roomType + " * " +  _level0.loader.contentHolder.roomsType);
			
			if(_level0.loader.contentHolder.roomsType == objetEvenement.RoomType)
			{
			   _level0.loader.contentHolder.listeSalle.addItem({label: objetEvenement.NomSalle, data:  objetEvenement.NoSalle});
			   _level0.loader.contentHolder.listeSalle.redraw();
			}
															
									
		objetEvenement = null;
    	trace("fin de evenementNouvelleSalle");
    	trace("*********************************************\n");
    }// end event
	
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDeconnecte(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurDeconnecte   " + objetEvenement.nomUtilisateur);
		removePersoFromListe(objetEvenement.nomUtilisateur);        	
    	trace("fin de evenementJoueurDeconnecte GestEven");
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
           		trace("un joueur enlever de la liste :   " + objetEvenement.nomUtilisateur);
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
			}
		}
		
		var str:String = new String();
		_level0.loader.contentHolder.listeTable.removeAll();
		count = this.listeDesTables.length;
		for (var i:Number = 0; i < count; i++)
	    {
			var iconName:String;
			if(this.listeDesTables[i].gameType == "mathEnJeu")
			{   
			   iconName = "maze";
			}
			else if (this.listeDesTables[i].gameType == "Course" ||this.listeDesTables[i].gameType == "Tournament")
			{
				iconName = "flags";
			}
			str = "  << " +  this.listeDesTables[i].tablName + " >>  " + this.listeDesTables[i].temps + " min. " ;
			
		    _level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no, icon: iconName});
			
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
        // parametre: noTable, tempsPartie , tablName  -- no , temps, tablName, maxNbPlayers, gameType
    	trace("*********************************************");
    	trace("debut de evenementNouvelleTable   " + objetEvenement.noTable + "  " + objetEvenement.tempsPartie + " " + objetEvenement.nameTable);
    	var str:String = new String();
    	// on ajoute une liste pour pouvoir inserer les joueurs quand ils vont entrer
        objetEvenement.listeJoueurs = new Array();
        //objetEvenement.no = objetEvenement.noTable;
		//objetEvenement.temps = objetEvenement.tempsPartie;
		//objetEvenement.tablName = objetEvenement.nameTable;
        
		this.listeDesTables.push(objetEvenement);
               				
		_level0.loader.contentHolder.listeTable.removeAll();
		var count:Number = this.listeDesTables.length;
		for (var i:Number = 0; i < count; i++)
		{
			var iconName:String;
			if(this.listeDesTables[i].gameType == "mathEnJeu")
			{   
			   iconName = "maze";
			}
			else if (this.listeDesTables[i].gameType == "Course" ||this.listeDesTables[i].gameType == "Tournament")
			{
				iconName = "flags";
			}
			str = "  << " +  this.listeDesTables[i].tablName + " >>  " + this.listeDesTables[i].temps + " min. " ;
				
			_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no, icon: iconName});
		}
		
        
		_level0.loader.contentHolder.chargementTables = "";
		
		_level0.loader.contentHolder.listeTable.redraw();
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
				break;
        	}
    	}
		
		var str:String = new String();
		_level0.loader.contentHolder.listeTable.removeAll();
		count = this.listeDesTables.length;
		
		for (var i:Number = 0; i < count; i++)
		{
			var iconName:String;
			if(this.listeDesTables[i].gameType == "mathEnJeu")
			{   
			   iconName = "maze";
			}
			else if (this.listeDesTables[i].gameType == "Course" ||this.listeDesTables[i].gameType == "Tournament")
			{
				iconName = "flags";
			}
			str = "  << " +  this.listeDesTables[i].tablName + " >>  " + this.listeDesTables[i].temps + " min. " ;
					
			_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no, icon: iconName});
		}
				
		if (this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2 && _level0.loader.contentHolder["p0"]._visible == false)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
		
		_level0.loader.contentHolder.listeTable.redraw();
		
		if(!endGame && objetEvenement.noTable == this.ourTable.getTableId()){
		   sortirSalle();
		   _level0.loader.contentHolder["att"].removeMovieClip();
		   _level0.loader.contentHolder.gotoAndPlay(1);
		   this.tableauDesPersonnages = new Array();		   		  		  
		}		
		objetEvenement = null;
    	trace("fin de evenementTableDetruite");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurEntreTable(objetEvenement:Object)
    {
        // parametre: noTable, nomUtilisateur, userRole, colorId
		//objetEvenement.nomUtilisateur  objetEvenement.userRole  objetEvenement.userColor  
    	trace("debut de evenementJoueurEntreTable   "+objetEvenement.noTable + "    " + objetEvenement.nomUtilisateur);
    	var i:Number;
    	var indice:Number = -1;
    	var str:String = new String();
    		
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
		for(var i:Number = 0; i < maxPlayers; i++)
        {
					trace(i + " avant this.listeDesPersonnages[i].nom: " + this.listeDesPersonnages[i].nom + " id:" + this.listeDesPersonnages[i].id);
	    }
		*/
		if(objetEvenement.noTable == this.ourTable.getTableId())
    	{   
			 trace(" evenementJoueurEntreTable length = " + objetEvenement.nomUtilisateur + " " + objetEvenement.userColor);
			 this.tableauDesPersonnages.push(new AdversaryPersonnage( 0, objetEvenement.nomUtilisateur, 
											  objetEvenement.userRole, 0, objetEvenement.userColor, ""));					 
    	}// if
		
		if(!inSelectPlayer)
		   showTableUsers();
    	
		// verify and refactor!!!!!!!!!!!!
		if(indice != -1)
    	{			   	
			// enlever la table de la liste si elle est pleine
			if(this.listeDesTables[indice].listeJoueurs.length == this.maxPlayers)
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
		
		 /// Verifie si est encore actuel!
		 _level0.loader.contentHolder.listeTable.removeAll();
		 for (var i:Number = 0; i < count; i++)
		 {
				var iconName:String;
				if(this.listeDesTables[i].gameType == "mathEnJeu")
				{   
				   iconName = "maze";
				}
				else if (this.listeDesTables[i].gameType == "Course" ||this.listeDesTables[i].gameType == "Tournament")
				{
					iconName = "flags";
				}
				str = "  << " +  this.listeDesTables[i].tablName + " >>  " + this.listeDesTables[i].temps + " min. " ;
					
				_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no, icon: iconName});
		 }
		
		if (this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2 && _level0.loader.contentHolder["p0"]._visible == false)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
    		
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
    	trace("debut de evenementJoueurQuitteTable   " + objetEvenement.noTable + "    " + objetEvenement.nomUtilisateur);
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
		
		if(!endGame && _level0.loader.contentHolder._currentframe > 3 && objetEvenement.noTable == this.ourTable.getTableId()){
		   getPersonnageByName(objetEvenement.nomUtilisateur).removeImage();
    	   _level0.loader.contentHolder.planche.getOutPerso(getPersonnageByName(objetEvenement.nomUtilisateur));		  
		  		     
		   // newsbox
		   var messageInfo:String = objetEvenement.nomUtilisateur + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.outMess; 
		   this.newsChat.addMessage(messageInfo);
		 }
		
		
    	// si la table est la notre (on choisit nos perso frame 3)
    	if(!endGame && objetEvenement.noTable == this.ourTable.getTableId())
    	{
			//first we get out the perso from the list
            removePersoFromListe(objetEvenement.nomUtilisateur);
			dessinerMenu();
		    remplirMenuPointage()
			// after we replace the list on the screen
			if(!inSelectPlayer)
			   showTableUsers();        	
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
			var iconName:String;
			if(this.listeDesTables[i].gameType == "mathEnJeu")
			{   
				   iconName = "maze";
			}
			else if (this.listeDesTables[i].gameType == "Course" ||this.listeDesTables[i].gameType == "Tournament")
			{
					iconName = "flags";
			}
			str = "  << " +  this.listeDesTables[i].tablName + " >>  " + this.listeDesTables[i].temps + " min. " ;
				
			_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no, icon: iconName});
		}
				
		if(this.listeDesTables.length == 0 &&  _level0.loader.contentHolder._currentframe == 2 && _level0.loader.contentHolder["p0"]._visible == false)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
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
    	trace("debut de evenementMessage   " + objetEvenement.message + "    " + objetEvenement.nomUtilisateur);
		objetEvenement = null;
    	trace("fin de evenementMessage");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementSynchroniserTemps(objetEvenement:Object)
    {
        // parametre: tempsRestant
    	trace("*********************************************");
    	trace("debut de evenementSynchroniserTemps   " + objetEvenement.tempsRestant);
    	_level0.loader.contentHolder.horlogeNum = objetEvenement.tempsRestant;
		objetEvenement = null;
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
	public function evenementPartieDemarree(objetEvenement:Object)
    {		
        // parametre: plateauJeu, positionJoueurs (nom, x, y), tempsPartie
        trace("*********************************************");
        trace("debut de evenementPartieDemarree   " + objetEvenement.tempsPartie + "   ");
        var i:Number;
        var j:Number;
        
		
		 
		var count:Number;
		var maTete:MovieClip;
		
		_level0.loader.contentHolder["att"].removeMovieClip();		 
		_level0.loader.contentHolder.gotoAndPlay(4);		
		_level0.loader.contentHolder.planche = new PlancheDeJeu(objetEvenement.plateauJeu, this);
				
        for(i in objetEvenement.positionJoueurs)
        {			
		    var iplayer =  getPersonnageByName(objetEvenement.positionJoueurs[i].nom);
		    iplayer.definirL(objetEvenement.positionJoueurs[i].x);
			iplayer.definirC(objetEvenement.positionJoueurs[i].y);
		    /* 
            for(j in tableauDesPersonnages)
            {
	            //trace(this.listeDesPersonnages[j].nom+" : "+objetEvenement.positionJoueurs[i].nom);
                if(tableauDesPersonnages[j].obtenirNom() == objetEvenement.positionJoueurs[i].nom)
                {
					trace("test color : " + this.tableauDesPersonnages[j].nom + " " +  objetEvenement.positionJoueurs[i].x + " " + objetEvenement.positionJoueurs[i].y);
	                //var idDessin:Number = //calculatePicture(this.tableauDesPersonnages[j].getIdPersonnage);
					//var idPers:Number = calculateIDPers(this.listeDesPersonnages[j].id, idDessin);
					//this.listeDesPersonnages[j].idPers = idPers;
					// to update clothes color
					//tableauDesPersonnages[j].colorID = objetEvenement.positionJoueurs[i].clocolor;
					//var cloCol:String = = getColorByID(objetEvenement.positionJoueurs[i].clocolor, tableauDesPersonnages[j].getIDessin());//objetEvenement.clothesColor;
					//var filterC:ColorMatrixFilter = _level0.loader.contentHolder.objGestionnaireEvenements.colorMatrixPerso(cloCol, tableauDesPersonnages[j].getIDessin());
				    //tableauDesPersonnages[j].setColorFilter(filterC);
					//tableauDesPersonnages[j].setColorFilter(filterC);
					tableauDesPersonnages[j].definirL(objetEvenement.positionJoueurs[i].x);
					tableauDesPersonnages[j].definirC(objetEvenement.positionJoueurs[i].y);
					///trace("XXXXXX - " + this.listeDesPersonnages[j].colorID + " " + this.listeDesPersonnages[j].clocolor + " " + this.listeDesPersonnages[j].nom);										
				}
			}*/
		}
				      
	    _level0.loader.contentHolder.planche.afficher();
											
        for(i in tableauDesPersonnages)
        {		
			//5 * tableauDesCases.length * tableauDesCases[0].length + 2 * idPers, tableauDesCases[ll][cc].obtenirClipCase()._x, tableauDesCases[ll][cc].obtenirClipCase()._y ,
			_level0.loader.contentHolder.planche.ajouterPersonnage(tableauDesPersonnages[i], tableauDesPersonnages[i].obtenirL(), tableauDesPersonnages[i].obtenirC());
			tableauDesPersonnages[i].initPlanche(_level0.loader.contentHolder.planche);
			tableauDesPersonnages[i].afficher();
			trace("Construction du personnage : " + this.tableauDesPersonnages[i].obtenirNom());
			var xState:Boolean;
			if(tableauDesPersonnages[i].getBrainiacState() == "true") 
			{				
				tableauDesPersonnages[i].setBrainiac(Boolean(true));
			    //tableauDesPersonnages[i].getReconnectionBrainiacAnimaton(brainiacTime);				        
	        }				
        }
		
		 _level0.loader.contentHolder.planche.setPerso(ourPerso);

		// color our head
		var idDessin:Number = ourPerso.getIDessin();
													
		// put the face of my avatar in the panel (next to my name)
		maTete = _level0.loader.contentHolder.maTete.attachMovie("tete" + idDessin, "maTete", -10099);
		maTete._x = -5;
		maTete._y = -30;
		// V3 head size
		maTete._xscale = 290;
		maTete._yscale = 290;
		UtilsBox.colorItMatrix(ourPerso.getColor(), maTete.headClo, idDessin);

        _level0.loader.contentHolder.horlogeNum = 60*objetEvenement.tempsPartie;
				
		_level0.loader.contentHolder.objectMenu.Boule.countTxt = "0";
		_level0.loader.contentHolder.objectMenu.Banane.countTxt = "0";
		_level0.loader.contentHolder.objectMenu.Livre.countTxt = 0;
		_level0.loader.contentHolder.objectMenu.piece.countTxt = 0;
		
		//newsbox
		var messageInfo:String = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.welcomeMess +  "       " + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.moveMess;
		this.newsChat.addMessage(messageInfo);

        remplirMenuPointage();
		if(!this.isOldGame)
		{   _level0.loader.contentHolder.planche.zoomer("out", 8);
		    if(this.ourTable.compareType("mathEnJeu"))
		    {
		       this.startAnimation();
		   
		    } else {
               _level0.loader.contentHolder.planche.startAnimationCourseI();		   
		       this.startAnimationCourse();		  		   
		    }
		}
		
		objetEvenement = null;
        trace("fin de evenementPartieDemarree    " + getTimer());
        trace("*********************************************\n");
    }
	
	// start animation 
	public function startAnimation(){
		
		var intervalIdA:Number = setInterval(animation, 250);	
		var num:Number = 8;
							
        function animation(){
	            num--;
				if(num < 4)
				   _level0.loader.contentHolder.planche.zoomer("in", 1);
		        if(num < 1)
		           clearInterval(intervalIdA);
				
		}
		
	}
	
	// start animation 
	public function startAnimationCourse(){
		
		var intervalIdC:Number = setInterval(animation, 100);
						
		var num:Number = 35;
	     						
        function animation(){
			   
				num--;
				if(num == 30)
				  _level0.loader.contentHolder.planche.startAnimationCourseII();				  
		        if(num == 18)
				  _level0.loader.contentHolder.objGestionnaireEvenements.startAnimation();
		        if(num < 1)
				  clearInterval(intervalIdC);				
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	//   "JoueurRejoindrePartie"
	public function evenementJoueurRejoindrePartie(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, idPersonnage, Pointage, Role, xPosition, yPosition
     	trace("*********************************************");
     	trace("debut de evenementJoueurRejoindrePartie   " + objetEvenement.nomUtilisateur + "    " + objetEvenement.idPersonnage + " " + objetEvenement.Pointage);
				 
		var idDessin:Number = UtilsBox.calculatePicture(objetEvenement.idPersonnage);
		var cloColor:String = getColorByID(objetEvenement.Color, idDessin);
		tableauDesPersonnages.push( new AdversaryPersonnage( objetEvenement.idPersonnage, objetEvenement.nomUtilisateur, 
											  objetEvenement.Role, idDessin, objetEvenement.Color, cloColor));
		//pa.setIDessin(idDessin);
		var n:Number = this.tableauDesPersonnages.length - 1;
		tableauDesPersonnages[n].modifierPointage(Number(objetEvenement.Pointage));
		tableauDesPersonnages[n].definirL(objetEvenement.xPosition);
		tableauDesPersonnages[n].definirC(objetEvenement.yPosition);
							
		_level0.loader.contentHolder.planche.ajouterPersonnage(tableauDesPersonnages[n], tableauDesPersonnages[n].obtenirL(), tableauDesPersonnages[n].obtenirC());
		tableauDesPersonnages[n].initPlanche(_level0.loader.contentHolder.planche);
		tableauDesPersonnages[n].afficher();
			
		dessinerMenu();
		remplirMenuPointage();
		   
		 //complete the message box  - newsbox
		 var messageInfo:String = objetEvenement.nomUtilisateur + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.InMess; 
		 this.newsChat.addMessage(messageInfo);
		
		objetEvenement = null;
		trace("fin de evenement evenementJoueurRejoindrePartie ");
        trace("*********************************************\n");
				
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
    public function eventPlayerPictureCanceled(objetEvenement:Object)
    {
        // param:  nomUtilisateur,
     	trace("*********************************************");
     	trace("begin eventPlayerPictureCanceled   ");
				
		var iplayer =  getPersonnageByName(objetEvenement.nomUtilisateur);
		iplayer.setIdPersonnage(0);
		if(!inSelectPlayer)
		   showTableUsers();
							   	
		objetEvenement = null;
		trace("end eventPlayerPictureCanceled");
    	trace("*********************************************\n");
    }// end function
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
    public function eventPlayerSelectedPicture(objetEvenement:Object)
    {
        // param:  nomUtilisateur, id
     	trace("*********************************************");
     	trace("begin eventPlayerSelectedNewPicture   ");
		
		var iplayer =  getPersonnageByName(objetEvenement.nomUtilisateur);
		iplayer.setIdPersonnage(objetEvenement.idPersonnage);
		if(!inSelectPlayer)
		   showTableUsers();
			   	
		objetEvenement = null;
		trace("end eventPlayerSelectedNewPicture ");
    	trace("*********************************************\n");
    }// end function
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDemarrePartie(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, idPersonnage
     	trace("debut de evenementJoueurDemarrePartie   "+objetEvenement.nomUtilisateur+"     "+objetEvenement.idPersonnage);
             
		var iplayer =  getPersonnageByName(objetEvenement.nomUtilisateur);
		iplayer.setIdPersonnage(objetEvenement.idPersonnage);
		if(!inSelectPlayer)
		   showTableUsers();
		trace("fin de evenementJoueurDemarrePartie");
    }// end function
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	function calculateMenu():Number
	{
		// we make an array to sort the players regarding theirs points 
		//var jouersStarted:Array = new Array();
		var i:Number;
		var playersNumber:Number = 0;
		for (i in tableauDesPersonnages) {
			if(!(tableauDesPersonnages[i].getRole() > 1  && this.ourTable.compareType("Tournament")))
				   playersNumber++;
		}// end for
				
		return playersNumber;
    }// end function
	
	////////////////////////////////////////////////////////////////////////////////////////////////////	
	function remplirMenuPointage()
    {
		//trace("*********************************************");
    	//trace("debut de remplirMenuPointage   ");
		//trace("type: " + this.typeDeJeu);
				
		// we make an array to sort the players regarding theirs points 
		var jouersStarted:Array = new Array();
		var i:Number;
		for (i in tableauDesPersonnages) {
					jouersStarted[i] = new Object();
			        jouersStarted[i].nomUtilisateur = tableauDesPersonnages[i].obtenirNom();
			        jouersStarted[i].pointage = tableauDesPersonnages[i].obtenirPointage();
			        jouersStarted[i].role = tableauDesPersonnages[i].getRole();
					jouersStarted[i].idessin = tableauDesPersonnages[i].getIDessin();		
					jouersStarted[i].win = tableauDesPersonnages[i].isOnFinish();
					jouersStarted[i].clocol = tableauDesPersonnages[i].getColor();
					
					//trace("Dans pointage : " + jouersStarted[i].nomUtilisateur + " " + this.listeDesPersonnages[i].nom );
		}// end for
		
		//sort the elements using a compare function
		jouersStarted.sort(compareByPointsDescending);
		//jouersStarted.reverse();
				
		// mettre les id en ordre : tabOrdonne.id contient les id des personnages en ordre de pointage
		// il suffit de mettre les MC correspondants sur le podium
		
		// to control the holes ...
		var count:Number = jouersStarted.length;
		for(i = 0; i < count; i++){
		      if(jouersStarted[i].role > 1 && this.ourTable.compareType("Tournament"))   
		          jouersStarted.splice(i,1);
		}
				
		var fondClip:MovieClip = new MovieClip();
		var colorTrans:ColorTransform = new ColorTransform();
				
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
			
			UtilsBox.colorItMatrix(jouersStarted[i].clocol , this["tete" + (i + 1)].headClo, jouersStarted[i].idessin);
	
    	} 
		delete jouersStarted;
    }// end function
	
	
    
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
		
		for(i = 0; i < taille; i++) {
					
			this.tabPodiumOrdonneID[i] = new Object();
			this.tabPodiumOrdonneID[i].nomUtilisateur = objetEvenement.statistiqueJoueur[i].nomUtilisateur;
			this.tabPodiumOrdonneID[i].pointage = objetEvenement.statistiqueJoueur[i].pointage;
			this.tabPodiumOrdonneID[i].role = objetEvenement.statistiqueJoueur[i].userRole;
			this.tabPodiumOrdonneID[i].position = objetEvenement.statistiqueJoueur[i].position;
			this.tabPodiumOrdonneID[i].idessin = 0; 
						
		}// end for
						
		//this.tabPodiumOrdonneID.sortOn("pointage");
		//sort the elements using a compare function
		this.tabPodiumOrdonneID.sort(compareByPositionAscending);
		//this.tabPodiumOrdonneID.reverse();
		
		taille = this.tabPodiumOrdonneID.length;
		// to find the picture
		for (i = 0; i < 12; i++) {
												
			for(k = 0; k < taille; k++){
				
				if(tableauDesPersonnages[i].obtenirNom() == this.tabPodiumOrdonneID[k].nomUtilisateur){
					this.tabPodiumOrdonneID[k].idessin = tableauDesPersonnages[i].getIDessin;
					this.tabPodiumOrdonneID[k].clocolor = tableauDesPersonnages[i].getColor();
					this.tabPodiumOrdonneID[k].role = tableauDesPersonnages[i].getRole();
					//trace(" OPPSS Patrie terminee : " + this.listeDesPersonnages[i].role);
				}
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
		//_level0.loader.contentHolder["fond_MiniGame"]._y += 400;
		_level0.loader.contentHolder.brainBox.removeMovieClip();
		_level0.loader.contentHolder.bananaBox.removeMovieClip();
		_level0.loader.contentHolder.toolTip.removeMovieClip();
		_level0.loader.contentHolder["deconnexion"].removeMovieClip(); 
		
		
		//s'assurer que la musique s'arrete en fin de partie
		_level0.loader.contentHolder.musique.stop();
		_level0.loader.contentHolder.musiqueDefault.stop();
	
    	_level0.loader.contentHolder.gotoAndStop(5);
    
    	Mouse.show(); 
  	
		// mettre les id en ordre : tabOrdonne.id contient les id des personnages en ordre de pointage
		// il suffit de mettre les MC correspondants sur le podium
		
		// to cut the holes ...
       for(i = 0; i <= taille; i++){
		      if(this.tabPodiumOrdonneID[i].role > 1 && this.ourTable.compareType("Tournament") )   
		         this.tabPodiumOrdonneID.splice(i,1);
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
			
			UtilsBox.colorItMatrix(this.tabPodiumOrdonneID[i - 1].clocolor , this["tete" + i ].headClo, this.tabPodiumOrdonneID[i - 1].idessin);
    	}
		
    	this.endGame = true;
		_level0.loader.contentHolder.objGestionnaireEvenements.deconnexion();
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
	public function compareByPositionAscending(element1, element2)
	{
		return element1.position - element2.position;
	}
	
	
	// methode used as compare function to sort our players by their points
	public function compareByPointsDescending(element1, element2)
	{
		return element2.pointage - element1.pointage;
	}
	
	// methode used as compare function to sort our players by their points
	public function compareByPositionDescending(element1, element2)
	{
		return element2.position - element1.position;
	}
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDeplacePersonnage(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, anciennePosition et nouvellePosition, pointage, bonus
    	trace("*********************************************");
    	trace("debut de evenementJoueurDeplacePersonnage (sans compter les rotations)  " + objetEvenement.nomUtilisateur + "   " + objetEvenement.anciennePosition.x+"   "+objetEvenement.anciennePosition.y+"   "+objetEvenement.nouvellePosition.x+"   "+objetEvenement.nouvellePosition.y+"   "+objetEvenement.collision+"   "+objetEvenement.pointage+"   "+objetEvenement.argent);
       	
        if(!endGame){  //to cancel after end game virtual players move's		
    	   
		   //trace("juste avant la teleportation nom du perso et param  " + objetEvenement.anciennePosition.x + " " +  objetEvenement.anciennePosition.y + " " + objetEvenement.nouvellePosition.x + " " +  objetEvenement.nouvellePosition.y);
		   //_level0.loader.contentHolder.planche.teleporterPersonnage(objetEvenement.nomUtilisateur, pt_initial.obtenirX(), pt_initial.obtenirY(), pt_final.obtenirX(), pt_final.obtenirY(), objetEvenement.collision);
   		  _level0.loader.contentHolder.planche.teleporterPersonnage(objetEvenement);

           // update players array
		   var iplayer =  getPersonnageByName(objetEvenement.nomUtilisateur);
		   iplayer.modifierPointage(objetEvenement.pointage);
		   // to put the flag
		   iplayer.getFinish(objetEvenement.bonus);	          
			   		
		   // show the results
		   remplirMenuPointage();
		   // newsbox
		   var messageInfo:String = objetEvenement.nomUtilisateur + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.moveMessage; 
		   this.newsChat.addMessage(messageInfo);
		}
		objetEvenement = null;
     	trace("fin de evenementJoueurDeplacePersonnage");
     	trace("*********************************************\n");
    } // end method  
    
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
		   var messageInfo:String = playerThat + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.bananaMess + playerUnder; 
		   this.newsChat.addMessage(messageInfo);
		   
		    getPersonnageByName(playerThat).tossBananaShell(getPersonnageByName(playerUnder));
		  	 		
		}else if(objetEvenement.objetUtilise == "Livre")
		{
		   var messageInfo:String = objetEvenement.joueurQuiUtilise + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.bookUsedMess; 
		   this.newsChat.addMessage(messageInfo);
		   
		}else if(objetEvenement.objetUtilise == "Brainiac")
		{
		   var messageInfo:String = objetEvenement.joueurQuiUtilise + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.brainiacUsedMess; 
		   this.newsChat.addMessage(messageInfo);
		   
		   
		   _level0.loader.contentHolder.planche.getPersonnageByName(playerUnder).setBrainiac(true);
		   
		   
		}else if(objetEvenement.objetUtilise == "Boule")
		{
		   var messageInfo:String = objetEvenement.joueurQuiUtilise + _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.cristallUsedMess; 
		   this.newsChat.addMessage(messageInfo);
		}
		
		objetEvenement = null;
     	trace("fin de evenementUtiliserObjet");
     	trace("*********************************************\n");
} // end methode	
	    
	//// function used to draw the points menu	
	////////////////////////////////////////////////////
	public function dessinerMenu()
	{
		// used to know the size of menu
       var playersNumber:Number = calculateMenu();
      // Create the base shape with blue color
      _level0.loader.contentHolder.createEmptyMovieClip("menuPointages", 5);
      _level0.loader.contentHolder.menuPointages._x = 451;
      _level0.loader.contentHolder.menuPointages._y = 60;
      UtilsBox.drawRoundedRectangle(_level0.loader.contentHolder.menuPointages, 100, 31 + 8 + playersNumber*21, 10, 0x2A57F6, 100);

      // add the intern black box there put the players lines
      _level0.loader.contentHolder.menuPointages.createEmptyMovieClip("mc_autresJoueurs", 11);
      _level0.loader.contentHolder.menuPointages.mc_autresJoueurs._x = 7;
      _level0.loader.contentHolder.menuPointages.mc_autresJoueurs._y = 31;
      UtilsBox.drawRoundedRectangle(_level0.loader.contentHolder.menuPointages.mc_autresJoueurs, 86, 2 + playersNumber*21, 3, 0x000000, 100);

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
        _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur" + i].attachMovie("faceHolder2","tete" + i, 40 + i, {_x:20, _y:5});
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


/*
  Method used to show or redraw persos on the frame 3
 */
private function showTableUsers()
{
	var listeDesPersonnages:Array = new Array(); ;
	var i:Number;
	
	// init to void the holders
	for(i = 0; i < 12; i++)
	{
	    this.drawUserVoidVisible(i);		
		_level0.loader.contentHolder["joueur" + i] = " ";
	}
	
	// first create array of persos to show
	var count:Number = tableauDesPersonnages.length;
	for(i = 0; i < count; i++)
	{		
	   listeDesPersonnages.push(new Object());
	   var n:Number = listeDesPersonnages.length;
	   listeDesPersonnages[n - 1].idDessin = tableauDesPersonnages[i].getIDessin();
	   listeDesPersonnages[n - 1].nom = tableauDesPersonnages[i].obtenirNom();
	   listeDesPersonnages[n - 1].clocolor = tableauDesPersonnages[i].getColor();
	}
   
    // if picture id == 0 we don't show him
	count = listeDesPersonnages.length;
	trace("test verify count in showUsers : " + count);
    for(i = 0; i < count; i++)
	{
		trace("test verify i in showUsers : " + i);
       _level0.loader.contentHolder["joueur" + i] =  listeDesPersonnages[i].nom;
	   if(listeDesPersonnages[i].idDessin > 0)
	   {
	      drawUserFrame3(i, listeDesPersonnages[i].clocolor, listeDesPersonnages[i].idDessin, _level0.loader.contentHolder["player" + i]);
	      _level0.loader.contentHolder["player" + i]._xscale = 90;
	      _level0.loader.contentHolder["player" + i]._yscale = 90;
	   }
	}                     					 
				
}// end method

public function drawUserFrame3(i:Number, colorC:String, idDessin:Number, movClip:MovieClip)
{
	 
	 var filterC:ColorMatrixFilter = UtilsBox.colorMatrixPerso(colorC, idDessin);
	//***********************************************
	// to load the perso .. use ClipLoader to know the moment of complet load
    // create them dinamicaly
	   var mcLoaderString = "myLoader" + i;
	   this["mcLoaderString"] = new MovieClipLoader();
	   var mclListenerString = "myListener" + i;
	   this["mclListenerString"] = new Object();
       this["mclListenerString"].onLoadComplete = function(target_mc:MovieClip) {
            		    			  
		   target_mc.filterC = filterC;
		   target_mc._xscale = 90;
		   target_mc._yscale = 90;
		   target_mc.gotoAndPlay(1);		   			             
		};
		this["mcLoaderString"].addListener(this["mclListenerString"]);
					
	   
	   this["mcLoaderString"].loadClip("Perso/persos" + idDessin + ".swf", movClip);
}

/*
 * Methode used to verify if all users are seted theirs perso's
*/
function testPlayers():Boolean
{
   	var verify:Boolean = true;
	var i:Number;
	for( i in tableauDesPersonnages) {
		if(tableauDesPersonnages[i].getIdPersonnage() == 0 || tableauDesPersonnages[i].getIDessin() == 0 ||
    		 tableauDesPersonnages[i].getIdPersonnage() == undefined || tableauDesPersonnages[i].getIDessin() == undefined){
			verify = false;
		}
		trace("test verify : " + tableauDesPersonnages[i].getIdPersonnage() + " and dessin " +  tableauDesPersonnages[i].getIDessin());
	}
	trace(" verify " + verify);
	return verify;
}// end methode 

// creating filters for plyerSelect
function calculColorsAndLoadPlayerSelect()
{
	this.inSelectPlayer = true;
   //var clothesCol:String = this.colorIt;
   var colorsFilters:Array = new Array();
   var i:Number;
   
   for(i = 0; i < 12; i++)
   {
	  colorsFilters.push(UtilsBox.colorMatrixPerso(getColorByID(this.clothesColorID, i + 1), i + 1));
   }
   
   var mcLoaderString = "myLoader";
	   this["mcLoaderString"] = new MovieClipLoader();
	   var mclListenerString = "myListener";
	   this["mclListenerString"] = new Object();
       this["mclListenerString"].onLoadComplete = function(target_mc:MovieClip) {
            		    			  
		   target_mc.filtersC = colorsFilters;
		   			             
		};
		this["mcLoaderString"].addListener(this["mclListenerString"]);
					
	   
	   this["mcLoaderString"].loadClip("GUI/playerSelect.swf", _level0.loader.contentHolder.selectHolder);
   
} /// end method calculColors...



// coloring with ColorMatrixFilter a movie
function colorItMatrixByID(colorID:Number, mov:MovieClip, idD:Number)
{
	  var clothesCol:String = getColorByID(colorID, idD);
      
	  // to obtain RGB values of our color
       var rr:Number = Number("0x" + clothesCol.substr(2,2).toString(10));
       var gg:Number = Number("0x" + clothesCol.substr(4,2).toString(10));
       var bb:Number = Number("0x" + clothesCol.substr(6,2).toString(10));
	   
	   //trace("rr : " + rr + " gg : " + gg + " bb : " + bb);

      // to obtain the multipliers
      // the RGB of base color of perso1 is 245,64,75
      switch(idD)
      {
           case 1:
			     rr = rr/255/0.96;  // to take in consideration the base color of the movie
                 gg = gg/255/0.251;
                 bb = bb/255/0.294;
				 //trace("Choix de la dessin 1"); // 245,64,75
            break;
            
			 case 2:
			     rr = rr/255/0.169;
                 gg = gg/255/0.741;
                 bb = bb/255/0.373;
				//trace("Choix de la dessin 2"); // 43,189/95
            break;
			
			 case 3:
                  rr = rr/255/0.741;
                 gg = gg/255/0.537;
                 bb = bb/255/0.165;
				// trace("Choix de la dessin 3"); // 189,137,42
            break;
			
			 case 4:
                rr = rr/255/0.188;
                gg = gg/255/0.584;
                bb = bb/255/0.29;
				//trace("Choix de la dessin 4"); // 48,149,74
            break;
			
			 case 5:
                rr = rr/255/0.27;
                gg = gg/255/0.314;
                bb = bb/255/0.53;
				//trace("Choix de la dessin 5");  // 69,80,136
            break;
			
			 case 6:
                 rr = rr/255/0.4;
                 gg = gg/255/0.2;
                 bb = bb/255/0.6;
				//trace("Choix de la dessin 6"); // 102,51,153
            break;
			
			 case 7:
                 rr = rr/255/0.059;
                 gg = gg/255/0.53;
                 bb = bb/255/0.204;
				//trace("Choix de la dessin 7"); // 15,136,52
            break;
			
			 case 8:
                 rr = rr/255;
                 gg = gg/255;
                 bb = bb/255;
				//trace("Choix de la dessin 8");  // 255.255.255
            break;
			
			
			 case 9:
                 rr = rr/255/0.3;
                 gg = gg/255/0.588;
                 bb = bb/255/0.29;
				//trace("Choix de la dessin 9");  //  79.150.74
            break;
			
			 case 10:
                 rr = rr/255;
                 gg = gg/255/0.12;
                 bb = bb/255/0.12;
				//trace("Choix de la dessin 10");  // 255.0.0 
            break;
			
			case 11:
                 rr = rr/255;
                 gg = gg/255;
                 bb = bb/255;
				 // trace("Choix de la dessin 11");   // 255.255.255
            break;
			
			case 12:
                 rr = rr/255/0.843;
                 gg = gg/255/0.019;
                 bb = bb/255/0.0118;
				//trace("Choix de la dessin 12");   //  215.5.3
            break;
			
            default:
                trace("Erreur Inconnue");
     }
   

     var matrix:Array = new Array();
     matrix = matrix.concat([rr, 0, 0, 0, 0]); // red
     matrix = matrix.concat([0, gg, 0, 0, 0]); // green
     matrix = matrix.concat([0, 0, bb, 0, 0]); // blue
     matrix = matrix.concat([0, 0, 0, 1, 0]); // alpha
		
   var filterC:ColorMatrixFilter = new ColorMatrixFilter(matrix);
   //trace("filter: " + filter.matrix);
   
   mov.filters = new Array(filterC);
} // end method

// modified code from source - www.adobe.com
 public static function drawRoundedRectangle(target_mc:MovieClip, boxWidth:Number, boxHeight:Number, cornerRadius:Number, fillColor:Number, fillAlpha:Number):Void {
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

// to take a good Id for our perso 
function haveThisId(idPers:Number):Boolean
{
   	var i:Number;
	for(i = 0; i < _level0.loader.contentHolder.tableauDesPersoChoisis.length; i++)
	{
		if(_level0.loader.contentHolder.tableauDesPersoChoisis[i] == idPers)
		   return true;
	}
	return false;
}
	
}// end class
