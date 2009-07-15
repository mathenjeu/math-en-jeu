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

class GestionnaireEvenements
{
    private var roomDescription:String;  // short room description taked from DB
	private var nomUtilisateur:String;    // user name of our  user
	private var userRole:Number;  // if 1 - simple user, if 2 - is admin(master), if 3 - is  prof
	private var numeroDuPersonnage:Number; // sert a associer la bonne image pour le jeu d'ile au tresor
	private var numberDesJoueurs:Number;
    public var  listeDesPersonnages:Array;   // liste associant les idPersonnage avec les nomUtilisateurs dans la table ou on est
    private var motDePasse:String;  // notre mot de passe pour pouvoir jouer
    private var nomSalle:String;  //  nom de la salle dans laquelle on est
    private var numeroTable:Number;   //   numero de la table dans laquelle on est
	private var tablName:String;     // name of the created table
    private var tempsPartie:Number;   //  temps que va durer la partie, en minutes
    private var idPersonnage:Number;   //  
    private var motDePasseSalle:String;   // le mot de passe de la salle dans laquelle on est
    private var listeDesJoueursDansSalle:Array;  // liste des joueurs dans la salle qu'on est. Un joueur contient un nom (nom)
    public var  listeDesDescriptionsSalles:Array; //liste des descriptions des salles 
    public var  listeDesTypesDeJeu:Array; //liste des TypesDeJeu de salles
	//private var activ:Boolean;                     // if game has type Tournament and the room is active
	public var  listeDesSalles:Array;    //  liste de toutes les salles
	private var listeNumeroJoueursSalles:Array;		//liste de numero de joueurs dans chaque salle
	private var listeChansons:Array;    //  liste de toutes les chansons
    private var listeDesJoueursConnectes:Array;   // la premiere liste qu'on recoit, tous les joueurs dans toutes les salles. Un joueur contient un nom (nom)
    //  liste de toutes les tables dans la salle ou on est
    //contient un numero (noTable), le temps (temps) et une liste de joueurs (listeJoueurs) un joueur de la liste contient un nom (nom)
    private var listeDesTables:Array;   // list of tables in our room with list of users in 
    private var objGestionnaireCommunication:GestionnaireCommunication;  //  pour caller les fonctions du serveur 
	private var tabPodiumOrdonneID:Array;			// id des personnages ordonnes par pointage une fois la partie terminee
	private var pointageMinimalWinTheGame:Number = -1 // pointage minimal a avoir droit d'atteindre le WinTheGame
	private var numeroJoueursDansSalle:Number = 0;
	public  var typeDeJeu:String = "MathEnJeu";
	
