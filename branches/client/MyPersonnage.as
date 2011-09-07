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

Transformed from Personnage class 2011 Oloieri Lilian
*********************************************************************/

import mx.transitions.Tween;
import mx.transitions.easing.*;
import flash.filters.*;
import mx.controls.Loader;

class MyPersonnage implements IOurPersonnage
{ 
    // role of user if 1 - simple user , if 2 - master(admin), if 3 - teacher
	private var role:Number;
	// reference to the MC
	private var image:MovieClip;
	private var position:Point;
	// personnage id - number that identify this persos
	private var numero:Number;           // ????
	private var prochainePosition:Point;
	private var l:Number;
	private var c:Number;
	private var pointage:Number;
	private var argent:Number;
	private var listeDesObjets:Object;
	private var faireCollision:String;   // sert a savoir s'il y a eu collision apres un deplacement et avec quoi
	private var nom:String;               // name of user that is master of pers
	private var boardCentre:Boolean;
	private var listeSurMagasin:Array;	 // sert a recuperer la liste d'objets du magasin lorsque qu'on va sur une case magasin
	private var minigameLoade:Boolean;
	
	private var colorId:Number; 
	private var clothesColor:String;
	private var colorFilter:ColorMatrixFilter; // filter to color our perso 
	
	private var brainiacState:Boolean;
	private var brainiacRestedTime:Number;
	private var bananaId:Number;  
	private var bananaState:Boolean;
	private var bananaRestedTime:Number;
	private var usedBook:Boolean;        // set in true if used one book in current question	
    // number used to identify the number of picture used for perso - from 1 to 12
	private var idClip:Number;          
	private var orient:String;           // orientation ... right or left
	private static var BRAINIAC_TIME:Number = 60;    //  constant for the time that brainiac is active
	private static var BANANA_TIME:Number = 90;    //  constant for the time that banana is active
	private var planche:PlancheDeJeu;
	// level id in flash doc
	private var level:Number;
	private var isOnFinish:Boolean;
	private var winGame:Boolean;
	
	private var moveVisibility:Number;  // The number of cases that our user can move. At the begining is 3. 
	                                    // With the each running correct answers the level increase by 1 
	
	public function getMoveSight():Number
	{
		return this.moveVisibility
	}
	
	// if we need to decrease we add negative number
	public function addMoveSight(addSight:Number)
	{
		this.moveVisibility += addSight;
		
		if(this.moveVisibility < 1)
		   this.moveVisibility = 1;
		if (this.moveVisibility > 6)
		{
			if(getBrainiac())
			{
		      this.moveVisibility = 7; 
			} else {
		      this.moveVisibility = 6;
			}
		}
		//trace("getBraniac " + this.ourPerso.getBrainiac());
	}
	
	public function setMoveSight(steps:Number)
	{
		this.moveVisibility = steps;
		
		if(this.moveVisibility < 1)
		   this.moveVisibility = 1;
		if (this.moveVisibility > 6)
		{
			if(getBrainiac())
			{
		      this.moveVisibility = 7; 
			} else {
		      this.moveVisibility = 6;
			}
		}
		//trace("getBraniac " + this.moveVisibility);
	}
	
	public function getFinish(bonus:Number)
	{ 
	  if(bonus > 0)
	  {
		  isOnFinish = true;
		  winGame = true;
	  }
		 
	}
	
	public function getOnFinish():Boolean
	{
		return isOnFinish;
	}
		
	public function getColorID():Number
	{
		return this.colorId;
	}
	
	public function setColorID(id:Number)
	{
		if(id != this.colorId){
		  this.colorId = id;
		  recalculateColor();
		}
	}
	
	public function recalculateColor()
	{
		this.clothesColor = _level0.loader.contentHolder.objGestionnaireEvenements.getColorByID(this.colorId, this.idClip);
	}
	
	public function getIDessin():Number
	{
		return this.idClip;
	}
	
	public function setIDessin(dessin:Number)
	{
		this.idClip = dessin;
		recalculateFilter();
	}
	
	public function getBananaState():Boolean
	{
		return this.bananaState;
	}
	
	public function setBananaState(bananaState:Boolean)
	{
		this.bananaState = bananaState;
	}
	
	public function setBananaTime(bananaTime:Number)
	{
		this.bananaRestedTime = bananaTime;
	}
	
	public function addBananaTime(bananaTime:Number)
	{
		this.bananaRestedTime += bananaTime;
	}
	
	public function decreaseBananaTime()
	{
		if(this.bananaRestedTime > 0)
		   this.bananaRestedTime--;
	}
	
	public function getBananaTime():Number
	{
		return this.bananaRestedTime;
	}
	
	
	public function setUsedBook(bool:Boolean)
	{
		this.usedBook = bool;
	}
	
	public function getUsedBook():Boolean
	{
		return this.usedBook;
	}
	
	public function setBananaId(idNumber:Number)
	{
		this.bananaId = idNumber;
	}
	
	public function getBananaId():Number
	{
		return this.bananaId;
	}
	
	public function setMinigameLoade(bool:Boolean)
	{
		this.minigameLoade = bool;
	}
		
	public function getMinigameLoade():Boolean
	{
		return this.minigameLoade;
	}
	
	public function setDirection(dir:String)
	{
		this.orient = dir;
	}
	
	public function getDirection():String
	{
		return this.orient;
	}
	
	public function decreaseBrainiacTime()
	{
		if(this.brainiacRestedTime > 0)
		   this.brainiacRestedTime--;
	}
	
	public function getBrainiacTime():Number
	{
		return this.brainiacRestedTime;
	}
	
	public function setBrainiacTime(time:Number)
	{
		this.brainiacRestedTime = time;
	}
	
	public function setBoardCentre(centre:Boolean)
	{
		this.boardCentre = centre;
	}
	
	public function getBoardCentre():Boolean
	{
		return this.boardCentre;
	}
	
	public function setColor(n:String)
	{
		clothesColor = n;
		recalculateFilter();
	}
	
	public function getColor():String
	{
		return clothesColor;
	}
	
	public function setRole(n:Number)
	{
		role = n;
	}
	
	public function getRole():Number
	{
		return role;
	}
	
