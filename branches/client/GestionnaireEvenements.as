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
	
	private var nomUtilisateur:String;    // notre nom d'utilisateur
	private var numeroDuPersonnage:Number; // sert a associer la bonne image pour le jeu d'ile au tresor
	private var numberDesJoueurs:Number;
    private var listeDesPersonnages:Array;   // liste associant les idPersonnage avec les nomUtilisateurs dans la table ou on est
    private var motDePasse:String;  // notre mot de passe pour pouvoir jouer
    private var nomSalle:String;  //  nom de la salle dans laquelle on est
    private var numeroTable:Number;   //   numero de la table dans laquelle on est
    private var tempsPartie:Number;   //  temps que va durer la partie, en minutes
    private var idPersonnage:Number;   //  le idPersonnage que nous avons choisi (le dessin)
    private var motDePasseSalle:String;   // le mot de passe de la salle dans laquelle on est
    private var listeDesJoueursDansSalle:Array;  // liste des joueurs dans la salle qu'on est. Un joueur contient un nom (nom)
    private var listeDesDescriptionsSalles:Array; //liste des descriptions des salles 
	private var activ:Boolean;                     // if game has type Tournament and the room is active
	private var listeDesSalles:Array;    //  liste de toutes les salles
	private var listeNumeroJoueursSalles:Array;		//liste de numero de joueurs dans chaque salle
	private var listeChansons:Array;    //  liste de toutes les chansons
    private var listeDesJoueursConnectes:Array;   // la premiere liste qu'on recoit, tous les joueurs dans toutes les salles. Un joueur contient un nom (nom)
    //  liste de toutes les tables dans la salle ou on est
    //contient un numero (noTable), le temps (temps) et une liste de joueurs (listeJoueurs) un joueur de la liste contient un nom (nom)
    private var listeDesTables:Array;
    private var objGestionnaireCommunication:GestionnaireCommunication;  //  pour caller les fonctions du serveur 
	private var tabPodiumOrdonneID:Array;			// id des personnages ordonnes par pointage une fois la partie terminee
	private var pointageMinimalWinTheGame:Number = -1 // pointage minimal a avoir droit d'atteindre le WinTheGame
	private var numeroJoueursDansSalle:Number=0;
	
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
        trace("debut du constructeur de gesEve      "+nom+"      "+passe);
        this.nomUtilisateur = nom;
        this.listeDesPersonnages = new Array();
        this.listeDesPersonnages.push(new Object());
        /*for(var i:Number = 0; i < 4; i++)
        {
            this.listeDesPersonnages.push(new Object());
        }*/
        this.motDePasse = passe;
        this.nomSalle = new String();
        this.motDePasseSalle = new String();
        this.listeDesSalles = new Array();
		this.listeDesDescriptionsSalles = new Array();
		this.listeNumeroJoueursSalles = new Array();
        this.listeDesTables = new Array();
		this.listeChansons = new Array();
        this.listeDesJoueursConnectes = new Array();
        this.listeDesJoueursDansSalle = new Array();
		this.tabPodiumOrdonneID = new Array();
		
		var url_serveur:String = _level0.configxml_mainnode.attributes.url_server;
		var port:Number = parseInt(_level0.configxml_mainnode.attributes.port, 10);
		
        this.objGestionnaireCommunication = new GestionnaireCommunication(Delegate.create(this, this.evenementConnexionPhysique), Delegate.create(this, this.evenementDeconnexionPhysique), url_serveur, port);
	
    	trace("fin du constructeur de gesEve");
    	trace("*********************************************\n");
    }
	
	function obtenirNumeroJoueurs():Number ///
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
    function entrerSalle(nSalle:String)
    {
        trace("*********************************************");
        trace("debut de entrerSalle      "+nSalle);
        this.nomSalle = nSalle;
        
        for(var i:Number = 0; i < listeDesSalles.length; i++)
        {
            if(listeDesSalles[i].nom == nSalle)
            {
	            //trace("joueurs dans salle : "+numeroJoueursDansSalle+" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	            numeroJoueursDansSalle=listeNumeroJoueursSalles[i].maxnbplayers;
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
        trace("debut de entrerTable     :"+nTable);
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
    function creerTable(temps:Number)
    {
        trace("*********************************************");
        trace("debut de creerTable     "+temps);
        this.objGestionnaireCommunication.creerTable(Delegate.create(this, this.retourCreerTable), Delegate.create(this, this.evenementJoueurDemarrePartie), temps);
        trace("fin de creerTable");
        trace("*********************************************\n");
    }
	
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function demarrerPartie(no:Number)
    {
        trace("*********************************************");
        trace("debut de demarrerPartie     "+no);
	
        this.idPersonnage = no;
        this.listeDesPersonnages[listeDesPersonnages.length-1].id = no;//3].id = no; //
        this.listeDesPersonnages[listeDesPersonnages.length-1].nom = this.nomUtilisateur;
        this.objGestionnaireCommunication.demarrerPartie(Delegate.create(this, this.retourDemarrerPartie), Delegate.create(this, this.evenementPartieDemarree), Delegate.create(this, this.evenementJoueurDeplacePersonnage), Delegate.create(this, this.evenementSynchroniserTemps), Delegate.create(this, this.evenementPartieTerminee), no);  
	
		trace("fin de demarrerPartie");
        trace("*********************************************\n");
    }
	
	/////////////////////////////////////////////////////////////////////////////////////////////////// 
    function demarrerMaintenant(niveau:String)
    {
        trace("*********************************************");
        trace("debut de demarrerMaintenant");
		trace("idPersonnage: "+ this.idPersonnage);
		trace("niveau des personnages virtuels : "+ niveau);
        this.objGestionnaireCommunication.demarrerMaintenant(Delegate.create(this, this.retourDemarrerMaintenant), this.idPersonnage, niveau);
        trace("fin de demarrerMaintenant");
        trace("*********************************************\n");
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    function deplacerPersonnage(pt:Point)
    {
        trace("*********************************************");
        trace("debut de deplacerPersonnage     "+pt.obtenirX()+"     "+pt.obtenirY());
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
    function repondreQuestion(str:String)
    {
        trace("*********************************************");
        trace("debut de repondreQuestion     "+str);
        this.objGestionnaireCommunication.repondreQuestion(Delegate.create(this, this.retourRepondreQuestion), str);  
        trace("fin de repondreQuestion");
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
    	trace("debut de retourConnexion     "+objetEvenement.resultat);
    
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
				//trace(objetEvenement.listeChansons);
				
				for(var k:Number = 0;  k< objetEvenement.listeChansons.length; k++)
				{
					this.listeChansons.push(objetEvenement.listeChansons[k]);
				}
				
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
        trace("debut de retourObtenirListeJoueurs   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {  
	        
            case "ListeJoueurs":
            	this.numberDesJoueurs=objetEvenement.listeNomUtilisateurs.length;
        		this.numeroJoueursDansSalle=objetEvenement.listeNomUtilisateurs.length;
        		
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
                for (var i:Number = 0; i < objetEvenement.listeNomSalles.length; i++)
                {
					// ce if est temporaire - ou devrait etre ameliore eventuellement
					// en anglais, il n'y a pas de salle Accromath
					/*trace("bbb " + objetEvenement.listeNomSalles[i].nom);
					if(_level0.loader.contentHolder.langue == "en" && objetEvenement.listeNomSalles[i].nom == "Accromath")
					{
						trace("ccc " + objetEvenement.listeNomSalles[i].nom);
					}
					else
					{
						trace("ddd " + objetEvenement.listeNomSalles[i].nom);
						
						this.listeDesSalles.push(objetEvenement.listeNomSalles[i]);
						_level0.loader.contentHolder.listeSalle.addItem(this.listeDesSalles[i].nom);
					}
					trace("salle " + i + " : " + this.listeDesSalles[i].nom);
					*/
					this.listeDesSalles.push(objetEvenement.listeNomSalles[i]);
					_level0.loader.contentHolder.listeSalle.addItem(this.listeDesSalles[i].nom );
					trace("salle " + i + " : " + this.listeDesSalles[i].nom);
					
					this.listeDesDescriptionsSalles.push(objetEvenement.listeDescrSalles[i]);
					_level0.loader.contentHolder.listeDescr.push(this.listeDesDescriptionsSalles[i].descriptions );
					trace("salle " + i + " : " + this.listeDesDescriptionsSalles[i].descriptions );
					
					this.listeNumeroJoueursSalles.push(objetEvenement.listeNumberoJSalles[i]);
					_level0.loader.contentHolder.listeNumeroJSalles.push(this.listeNumeroJoueursSalles[i].maxnbplayers );
					trace("salle " + i + " : " + objetEvenement.listeNumberoJSalles[i].maxnbplayers);
					
				}
				for (var i:Number = 0; i < objetEvenement.listeNomSalles.length; i++)
                {
					if(_level0.loader.contentHolder.langue == "en" && objetEvenement.listeNomSalles[i].nom == "Accromath")
					{
						_level0.loader.contentHolder.listeSalle.removeItemAt(i);
						trace("salle enlevee --> " + i + " : " + this.listeDesSalles[i].nom);
					}
				}
				activ = objetEvenement.isActiveRoom;
				
				_level0.loader.contentHolder.isActiv = objetEvenement.isActiveRoom;
				trace("salle active : " + _level0.loader.contentHolder.isActiv);
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
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourEntrerSalle(objetEvenement:Object)
    {
        //objetEvenement.resultat = Ok, CommandeNonReconnue, ParametrePasBon, JoueurNonConnecte, MauvaisMotDePasseSalle, SalleNonExistante, JoueurDansSalle
        trace("*********************************************");
        trace("debut de retourEntrerSalle   "+objetEvenement.resultat);
        switch(objetEvenement.resultat)
        {
            case "Ok":
                this.objGestionnaireCommunication.obtenirListeJoueursSalle(Delegate.create(this, this.retourObtenirListeJoueursSalle), Delegate.create(this, this.evenementJoueurEntreSalle), Delegate.create(this, this.evenementJoueurQuitteSalle))
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
                for(var i:Number=0;i<objetEvenement.listeNomUtilisateurs.length;i++)
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
                for (var i:Number = 0; i < objetEvenement.listeTables.length; i++)
                {
                    this.listeDesTables.push(objetEvenement.listeTables[i]);
                    str = "Table  "+this.listeDesTables[i].no+"     "+this.listeDesTables[i].temps+" min.";
                    for (var j:Number = 0; j < this.listeDesTables[i].listeJoueurs.length; j++)
                    {
                        str = str+"\n   - "+this.listeDesTables[i].listeJoueurs[j].nom
                    }
                    _level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[i].no});
                }
				if (objetEvenement.listeTables.length == 0)
				{
					_level0.loader.contentHolder.chargementTables = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.aucuneTable;
				}
				else
				{
					_level0.loader.contentHolder.chargementTables = "";
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
        // parametre : noTable
        trace("*********************************************");
        trace("debut de retourCreerTable   "+objetEvenement.resultat +"    "+objetEvenement.noTable);
        var movClip:MovieClip;
        trace("Creer table >>>>>>>>>>>>>>> numeroJoueursDansSalle: "+numeroJoueursDansSalle);
        switch(objetEvenement.resultat)
        {
            case "NoTable":
                this.numeroTable = objetEvenement.noTable;
                _level0.loader.contentHolder.gotoAndPlay(3);
                _level0.loader.contentHolder.noms=new Array();
                for(var i:Number=0; i<numeroJoueursDansSalle-1; i++)
                {
	                _level0.loader.contentHolder.noms[i] = _root.joueurInconnu;
                }
                _level0.loader.contentHolder.noms[numeroJoueursDansSalle-1] = this.nomUtilisateur;
                
				_level0.loader.contentHolder.nomJ1 = _root.joueurInconnu;// 4 joueurs
                _level0.loader.contentHolder.nomJ2 = _root.joueurInconnu;// 4 joueurs
                _level0.loader.contentHolder.nomJ3 = _root.joueurInconnu;// 4 joueurs
                _level0.loader.contentHolder.nomJ4 = this.nomUtilisateur;// 4 joueurs

				for(var i:Number = 0; i < numeroJoueursDansSalle-1; i++)
                {
	                this.listeDesPersonnages.push(new Object());
                    this.listeDesPersonnages[i].nom = "Inconnu";
                    this.listeDesPersonnages[i].id = 0;
                    movClip = _level0.loader.contentHolder.refLayer.attachMovie("Personnage0","b"+i,i);
                    movClip._x = 450;
                    movClip._y = 150 + i*60;
					movClip._xscale -= 70;
					movClip._yscale -= 70;
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
	
	
    //  on ne s'ajoute pas a la liste des joueur dans cette table, c grave ??  c correct pour quand on veut sortir....
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function retourEntrerTable(objetEvenement:Object)
    {
        //   objetEvenement.resultat = ListePersonnageJoueurs, CommandeNonReconnue,  ParametrePasBon, JoueurNonConnecte, JoueurPasDansSalle, TableNonExistante, TableComplete
        trace("*********************************************");
        trace("debut de retourEntrerTable   "+objetEvenement.resultat);
        var movClip:MovieClip;
        switch(objetEvenement.resultat)
        {
            case "ListePersonnageJoueurs":
				for (var i:Number = 0; i < this.listeDesTables.length; i++)
                {
                    if(this.listeDesTables[i].no == numeroTable)
                    {
                        tempsPartie = this.listeDesTables[i].temps;
                        break;
                    }
                }
				
                _level0.loader.contentHolder.gotoAndPlay(3);
                for(var i:Number=0; i<numeroJoueursDansSalle-1; i++)
                {
	                _level0.loader.contentHolder.noms[i] = objetEvenement.listePersonnageJoueurs[i].nom;;
                }
                _level0.loader.contentHolder.noms[numeroJoueursDansSalle-1] = nomUtilisateur;
                
				_level0.loader.contentHolder.nomJ1 = objetEvenement.listePersonnageJoueurs[0].nom;// 4 joueurs
                _level0.loader.contentHolder.nomJ2 = objetEvenement.listePersonnageJoueurs[1].nom;// 4 joueurs
                _level0.loader.contentHolder.nomJ3 = objetEvenement.listePersonnageJoueurs[2].nom;// 4 joueurs
                _level0.loader.contentHolder.nomJ4 = nomUtilisateur;								// 4 joueurs
             
				for(var i:Number = 0; i < numeroJoueursDansSalle-1; i++)
                {
					this.listeDesPersonnages[i].nom = objetEvenement.listePersonnageJoueurs[i].nom;
                    this.listeDesPersonnages[i].id = objetEvenement.listePersonnageJoueurs[i].idPersonnage;
		    
		     		_level0.loader.contentHolder.tableauDesPersoChoisis.push(Number(objetEvenement.listePersonnageJoueurs[i].idPersonnage));
		    
                    movClip = _level0.loader.contentHolder.refLayer.attachMovie("Personnage"+objetEvenement.listePersonnageJoueurs[i].idPersonnage,"b"+i,i);
                    movClip._x = 450;
					movClip._y = 140 + i*60;
					movClip._xscale -= 70;
					movClip._yscale -= 70;                
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
            break;
			
            case "TableComplete":
                trace("Table complete");
            break;
			
            default:
                trace("Erreur Inconnue");
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
				for(var i = 0;i<this.listeDesTables.length;i++)
				{
					if(this.listeDesTables[i].no == this.numeroTable)
					{
						indice = i;
						break;
					}
				}

				// on s'enleve de la liste des joueurs 
            	for(var j=0;j<this.listeDesTables[indice].listeJoueurs.length;j++)
            	{
                	if(this.listeDesTables[indice].listeJoueurs[j].nom == this.nomUtilisateur)
                	{
                    	this.listeDesTables[indice].listeJoueurs.splice(j,1);
                    	break;
                	}
            	}
				
			 	delete this.listeDesTables;
			 	this.listeDesTables = new Array();
			 	delete this.listeDesJoueursConnectes;
			 	this.listeDesJoueursConnectes = new Array();
			 	objGestionnaireCommunication.obtenirListeJoueurs(Delegate.create(this, this.retourObtenirListeJoueurs), Delegate.create(this, this.evenementJoueurConnecte), Delegate.create(this, this.evenementJoueurDeconnecte));
						
			 	this.numeroTable = 0;
			 	this.idPersonnage = 0;
			 	delete this.listeDesPersonnages;
			 	this.listeDesPersonnages = new Array();
			 	_level0.loader.contentHolder.listeTable.removeAll();
			 
			 	for (var i:Number = 0; i < this.listeDesTables.length; i++)
			 	{
					str = "Table  "+this.listeDesTables[i].no+"     "+this.listeDesTables[i].temps+" min.";
					for (var j:Number = 0; j < this.listeDesTables[i].listeJoueurs.length; j++)
					{
						str = str+"\n   - "+this.listeDesTables[i].listeJoueurs[j].nom
					}
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
        trace("debut de retourDemarrerMaintenant   "+objetEvenement.resultat);

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
							case "A":
								_level0.loader.contentHolder.box_question.btn_a._visible = false;
							break;
							
							case "B":
								_level0.loader.contentHolder.box_question.btn_b._visible = false;
							break;
							
							case "C":
								_level0.loader.contentHolder.box_question.btn_c._visible = false;
							break;
							
							case "D":
								_level0.loader.contentHolder.box_question.btn_d._visible = false;
							break;
							
							case "E":
								_level0.loader.contentHolder.box_question.btn_d._visible = false;
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
        trace("debut de evenementConnexionPhysique");
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
    	trace("debut de evenementJoueurQuitteSalle   "+objetEvenement.nomUtilisateur);
    	for(var i:Number = 0;i<this.listeDesJoueursDansSalle.length;i++)
    	{
        	if(this.listeDesJoueursDansSalle[i] == objetEvenement.nomUtilisateur)
        	{
            	this.listeDesJoueursDansSalle.splice(i,1);
           		trace("un joueur enlever de la liste :   "+objetEvenement.nomUtilisateur);
            	break;
        	}
    	}
    	trace("fin de evenementJoueurQuitteSalle");
    	trace("*********************************************\n");
    }
    
	//  temps de la partie : est-ce que ca marche?
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementNouvelleTable(objetEvenement:Object)
    {
        // parametre: noTable, tempsPartie
    	trace("*********************************************");
    	trace("debut de evenementNouvelleTable   "+objetEvenement.noTable+"    "+objetEvenement.tempsPartie);
    	var str:String = new String();
    	// on ajoute une liste pour pouvoir inserer les joueurs quand ils vont entrer
        objetEvenement.listeJoueurs = new Array();
        objetEvenement.no = objetEvenement.noTable;
		objetEvenement.temps = objetEvenement.tempsPartie;
        this.listeDesTables.push(objetEvenement);
        str = "Table  "+this.listeDesTables[this.listeDesTables.length-1].no+"      "+this.listeDesTables[this.listeDesTables.length-1].temps+" min.";
        _level0.loader.contentHolder.listeTable.addItem({label : str, data : this.listeDesTables[this.listeDesTables.length-1].no});
    	trace("fin de evenementNouvelleTable");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementTableDetruite(objetEvenement:Object)
    {
    	trace("*********************************************");
    	trace("debut de evenementTableDetruite   ");
    	var i:Number;
    	for(i = 0;i<this.listeDesTables.length;i++)
    	{
        	if(this.listeDesTables[i].no == objetEvenement.noTable)
        	{
            	this.listeDesTables.splice(i,1);
            	break;
        	}
    	}
    	trace("fin de evenementTableDetruite");
    	trace("*********************************************\n");
    }
	
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementJoueurEntreTable(objetEvenement:Object)
    {
        // parametre: noTable, nomUtilisateur
    	trace("*********************************************");
    	trace("debut de evenementJoueurEntreTable   "+objetEvenement.noTable+"    "+objetEvenement.nomUtilisateur);
    	var i:Number;
    	var j:Number;
    	var indice:Number;
    	var str:String = new String();
    	indice = -1;
	
		//	musique();
	
    	for(i = 0;i<this.listeDesTables.length;i++)
    	{
        	if(this.listeDesTables[i].no == objetEvenement.noTable)
        	{
            	this.listeDesTables[i].listeJoueurs.push(new Object());
            	this.listeDesTables[i].listeJoueurs[this.listeDesTables[i].listeJoueurs.length-1].nom = objetEvenement.nomUtilisateur;
            	indice = i;
            	break;
        	}
    	}
    	if(objetEvenement.noTable == this.numeroTable)
    	{
	    	var alreadyWas:Boolean=false;
	    	
            for(i = 0; i < numeroJoueursDansSalle; i++)//nbmaxJoueurs
            {
	            var itIsInconnu:Boolean=(listeDesPersonnages[i].nom.substr(0,7)=="Inconnu");
	            //for(var j:Number=0;j<numeroJoueursDansSalle;j++){
	            	
	           		//trace(listeDesPersonnages[i].nom); 
            	//}
            	
                //if(listeDesPersonnages[i].nom == "Inconnu" || listeDesPersonnages[i].nom == "Inconnu1" || listeDesPersonnages[i].nom == "Inconnu2" || listeDesPersonnages[i].nom == "Inconnu3")
				if(itIsInconnu&&(!alreadyWas))
                 {
                    listeDesPersonnages[i].nom = objetEvenement.nomUtilisateur;
                    _level0.loader.contentHolder.nomJ1 = listeDesPersonnages[0].nom;
                    _level0.loader.contentHolder.nomJ2 = listeDesPersonnages[1].nom;
                    _level0.loader.contentHolder.nomJ3 = listeDesPersonnages[2].nom;
                    alreadyWas=true;//break;
                } else
                	_level0.loader.contentHolder.noms[i] = listeDesPersonnages[i].nom;
	                
            }
    	}
    	if(indice != -1)
    	{
        	for(i=0;i<this.listeDesTables.length;i++)
        	{
            	if(_level0.loader.contentHolder.listeTable.getItemAt(i).data == objetEvenement.noTable)
            	{
                	str = "Table  "+this.listeDesTables[indice].no+"                    "+this.listeDesTables[indice].temps+" min.";
                	for (j= 0; j < this.listeDesTables[indice].listeJoueurs.length; j++)
                	{
                    	str = str+"\n   - "+this.listeDesTables[indice].listeJoueurs[j].nom;
                	}
                	_level0.loader.contentHolder.listeTable.replaceItemAt(i, {label : str, data : objetEvenement.noTable});
            	}
        	}
        	// enlever la table de la liste si elle est pleine
			// a modifier si on est moins de 4
        	if(this.listeDesTables[indice].listeJoueurs.length == numeroJoueursDansSalle)
        	{
            	for(i;i<this.listeDesTables.length;i++)
            	{
                	if(_level0.loader.contentHolder.listeTable.getItemAt(i).data == this.listeDesTables[indice].noTable)
                	{
                    	_level0.loader.contentHolder.listeTable.removeItemAt(i);
		    			break;
                	}
            	}
        	}
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
            	if(itIsMe&&(!alreadyWas))
            	{
                	listeDesPersonnages[i].nom = "Inconnu"+i;
                	_level0.loader.contentHolder.noms[i] = "Inconnu";
                	_level0.loader.contentHolder.nomJ1 = listeDesPersonnages[0].nom;
                	_level0.loader.contentHolder.nomJ2 = listeDesPersonnages[1].nom;
                	_level0.loader.contentHolder.nomJ3 = listeDesPersonnages[2].nom;
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
            	for(j=0;j<this.listeDesTables[indice].listeJoueurs.length;j++)
            	{
                	if(this.listeDesTables[indice].listeJoueurs[j].nom == objetEvenement.nomUtilisateur)
                	{
                    	this.listeDesTables[indice].listeJoueurs.splice(j,1);
                    	break;
                	}
            	}
            	// on modifie la liste d'affichage des joueurs
            	for(i;i<_level0.loader.contentHolder.listeTable.length;i++)
            	{
                	if(_level0.loader.contentHolder.listeTable.getItemAt(i).data == objetEvenement.noTable)
                	{
                    	//tableAffichee == true;  // la table etait au prealable affichee
                    	// si la table contient encore des joueurs
                    	if(this.listeDesTables[indice].listeJoueurs.length != 0)
                    	{
                        	str = "Table  "+this.listeDesTables[indice].no+"                    "+this.listeDesTables[indice].temps+" min.";
                        	for (j= 0; j < this.listeDesTables[indice].listeJoueurs.length; j++)
                        	{
                            	str = str+"\n   - "+this.listeDesTables[indice].listeJoueurs[j].nom;
                        	}
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
                	str = "Table  "+this.listeDesTables[indice].no+"                    "+this.listeDesTables[indice].temps+" min.";
                	for (j= 0; j < this.listeDesTables[indice].listeJoueurs.length; j++)
                	{
                    	str = str+"\n   - "+this.listeDesTables[indice].listeJoueurs[j].nom;
                	}
                	_level0.loader.contentHolder.listeTable.replaceItemAt(i, {label : str, data : objetEvenement.noTable});
            	}	    
        	}
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
    	trace("fin de evenementSynchroniserTemps    "+_level0.loader.contentHolder.tempsTable*60);
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

		// put the face of my avatar in the panel (next to my name)
		_level0.loader.contentHolder.myObj=new Object();
		_level0.loader.contentHolder.myObj.myID=this.listeDesPersonnages[numeroJoueursDansSalle-1].id;//nbmaxJoueurs // 3
		_level0.loader.contentHolder.myObj.myNom=this.listeDesPersonnages[numeroJoueursDansSalle-1].nom;
		
		var maTete:MovieClip = _level0.loader.contentHolder.maTete.attachMovie("tete"+this.listeDesPersonnages[3].id, "maTete", -10099);
		maTete._x = -7;
		maTete._y = -6;
		maTete._xscale = 55;
		maTete._yscale = 55;
	
		// ici on initie les noms et pointages des adversaire (dans le panneau qui descend)
		// et on met la face de leur avatar a cote de leur nom
		// Initialise our opponents' name and score
		// and put the face of our opponents' avatar in the panel (next to their name)
		
		//using in win's table
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.nomJoueur1 = this.listeDesPersonnages[0].nom;
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.pointageJoueur1 = 0;
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.idStart=this.listeDesPersonnages[0].id;
		var tete0:MovieClip = _level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.tete1.attachMovie("tete"+this.listeDesPersonnages[0].id, "Tete0", -10100);
		tete0._x = -7;
		tete0._y = -6;
		tete0._xscale = 55;
		tete0._yscale = 55;
	
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.nomJoueur2 = this.listeDesPersonnages[1].nom;
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.pointageJoueur2 = 0;
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.idStart=this.listeDesPersonnages[1].id;
		var tete1:MovieClip = _level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.tete2.attachMovie("tete"+this.listeDesPersonnages[1].id, "Tete1", -10101);
		tete1._x = -7;
		tete1._y = -6;
		tete1._xscale = 55;
		tete1._yscale = 55;
	
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.nomJoueur3 = this.listeDesPersonnages[2].nom;
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.pointageJoueur3 = 0;
		_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.idStart=this.listeDesPersonnages[2].id;
		
		var tete2:MovieClip = _level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.tete3.attachMovie("tete"+this.listeDesPersonnages[2].id, "Tete2", -10102);
		tete2._x = -7;
		tete2._y = -6;
		tete2._xscale = 55;
		tete2._yscale = 55;

        for(i = 0; i < numeroJoueursDansSalle; i++)//4 //nbmaxJoueurs
        {
            if(this.listeDesPersonnages[numeroJoueursDansSalle-1].nom == objetEvenement.positionJoueurs[i].nom)
            {
                _level0.loader.contentHolder.planche = new PlancheDeJeu(objetEvenement.plateauJeu, i, _level0.loader.contentHolder.gestionnaireInterface);
            }
        }
        _level0.loader.contentHolder.planche.afficher();
        //trace("longueur de la liste des noms et mon nom    :"+this.listeDesPersonnages.length+"   "+this.listeDesPersonnages[3].nom);
        for(i = 0; i < objetEvenement.positionJoueurs.length; i++)
        {
            for(j = 0; j < numeroJoueursDansSalle; j++)
            {
                if(this.listeDesPersonnages[j].nom == objetEvenement.positionJoueurs[i].nom)
                {
                    _level0.loader.contentHolder.planche.ajouterPersonnage(this.listeDesPersonnages[j].nom, objetEvenement.positionJoueurs[i].x, objetEvenement.positionJoueurs[i].y, i, this.listeDesPersonnages[j].id);
		    		trace("Construction du personnage : " + this.listeDesPersonnages[j].nom + " " + objetEvenement.positionJoueurs[i].x + " " + objetEvenement.positionJoueurs[i].y + " " + i + " " + this.listeDesPersonnages[j].id);
				}
            }
        }
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
        for (var i:Number = 0; i < numeroJoueursDansSalle-1; i++)
        {
        	if(listeDesPersonnages[i].nom == objetEvenement.nomUtilisateur)
        	{
            	this.listeDesPersonnages[i].id = objetEvenement.idPersonnage;
            	_level0.loader.contentHolder.refLayer["b"+i].removeMovieClip();
            	movClip = _level0.loader.contentHolder.refLayer.attachMovie("Personnage"+objetEvenement.idPersonnage,"b"+i,i);
            	movClip._x = 450;
            	movClip._y = 148 + i*60;
	    		movClip._xscale -= 77;
	    		movClip._yscale -= 77;
		
	    		_level0.loader.contentHolder.tableauDesPersoChoisis.push(Number(objetEvenement.idPersonnage));
	    
            	break;
        	}
        }
    	trace("fin de evenementJoueurDemarrePartie");
    	trace("*********************************************\n");
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public function evenementPartieTerminee(objetEvenement:Object)
    {
        // parametre: 
    	trace("*********************************************");
    	trace("debut de evenementPartieTerminee   "+objetEvenement.statistiqueJoueur);
    /*
		trace("1 joueur  "+objetEvenement.statistiqueJoueur[0].nomUtilisateur+"   "+objetEvenement.statistiqueJoueur[0].pointage);
    	trace("2 joueur  "+objetEvenement.statistiqueJoueur[1].nomUtilisateur+"   "+objetEvenement.statistiqueJoueur[1].pointage);
    	trace("3 joueur  "+objetEvenement.statistiqueJoueur[2].nomUtilisateur+"   "+objetEvenement.statistiqueJoueur[2].pointage);
    	trace("4 joueur  "+objetEvenement.statistiqueJoueur[3].nomUtilisateur+"   "+objetEvenement.statistiqueJoueur[3].pointage);
    	trace("*********************************************");*/
    	
    
    	var k:Number = 0;
    	var nomMax:Number = -1;
    	var indice:Number = 0;  // indice du plus grand
    	var nomK:String;
    	var pointageK:String;
    	var tabOrdonne:Array = new Array();
    	var taille:Number = objetEvenement.statistiqueJoueur.length;
		var deconnecte = _level0.loader.contentHolder.deconnecte;
       
		// trouver une facon de faire fonctionner ces lignes :
		_root.vrai_txt.removeTextField();
		_root.faux_txt.removeTextField();
		_root.reponse_txt.removeTextField();
		_root.penalite_txt.removeTextField();
		_root.secondes_txt.removeTextField();
		
		
		//  retourner les jouers deconnecte'
		var jUndefined:Number; //qui est deconnecte'
		var i,j:Number;
		var itExist:Boolean;
		var jouersStarted:Array =new Array();
		
		//trace("-------------------------");nbmaxJoueurs
		jouersStarted[0] = new Object();
		jouersStarted[0].nomUtilisateur=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.nomJoueur1;
		jouersStarted[0].pointage=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.pointageJoueur1;
		jouersStarted[0].idS=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.idStart;
		
		jouersStarted[1] = new Object();
		jouersStarted[1].nomUtilisateur=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.nomJoueur2;
		jouersStarted[1].pointage=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.pointageJoueur2;
		jouersStarted[1].idS=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.idStart;

		
		jouersStarted[2] = new Object();
		jouersStarted[2].nomUtilisateur=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.nomJoueur3;
		jouersStarted[2].pointage=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.pointageJoueur3;
		jouersStarted[2].idS=_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.idStart;

		
		jouersStarted[3] = new Object();
		jouersStarted[3].nomUtilisateur=_level0.loader.contentHolder.myObj.myNom;
		jouersStarted[3].idS=_level0.loader.contentHolder.myObj.myID;
		jouersStarted[3].pointage=objetEvenement.statistiqueJoueur[0].pointage;
		for (i=0;i<numeroJoueursDansSalle;i++) {
			if(jouersStarted[numeroJoueursDansSalle-1].nomUtilisateur==objetEvenement.statistiqueJoueur[i].nom) 
				jouersStarted[numeroJoueursDansSalle-1].pointage=objetEvenement.statistiqueJoueur[i].pointage;
		}

		/*trace("1 joueur  "+jouersStarted[0].nomUtilisateur+"   "+jouersStarted[0].pointage+"   "+jouersStarted[0].idS);
    	trace("2 joueur  "+jouersStarted[1].nomUtilisateur+"   "+jouersStarted[1].pointage+"   "+jouersStarted[1].idS);
    	trace("3 joueur  "+jouersStarted[2].nomUtilisateur+"   "+jouersStarted[2].pointage+"   "+jouersStarted[2].idS);
    	trace("4 joueur  "+jouersStarted[3].nomUtilisateur+"   "+jouersStarted[3].pointage+"   "+jouersStarted[3].idS);	*/
    	
			
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
 
    	while(k < numeroJoueursDansSalle)//nbmaxJoueurs// <=3
    	{
	    	for(i=0; i< numeroJoueursDansSalle;i++)//nbmaxJoueurs // <=3
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
	    	tabOrdonne[k].id = jouersStarted[indice].idS;
	   
	   		k++;
	    	nomMax = -1;
	    	jouersStarted[indice].pointage = -1;
	    	indice = 0;
    	}
    	
    	/*
    	trace("1 joueur  "+tabOrdonne[0].nom+"   "+tabOrdonne[0].pointage+"   "+tabOrdonne[0].id);
    	trace("2 joueur  "+tabOrdonne[1].nom+"   "+tabOrdonne[1].pointage+"   "+tabOrdonne[1].id);
    	trace("3 joueur  "+tabOrdonne[2].nom+"   "+tabOrdonne[2].pointage+"   "+tabOrdonne[2].id);
    	trace("4 joueur  "+tabOrdonne[3].nom+"   "+tabOrdonne[3].pointage+"   "+tabOrdonne[3].id);	*/
    	/*
    	// a modifier quand il y aura moins de 4 joueurs
    	if(tabOrdonne[0].nom != undefined)
    	{*/
			_level0.loader.contentHolder.nom1 = tabOrdonne[0].nom;	//nbmaxJoueurs
			_level0.loader.contentHolder.pointage1 = tabOrdonne[0].pointage;/*
    	}
    	else
    	{
	    	_level0.loader.contentHolder.nom1 = _level0.loader.contentHolder.deconnecte;
			_level0.loader.contentHolder.pointage1 = 0;
    	}
    
    	if(tabOrdonne[1].nom != undefined)
    	{*/
			_level0.loader.contentHolder.nom2 = tabOrdonne[1].nom;	
			_level0.loader.contentHolder.pointage2 = tabOrdonne[1].pointage;/*
    	}
    	else
    	{
			_level0.loader.contentHolder.nom2 = _level0.loader.contentHolder.deconnecte;
	    	_level0.loader.contentHolder.pointage2 = 0;
    	}
    
    	if(tabOrdonne[2].nom != undefined)
    	{*/
			_level0.loader.contentHolder.nom3 = tabOrdonne[2].nom;	
			_level0.loader.contentHolder.pointage3 = tabOrdonne[2].pointage;/*
    	}
    	else
    	{
	    	_level0.loader.contentHolder.nom3 = _level0.loader.contentHolder.deconnecte;
			_level0.loader.contentHolder.pointage3 = 0;
    	}
    
    	if(tabOrdonne[3].nom != undefined)
    	{*/
			_level0.loader.contentHolder.nom4 = tabOrdonne[3].nom;	
			_level0.loader.contentHolder.pointage4 = tabOrdonne[3].pointage;/*
    	}
    	else
    	{
	    	_level0.loader.contentHolder.nom4 = _level0.loader.contentHolder.deconnecte;
			_level0.loader.contentHolder.pointage4 = 0;
    	}
		*/
		
		// mettre les id en ordre : tabOrdonne.id contient les id des personnages en ordre de pointage
		// il suffit de mettre les MC correspondants sur le podium
		var w:Number = 0;
		var z:Number = 0;
		for(w=0;w<numeroJoueursDansSalle;w++)//nbmaxJoueurs // w<=3
		{/*
			for(z=0;z<=3;z++)
			{	
				if (tabOrdonne[w].nom == listeDesPersonnages[z].nom)
				{
					tabOrdonne[w].id = listeDesPersonnages[z].id;
				}
			}*/
			this.tabPodiumOrdonneID[w] =  tabOrdonne[w].id;
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
	
		if(_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.nomJoueur1 == objetEvenement.nomUtilisateur)//nbmaxJoueurs
		{
			_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur1.pointageJoueur1 = objetEvenement.pointage;
			//_level0.loader.contentHolder.argentAdversaire1 = objetEvenement.argent;
		}
	
		if(_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.nomJoueur2 == objetEvenement.nomUtilisateur)
		{
			_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur2.pointageJoueur2 = objetEvenement.pointage;
			//_level0.loader.contentHolder.argentAdversaire2 = objetEvenement.argent;
		}
	
		if(_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.nomJoueur3 == objetEvenement.nomUtilisateur)
		{
			_level0.loader.contentHolder.menuPointages.mc_autresJoueurs.mc_joueur3.pointageJoueur3 = objetEvenement.pointage;
			//_level0.loader.contentHolder.argentAdversaire3 = objetEvenement.argent;
		}
	
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