	private var moveVisibility:Number;  // The number of cases that user can move. At the begining is 3. 
	                                    // With the 3 running correct answers the level increase by 1  
	
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
		trace("motDePasseSalle : " + motDePasseSalle);	
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
	
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //                                  CONSTRUCTEUR
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function GestionnaireEvenements(nom:String, passe:String)
    {
        trace("*********************************************");
        trace("debut du constructeur de gesEve      " + nom + "      " + passe);
        this.nomUtilisateur = nom;
        this.listeDesPersonnages = new Array();
        this.listeDesPersonnages.push(new Object());  // why ??????????? why without name ??????????
       
        this.motDePasse = passe;
        this.nomSalle = new String();
        this.motDePasseSalle = new String();
        this.listeDesSalles = new Array();
		this.listeDesDescriptionsSalles = new Array();
		this.listeNumeroJoueursSalles = new Array();
		this.listeDesTypesDeJeu = new Array();
        this.listeDesTables = new Array();
		this.listeChansons = new Array();
        this.listeDesJoueursConnectes = new Array();
        this.listeDesJoueursDansSalle = new Array();
		this.tabPodiumOrdonneID = new Array();
		
		this.moveVisibility = 3;
		
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port, 10);
		
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
		return this.numeroJoueursDansSalle;
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
		return this.numeroJoueursDansSalle;
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
    function createRoom(nameRoom:String, description:String, pass:String, fromDate:String, toDate:String)
    {
        trace("*********************************************");
        trace("debut de createRoom     :" + nameRoom);
        this.objGestionnaireCommunication.createRoom(Delegate.create(this, this.retourCreateRoom), nameRoom, description, pass, fromDate, toDate);
        trace("fin de createRoom");
        trace("*********************************************\n");
    }

	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function entrerSalle(nSalle:String)
    {
        trace("*********************************************");
        trace("debut de entrerSalle      " + nSalle);
        this.nomSalle = nSalle;
        
        for(var i:Number = 0; i < listeDesSalles.length; i++)
        {
            if(listeDesSalles[i].nom == nSalle)
            {
	            numeroJoueursDansSalle = listeNumeroJoueursSalles[i].maxnbplayers;
	            typeDeJeu = listeDesSalles[i].typeDeJeu;
	            
                if(listeDesSalles[i].possedeMotDePasse == true)
                {
                    this.motDePasseSalle = "";   // afficher une fenetre de demande de mot de passe
                }
                else
                {
                    this.motDePasseSalle = "";
                }
                break;
            }
        }
        this.objGestionnaireCommunication.entrerSalle(Delegate.create(this, this.retourEntrerSalle), this.nomSalle, this.motDePasseSalle);
        trace("fin de entrerSalle");
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
    function creerTable(temps:Number, nameTable:String)
    {
        trace("*********************************************");
        trace("debut de creerTable     " + temps);
        this.objGestionnaireCommunication.creerTable(Delegate.create(this, this.retourCreerTable), Delegate.create(this, this.evenementJoueurDemarrePartie), temps, nameTable);
        trace("fin de creerTable");
        trace("*********************************************\n");
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function demarrerPartie(no:Number)
    {
        trace("*********************************************");
        trace("debut de demarrerPartie     " + no);
		
        var idDessin:Number=((no-10000)-(no-10000)%100)/100;
        
        this.idPersonnage = no;
        trace("########### idDessin= " + idDessin + " this.idPersonnage= " + this.idPersonnage);
        if(no < 0) no = -no;
        this.listeDesPersonnages[numeroJoueursDansSalle-1].id = no;
        this.listeDesPersonnages[numeroJoueursDansSalle-1].nom = this.nomUtilisateur;
		 this.listeDesPersonnages[numeroJoueursDansSalle-1].role = this.userRole;
        this.objGestionnaireCommunication.demarrerPartie(Delegate.create(this, this.retourDemarrerPartie), Delegate.create(this, this.evenementPartieDemarree), Delegate.create(this, this.evenementJoueurDeplacePersonnage), Delegate.create(this, this.evenementSynchroniserTemps), Delegate.create(this, this.evenementPartieTerminee), no);//this.idPersonnage);//  
	
		trace("fin de demarrerPartie");
        trace("*********************************************\n");
    }
	
	/////////////////////////////////////////////////////////////////////////////////////////////////// 
    function demarrerMaintenant(niveau:String)
    {
        trace("*********************************************");
        trace("debut de demarrerMaintenant");
		trace("idPersonnage: " + this.idPersonnage);
		trace("niveau des personnages virtuels : " + niveau);
        this.objGestionnaireCommunication.demarrerMaintenant(Delegate.create(this, this.retourDemarrerMaintenant), this.idPersonnage, niveau);
        trace("fin de demarrerMaintenant");
        trace("*********************************************\n");
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function deplacerPersonnage(pt:Point)
    {
        trace("*********************************************");
        trace("debut de deplacerPersonnage     " + pt.obtenirX() + "     " + pt.obtenirY());
        this.objGestionnaireCommunication.deplacerPersonnage(Delegate.create(this, this.retourDeplacerPersonnage), _level0.loader.contentHolder.planche.calculerPositionOriginale(pt.obtenirX(), pt.obtenirY()));  
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
	function utiliserObjet(id:Number)
    {
        trace("*********************************************");
        trace("debut de utiliserObjet : " + id);
        this.objGestionnaireCommunication.utiliserObjet(Delegate.create(this, this.retourUtiliserObjet), id);  
        trace("fin de utiliserObjet");
        trace("*********************************************\n");
    }
	
	///////////////////////////////////////////////////////////////////////////////////////////////////   
	public function definirPointageApresMinigame(points:Number)
	{
		trace("*********************************************");
		trace("debut de definirPointageApresMinigame  ds gestEve   "+points);
		this.objGestionnaireCommunication.definirPointageApresMinigame(Delegate.create(this, this.retourDefinirPointageApresMinigame), points);  
		trace("fin de definirPointageApresMinigame");
		trace("*********************************************\n");
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////   
	public function definirArgentApresMinigame(argent:Number)
	{
		trace("*********************************************");
		trace("debut de definirArgentApresMinigame  ds gestEve   "+argent);
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

	// here we control if username contain the  'master'
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
    
        switch(objetEvenement.resultat)
        {
			case "OkEtPartieDejaCommencee":
			//A faire plus tard
			trace("<<<<<<<<<<<<<<<< deja connecte >>>>>>>>>>>>>>>>>>>");
			break;

			case "Musique":
				this.objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), Delegate.create(this, this.evenementJoueurConnecte), Delegate.create(this, this.evenementJoueurDeconnecte));

				//trace("objEvenement");
				trace("Q musique " + objetEvenement.listeChansons.length);
				
				for(var k:Number = 0;  k < objetEvenement.listeChansons.length; k++)
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
     	trace("fin de retourConnexion");
     	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirListeJoueurs(objetEvenement:Object)
    {
        //  objetEvenement.resultat = ListeJoueurs, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourObtenirListeJoueurs   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {  
	        
            case "ListeJoueurs":
            	this.numberDesJoueurs = objetEvenement.listeNomUtilisateurs.length;
        		//this.numeroJoueursDansSalle=objetEvenement.listeNomUtilisateurs.length;
        		
                for(var i:Number=0;i<objetEvenement.listeNomUtilisateurs.length;i++)
                {
                    this.listeDesJoueursConnectes.push(objetEvenement.listeNomUtilisateurs[i]);
                }
                this.objGestionnaireCommunication.obtenirListeSalles(Delegate.create(this, this.retourObtenirListeSalles));
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
        trace("fin de retourObtenirListeJoueur");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourObtenirListeSalles(objetEvenement:Object)
    {
        //   objetEvenement.resultat = ListeSalles, CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourObtenirListeSalles   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "ListeSalles":
			    this.listeDesSalles.removeAll();
                for (var i:Number = 0; i < objetEvenement.listeNomSalles.length; i++)
                {
					
					this.listeDesSalles.push(objetEvenement.listeNomSalles[i]);
					_level0.loader.contentHolder.listeSalle.addItem(this.listeDesSalles[i].nom );
					trace("salle " + i + " : " + this.listeDesSalles[i].nom);
					_level0.listeRooms.addItem(this.listeDesSalles[i].nom );
										
					this.listeDesDescriptionsSalles.push(objetEvenement.listeDescrSalles[i]);
					_level0.loader.contentHolder.listeDescr.push(this.listeDesDescriptionsSalles[i].descriptions );
					trace("salle " + i + " : " + this.listeDesDescriptionsSalles[i].descriptions );
					
					this.listeNumeroJoueursSalles.push(objetEvenement.listeNumberoJSalles[i]);
					_level0.loader.contentHolder.listeNumeroJSalles.push(this.listeNumeroJoueursSalles[i].maxnbplayers );
					trace("salle " + i + " : " + objetEvenement.listeNumberoJSalles[i].maxnbplayers);
					
					this.listeDesTypesDeJeu.push(objetEvenement.typeDeJeuAll[i]);
					_level0.loader.contentHolder.listeDesTypesDeJeu.push(this.listeDesTypesDeJeu[i].typeDeJeu );
					trace("salle " + i + " : " + objetEvenement.typeDeJeuAll[i].typeDeJeu + " ~ " +this.listeDesTypesDeJeu[i].typeDeJeu);
					

				}
				for (var i:Number = 0; i < objetEvenement.listeNomSalles.length; i++)
                {
					if(_level0.loader.contentHolder.langue == "en" && objetEvenement.listeNomSalles[i].nom == "Accromath")
					{
						_level0.loader.contentHolder.listeSalle.removeItemAt(i);
						trace("salle enlevee --> " + i + " : " + this.listeDesSalles[i].nom);
					}
				}
				//activ = objetEvenement.isActiveRoom;
				
				//_level0.loader.contentHolder.isActiv = objetEvenement.isActiveRoom;
				//trace("salle active : " + _level0.loader.contentHolder.isActiv);
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
                trace("Erreur Inconnue");
        }
        trace("fin de retourObtenirListeSalles");
        trace("*********************************************\n");
    }
	
	//*****************************************************************************************
	 ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourCreateRoom(objetEvenement:Object)
    {
        //   objetEvenement.resultat = , CommandeNonReconnue, ParametrePasBon ou JoueurNonConnecte
        trace("*********************************************");
        trace("debut de retourCreateRoom   " + objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "OK":
               
			trace("room created  ");
			this.objGestionnaireCommunication.obtenirListeSalles(Delegate.create(this, this.retourObtenirListeSalles));

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
        trace("fin de retourCreateRoom");
        trace("*********************************************\n");
    }
	//*****************************************************************************************
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourEntrerSalle(objetEvenement:Object)
    {
        //objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, MauvaisMotDePasseSalle, SalleNonExistante, JoueurDansSalle
        trace("*********************************************");
        trace("debut de retourEntrerSalle   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "Ok":
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
                this.motDePasseSalle = "";
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
                for(var i:Number=0; i < objetEvenement.listeNomUtilisateurs.length; i++)
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
                for (var i:Number = 0; i < objetEvenement.listeTables.length; i++)
                {
                    this.listeDesTables.push(objetEvenement.listeTables[i]);
					
                    str = objetEvenement.listeTables[i].no + ".  *" +  objetEvenement.listeTables[i].tablName + "*  " + objetEvenement.listeTables[i].temps + " min. " ;
					/*
                    for (var j:Number = 0; j < objetEvenement.listeTables[i].listeJoueurs.length; j++)
                    {
                        str = str + "\n -  " + this.listeDesTables[i].listeJoueurs[j].nom;
						trace(this.listeDesTables[i].listeJoueurs[j].nom);
                    }
                    str = str +  "\n  ";
					*/
					_level0.loader.contentHolder.listeTable.addItem({label : str, data : objetEvenement.listeTables[i].no});
                }
				if ( objetEvenement.listeTables.length == 0)//objetEvenement.listeTables.length == 0)
				{
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
              
                var j:Number=0;
				for(var i:Number = 0; i < numeroJoueursDansSalle-1; i++)
                {
	                if(i>3) j=1;
					if(i>7) j=2;
					if(i>11) j=3;
	                this.listeDesPersonnages.push(new Object());
                    this.listeDesPersonnages[i].nom = "Inconnu";
                    this.listeDesPersonnages[i].id = 0;
					this.listeDesPersonnages[i].role = 0;
                    /*
					var m:Number=i+2;
                    
                    movClip = _level0.loader.contentHolder.refLayer.attachMovie("Personnage0","b"+i,i);
                    movClip._x = 510-j*60;
                    movClip._y = 150 + i*60-j*240;
					movClip._xscale -= 70;
					movClip._yscale -= 70;
					*/
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
			 
            case "JoueurDansTable":
                trace("Joueur dans table");
            break;
			 
            default:
                trace("Erreur Inconnue");
        }
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
				for (var i:Number = 0; i < this.listeDesTables.length; i++)
                {
                    if(this.listeDesTables[i].no == numeroTable)
                    {
                        tempsPartie = this.listeDesTables[i].temps;
						tablName = this.listeDesTables[i].tablName;
                        break;
                    }
                }
				
                _level0.loader.contentHolder.gotoAndPlay(3);
				/*
                for(var i:Number=0; i < numeroJoueursDansSalle-1; i++)
                {
	                //_level0.loader.contentHolder.noms[i] = objetEvenement.listePersonnageJoueurs[i].nom;;
	                trace("objetEvenement"+i+" "+objetEvenement.listePersonnageJoueurs[i].nom);
                }
                */
                _level0.loader.contentHolder.nomJ4 = nomUtilisateur;								
                
                //trace("---------------- Remplir listeDesPersonnages num:"+objetEvenement.listePersonnageJoueurs.length);
                var j:Number = 0;
				for(var i:Number = 0; i < numeroJoueursDansSalle-1; i++)
                {
	                if(i>3) {j=1;}
					if(i>7) {j=2;}
					if(i>11) {j=3;}
					//var tmpNom:String  = objetEvenement.listePersonnageJoueurs[i].nom;
					this.listeDesPersonnages.push(new Object());
					this.listeDesPersonnages[i].nom = objetEvenement.listePersonnageJoueurs[i].nom;
                    this.listeDesPersonnages[i].id = objetEvenement.listePersonnageJoueurs[i].idPersonnage;
					this.listeDesPersonnages[i].role = objetEvenement.listePersonnageJoueurs[i].userRoles;
		    
                    //trace(i+" "+this.listeDesPersonnages[i].nom+" "+tmpNom+", de serveur:"+objetEvenement.listePersonnageJoueurs[i].nom);
                    
                    var nextID:Number = 0;
                    for(var k=0; k < objetEvenement.listePersonnageJoueurs.length; k++)
                    	if(objetEvenement.listePersonnageJoueurs[k].nom.substr(0,7) != "Inconnu") nextID++;
                    
                    var idDessin:Number = ((this.listeDesPersonnages[i].id-10000)-(this.listeDesPersonnages[i].id-10000)%100)/100;
					var idPers:Number = this.listeDesPersonnages[i].id-10000-idDessin*100;
					
					if(this.listeDesPersonnages[i].id == 0) idDessin = 0;
					   _level0.loader.contentHolder.tableauDesPersoChoisis.push(Number(nextID));//
		    
                    if(idDessin != 0)
					{
                       movClip = _level0.loader.contentHolder.refLayer.attachMovie("Personnage" + idDessin,"b" + i,i);
                       _level0.loader.contentHolder["joueur"+(i+1)] = objetEvenement.listePersonnageJoueurs[i].nom;
                       //_level0.loader.contentHolder["dtCadre"+i+1]["joueur"+i]=this.listeDesPersonnages[i].nom;
                       trace("idPers="+idPers+" idDessin="+idDessin+" a connecte "+i+" "+_level0.loader.contentHolder["joueur"+(i+1)]);
                       movClip._x = 510-j*60;
                       movClip._y = 150 + i*60-j*240;
					   movClip._xscale -= 70;
					   movClip._yscale -= 70;
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
				for(var i = 0; i < this.listeDesTables.length; i++)
				{
					if(this.listeDesTables[i].no == this.numeroTable)
					{
						indice = i;
						break;
					}
				}

				 _level0.loader.contentHolder.listeTable.removeAll();
				// on s'enleve de la liste des joueurs 
            	for(var j=0; j < this.listeDesTables[indice].listeJoueurs.length; j++)
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
				
				if (this.listeDesTables.length == 0 )
		        {
			      
			       _level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
				   _level0.loader.contentHolder.txtChargementTables._visible = true;
		        }
				
			 	//delete this.listeDesTables;
			 	//this.listeDesTables = new Array();
			 	//delete this.listeDesJoueursConnectes;
			 	//this.listeDesJoueursConnectes = new Array();
                // 	objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), Delegate.create(this, this.evenementJoueurConnecte), Delegate.create(this, this.evenementJoueurDeconnecte));
                this.objGestionnaireCommunication.obtenirListeJoueursSalle(Delegate.create(this, this.retourObtenirListeJoueursSalle), Delegate.create(this, this.evenementJoueurEntreSalle), Delegate.create(this, this.evenementJoueurQuitteSalle));						
			 	/*
				this.numeroTable = 0;
			 	this.idPersonnage = 0;
			 	delete this.listeDesPersonnages;
			 	this.listeDesPersonnages = new Array();
			 	_level0.loader.contentHolder.listeTable.removeAll();
			    */
			 	for (var i:Number = 0; i < this.listeDesTables.length; i++)
			 	{
					str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
					
					_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
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
                trace("Erreur Inconnue");
        }
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
            for(var i = 0; i < this.listeDesTables.length; i++)
    	    {
        	   for(var j=0; j < this.listeDesTables[i].listeJoueurs.length; j++)
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
			 for (var i:Number = 0; i < this.listeDesTables.length; i++)
			 {
				str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
				_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
			 }
					
		     if (this.listeDesTables.length == 0 )
		     {
			   _level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
			   _level0.loader.contentHolder.txtChargementTables._visible = true;
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
        trace("fin de retourSortirJoueurTable");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourDemarrerPartie(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, JoueurPasDansTable, TableNonComplete
        trace("*********************************************");
        trace("debut de retourDemarrerPartie   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "Ok":
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
    	trace("fin de retourDeconnexion");
    	trace("*********************************************\n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourDeplacerPersonnage(objetEvenement:Object)
    {
        //   objetEvenement.resultat = Question, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
		var question:MovieClip;
	
      	trace("*********************************************");
      	trace("debut de retourDeplacerpersonnage   "+objetEvenement.resultat);
      	switch(objetEvenement.resultat)
        {
            case "Question":
		     	_level0.loader.contentHolder.url_question = objetEvenement.question.url;
		     	_level0.loader.contentHolder.type_question = objetEvenement.question.type;
				_level0.loader.contentHolder.box_question.gotoAndPlay(2);

				switch(objetEvenement.question.type)
			 	{
		     		case "ChoixReponse":
						trace("type = ChoixReponse");
		     		break;
	
		     		case "VraiFaux":
						trace("type = VraiFaux");
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
		 		}
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
			 		 
            case "JoueurNonConnecte":
                trace("Joueur non connecte");
            break; 
			 
            default:
                trace("Erreur Inconnue");
        }
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
				//_level0.loader.contentHolder.planche.obtenirPerso().enleverObjet("pieceFixe");
				if(_level0.loader.contentHolder.planche.obtenirPerso().obtenirNombreObjet() <= 10)
				{
					trace("nom de l'objet : " + objetEvenement.argent.type);
					_level0.loader.contentHolder.planche.obtenirPerso().ajouterImageBanque(_level0.loader.contentHolder.planche.obtenirPerso().obtenirNombreObjet(), objetEvenement.argent.type,  _level0.loader.contentHolder.planche.obtenirPerso().obtenirNombreObjet(), 75);
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
        }
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
						trace("mauvaise reponse (livre) : " +objetEvenement.objetUtilise.mauvaiseReponse);
					
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
				
					case "OK":
						trace("banane ou potion");
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
                trace("on a le pointage total: "+objetEvenement.pointage + " Il reste a l'utiliser...");
				// modifier le pointage
				_level0.loader.contentHolder.pointageJoueur = objetEvenement.pointage;
				_level0.loader.contentHolder.planche.obtenirPerso().modifierPointage(objetEvenement.pointage);
				// il faut mettre a jour le pointage
				// qu'arrive-t-il s'il y a des delais et que le perso c'est deja deplace?
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
    	trace("fin de retourDefinirPointageApresMinigame");
    	trace("*********************************************\n");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////    
    public function retourDefinirArgentApresMinigame(objetEvenement:Object)
    {
	    //   objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte
    	trace("*********************************************");
    	trace("debut de retourDefinirArgentApresMinigame   "+objetEvenement.resultat);
    	switch(objetEvenement.resultat)
        {
            case "Argent":
                trace("on a l'argent total: "+objetEvenement.argent + " Il reste a l'utiliser...");
				// modifier l'argent
				_level0.loader.contentHolder.argentJoueur = objetEvenement.argent;
				_level0.loader.contentHolder.planche.obtenirPerso().modifierArgent(objetEvenement.argent);
				//_level0.loader.contentHolder.planche.obtenirPerso().ajouterImageBanque(_level0.loader.contentHolder.planche.obtenirPerso().obtenirNombreObjet(), "pieceFixe", _level0.loader.contentHolder.planche.obtenirPerso().obtenirNombreObjet(), 75);
				//todo corriger la ligne precedente si probleme

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
    	trace("debut de retourRepondreQuestion   "+objetEvenement.resultat);
    	trace("deplacement accepte oui ou non  :  "+objetEvenement.deplacementAccepte);   
    	trace("url explication  :  "+objetEvenement.explication);
    	trace("nouveau pointage  :  "+objetEvenement.pointage);
    	trace("nouvel argent  :  "+objetEvenement.argent);
    	trace("collision  :"+objetEvenement.collision);
      
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
					
					trace("nouvelle position (sans compter les rotations): x = " + objetEvenement.nouvellePosition.x + "; y = " +objetEvenement.nouvellePosition.y);
					_level0.loader.contentHolder.planche.obtenirPerso().definirProchainePosition(_level0.loader.contentHolder.planche.calculerPositionTourne(pt.obtenirX(), pt.obtenirY()), objetEvenement.collision);
					
					// modifier le pointage
					_level0.loader.contentHolder.planche.obtenirPerso().modifierPointage(objetEvenement.pointage);
					_level0.loader.contentHolder.planche.obtenirPerso().modifierArgent(objetEvenement.argent);
					_level0.loader.contentHolder.sortieDunMinigame = false; 
					this.moveVisibility = objetEvenement.moveVisibility;
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
			     		trace("deplacement refuse");
			     		_level0.loader.contentHolder.url_retro = objetEvenement.explication;

						_level0.loader.contentHolder.box_question.monScroll._visible = false;
						var ptX:Number = _level0.loader.contentHolder.box_question.monScroll._x;
						var ptY:Number = _level0.loader.contentHolder.box_question.monScroll._y;
						_level0.loader.contentHolder.box_question.attachMovie("GUI_retro","GUI_retro", 100, {_x:ptX, _y:ptY});
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
     	trace("fin de retourRepondreQuestion");
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
            this.objGestionnaireCommunication.connexion(Delegate.create(this, this.retourConnexion), this.nomUtilisateur, this.motDePasse);
		}
        else
        {
			this.utiliserPortSecondaire();
        }
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
        trace("fin de evenementConnexionPhysiqueTunneling");
        trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementDeconnexionPhysique(objetEvenement:Object)
    {
        trace("*********************************************");
    	trace("debut de evenementDeconnexionPhysique   ");
    	trace("fin de evenementDeconnexionPhysique");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurConnecte(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurConnecte   "+objetEvenement.nomUtilisateur);
    	trace("fin de evenementJoueurConnecte");
    	trace("*********************************************\n");
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDeconnecte(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurDeconnecte   "+objetEvenement.nomUtilisateur);
    	trace("fin de evenementJoueurDeconnecte");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurEntreSalle(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurEntreSalle   "+objetEvenement.nomUtilisateur);
    	this.listeDesJoueursDansSalle.push(objetEvenement.nomUtilisateur);
    	trace("fin de evenementJoueurEntreSalle");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurQuitteSalle(objetEvenement:Object)
    {
        // parametre: nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurQuitteSalle   " + objetEvenement.nomUtilisateur);
    	
		for(var i:Number = 0;i<this.listeDesJoueursDansSalle.length;i++)
    	{
        	if(this.listeDesJoueursDansSalle[i] == objetEvenement.nomUtilisateur)
        	{
            	this.listeDesJoueursDansSalle.splice(i,1);
           		trace("un joueur enlever de la liste :   "+objetEvenement.nomUtilisateur);
            	break;
        	}
        	
    	}
		
		for(i = 0; i < this.listeDesTables.length; i++)
    	{
        	for(var j=0; j < this.listeDesTables[i].listeJoueurs.length; j++)
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
		for (var i:Number = 0; i < this.listeDesTables.length; i++)
	    {
			str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
			_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
		}
    	
		
		if (this.listeDesTables.length == 0)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
		
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
        /*
		for (var j:Number = 0; j < this.listeDesTables[this.listeDesTables.length-1].listeJoueurs.length; j++)
        {
            str = str + "\n -  " + this.listeDesTables[this.listeDesTables.length-1].listeJoueurs[j].nom;
			trace(" xxx " + this.listeDesTables[this.listeDesTables.length-1].listeJoueurs[j].nom);
        }
		*/
				
		  _level0.loader.contentHolder.listeTable.removeAll();
			 for (var i:Number = 0; i < this.listeDesTables.length; i++)
			 {
				str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
				_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
			 }
		//_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[this.listeDesTables.length-1].no});
        
		_level0.loader.contentHolder.chargementTables = "";
		
		for(var i:Number = 0; i < numeroJoueursDansSalle; i++)
                {
					trace(i + ": "+this.listeDesPersonnages[i].nom+" id:"+this.listeDesPersonnages[i].id);
				}
    	trace("fin de evenementNouvelleTable");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementTableDetruite(objetEvenement:Object)
    {
    	trace("*********************************************");
    	trace("debut de evenementTableDetruite   ");
    	
		var i:Number;
    	for(i = 0; i < this.listeDesTables.length; i++)
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
		for (var i:Number = 0; i < this.listeDesTables.length; i++)
		{
			str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
			_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
		}
				
		if (this.listeDesTables.length == 0)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
								
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
	
    	for(i = 0; i < this.listeDesTables.length; i++)
    	{
        	if(this.listeDesTables[i].no == objetEvenement.noTable)
        	{
            	this.listeDesTables[i].listeJoueurs.push(new Object());
            	this.listeDesTables[i].listeJoueurs[this.listeDesTables[i].listeJoueurs.length - 1].nom = objetEvenement.nomUtilisateur;
            	indice = i;
            	break;
        	}
    	}
    	
		if(objetEvenement.noTable == this.numeroTable)
    	{
	    	var alreadyWas:Boolean = false;
	    	
            for(i = numeroJoueursDansSalle-1; i >=0 ; i--)//nbmaxJoueurs
            {
	            var itIsInconnu:Boolean = (listeDesPersonnages[i].nom.substr(0,7)=="Inconnu");
	            
            	
                //if(listeDesPersonnages[i].nom == "Inconnu" || listeDesPersonnages[i].nom == "Inconnu1" || listeDesPersonnages[i].nom == "Inconnu2" || listeDesPersonnages[i].nom == "Inconnu3")
				if(itIsInconnu && (!alreadyWas))
                 {
                    listeDesPersonnages[i].nom = objetEvenement.nomUtilisateur;
					listeDesPersonnages[i].role = objetEvenement.userRole;
					_level0.loader.contentHolder["joueur"+(i+1)] = listeDesPersonnages[i].nom;
                    alreadyWas = true;
                } else
                {
	                
                	//_level0.loader.contentHolder["dtCadre"+i+1]["joueur"+i]=_level0.loader.contentHolder.noms[i] = listeDesPersonnages[i].nom;
                	//trace("_level0.loader.contentHolder[\"dtCadre\"+i+1][\"joueur\"+i] "+_level0.loader.contentHolder["dtCadre"+i+1]["joueur"+i]);
            	}
            	
            }// for
            
    	}// if
		
    	if(indice != -1)
    	{
			/*
        	for(i = 0; i < this.listeDesTables.length; i++)
        	{
            	if(_level0.loader.contentHolder.listeTable.getItemAt(i).data == objetEvenement.noTable)
            	{
                	str =  _level0.loader.contentHolder.listeTable.getItemAt(i).label; // "Table. " + this.listeDesTables[indice].no + "  " + this.listeDesTables[indice].temps + " min.";
                	//for (j= 0; j < this.listeDesTables[indice].listeJoueurs.length; j++)
                	//{
                    	str = str + " - " + this.listeDesTables[indice].listeJoueurs[this.listeDesTables[i].listeJoueurs.length - 1].nom + "\n   ";
                	//}
					trace(str);
                	_level0.loader.contentHolder.listeTable.replaceItemAt(i, {label : str, data : objetEvenement.noTable});
            	}
        	} */
        	// enlever la table de la liste si elle est pleine
			
        	if(this.listeDesTables[indice].listeJoueurs.length == numeroJoueursDansSalle)
        	{
            	for(i = 0; i < this.listeDesTables.length; i++)
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
			 for (var i:Number = 0; i < this.listeDesTables.length; i++)
			 {
				str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
				_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
			 }
		
		if (this.listeDesTables.length == 0)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
    	
		for(var i:Number = 0; i < numeroJoueursDansSalle; i++)
                {
					trace(i+"this.listeDesPersonnages[i].nom: "+this.listeDesPersonnages[i].nom+" id:"+this.listeDesPersonnages[i].id);
				}
    	trace("fin de evenementJoueurEntreTable");
    	trace("*********************************************\n");
    }
	
    //  est-ce qu'on recoit cet eve si on quitte notre table ?????????  NON NON NON
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurQuitteTable(objetEvenement:Object)
    {
        // parametre: noTable, nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurQuitteTable   "+objetEvenement.noTable+"    "+objetEvenement.nomUtilisateur);
    	var indice:Number = -1;
    	var i:Number;
    	var j:Number;
    	var tableAffichee:Boolean = false;
    	var str:String = new String();
    	for(i = 0;i<this.listeDesTables.length;i++)
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
	    	var alreadyWas:Boolean=false;
	    	
        	for(i = 0; i < numeroJoueursDansSalle; i++)
        	{
	        	var itIsMe:Boolean=(listeDesPersonnages[i].nom == objetEvenement.nomUtilisateur);
            	//  on enleve le nom du joueur dans la liste et a l'ecran
            	if(itIsMe && (!alreadyWas))
            	{
                	listeDesPersonnages[i].nom = "Inconnu"+i;
                	_level0.loader.contentHolder.noms[i] = "Inconnu";
                	alreadyWas=true;
                	break;
            	}
            	
            		
        	}
        	// oupsss, on dirait qu'il est impossible d'aller changer l'image du perso...
        	// est-ce que c'est necessaire ?
    	}
    	// si ce n'est pas notre table
    	else
    	{
        	// si la table existe
        	if(indice != -1)
        	{
            	// on enleve le joueur de la liste des joueurs de la table en question
            	for(j = 0; j < this.listeDesTables[indice].listeJoueurs.length; j++)
            	{
                	if(this.listeDesTables[indice].listeJoueurs[j].nom == objetEvenement.nomUtilisateur)
                	{
                    	this.listeDesTables[indice].listeJoueurs.splice(j,1);
                    	break;
                	}
            	}
            	
				/*// on modifie la liste d'affichage des joueurs
            	for(i = 0; i < _level0.loader.contentHolder.listeTable.length; i++)
            	{
                	if(_level0.loader.contentHolder.listeTable.getItemAt(i).data == objetEvenement.noTable)
                	{
						
                    	//tableAffichee == true;  // la table etait au prealable affichee
                    	// si la table contient encore des joueurs
                    	if(this.listeDesTables[indice].listeJoueurs.length != 0)
                    	{
                        	str = "Table  " + this.listeDesTables[indice].no + "   " + this.listeDesTables[indice].temps + " min.";
                        	for (j= 0; j < this.listeDesTables[indice].listeJoueurs.length; j++)
                        	{
                            	str = str + "\n   - " + this.listeDesTables[indice].listeJoueurs[j].nom;
								
                        	}
							str = str +  "\n  ";
                        	_level0.loader.contentHolder.listeTable.replaceItemAt(i, {label : str, data : objetEvenement.noTable});
                        	break;
                    	}
                    	// si la table est vide on ne l'affiche plus
                    	else
                    	{
                        	_level0.loader.contentHolder.listeTable.removeItemAt(i);
                        	break;
                    	} 
                	}
            	}
	    
       		    // si la table n'etait pas affichee, on l'affiche
            	if(tableAffichee == false)
            	{
                	str = "Table  " + this.listeDesTables[indice].no + "  " + this.listeDesTables[indice].temps + " min.";
                	for (j= 0; j < this.listeDesTables[indice].listeJoueurs.length; j++)
                	{
                    	str = str + "\n  - " + this.listeDesTables[indice].listeJoueurs[j].nom;
                	}
					str = str +  "\n  ";
                	_level0.loader.contentHolder.listeTable.replaceItemAt(i, {label : str, data : objetEvenement.noTable});
            	}	*/    
        	}
    	}
		  _level0.loader.contentHolder.listeTable.removeAll();
			 for (var i:Number = 0; i < this.listeDesTables.length; i++)
			 {
				str = this.listeDesTables[i].no + ".  *" +  this.listeDesTables[i].tablName + "*  " + this.listeDesTables[i].temps + " min. " ;
				_level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
			 }
    	for(var i:Number = 0; i < numeroJoueursDansSalle; i++)
                {
					trace(i+": "+this.listeDesPersonnages[i].nom+" id:"+this.listeDesPersonnages[i].id);
				}
    				
		if(this.listeDesTables.length == 0)
		{
			_level0.loader.contentHolder.txtChargementTables._visible = true;
			_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
		}
		
		
		trace("fin de evenementJoueurQuitteTable");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementMessage(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, message
    	trace("*********************************************");
    	trace("debut de evenementMessage   "+objetEvenement.message+"    "+objetEvenement.nomUtilisateur);
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
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
	public function evenementPartieDemarree(objetEvenement:Object)
    {
        // parametre: plateauJeu, positionJoueurs (nom, x, y), tempsPartie
        trace("*********************************************");
        trace("debut de evenementPartieDemarree   "+objetEvenement.tempsPartie+"   "+getTimer());
        var i:Number;
        var j:Number;
        _level0.loader.contentHolder["att"].removeMovieClip();
        gotoAndPlay(4);
        for(i = 0; i < objetEvenement.plateauJeu[0].length; i++)
        {
            _level0.loader.contentHolder.tab.push(new Array());
            for(j = 0; j < objetEvenement.plateauJeu.length; j++)
            {
                _level0.loader.contentHolder.tab[i][j] = objetEvenement.plateauJeu[i][j];
				//trace(" _level0.loader.contentHolder.tab[i][j] : " + objetEvenement.plateauJeu[i][j]);
            }
        }

		// ici on initie les noms et pointages des adversaire (dans le panneau qui descend)
		// et on met la face de leur avatar a cote de leur nom
		// Initialise our opponents' name and score
		// and put the face of our opponents' avatar in the panel (next to their name)
 		
		j=0;
		for(i=0;i<numeroJoueursDansSalle-1;i++)
		{
       				
			if((undefined != this.listeDesPersonnages[i].nom) && ("Inconnu" != this.listeDesPersonnages[i].nom) && !(this.listeDesPersonnages[i].role == 2 && this.typeDeJeu == "Tournament")){
				
				
				_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(j+1)]["nomJoueur"+(j+1)] = this.listeDesPersonnages[i].nom;
				_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(j+1)]["pointageJoueur"+(j+1)] = 0;
				_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(j+1)]._visible=true;
				var idDessin:Number =((this.listeDesPersonnages[i].id-10000)-(this.listeDesPersonnages[i].id-10000)%100)/100;
				var idPers:Number = this.listeDesPersonnages[i].id-10000-idDessin*100;
				_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(j+1)].idStart=idDessin;//this.listeDesPersonnages[i].id;
				_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(j+1)].idPers=idPers;
			
				trace(i+" nom:"+this.listeDesPersonnages[i].nom + " id:" + idPers);//this.listeDesPersonnages[i].id);
				this.listeDesPersonnages[i].numPointage=j;
				this["tete"+j]=new MovieClip();
				this["tete"+j] = _level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(j+1)]["tete"+(j+1)].attachMovie("tete"+idDessin/*this.listeDesPersonnages[i].id*/, "Tete"+j, -10100+j);
				this["tete"+j]._x = -7;
				this["tete"+j]._y = -6;
				this["tete"+j]._xscale = 55;
				this["tete"+j]._yscale = 55;
				j++;
			}
			

		}
		for(i=j;i<12;i++)
		{
			
				_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(i+1)]["nomJoueur"+(i+1)] = undefined;
				_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(i+1)]["pointageJoueur"+(i+1)] = -1;
		 	
		}
		// put the face of my avatar in the panel (next to my name)
		_level0.loader.contentHolder.myObj = new Object();
		var idDessin:Number=((this.listeDesPersonnages[numeroJoueursDansSalle-1].id-10000)-(this.listeDesPersonnages[numeroJoueursDansSalle-1].id-10000)%100)/100;
		var idPers:Number=this.listeDesPersonnages[numeroJoueursDansSalle-1].id-10000-idDessin*100;
		_level0.loader.contentHolder.myObj.myID = idDessin;
		_level0.loader.contentHolder.myObj.myIDPers = idPers;
		_level0.loader.contentHolder.myObj.myNom = this.listeDesPersonnages[numeroJoueursDansSalle-1].nom;
		
		var maTete:MovieClip = _level0.loader.contentHolder.maTete.attachMovie("tete"+idDessin/*this.listeDesPersonnages[numeroJoueursDansSalle-1].id*/, "maTete", -10099);
		maTete._x = -7;
		maTete._y = -6;
		maTete._xscale = 55;
		maTete._yscale = 55;
		
		
        for(i = 0; i < objetEvenement.positionJoueurs.length; i++)//4 //nbmaxJoueurs// numeroJoueursDansSalle
        {
            if(this.listeDesPersonnages[numeroJoueursDansSalle-1].nom == objetEvenement.positionJoueurs[i].nom)
            {
	            //trace(this.listeDesPersonnages[numeroJoueursDansSalle-1].nom+" N"+i+" starts game");
	            var idDessin:Number=((this.listeDesPersonnages[numeroJoueursDansSalle-1].id-10000)-(this.listeDesPersonnages[numeroJoueursDansSalle-1].id-10000)%100)/100;
				var idPers:Number=this.listeDesPersonnages[numeroJoueursDansSalle-1].id-10000-idDessin*100;
				
                _level0.loader.contentHolder.planche = new PlancheDeJeu(objetEvenement.plateauJeu, this.listeDesPersonnages[numeroJoueursDansSalle-1].id, _level0.loader.contentHolder.gestionnaireInterface);
            }
        }
        _level0.loader.contentHolder.planche.afficher();
        //trace("longueur de la liste des noms envoyes par serveur    :"+objetEvenement.positionJoueurs.length);
        for(i = 0; i < objetEvenement.positionJoueurs.length; i++)
        {
            for(j = 0; j < numeroJoueursDansSalle; j++)
            {
	            //trace(this.listeDesPersonnages[j].nom+" : "+objetEvenement.positionJoueurs[i].nom);
                if(this.listeDesPersonnages[j].nom == objetEvenement.positionJoueurs[i].nom)
                {
	                var idDessin:Number=((this.listeDesPersonnages[j].id-10000)-(this.listeDesPersonnages[j].id-10000)%100)/100;
					var idPers:Number=this.listeDesPersonnages[j].id-10000-idDessin*100;
					if(idDessin<0) idDessin=12;
					//if(idPers<0) idPers=-idPers;
					
                    _level0.loader.contentHolder.planche.ajouterPersonnage(this.listeDesPersonnages[j].nom, objetEvenement.positionJoueurs[i].x, objetEvenement.positionJoueurs[i].y, idPers, idDessin, this.listeDesPersonnages[j].role);//this.listeDesPersonnages[j].id);
		    		trace("Construction du personnage : " + this.listeDesPersonnages[j].nom + " " + objetEvenement.positionJoueurs[i].x + " " + objetEvenement.positionJoueurs[i].y + " idDessin:" + idDessin + " idPers:" + idPers);//this.listeDesPersonnages[j].id);
				}
            }
        }
        //_level0.loader.contentHolder.planche.afficher();
        _level0.loader.contentHolder.horlogeNum = 60*objetEvenement.tempsPartie;

        trace("fin de evenementPartieDemarree    "+getTimer());
        trace("*********************************************\n");
    }  
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDemarrePartie(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, idPersonnage
     	trace("*********************************************");
     	trace("debut de evenementJoueurDemarrePartie   "+objetEvenement.nomUtilisateur+"     "+objetEvenement.idPersonnage);
        var movClip:MovieClip;
        var j:Number=0;
        for (var i:Number = 0; i < numeroJoueursDansSalle-1; i++)
        {
	        
			if(i>3) j=1;
			if(i>7) j=2;
			if(i>11) j=3;
        	if(listeDesPersonnages[i].nom == objetEvenement.nomUtilisateur)
        	{
            	this.listeDesPersonnages[i].id = objetEvenement.idPersonnage;
            	_level0.loader.contentHolder.refLayer["b"+i].removeMovieClip();
            	var idDessin:Number=((this.listeDesPersonnages[i].id-10000)-(this.listeDesPersonnages[i].id-10000)%100)/100;
				var idPers:Number=this.listeDesPersonnages[i].id-10000-idDessin*100;
            	movClip = _level0.loader.contentHolder.refLayer.attachMovie("Personnage"+idDessin,"b"+idPers,100*i);
            	movClip._x = 510-j*60;
                movClip._y = 150 + i*60-j*240;
				movClip._xscale -= 70;
				movClip._yscale -= 70;
				
		
	    		_level0.loader.contentHolder.tableauDesPersoChoisis.push(Number(objetEvenement.idPersonnage));
	    
            	break;
        	}
        	
        }
        /*for(var i:Number = 0; i < numeroJoueursDansSalle; i++)
                {
					trace(i+": "+this.listeDesPersonnages[i].nom+" id:"+this.listeDesPersonnages[i].id);
				}*/
    	trace("fin de evenementJoueurDemarrePartie");
    	trace("*********************************************\n");
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementPartieTerminee(objetEvenement:Object)
    {
        // parametre: 
    	trace("*********************************************");
    	trace("debut de evenementPartieTerminee   " + objetEvenement.statistiqueJoueur);
   
        var i,j:Number; 
		var k:Number = 0;
		var indice:Number = 0;  // indice du plus grand
		var nomMax:Number = -1;
		
		for(i = 0; i < objetEvenement.statistiqueJoueur.length; i++)
			trace(i+" joueur objetEvenement "+objetEvenement.statistiqueJoueur[i].nomUtilisateur+"   "+objetEvenement.statistiqueJoueur[i].pointage);
		    	
    	var nomK:String;
    	var tabOrdonne:Array = new Array();
    	var taille:Number = objetEvenement.statistiqueJoueur.length;
		       
		// trouver une facon de faire fonctionner ces lignes :
		_root.vrai_txt.removeTextField();
		_root.faux_txt.removeTextField();
		_root.reponse_txt.removeTextField();
		_root.penalite_txt.removeTextField();
		_root.secondes_txt.removeTextField();
		
			
		//var numeroJoueursConnecte:Number=0;
		// jouersStarted est liste de nom de joueurs et leurs pointage et IDs 
		var jouersStarted:Array =new Array();
		
		for (i = 0; i < taille; i++) {
					
			jouersStarted[i] = new Object();
			jouersStarted[i].nomUtilisateur = objetEvenement.statistiqueJoueur[i].nomUtilisateur;
			jouersStarted[i].pointage = objetEvenement.statistiqueJoueur[i].pointage;
			jouersStarted[i].role = objetEvenement.statistiqueJoueur[i].userRole;
						
		}// end for
		   	
    	//ranger les joueurs en dependant des pointages
    	for(k = 0; k < jouersStarted.length; k++)
    	{
	    	for(i = 0; i < jouersStarted.length; i++)
	    	{
		    	
				if(String(jouersStarted[i].pointage) == "Gagnant" || String(jouersStarted[i].pointage) == "Winner")
		    	{
			    	nomMax = 9999;
			    	indice = i;
		    	}
		 
		 		if(Number(jouersStarted[i].pointage) > nomMax)
		    	{
			    	nomMax = jouersStarted[i].pointage;
			    	indice = i;
		    	}
	    	}
	    
	    	tabOrdonne[k] = new Object();
	    	tabOrdonne[k].nom = jouersStarted[indice].nomUtilisateur;
	    	tabOrdonne[k].pointage = jouersStarted[indice].pointage;
	    	tabOrdonne[k].role = jouersStarted[indice].role;
	   
	   		nomMax = -1;
	    	jouersStarted[indice].pointage = -1;
	    	indice = 0;
    	}
		
		// to find the picture
		for (i=0; i < 12; i++) {
												
			for(k = 0; k < jouersStarted.length; k++){
				
				if(listeDesPersonnages[i].nom == tabOrdonne[k].nom)
					tabOrdonne[k].id = ((this.listeDesPersonnages[i].id-10000)-(this.listeDesPersonnages[i].id-10000)%100)/100;
			
			}	
		} // end find the picture
		
		
    	    	
  		_level0.loader.contentHolder.miniGameLayer["magasin"].removeMovieClip();
    	_level0.loader.contentHolder["boutonFermer"].removeMovieClip();
		_level0.loader.contentHolder["banane"].removeMovieClip();
		_level0.loader.contentHolder["bananeUser"].removeMovieClip();
		_level0.loader.contentHolder["GUI_utiliserObjet"].removeMovieClip();
		_level0.loader.contentHolder["box_question"].removeMovieClip();
		_level0.loader.contentHolder["fond_MiniGame"]._y += 400;
		
		//s'assurer que la musique s'arrete en fin de partie
		_level0.loader.contentHolder.musique.stop();
		_level0.loader.contentHolder.musiqueDefault.stop();
	
    	_level0.loader.contentHolder.gotoAndStop(5);
    
    	Mouse.show();
 
  	
		// mettre les id en ordre : tabOrdonne.id contient les id des personnages en ordre de pointage
		// il suffit de mettre les MC correspondants sur le podium
		
		// to cut the holes ...
		for(i = 0; i < jouersStarted.length; i++){
		   if((tabOrdonne[i].role == 2 && this.typeDeJeu == "Tournament"))   
		      tabOrdonne.removeItemAt(i);
		}
		
		// demonstrate the result
		for(i = 0; i < jouersStarted.length; i++){
					
			_level0.loader.contentHolder["nom"+(i+1)] = tabOrdonne[i].nom;	
			_level0.loader.contentHolder["pointage"+(i+1)] = tabOrdonne[i].pointage;
			this.tabPodiumOrdonneID[i] =  Number(tabOrdonne[i].id);
			
			//trace(i+" "+tabOrdonne[i].nom+" = "+tabOrdonne[i].pointage+" pts, id:"+tabOrdonne[i].role);
    	}
    	        	    
    	trace("fin de evenementPartieTerminee    ");
    	trace("*********************************************\n");
    }  
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurDeplacePersonnage(objetEvenement:Object)
    {
        // parametre: nomUtilisateur, anciennePosition et nouvellePosition, pointage
    	trace("*********************************************");
    	trace("debut de evenementJoueurDeplacePersonnage (sans compter les rotations)  "+objetEvenement.nomUtilisateur+"   "+objetEvenement.anciennePosition.x+"   "+objetEvenement.anciennePosition.y+"   "+objetEvenement.nouvellePosition.x+"   "+objetEvenement.nouvellePosition.y+"   "+objetEvenement.collision+"   "+objetEvenement.pointage+"   "+objetEvenement.argent);
   
    	var pt_initial:Point = new Point();
    	var pt_final:Point = new Point();
     
    	pt_initial = _level0.loader.contentHolder.planche.calculerPositionTourne(objetEvenement.anciennePosition.x, objetEvenement.anciennePosition.y);
     
    	pt_final = _level0.loader.contentHolder.planche.calculerPositionTourne(objetEvenement.nouvellePosition.x, objetEvenement.nouvellePosition.y);
   
		trace("juste avant la teleportation   nom du perso et param  ");
		_level0.loader.contentHolder.planche.teleporterPersonnage(objetEvenement.nomUtilisateur, pt_initial.obtenirX(), pt_initial.obtenirY(), pt_final.obtenirX(), pt_final.obtenirY(), objetEvenement.collision);
	
		for(var i:Number=0;i<numeroJoueursDansSalle;i++)
			if(_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(i+1)]["nomJoueur"+(i+1)] == objetEvenement.nomUtilisateur)//nbmaxJoueurs
		
				_level0.loader.contentHolder.menuPointages.mc_autresJoueurs["mc_joueur"+(i+1)]["pointageJoueur"+(i+1)] = objetEvenement.pointage;
			//_level0.loader.contentHolder.argentAdversaire1 = objetEvenement.argent;
		
		
     	trace("fin de evenementJoueurDeplacePersonnage");
     	trace("*********************************************\n");
    }   
    
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
}