	public function setBrainiac(stateVar:Boolean)
	{
		brainiacState = stateVar;
	}
	
	public function getBrainiac():Boolean
	{
		return brainiacState;
	}
   	
	public function obtenirNom():String
	{
		return this.nom;
	}
	       
	////////////////////////////////////////////////////////////
	public function acheterObjet(id:Number, objectName:String)
	{
		listeDesObjets[objectName].push(id);
		ajouterImageBanque(objectName);
	}
	
	////////////////////////////////////////////////////////////    
	public function afficherObjets()
	{
		var i:Number;		
		for(var nom:String in listeDesObjets)
		{
			for(i in listeDesObjets[nom]) 
			trace(listeDesObjets[nom][i] + " - id : ");
		}		
	}
	
	////////////////////////////////////////////////////////////   
	public function obtenirNombreObjet()
	{
		var total:Number = 0;
		for(var nom:String in listeDesObjets)
		{
			total += listeDesObjets[nom].length; 			
		}
		
		//trace("total : " + total);
		return total;
	}
	
	////////////////////////////////////////////////////////////
	public function ajouterObjet(id:Number, objectName:String)
	{
		listeDesObjets[objectName].push(id);
		ajouterImageBanque(objectName);
	}
	
	////////////////////////////////////////////////////////////
	public function obtenirObjets():Object
	{
		return this.listeDesObjets;
	}
	
	
	public function objectInListe(nomObj:String):Boolean
	{
		return this.listeDesObjets[nomObj].length >= 1;
		
	}
	
	////////////////////////////////////////////////////////////
	// - sert a ajouter une objet a la banque d'objets du personnage
	
	public function ajouterImageBanque(nomObj:String)
	{		
		_level0.loader.contentHolder.objectMenu[nomObj].countTxt = Number(_level0.loader.contentHolder.objectMenu[nomObj].countTxt) + 1;
				
		function peutUtiliserObjet(nomObjet:String):Boolean
		{
			switch(nomObjet)
			{
				case "pieceFixe":
					return false;
				case "PotionGros":
					return true;
				case "PotionPetit":
					return true;
				case "Banane":
					return true;
				case "Livre":
					{
						if(_level0.loader.contentHolder.objGestionnaireEvenements.obtenirGestionnaireCommunication().obtenirEtatClient() != Etat.ATTENTE_REPONSE_QUESTION.no) return false;
						if((_level0.loader.contentHolder.type_question != "MULTIPLE_CHOICE") && (_level0.loader.contentHolder.type_question != "MULTIPLE_CHOICE_3") && (_level0.loader.contentHolder.type_question != "MULTIPLE_CHOICE_5" )) return false;
						if(_level0.loader.contentHolder.objGestionnaireEvenements.getOurPerso().getUsedBook()) return false;
						return true;
					}
				case "Boule":
					{
						if(_level0.loader.contentHolder.objGestionnaireEvenements.obtenirGestionnaireCommunication().obtenirEtatClient() != Etat.ATTENTE_REPONSE_QUESTION.no) return false;
						return true;
					}
				default: return true;
			}
			return false;
		}
		
		var listeObjets:Object = this.obtenirObjets();
				
		// function used to make action of the object on that on click
		_level0.loader.contentHolder.objectMenu[nomObj + "_mc"].onRelease = function()
		{
			if(peutUtiliserObjet(nomObj)&& (listeObjets[nomObj].length >= 1))
			{
				var objID:Number = listeObjets[nomObj][listeObjets[nomObj].length - 1];
				if(nomObj == "Banane"){
                      
					  _level0.loader.contentHolder.planche.obtenirPerso().setBananaId(objID);
					  _level0.loader.contentHolder.planche.obtenirPerso().prepareBananaToUse();		
			          var bananaClip:MovieClip;
                      //bananaClip = _level0.loader.contentHolder.attachMovie("bananaToss", "toss", 2021, objID);
					  bananaClip = _level0.loader.contentHolder.createEmptyMovieClip("toss", 2021);
					  _level0.loader.contentHolder.toss.loadMovie("GUI/bananaToss.swf");
					  
					  
					  bananaClip._x = 30;
                      bananaClip._y = 40;
					  
		        }else{
					trace("Enlever obj - " + nomObj);
					_level0.loader.contentHolder.objGestionnaireEvenements.utiliserObjet(objID, "NA");
					_level0.loader.contentHolder.planche.obtenirPerso().enleverObjet(nomObj);
				}
				
				if(nomObj == "Livre")
				   _level0.loader.contentHolder.planche.obtenirPerso().setUsedBook(true);
				
				_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._xscale = _level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._yscale = 100;
				_level0.loader.contentHolder.objectMenu[nomObj + "_mc"]._alpha = 100;
												
			}//end 1st if
		};		
		
		//trace("--- FIN ajouterImageBanque ! ---");
	}
	
	
	
	////////////////////////////////////////////////////////////
	// -sert a enlever un objet a un personnage :
	// on enleve les donnees relatives a cet objet dans les listes
	// du personnage. on enleve ensuite l'image de l'objet
	//
	public function enleverObjet(n:String)
	{		
	    trace("enlever - " + n + listeDesObjets[n].length );
		listeDesObjets[n].pop();
		_level0.loader.contentHolder.objectMenu[n].countTxt = Number(_level0.loader.contentHolder.objectMenu[n].countTxt) - 1;		
	}	
	
	
	////////////////////////////////////////////////////////////
	public function zoomer(valeur:Number)
	{		
		image._xscale += valeur;
		image._yscale += valeur;
	}
	
	////////////////////////////////////////////////////////////
	public function translater(la:Number, ha:Number)
	{
		image._x += la;
		image._y += ha;
		
		this.position.definirX(position.obtenirX() + la);
		this.position.definirY(position.obtenirY() + ha);
		this.prochainePosition.definirX(prochainePosition.obtenirX() + la);
		this.prochainePosition.definirY(prochainePosition.obtenirY() + ha);
	}
	
	////////////////////////////////////////////////////////////
	public function obtenirL():Number
	{
		return l;
	}
	
	////////////////////////////////////////////////////////////
	public function definirL(ll:Number)
	{
		l = ll;
	}
	
	////////////////////////////////////////////////////////////
	public function obtenirC():Number
	{
		return c;
	}
	
