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
import mx.controls.Alert;
import flash.utils.*;
import mx.utils.*;
import flash.geom.Transform;
import flash.geom.ColorTransform;
import flash.filters.ColorMatrixFilter;

class GestionnaireEvenementsProfModule
{
    private var nomUtilisateur:String;    // user name of our  user
	private var motDePasse:String;        // le mot de passe du user pour se connecter au serveur
	private var abreviationLangue:String; // la langue desiree par le client pour la duree de la connection
	private var userRole:Number;          // if 1 - simple user, if 2 - is admin(master), if 3 - is  prof
	public var keywordsMap:Object;
	public var gameTypesMap:Object;
	private var report:RoomReport;
	public var  listeDesSalles:Array;    //  liste de toutes les salles de l'utilisateur
	private var nouvellesSalles:Boolean; //  denote le fait que l'array 'listeDesSalles' a ete update depuis la dernier 
										 //  command 'ObtenirListeSallesProf'
	private var listeChansons:Array;     //  liste de toutes les chansons 
    private var objGestionnaireCommunication:GestionnaireCommunicationProfModule;  //  pour caller les fonctions du serveur 	

  
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                  CONSTRUCTEUR
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function GestionnaireEvenementsProfModule(nom:String, passe:String, abrevLangue:String, url_server:String, port:Number)
    {
        trace("*********************************************");
        trace("debut du constructeur de gesEve      " + nom + " " + url_server + " " + port);
        this.nomUtilisateur = nom;
        this.motDePasse = passe;
		this.abreviationLangue = abrevLangue;
		this.keywordsMap = new Object();
		this.gameTypesMap = new Object();
        this.listeDesSalles = new Array();
		this.listeChansons = new Array();
		this.nouvellesSalles = false;
		this.report = null;
        this.objGestionnaireCommunication = 
				new GestionnaireCommunicationProfModule(
							Delegate.create(this, this.evenementConnexionPhysique), 
							Delegate.create(this, this.evenementDeconnexionPhysique), 
							url_server, port);
    	trace("fin du constructeur de gesEve");
    	trace("*********************************************\n");
    }
		
	//Indique qu'une nouvelle liste de salles est arrivée depuis que le dernier GUI update du client.
	function nouvelleListeDesSalles() : Boolean
	{
		return this.nouvellesSalles;
	}
	//Indique que le client vient d'updater son GUI en utilisant la liste des salles la plus récente.
	function nouvelleListeDesSallesRecue()
	{
		this.nouvellesSalles = false;
	}
	function obtenirRapport():RoomReport
	{
		return this.report;
	}
	function obtenirUserRole():Number
	{
		return userRole;
	}
		
    ///////////////////////////////////////////////////////////////////////////////////////////////////
	function utiliserPortSecondaire()
	{
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server_secondaire;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port_secondaire, 10);
		
        this.objGestionnaireCommunication = 
			new GestionnaireCommunicationProfModule(Delegate.create(this, this.evenementConnexionPhysique2), 
													Delegate.create(this, this.evenementDeconnexionPhysique), 
													url_serveur, port);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////
	function tryTunneling()
	{
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server_tunneling;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port_tunneling, 10);

