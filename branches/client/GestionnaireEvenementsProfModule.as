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
	private var userRole:Number;  // if 1 - simple user, if 2 - is admin(master), if 3 - is  prof
	private var motDePasse:String;  // notre mot de passe pour pouvoir jouer
	private var langue;
   	public var  listeDesSalles:Array;    //  liste de toutes les salles                !!!! Combiner ici tout dans un Objet	
	private var listeChansons:Array;    //  liste de toutes les chansons    
    private var objGestionnaireCommunication:GestionnaireCommunicationProfModule;  //  pour caller les fonctions du serveur 	
	
	
	
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                  CONSTRUCTEUR
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function GestionnaireEvenementsProfModule(nom:String, passe:String, langue:String)
    {
        trace("*********************************************");
        trace("debut du constructeur de gesEve      " + nom + "      " + passe);
        this.nomUtilisateur = nom;
        this.motDePasse = passe;
		this.langue = langue;
        this.listeDesSalles = new Array();
		this.listeChansons = new Array();
        var url_serveur:String = _level0.configxml_mainnode.attributes.url_server;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port, 10);
						
        this.objGestionnaireCommunication = new GestionnaireCommunicationProfModule(Delegate.create(this, this.evenementConnexionPhysique), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
	
    	trace("fin du constructeur de gesEve");
    	trace("*********************************************\n");
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
		
    ///////////////////////////////////////////////////////////////////////////////////////////////////
	function utiliserPortSecondaire()
	{
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server_secondaire;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port_secondaire, 10);
		
        this.objGestionnaireCommunication = new GestionnaireCommunicationProfModule(Delegate.create(this, this.evenementConnexionPhysique2), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	function tryTunneling()
	{
		//var url_serveur:String = _level0.configxml_mainnode.attributes.url_server_tunneling;
		//var port:Number = parseInt(_level0.configxml_mainnode.attributes.port_tunneling, 10);
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server_secondaire;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port_secondaire, 10);
		
        //this.objGestionnaireCommunication = new GestionnaireCommunicationTunneling(Delegate.create(this, this.evenementConnexionPhysiqueTunneling), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
		this.objGestionnaireCommunication = new GestionnaireCommunicationProfModule(Delegate.create(this, this.evenementConnexionPhysiqueTunneling), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
	}
		
	///////////////////////////////////////////////////////////////////////////////////////////////////
    function createRoom(nameRoom:String, description:String, pass:String, fromDate:String, toDate:String, defaultTime:String, roomCategories:String, gameTypes:String)
    {
        trace("*********************************************");
        trace("debut de createRoom     :" + nameRoom + " " + toDate + " " + gameTypes);
        this.objGestionnaireCommunication.createRoom(Delegate.create(this, this.retourCreateRoom), nameRoom, description, pass, fromDate, toDate, defaultTime, roomCategories, gameTypes);
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
        // (en termes plus informatiques, on appelle ca un eventHandler -> fonction qui gere
        // les evenements). Selon la fonction que vous appelerez, il y aura differentes valeurs
        // dedans. Ici, il y a juste une valeur qui est succes qui est de type booleen
    	// objetEvenement.resultat = Ok, JoueurNonConnu, JoueurDejaConnecte
    	trace("*********************************************");
    	trace("debut de retourConnexion     " + objetEvenement.resultat);
        // param:  userRoleMaster == 1 if simple user or 2 if master
    	var dejaConnecte:MovieClip;
		
        switch(objetEvenement.resultat)
        {
			case "Musique":
			    // TO DO - play 
				trace("Q musique " + objetEvenement.listeChansons.length);
				var count:Number = objetEvenement.listeChansons.length;
				for(var k:Number = 0;  k < count; k++)
				{
					this.listeChansons.push(objetEvenement.listeChansons[k]);
					trace(objetEvenement.listeChansons[k]);
				}
				
				this.userRole = objetEvenement.userRoleMaster; 
				
				this.objGestionnaireCommunication.obtenirListeSallesProf(Delegate.create(this, this.retourObtenirListeSalles));
				trace("La connexion a marche");
			break;
			 
            case "JoueurNonConnu":
                trace("Joueur non connu");
            break;
             
			case "JoueurDejaConnecte":
	  									
	     		dejaConnecte = _level0.attachMovie("GUI_erreur", "DejaConnecte", 9999);
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
                for (var i:Number = 0; i < count; i++)
                {
					this.listeDesSalles.push(objetEvenement.listeNomSalles[i]);
					trace("salle " + i + " : " + this.listeDesSalles[i].nom);
				}
												
				_level0.gotoAndStop(2);
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
		
        trace("fin de retourObtenirListeSalles" + " " + objetEvenement.resultat);
        objetEvenement = null;
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
			this.objGestionnaireCommunication.obtenirListeSallesProf(Delegate.create(this, this.retourObtenirListeSalles));
			

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
            this.objGestionnaireCommunication.connexion(Delegate.create(this, this.retourConnexion), this.nomUtilisateur, this.motDePasse, this.langue);
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
			// a refaire
			/*
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
			
			
			horsService.linkGUI_erreur.setTextFormat(formatLink);*/
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
    //////////////////////////////////////////////////////////////////////////////////////////
   
	
	public function obtenirGestionnaireCommunication():GestionnaireCommunicationProfModule
	{
		return objGestionnaireCommunication;
	}

	
}// end class