	////////////////////////////////////////////////////////////
	public function definirC(cc:Number)
	{
		c = cc;
	}
	
	////////////////////////////////////////////////////////////
	public function getIdPersonnage():Number
	{
		return numero;
	}
	
	////////////////////////////////////////////////////////////
	public function setIdPersonnage(n:Number)
	{
		numero = n;
		if(n == 0)
		   setIDessin(0);
		else
		   setIDessin(UtilsBox.calculatePicture(n));
	}
	
	////////////////////////////////////////////////////////////
	public function obtenirPointage():Number
	{
		return pointage;
	}
	
	////////////////////////////////////////////////////////////
	public function modifierPointage(points:Number)
	{
		pointage = points;
	}
	
	////////////////////////////////////////////////////////////
	public function obtenirArgent():Number
	{
		return argent;
	}
	
	
	////////////////////////////////////////////////////////////
	public function modifierArgent(x:Number)
	{
		argent = x;
		_level0.loader.contentHolder.objectMenu["piece"].countTxt = x;
	}
	
	////////////////////////////////////////////////////////////
	public function obtenirImage():MovieClip
	{
		return image;
	}
	
	////////////////////////////////////////////////////////////
	public function definirImage(i:MovieClip)
	{
		image = i;
	}
	
	////////////////////////////////////////////////////////////
	public function obtenirPosition():Point
	{
		return position;
	}
	
	
	////////////////////////////////////////////////////////////
	public function definirPosition(p:Point, ll:Number, cc:Number)
	{
		//trace("test drift :" + image._x + " " + image._y);
		image._x = p.obtenirX();
		image._y = p.obtenirY();
		//trace("test drift after :" + image._x + " " + image._y);
		this.position.definirX(p.obtenirX());
		this.position.definirY(p.obtenirY());
		this.l = ll;
		this.c = cc;
	}
	
	////////////////////////////////////////////////////////////
	public function obtenirProchainePosition():Point
	{
		return prochainePosition;
	}
	
	////////////////////////////////////////////////////////////
	// retourne la liste des objets que le magasin ou on se trouve contient
	//
	public function obtenirMagasin():Array
	{
		return listeSurMagasin;
	}
	
	////////////////////////////////////////////////////////////
	// fixe les objets contenus dans un magasin
	//
	public function definirMagasin(mag:Array)
	{
		listeSurMagasin = mag;
	}	
	
    ////////////////////////////////////////////////////////////
	// When buy an object this function put the 0 for the id of the object 
	// in liste in Shop - in this case it became unvalid object - it is selled
	//
	public function buyObject(idTake:Number, objectType:String, idRetour:Number)
	{
		ajouterObjet(idTake, objectType); 
		removeShopObject(idTake);
		putNewShopObject(idRetour, objectType);					
	}
	
	public function removeShopObject(idRemove:Number)
	{		
		var count:Number =  listeSurMagasin.length;
				
		for(var j:Number = 0; j < count; j++)
		{
			if(listeSurMagasin[j].id == idRemove)
			   listeSurMagasin[j].id = 0;
			   
		}		
	}
	
	////////////////////////////////////////////////////////////
	// After buy an object the server sent the new id for the same object 
	// in the shop - the object became valid - the next object of the same 
	// type to sell to player 
	//
	public function putNewShopObject(idPut:Number, objetType:String)
	{
		var count:Number =  listeSurMagasin.length;
		for(var j:Number = 0; j < count; j++)
		{
			//trace(" control put new - " + j + " " + listeSurMagasin[j].id);
			if(listeSurMagasin[j].type == objetType)
			   listeSurMagasin[j].id = idPut;			   			   
		}
	}
	