		this.objGestionnaireCommunication = 
			new GestionnaireCommunicationProfModule(Delegate.create(this, this.evenementConnexionPhysiqueTunneling), 
													Delegate.create(this, this.evenementDeconnexionPhysique), 
													url_serveur, port);
	}
		
	///////////////////////////////////////////////////////////////////////////////////////////////////
    function createRoom(objRoom:Object)
    {
        trace("*********************************************");
        trace("debut de createRoom     ");
        this.objGestionnaireCommunication.createRoom(Delegate.create(this, this.retourCreateRoom), objRoom);
        trace("fin de createRoom");
        trace("*********************************************\n");
    }

	function updateRoom(objRoom:Object)
    {
        trace("*********************************************");
        trace("debut de updateRoom     ");
        this.objGestionnaireCommunication.updateRoom(Delegate.create(this, this.retourUpdateRoom), objRoom);
        trace("fin de updateRoom");
        trace("*********************************************\n");
    }

	function deleteRoom(roomId:String)
    {
        trace("*********************************************");
        trace("debut de deleteRoom     :" + roomId);
        this.objGestionnaireCommunication.deleteRoom(Delegate.create(this, this.retourDeleteRoom), roomId);
        trace("fin de deleteRoom");
        trace("*********************************************\n");
    }

	///////////////////////////////////////////////////////////////////////////////////////////////////
    function reportRoom(roomId:Number)
    {
        trace("*********************************************");
        trace("begin of reportRoom     :" + roomId);
        this.objGestionnaireCommunication.reportRoom(Delegate.create(this, this.retourReportRoom), roomId);
        trace("end reportRoom");
        trace("*********************************************\n");
    }
			
	///////////////////////////////////////////////////////////////////////////////////////////////////
	function deconnexion()
	{
		trace("*********************************************");
        trace("debut de deconnexion");
		this.objGestionnaireCommunication.deconnexion(Delegate.create(this, this.retourDeconnexion));
		trace("fin de deconnexion");
        trace("*********************************************\n");
	}

	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                  fonctions retour
    ///////////////////////////////////////////////////////////////////////////////////////////////////
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourConnexion(objetEvenement:Object)
    {
    	// c'est la fonction qui va etre appellee lorsque le GestionnaireCommunication aura
        // recu la reponse du serveur
        // objetEvenement est un objet qui est propre a chaque fonction comme retourConnexion
        // Selon la fonction que vous appelerez, il y aura differentes valeurs
        // dedans. Ici, il y a juste une valeur qui est succes qui est de type booleen
    	// objetEvenement.resultat = DBInfo, JoueurNonConnu, JoueurDejaConnecte, JoueurNonAutorise
    	trace("*********************************************");
    	trace("debut de retourConnexion     " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
			//Content of objetEvenement:
			//    listeChansons:Array
			//    keywordsMap:Object   (keywordsMap[keyword_id]= kname,group,gname)
			//    gameTypesMap:Object  (gameTypesMap[game_type_id]= game type name)
			//    userRole:Number      (1=user has normal account, 2=user has admin account, 3=user has prof account)
			case "DBInfo":
			    this.listeChansons = new Array();
				this.keywordsMap = new Object();
				this.gameTypesMap = new Object();
				var count:Number = objetEvenement.listeChansons.length;
				for(var k:Number=0;  k<count; k++)
					this.listeChansons.push(objetEvenement.listeChansons[k]);
				for (var id:String in objetEvenement.keywordsMap)
					this.keywordsMap[id] = objetEvenement.keywordsMap[id].split(",");
				for (var id:String in objetEvenement.gameTypesMap)
					this.gameTypesMap[id] = objetEvenement.gameTypesMap[id];
				this.userRole = objetEvenement.userRoleMaster;  //1==ordinary user, 2==admin, 3==prof
				this.objGestionnaireCommunication.obtenirListeSallesProf(
														Delegate.create(this, this.retourObtenirListeSalles));
				trace("La connexion a marche");
				break;
			 
            case "JoueurNonConnu":
                trace("Joueur non connu");
            	break;
             
			case "JoueurDejaConnecte":
				var dejaConnecte = _level0.attachMovie("GUI_erreur", "DejaConnecte", 9999);
				dejaConnecte.textGUI_erreur.text = _root.texteSource_xml.firstChild.attributes.GUIdejaConnecte;
                trace("Joueur deja connecte");
            	break;
			case "JoueurNonAutorise":
				trace("Joueur non autorisé à entrer dans le module prof");
				break;

            default:
            	trace("Erreur Inconnue");
        }
		objetEvenement = null;
     	trace("fin de retourConnexion");
     	trace("*********************************************\n");
    }
		
  
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirListeSalles(objetEvenement:Object)
    {
        //objetEvenement.resultat = ListeSalles, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourObtenirListeSalles   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
		{
			case "ListeSalles":
				this.listeDesSalles = new Array();
				var count:Number = objetEvenement.listeNomSalles.length;
            	for (var i:Number = 0; i < count; i++) {
					this.listeDesSalles.push(objetEvenement.listeNomSalles[i]);
					trace("salle " + i + " : " + this.listeDesSalles[i].names["fr"]);
				}
				nouvellesSalles = true;
				_level0.unloadDataTransferAnimation(); //this was loaded when the command was sent
				//If we are already in frame 2, all we need is to refresh the GUI.
				//Otherwise gotoAndStop(2) will automatically refresh the GUI
				if (_level0._currentframe == 2) 
					_level0.updateRoomDataGrid();
				else
					gotoAndStop(2);
            	break;
			 
            case "CommandeNonReconnue":
            case "ParametrePasBon":
            case "JoueurNonConnecte":
			default:
	        	trace("Erreur ObtenirListeSalles: " + objetEvenement.resultat);
    }
        trace("fin de retourObtenirListeSalles" + " " + objetEvenement.resultat);
        objetEvenement = null;
		trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////	 
    public function retourCreateRoom(objetEvenement:Object)
    {
        //objetEvenement.resultat = OK, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourCreateRoom   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "OK":
				trace("room created");
				//Do we have to do this?  Since clients can't have more than one connection to the
				//server at a time, this list can be maintained locally, saving a call to the server.
				this.objGestionnaireCommunication.obtenirListeSallesProf(
													Delegate.create(this, this.retourObtenirListeSalles));
		        break;
            default:
				trace("Résultat de l'opération: " + objetEvenement.resultat);
        }
		objetEvenement = null;
        trace("fin de retourCreateRoom");
        trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	public function retourUpdateRoom(objetEvenement:Object)
    {
        //objetEvenement.resultat = OK, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourUpdateRoom   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "OK":
				trace("room updated");
				//Do we have to do this?  Since clients can't have more than one connection to the
				//server at a time, this list can be maintained locally, saving a call to the server.
				this.objGestionnaireCommunication.obtenirListeSallesProf(
													Delegate.create(this, this.retourObtenirListeSalles));
		        break;
            default:
				trace("Résultat de l'opération: " + objetEvenement.resultat);
        }
		objetEvenement = null;
        trace("fin de retourUpdateRoom");
        trace("*********************************************\n");
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////////////	
	public function retourDeleteRoom(objetEvenement:Object)
    {
        //objetEvenement.resultat = OK, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourDeleteRoom   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "OK":
				trace("room deleted");
				//Do we have to do this?  Since clients can't have more than one connection to the
				//server at a time, this list can be maintained locally, saving a call to the server.
				this.objGestionnaireCommunication.obtenirListeSallesProf(
													Delegate.create(this, this.retourObtenirListeSalles));
		        break;
            default:
				trace("Résultat de l'opération: " + objetEvenement.resultat);
        }
		objetEvenement = null;
        trace("fin de retourDeleteRoom");
        trace("*********************************************\n");
    }
		
	////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourReportRoom(objetEvenement:Object)
    {
        //objetEvenement.resultat = OK, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourReportRoom   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "OK":
	            trace("report created");
				this.report = objetEvenement.report;
				_level0.unloadDataTransferAnimation(); //this was loaded when the command was sent
				//If we are already in frame 4, all we need is to refresh the GUI.
				//Otherwise gotoAndStop(4) will automatically refresh the GUI
				if (_level0._currentframe == 4) 
					_level0.updateReport();
				else
					gotoAndStop(4);
            	break;
            
			default:
				trace("Résultat de l'opération: " + objetEvenement.resultat);
        }
		objetEvenement = null;
        trace("fin de retourReportRoom");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourDeconnexion(objetEvenement:Object)
    {
        // objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
    	trace("*********************************************");
    	trace("debut de retourDeconnexion   "+objetEvenement.resultat);
    	switch(objetEvenement.resultat)
        {
            case "Ok":
                trace("deconnexion");
            break;
			
			default:
				trace("Résultat de l'opération: " + objetEvenement.resultat);
        }
		objetEvenement = null;
    	trace("fin de retourDeconnexion");
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
            this.objGestionnaireCommunication.connexion(
									Delegate.create(this, this.retourConnexion), 
									this.nomUtilisateur, this.motDePasse, this.abreviationLangue);
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
            this.objGestionnaireCommunication.connexion(
									Delegate.create(this, this.retourConnexion), 
									this.nomUtilisateur, this.motDePasse, this.abreviationLangue);
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
            this.objGestionnaireCommunication.connexion(
									Delegate.create(this, this.retourConnexion), 
									this.nomUtilisateur, this.motDePasse, this.abreviationLangue);
		}
        else
        {
            trace("pas de connexion physique");
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
    public function evenementMessage(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, message
    	trace("*********************************************");
    	trace("debut de evenementMessage   " + objetEvenement.message + "    " + objetEvenement.nomUtilisateur);
		objetEvenement = null;
    	trace("fin de evenementMessage");
    	trace("*********************************************\n");
    }
	
	
	
 	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      Autres
    ///////////////////////////////////////////////////////////////////////////////////////////////////
	
}// end class