	public function initPlanche(planchet:PlancheDeJeu)
	{
		this.planche = planchet;
		this.level = 5 * planche.obtenirTableauDesCases().length * planche.obtenirTableauDesCases()[0].length + 2 * this.numero;
		var xx:Number = planche.obtenirTableauDesCases()[this.l][this.c].obtenirClipCase()._x;
		var yy:Number = planche.obtenirTableauDesCases()[this.l][this.c].obtenirClipCase()._y;
		this.position = new Point(xx,yy);
		this.prochainePosition = new Point(xx,yy);
		recalculateFilter();
		var filterC:ColorMatrixFilter = this.colorFilter;	
		// to load the perso .. use ClipLoader to know the moment of complet load
		var myLoader:MovieClipLoader = new MovieClipLoader();
	    //myLoader.addListener(image);
		var ourName:String = nom;
		var mclListener:Object = new Object();
        mclListener.onLoadComplete = function(target_mc:MovieClip) {
			target_mc.filterC = filterC;
		    target_mc.nom = ourName;					
			target_mc.gotoAndPlay("bored");
			
			// assure que le clip a la bonne orientation
			target_mc._xscale = - Math.abs(target_mc._xscale);
			target_mc.dtNom._xscale = - Math.abs(target_mc._xscale);
			target_mc.dtNom._x = 42;
			target_mc._visible = true;	
        };
		myLoader.addListener(mclListener);
		
		this.orient = "right";
		if(!(role > 1 && _level0.loader.contentHolder.objGestionnaireEvenements.getOurTable().compareType("Tournament"))){  
  
          image =  _level0.loader.contentHolder.referenceLayer.createEmptyMovieClip("Personnage" + numero, level);
		  myLoader.loadClip("Perso/perso" + this.idClip + ".swf", image);
		  image._visible = false;					
		}		
		this.boardCentre = false;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	//	CONSTRUCTEUR
	//////////////////////////////////////////////////////////////////////////////////////
	public function MyPersonnage(idPers:Number, nom:String, role:Number, idClip:Number, color:Number, colorString:String)
	{
		//trace(" idPers : " + idPers + " nom : "+ nom + " role : " + role + " nomClip : " + nomClip + " ll : " + ll + " cc : " + cc + " cloColor : " + cloColor + "planches : " + planchet);
		this.level = null;		
		this.numero = idPers;		
		this.colorId = color;
		this.clothesColor = colorString;
		this.idClip = idClip;
       	
		this.brainiacState = false;
		this.brainiacRestedTime = 0; 		
		this.bananaState = false;
	    this.bananaRestedTime = 0;					
		this.pointage = 0;
		this.argent = 0;
		this.listeDesObjets = new Object();
		this.moveVisibility = 3;
		
		// each array will contain the ID's of objects in possession
		//this.listeDesObjets["piece"] = new Array();
		this.listeDesObjets["Banane"] = new Array();
		this.listeDesObjets["Livre"] = new Array();
		this.listeDesObjets["Boule"] = new Array();
		
		this.faireCollision = null;
		this.nom = nom;
		//this.listeSurMagasin = mag;
		this.minigameLoade = false;
		this.role = role;
		this.boardCentre = false;
	}// end constr
	
	private function recalculateFilter()
	{
		recalculateColor();
		if(this.clothesColor > 0 && this.idClip > 0)
		   this.colorFilter = UtilsBox.colorMatrixPerso(this.clothesColor, this.idClip);
		else
		   this.colorFilter = undefined;		
	}
	
	
	////////////////////////////////////////////////////////////
	public function deplacePersonnage()
	{
		var pourcent:Number;
		var dx:Number = 0;
		var dy:Number = 0;
		var reafficher1:Boolean = true;
		var reafficher2:Boolean = false;
				
		dx = this.prochainePosition.obtenirX() - this.position.obtenirX();  
		dy = this.prochainePosition.obtenirY() - this.position.obtenirY();
		
		//trace("ds deplacePersonnage " + this.prochainePosition.obtenirX() + " " + this.position.obtenirX());
		//trace("ds deplacePersonnage " + dx + " " + dy);
		
		if( boardCentre && planche.getRepostCases())
		{
			planche.effacerCasesPossibles();
			planche.afficherCasesPossibles();
			planche.setRepostCases(false);
			// trace("Test sur retour 1 " + boardCentre);

			return;			
		}
		else if( boardCentre  &&  !planche.getShowCases()) // to consider the case that we don't have possibility to move 
		{
			planche.afficherCasesPossibles();
			//trace("Test sur retour 2 " + boardCentre);
			return;
		}
		else if( boardCentre ) //dx == 0 && dy == 0 && image._currentFrame == 1) 
		{
			//  trace("Test sur retour 3 " + boardCentre);
			return;
		}
		
		if ((dx == 0) && (dy == 0))
		{
			if(image._currentFrame != 1 && image._currentFrame < 90)
			{
				// place le personnage au repos et de face
				image.gotoAndStop(1);
		
				switch(this.faireCollision)
				{
					case "piece":
						// l'argent est ajoute au personnage dans les fichiers gestEve et gestComm
						// en gros, ici, on enleve l'image du board
    					planche.enleverPiece(this.l, this.c);
						planche.modifierNumeroCase(this.l, this.c, -10000);
						_level0.loader.contentHolder.array_sons[9].start(0,1);
						this.faireCollision = null;
					break;
				
					case "magasin":
						//si notre personnage tombe sur un magasin, on charge le GUI_magasin
							this.minigameLoade = true;
							reafficher1 = false;
							planche.effacerCasesPossibles();
							//_root.objGestionnaireInterface.effacerBoutons(1);			
							
							// on charge le magasin : on ajuste sa position a l'ecran
							// on indique qu'il ne s'agit pas d'un mini-game.
						
							var minigame:MovieClip;   
							minigame = _level0.loader.contentHolder.miniGameLayer.attachMovie("GUI_magasin", "magasin", 9997);
							minigame._x = 0;
							minigame._y = 0;
							minigame._xscale = 100;
							minigame._yscale = 100;
							//minigame._width = 550;
							//minigame._height= 400;
							minigame._visible = false;
						
						
							//variables qui servent pour un fade-in/fade-out a l'entree du magasin
							var oListener:Object = new Object();  
							var twMove:Tween;
							var twMove2:Tween;
							var mClip1:MovieClip;
	
							mClip1 = _level0.loader.contentHolder.attachMovie("masque", "masqueA", 1,{_x:270, _y: 200});
							mClip1._alpha = 0;
						
							twMove = new Tween(mClip1, "_alpha", Regular.easeOut, 0, 100, 1, true);
							twMove.addListener(oListener);
	
							oListener.onMotionFinished = function():Void 
							{ 
								twMove2 = new Tween(mClip1, "_alpha", Regular.easeOut, 100, 0, 1, true);
								_level0.loader.contentHolder.menuOutils._visible = false;
								//_level0.loader.contentHolder.enteteHolder._visible = false;
								_level0.loader.contentHolder.horloge._visible = false;// ?????  a think is not correct any more all 3 statements
								minigame._visible = true;
							}
						
							this.faireCollision = null;
						
					break;
				
					// Pour tous les objets, on se comporte de la meme maniere :
					// on enleve l'objet (meme si on en a 10 : parce que le serveur nous dit qu'on l'enleve)
					// si necessaire, on l'ajoute a nos objets
					
					case "Livre":
						planche.enleverObjet(this.l, this.c);
						planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
				       
					break;
				
					case "Telephone":
						planche.enleverObjet(this.l, this.c);
						planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                        
					break;
				
					case "Boule":
						planche.enleverObjet(this.l, this.c);
						planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                      
					break;
				
					case "PotionGros":
						planche.enleverObjet(this.l, this.c);
						planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                       
					break;
				
					case "PotionPetit":
						planche.enleverObjet(this.l, this.c);
						planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                       
					break;
				
					case "Banane":
						planche.enleverObjet(this.l, this.c);
						planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
                        
					break;
					
					case "Brainiac":
					   	planche.enleverObjet(this.l, this.c);
						planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
						getBrainiacAnimaton(BRAINIAC_TIME);
                        
					break;

				
					default :
						//trace("pas de collision");	
					break;
				}// switch(this.faireCollision)
							
				if(planche.estCaseSpeciale(this.l, this.c) &&  _level0.loader.contentHolder.sortieDunMinigame == false)
				{
						this.minigameLoade = true;
						planche.effacerCasesPossibles();
											
						_level0.loader.contentHolder.minigameToLoad = _level0.loader.contentHolder.url_Minigames[Math.floor(Math.random() * _level0.loader.contentHolder.url_Minigames.length)];

						//variables qui servent pour un fade-in/fade-out a l'entree du magasin
						var oListener:Object = new Object();  
						var twMove:Tween;
						var twMove2:Tween;
						var mClip1:MovieClip;
	
						mClip1 = _level0.loader.contentHolder.attachMovie("masque", "masqueA", 9998,{_x:270, _y: 200});
						mClip1._alpha = 0;
						
						var minigame:MovieClip;
						minigame = _level0.loader.contentHolder.miniGameLayer.attachMovie("GUI_minigame", "Minigame", 9997);
						minigame._visible = false;
					
						switch(_level0.loader.contentHolder.minigameToLoad)
						{
							case "ileTresor.swf":
								minigame._x = 40;
								minigame._y = -40;
								minigame._width = 550;
								minigame._height= 400;
							break;
							
							case "balleAuMur2.swf":
								minigame._x = 0;
								minigame._y = 40;
								minigame._width = 550;
								minigame._height= 400;
							break;
							
							default:
								minigame._x = 15;
								minigame._y = - 40;
								minigame._width = 485;
								minigame._height= 385;
							break;
						}
						
						twMove = new Tween(mClip1, "_alpha", Regular.easeOut, 0, 100, 1, true);
						twMove.addListener(oListener);
	
						oListener.onMotionFinished = function():Void 
						{ 
							twMove2 = new Tween(mClip1, "_alpha", Regular.easeOut, 100, 0, 1, true);
							minigame._visible = true;
						}
					} // if(_level0.loader.contentHolder.planche.estCaseSpeciale(this.l, this.c) &&  _level0.loader.contentHolder.sortieDunMinigame == false)
					else reafficher2 = true;
				
			}// if(image._currentFrame != 1 && image._currentFrame < 90)
			
			if(reafficher1 && reafficher2) this.minigameLoade = false;
			
			//Si le perso est le mien et qu'il est au repos, mais que le board n'est pas centre
			if(planche.recentrerBoard(this.l, this.c, false))
			{
					if(!this.minigameLoade)
					{
						planche.afficherCasesPossibles();
						planche.setRepostCases(false);
						
					}
					boardCentre = true;
					//trace(_level0.loader.contentHolder.horlogeNum + "temps restant");
					
			}			
			return; // pour ne pas faire le reste des verifications inutilement si dx == dy == 0
		} // if ((dx == 0) && (dy == 0))
		
		//pour le reste, ((dx == 0)&&(dy == 0) != 1)		
		if(Math.abs(dx) > Math.abs(dy))
		{
			if(Math.abs(dx) > 10)
			{
				pourcent = 10/Math.abs(dx);
				dx *= pourcent;
				dy *= pourcent;
				dy = Math.round(dy);
			}
		}
		else(Math.abs(dy) > Math.abs(dx))
		{
			if(Math.abs(dy) > 10)
			{
				pourcent = 10/Math.abs(dy);
				dx *= pourcent;
				dy *= pourcent;
			}
		}
						
		//trace("trace drift dx : " + dx + " dy : " + dy + " control : " + dx/dy);
		if (dy < 0)
		{
			if(image._currentFrame < 70)
			{
				image.gotoAndPlay(70);
				//trace("perso " + image._currentFrame);
			}
			
		}
		else
		{
			if(image._currentFrame > 69)
			{
				image.gotoAndPlay(10);
			}
		}
				
		if (dx >= 0)
		{
			// assure que le clip a la bonne orientation
			image._xscale = - Math.abs(image._xscale);
			image.dtNom._xscale = - Math.abs(image._xscale);
			image.dtNom._x = 42;
			this.orient = "right";
		}
		else
		{
			// flip le clip pour aller vers la gauche
			image._xscale =  Math.abs(image._xscale);
			image.dtNom._xscale =   Math.abs(image._xscale);
			image.dtNom._x = - 42;
			this.orient = "left";
		}
		
		
		// deplace le clip
		image._x += dx;  
		image._y += dy;
		position.definirX(position.obtenirX()+ dx);
		position.definirY(position.obtenirY()+ dy);
	
		// Si le deplacement voulu n'est pas nul mais que le personnage est au repos
		//trace("avant le if le frame  :   "+this.image._currentFrame);
		if (((dx != 0) || (dy != 0)) && (this.image._currentFrame == 1 || this.image._currentFrame > 89))
		{
			// place le clip du personnage au debut de la sequence de deplacement
			image.gotoAndPlay(10);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	public function afficher()
	{
		image._visible = true;
		image._x = position.obtenirX();  
		image._y = position.obtenirY();
	}
	
	public function afficherAutreDir()
	{
	       // assure que le clip a la bonne orientation
			image._xscale = - Math.abs(image._xscale);
			image.dtNom._xscale = - Math.abs(image._xscale);
			image.dtNom._x = 42;
			this.orient = "right";
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	public function cachePersonnage()
	{
		image._visible = false;
	}	
	
	public function removeImage()
	{
		image.removeMovieClip();
	}
	//////////////////////////////////////////////////////////////////////////////////////
	////  pt contient la ligne et la colonne PAS LES X et Y
	public function definirProchainePosition(pt:Point, str:String)
	{
		planche.getCase(this.l, this.c).retirerPersonnage(this);
		
		this.l = pt.obtenirX();
		this.c = pt.obtenirY();
		//trace("definirProchainePosition : pt " + pt.obtenirY());
  
		planche.ajouterPersonnage(this, this.l, this.c);
		
		this.prochainePosition.definirX(planche.getCase(this.l, this.c).obtenirClipCase()._x);
		this.prochainePosition.definirY(planche.getCase(this.l, this.c).obtenirClipCase()._y);
		
		//trace("definirProchainePosition : def Y " + _level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].obtenirClipCase()._y);
		
		this.faireCollision = str;
		this.boardCentre = false;		
		//trace("board " + this.boardCentre);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	public function obtenirX():Number
	{
		return position.obtenirX();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	public function obtenirY():Number
	{
		return position.obtenirY();
	}
		
	//////////////////////////////////////////////////////////////////////////////////////
	public function rire()
	{
		image.gotoAndPlay("rire");
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	public function tossBanana()
	{
		this.image.gotoAndPlay("tossing");				
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	public function slippingBanana()
	{
		//correctStateBeforeBanane()
		
		image.gotoAndPlay("slipping");
					    		
		// in this 3 lines we transfer player from Brainiac state to Banana state
		setBrainiac(false);
		setBrainiacTime(0);
		setBananaState(true);
		
		if(!_level0.loader.contentHolder.objGestionnaireEvenements.endGame)
		{
		   setBananaTimer(BANANA_TIME);
		}
		// after inform user about Banane
		//funcToCallMessage(); 
		
		addMoveSight(-2);
		//planche.setRepostCases(true);						 		
	}
	
	public function correctStateBeforeBanane(adverName:String)
	{
		funcToCallMessage(adverName); 
		//if the player is in the minigame 
		if(getMinigameLoade())
		{		      
			  if(_level0.loader.contentHolder.miniGameLayer["Minigame"])
			  {
                  _level0.loader.contentHolder.miniGameLayer["Minigame"].loader.contentHolder.quitter(true);
			  }else  if(_level0.loader.contentHolder.miniGameLayer["magasin"])
			  {
                  _level0.loader.contentHolder.miniGameLayer["magasin"].loader.contentHolder.quitter();
			  }
			 		 			
		// if the player read at the moment a question
		}else if(_level0.loader.contentHolder.box_question.monScroll._visible)
		{    		   
			_level0.loader.contentHolder.objGestionnaireEvenements.cancelQuestion();
			_level0.loader.contentHolder.box_question.gotoAndPlay(9);
			_level0.loader.contentHolder.sortieDunMinigame = false;
			  		
		// if the player read a feedback of a question 
		}else if(_level0.loader.contentHolder.box_question.GUI_retro.texteTemps._visible) 
		{
			// catch the rested time to be used after banana show
			var tempsRested:Number = _level0.loader.contentHolder.box_question.GUI_retro.tempsPenalite;
								
			_level0.loader.contentHolder.box_question.monScroll._visible = false;
			_level0.loader.contentHolder.box_question._visible = false;
			_level0.loader.contentHolder.box_question.GUI_retro.removeMovieClip();
				
			// setTimeout( Function, delay in miliseconds, arguments)
            _global.timerInterval = setInterval(this,"funcToRecallFeedback", 7000, tempsRested);			 
			  
		}
		planche.effacerCasesPossibles();
		planche.setRepostCases(false);
		planche.setShowCases(true);
	}
	
	function funcToRecallFeedback(tempsRested:Number):Void
    {
        //and now continue to show the feedback 
		_level0.loader.contentHolder.box_question._visible = true;
		_level0.loader.contentHolder.box_question.monScroll._visible = true;
		var ptX:Number = _level0.loader.contentHolder.box_question.monScroll._x;
		var ptY:Number = _level0.loader.contentHolder.box_question.monScroll._y;
		_level0.loader.contentHolder.box_question.attachMovie("GUI_retro","GUI_retro", 100, {_x:ptX, _y:ptY});
	    _level0.loader.contentHolder.box_question.GUI_retro.timeX = tempsRested;
		clearInterval(_global.timerInterval);
     
    } // end methode
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	public function rest()
	{
		this.image.gotoAndStop("rest");
	}
	
	// used to put the Braniac animation on the player  for the specified time
	public function getBrainiacAnimaton(brainiacTime:Number)
	{		
	   var playerUnder:String = this.nom;
	   // to color our perso
	   
	   // to set off Banana effects on the player
	   this.cancelBanana();
	   
	   var filterC:ColorMatrixFilter = this.colorFilter;
						
	   if(this.brainiacRestedTime == 0)
	   {
	      //******************************** new one *************
		  // to load the perso .. use ClipLoader to know the moment of complet load
		  var myLoader:MovieClipLoader = new MovieClipLoader();
	  	
		  if(this.orient == "right")
		  {
				image._xscale = Math.abs(image._xscale);
			    image.dtNom._xscale = Math.abs(image._xscale);
			    image.dtNom._x = - 42;
				this.orient == "left";
		  }
		  image._visible = false;
		  var nameX:String = this.nom;
		  var orientDir:String = this.orient;
		  var mclListener:Object = new Object();
          mclListener.onLoadComplete = function(target_mc:MovieClip) {
           
			target_mc.filterC = filterC; 
			target_mc.nom = nameX;
			target_mc.gotoAndPlay("grow");
			target_mc._visible = true;			
			
          };
		  myLoader.addListener(mclListener);

          myLoader.loadClip("Perso/perso" + this.idClip + "brainiac.swf", image); 
		  		
		  setBrainiacTimer(this);
			 
		  endOnBrainiac();
		  
	   }else if(this.brainiacRestedTime > 0)
	   {
		   
	   }
	   
	   this.brainiacRestedTime += brainiacTime;
	   
	   _level0.loader.contentHolder.planche.setRepostCases(true);
	
	} // end of getBraniacAnimation
	
	// used to put the Braniac animation on the player  for the specified time
	public function getReconnectionBrainiacAnimaton(brainiacTime:Number)
	{		
	   var playerUnder:MyPersonnage = this;
	   // to color our perso
	   
	   // to set off Banana effects on the player
	   this.cancelBanana();
	   
	   var filterC:ColorMatrixFilter = this.colorFilter;
						
	   if(this.brainiacRestedTime == 0)
	   {
	      //******************************** new one *************
		  // to load the perso .. use ClipLoader to know the moment of complet load
		  var myLoader:MovieClipLoader = new MovieClipLoader();
	  	
		  if(this.orient == "right")
		  {
				image._xscale = Math.abs(image._xscale);
			    image.dtNom._xscale = Math.abs(image._xscale);
			    image.dtNom._x = - 42;
				this.orient == "left";				
		  } 
		  var nameX:String = this.nom;
		  var orientDir:String = this.orient;
		  image._visible = false;
		  var mclListener:Object = new Object();
          mclListener.onLoadComplete = function(target_mc:MovieClip) {
           		
			target_mc.filterC = filterC; 
			target_mc.nom = nameX;
			target_mc.gotoAndPlay("bored");
			target_mc._visible = true;			
          };
		  myLoader.addListener(mclListener);

          myLoader.loadClip("Perso/perso" + this.idClip + "brainiac.swf", image); 
		  		 
		  setBrainiacTimer(this);
			 
		  endOnBrainiac();
		  
	   }else if(this.brainiacRestedTime > 0)
	   {
		   
	   }
	   
	   this.brainiacRestedTime += brainiacTime;
	   
	   planche.setRepostCases(true);
	
	} // end of getReconnectionBraniacAnimation
	
	public function endOnBrainiac()
	{
		
		var playerUnder:MyPersonnage = this;
		var id:Number = this.idClip;
		var restedTime:Number;
		var filterC:ColorMatrixFilter = this.colorFilter;
			   	  	    
	    var intervalIDEndBrain = setInterval(etapeEndBrain, 1000, playerUnder);	
		
		function etapeEndBrain():Void
		{    
		   var image:MovieClip = playerUnder.obtenirImage(); 
		   playerUnder.decreaseBrainiacTime();
		   restedTime = playerUnder.getBrainiacTime(); 
		   //trace("test brainiac2 " + image._currentFrame)
           		  
		   if( restedTime == 1 && (image._currentFrame == 1 || image._currentFrame == 90))
		   {
			  image.gotoAndPlay("down");
			  
		   }
		   else if( restedTime == 0 && (image._currentFrame == 1 ||	image._currentFrame == 90 || image._currentFrame == 96))
	       {
			  playerUnder.setBrainiac(false);
			  if(playerUnder.getDirection() == "right")
			  {
				image._xscale = Math.abs(image._xscale);
			    image.dtNom._xscale = Math.abs(image._xscale);
			    image.dtNom._x = - 42;
				playerUnder.setDirection("left");
		      }
			  image._visible = false;
			  myLoader.loadClip("Perso/perso" + id + ".swf", image); 
			  clearInterval(intervalIDEndBrain);
			  
		   }  
		  			
		}// end method etapeEndBrain   
	   
	    var myLoader:MovieClipLoader = new MovieClipLoader();
		var mclListener:Object = new Object();
		
        mclListener.onLoadComplete = function(target_mc:MovieClip) {
           	target_mc.filterC = filterC;
			target_mc.gotoAndPlay("bored");
			target_mc.nom = playerUnder.obtenirNom();
			target_mc._visible = true;
						
        };
		myLoader.addListener(mclListener);
		
	}//end endOnBrainiac	
	
	// this function is used to put on the Sprite the Timer of the Braniac
	// after the time finished it must disapear
	private function setBrainiacTimer(playerUnder:MyPersonnage)
	{
		  		   
		   //first on put on the sprite the box for the timer if is our perso		
		   _level0.loader.contentHolder.attachMovie("brainBox", "brainBox", 6);//_level0.loader.contentHolder.getNextHigesthDepth());
		   
		   if(_level0.loader.contentHolder.objGestionnaireEvenements.getListLength() >= 11)
		   {
		      trace("liste - " + _level0.loader.contentHolder.objGestionnaireEvenements.getListLength());
			  _level0.loader.contentHolder.brainBox._x = 400;
		   }
		   else if(_level0.loader.contentHolder.objGestionnaireEvenements.getListLength() < 11){ 
    	      _level0.loader.contentHolder.brainBox._x = 460;
		   }
			  
		   //_level0.loader.contentHolder.brainBox._xscale = 90;
		   _level0.loader.contentHolder.brainBox._y = 304;
		
		   //create text field to put info in
		   _level0.loader.contentHolder.brainBox.createTextField("brainiacTime", _level0.loader.contentHolder.brainBox.getNextHigesthDepth(), 5, 18, 40, 30);
		
		   _level0.loader.contentHolder.brainBox.nameObject = "BRAINIAC";
		   // Make the field dynamic text field
           _level0.loader.contentHolder.brainBox.brainiacTime.type = "dynamic";
           with(_level0.loader.contentHolder.brainBox.brainiacTime)
           {
	          multiline = false;
	          background = false;
	          //text = "5";
	          textColor = 0xFFFFFF;
	          border = false;
	          _visible = true;
	          //autoSize = true;
           }
   
           var formatTimer:TextFormat = new TextFormat();
           formatTimer.bold = true;
           formatTimer.size = 21;
           formatTimer.font = "ArialBlack";
           formatTimer.align = "Center";
           _level0.loader.contentHolder.brainBox.brainiacTime.setNewTextFormat(formatTimer);
						
		if(_global.intervalIdBrain != null) {
		
             clearInterval(_global.intervalIdBrain);
        }

		_global.intervalIdBrain = setInterval(brainTimerSet, 1000, playerUnder);	// sert pour attendre la jusqu'a la fin de action de Braniac
	   
	    function brainTimerSet(playerUnder:MyPersonnage){
	      
		   var timeX:Number = playerUnder.getBrainiacTime(); 
		   _level0.loader.contentHolder.brainBox.brainiacTime.text = timeX;
		   
		   		   
		   if(timeX == 0)
	       {			   
			   playerUnder.addMoveSight(-1);
			   // if is banana there it will repost cases
			   if(!playerUnder.getBananaState())
			      _level0.loader.contentHolder.planche.setRepostCases(true);
			  // to remove the timer box
			  _level0.loader.contentHolder.brainBox.removeMovieClip();
		      clearInterval(_global.intervalIdBrain);
					
		   } // end if		   
	   } // end function brainTimerSet		
	}// end function  setBrainiacTimer
	
	private function cancelBanana()
	{
		bananaState = false;
	    bananaRestedTime = 1;
		//_level0.loader.contentHolder.objGestionnaireEvenements.setBrainiacTimer(playerUnder); 
	}
	
	public function prepareBananaToUse()
	{
		enleverObjet("Banane");	
	}
	
	// used for Banana action on the game
	public function tossBananaShell(playerTo:IPersonnage)
	{
	    // phase 1 - remove old shell_mc
	   //_level0.loader.contentHolder.referenceLayer.shell_mc.removeMovieClip();
	   // phase 1 - player toss banana
	   tossBanana();
	   
	   // phase 2 - banana shell fly to the player that support the action
	   var intervalId:Number;
	   var num:Number = playerTo.getIdPersonnage();
	   var coorByX:Number = obtenirX() - 10;
	   var coorByY:Number = obtenirY() - obtenirImage()._height;
	   intervalId = setInterval(attendre, 3000, playerTo);	// sert pour attendre la jusqu'a la fin de action de 
	   
	   function attendre(){
	     
		 clearInterval(intervalId);     
	   	   
	     var coorToX:Number =  playerTo.obtenirProchainePosition().obtenirX();
	     var coorToY:Number =  playerTo.obtenirProchainePosition().obtenirY()- 15;
		 
		 var nameTo:String = playerTo.obtenirNom();
		 _level0.loader.contentHolder.referenceLayer.attachMovie("bananaShell", "shell_mc" + nameTo + intervalId, _level0.loader.contentHolder.referenceLayer.getNextHighestDepth(), {_x:coorByX, _y:coorByY});
		 
		 var twMoveX:Tween = new Tween(_level0.loader.contentHolder.referenceLayer["shell_mc"  + nameTo + intervalId], "_x", Strong.easeOut, coorByX, coorToX, 1, true);
		 var twMoveY:Tween = new Tween(_level0.loader.contentHolder.referenceLayer["shell_mc"  + nameTo + intervalId], "_y", Strong.easeOut, coorByY, coorToY, 1, true);
		 var twMoveRot:Tween = new Tween(_level0.loader.contentHolder.referenceLayer["shell_mc"  + nameTo + intervalId], "_rotation", Strong.easeOut, 0, 360, 1, true);
		 
		 _level0.loader.contentHolder.referenceLayer["shell_mc"  + nameTo + intervalId].swapDepths(_level0.loader.contentHolder.referenceLayer["Personnage" + num]);
		 
		 //trace("fin attendre?");
				 
	   }// end attendre
	 //trace("fin toss banana shell?");
	 
     var intervalIdToss:Number;
  	 intervalIdToss = setInterval(tossIt, 5000, playerTo);	// sert pour attendre la jusqu'a la fin de action de 
     function tossIt(){
		 clearInterval(intervalIdToss);
		 playerTo.slippingBanana();       
	 } 

	  	
  }// end function 
  
  // function to display the message of tossed banana
  function funcToCallMessage(adverName:String)
  {
      // we put the message only if the game is not in the way to finish
	  if(_level0.loader.contentHolder.horlogeNum > 3)
	  {
			var twMove:Tween;
            var guiBanane:MovieClip
		    guiBanane = _level0.loader.contentHolder.attachMovie("GUI_banane", "banane", 9998);
		    guiBanane._y = 200;
            guiBanane._x = 275;
			
		    _level0.loader.contentHolder["banane"].nomCible = " ";
	        _level0.loader.contentHolder["banane"].nomJoueurUtilisateur = adverName;
	       
	  }
  }// end method
  
  //*****************************************************************************************
	// this function is used to put on the Sprite the Timer of the Banana
	// after the time finished it must disapear
	function setBananaTimer(timeS:Number)
	{
		var perso:MyPersonnage = this;
				
		//first on put on the sprite the box for the timer
		var banBox:MovieClip = 	_level0.loader.contentHolder.attachMovie("bananaBox", "bananaBox", 7);
		
		 if(_level0.loader.contentHolder.objGestionnaireEvenements.getListLength() >= 11)
		 {
		    _level0.loader.contentHolder.bananaBox._x = 400;
		 }
		 else  if(_level0.loader.contentHolder.objGestionnaireEvenements.getListLength() < 11){
		    _level0.loader.contentHolder.bananaBox._x = 460;
		 }
		//_level0.loader.contentHolder.bananaBox._xscale = 90;
		_level0.loader.contentHolder.bananaBox._y = 304;
		
		_level0.loader.contentHolder.bananaBox.nameObject = _level0.loader.contentHolder.texteSource_xml.firstChild.attributes.bananaLabel;
		
		//create text field to put info in
		_level0.loader.contentHolder.bananaBox.createTextField("bananaTime", _level0.loader.contentHolder.branBox.getNextHigesthDepth(), 5, 18, 40, 30);
		
		 // Make the field dynamic text field
        _level0.loader.contentHolder.bananaBox.bananaTime.type = "dynamic";
        //_level0.loader.contentHolder.branBox.braniacTime.variable = "timeRest";
        with(_level0.loader.contentHolder.bananaBox.bananaTime)
        {
	       multiline = false;
	       background = false;
	       //text = "5";
	       textColor = 0xFFFFFF;
	       border = false;
	       _visible = true;
	       //autoSize = true;
        }
   
        var formatTimer:TextFormat = new TextFormat();
        formatTimer.bold = true;
        formatTimer.size = 21;
        formatTimer.font = "ArialBlack";
        formatTimer.align = "Center";
        _level0.loader.contentHolder.bananaBox.bananaTime.setNewTextFormat(formatTimer);
		
		perso.addBananaTime(timeS);
		
		if(_global.intervalIdBanana != null ) {
		          
			 clearInterval(_global.intervalIdBanana);
        }
		
	    _global.intervalIdBanana = setInterval(bananaTimerSet, 1000, perso);
		
		// to not see the timer if the game is ended
		if(_level0.loader.contentHolder.objGestionnaireEvenements.endGame)
		{
			 _level0.loader.contentHolder.bananaBox.removeMovieClip();
			 clearInterval(_global.intervalIdBanana);
		}
		
	   
	   function bananaTimerSet(playerUnder){	       
		   
		  playerUnder.decreaseBananaTime(); 
		  
		   if(_level0.loader.contentHolder.objGestionnaireEvenements.endGame)
		   {
			   playerUnder.setBananaTime(0);
		   }
		   var time:Number = playerUnder.getBananaTime(); 
		   _level0.loader.contentHolder.bananaBox.bananaTime.text = time;
		  		   
		   // to remove the timer box
		   if(time < 1)
		   {  
		      _level0.loader.contentHolder.bananaBox.removeMovieClip();		     	      
			  perso.addMoveSight(2);
			  _level0.loader.contentHolder.planche.setRepostCases(true);
			  
			  playerUnder.setBananaState(false);
			  clearInterval(_global.intervalIdBanana);
		   }
			
	   } // end function bananaTimerSet
		
	}// end function  setBananaTimer
	   	
	
} // end class