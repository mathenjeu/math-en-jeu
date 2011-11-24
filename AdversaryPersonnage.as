
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

Changed 2009 Oloieri Lilian
*********************************************************************/

import mx.transitions.Tween;
import mx.transitions.easing.*;
import flash.filters.*;
import mx.controls.Loader;
//import flash.filters.ColorMatrixFilter;

class AdversaryPersonnage implements IPersonnage
{
	// role of user if 1 - simple user , if 2 - master(admin)
	private var role:Number;             
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
	
	private var colorId:Number; 
	private var clothesColor:String;
	private var colorFilter:ColorMatrixFilter; // filter to color our perso 
	private var brainiacState:Boolean;
	private var brainiacRestedTime:Number;
	private var bananaId:Number;	
	private var bananaState:Boolean;
	private var bananaRestedTime:Number;	
	
	private var idClip:Number;           // number used to identify the movie used for perso - from 1 to 12
	private var orient:String;           // orientation ... right or left
	
	private static var BRAINIAC_TIME:Number = 60;    //  constant for the time that brainiac is active
	private var planche:PlancheDeJeu;
	private var level:Number;
	//used on Tournament to indicate that are on a finish line
	private var isOnFinish:Boolean;
	private var winGame:Boolean;
	
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

	
	public function getColorFilter():ColorMatrixFilter
	{
		return this.colorFilter;
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
		
	public function setBananaId(idNumber:Number)
	{
		this.bananaId = idNumber;
	}
	
	public function getBananaId():Number
	{
		return this.bananaId;
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
			
	public function setColor(n:String)
	{
		clothesColor = n;
		recalculateFilter();
	}
	
	public function getColor():String
	{
		return clothesColor;
	}
	
	public function recalculateColor()
	{
		this.clothesColor = _level0.loader.contentHolder.objGestionnaireEvenements.getColorByID(this.colorId, this.idClip);
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
   
	////////////////////////////////////////////////////////////
	public function obtenirNom():String
	{
		return this.nom;
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
	public function obtenirPointage():Number
	{
		return pointage;
	}
	
	////////////////////////////////////////////////////////////
	public function modifierPointage(x:Number)
	{
		pointage = x;
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
	public function getIdPersonnage():Number
	{
		return numero;
	}
	
	////////////////////////////////////////////////////////////
	public function setIdPersonnage(n:Number)
	{
		numero = n;
		trace("set id - adversary - " + n + " " + numero);
		if(n == 0)
		   setIDessin(0);
		else
		   setIDessin(UtilsBox.calculatePicture(n));
		
	}
		   	
	//////////////////////////////////////////////////////////////////////////////////////
	//	CONSTRUCTEUR
	//////////////////////////////////////////////////////////////////////////////////////
	function AdversaryPersonnage(idPers:Number, nom:String, role:Number, idClip:Number, color:Number, colorString:String)
	{
		//trace(" idPers : " + idPers + " nom : "+ nom + " role : " + role + " nomClip : " + nomClip + " ll : " + ll + " cc : " + cc + " cloColor : " + cloColor + "planches : " + planches);
			
		this.numero = idPers;
		this.colorId = color;
		this.idClip = idClip;
		this.clothesColor = colorString;		
        //this.colorFilter = UtilsBox.colorMatrixPerso(this.clothesColor, this.idClip);
			
		this.brainiacState = false;
		this.brainiacRestedTime = 0; 
		
		this.bananaState = false;
	    this.bananaRestedTime = 0;		
		this.orient = "right";
       				
		this.pointage = 0;
		this.argent = 0;
		this.listeDesObjets = new Object();
		
		this.faireCollision = null;
		this.nom = nom;
		this.role = role;
		this.image._visible = false;
		
	}// end constr
	
	 function goBored(){ 			
			   gotoAndPlay("bored");
			}
	
	public function initPlanche(planchet:PlancheDeJeu)
	{
		this.planche = planchet;
		this.level = _level0.loader.contentHolder.referenceLayer.getNextHighestDepth();//5 * planche.obtenirTableauDesCases().length * planche.obtenirTableauDesCases()[0].length + 2 * this.numero;
		trace("Level - " + level);
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
			
			_global.setTimeout(goBored, Math.random(5000));

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
	}	
	
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
					
		dx = this.prochainePosition.obtenirX() - this.position.obtenirX();  
		dy = this.prochainePosition.obtenirY() - this.position.obtenirY();
				
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
						faireCollision = null;						
					break;
				
					// Pour tous les objets, on se comporte de la meme maniere :
					case "Livre":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;				       
					break;
				
					case "Telephone":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;                        
					break;
				
					case "Boule":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;                      
					break;
									
					case "Banane":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;                        
					break;
					
					case "Brainiac":
					   	_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
						getBrainiacAnimaton(BRAINIAC_TIME);                        
					break;
				
					default :
						//trace("pas de collision");	
					break;
				}// switch(this.faireCollision)
							
			}// if(image._currentFrame != 1 && image._currentFrame < 90)
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
	
	public function showPersonnage()
	{
		image._visible = true;
	}
	
	public function removeImage()
    {
		_level0.loader.contentHolder.referenceLayer["Personnage" + numero].removeMovieClip();
		trace("we remove image from adversary ++++++++++");
		//removeMovieClip(image);
		//image.unloadMovie();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	////  pt contient la ligne et la colonne PAS LES X et Y
	public function definirProchainePosition(pt:Point, str:String)
	{
		_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].retirerPersonnage(this);
		
		this.l = pt.obtenirX();
		this.c = pt.obtenirY();
		//trace("definirProchainePosition : pt " + pt.obtenirY());
  
		_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].ajouterPersonnage(this);
		
		this.prochainePosition.definirX(_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].obtenirClipCase()._x);
		this.prochainePosition.definirY(_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].obtenirClipCase()._y);
		
		//trace("definirProchainePosition : def Y " + _level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].obtenirClipCase()._y);
		
		this.faireCollision = str;				
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
		image.gotoAndPlay("tossing");		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	public function slippingBanana()
	{
		//this.image.gotoAndStop("rest");
		image.gotoAndPlay("slipping");
		
		// in this 3 lines we transfer player from Brainiac state to Banana state
		setBrainiac(false);
		setBrainiacTime(0);
		setBananaState(true);		
	}
	
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
		  		 
		  endOnBrainiac();
		  
	   }else if(this.brainiacRestedTime > 0)
	   {
		   
	   }
	   
	   this.brainiacRestedTime += brainiacTime;	  
	} // end of getBraniacAnimation
	
	// used to put the Braniac animation on the player  for the specified time
	public function getReconnectionBrainiacAnimaton(brainiacTime:Number)
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
			target_mc.gotoAndPlay("bored");
			target_mc._visible = true;
						
          };
		  myLoader.addListener(mclListener);

          myLoader.loadClip("Perso/perso" + this.idClip + "brainiac.swf", image); 
		 	 
		  endOnBrainiac();
		  
	   }else if(this.brainiacRestedTime > 0)
	   {
		   
	   }
	   
	   this.brainiacRestedTime += brainiacTime;
	 	
	} // end of getReconnectionBraniacAnimation
	
	public function endOnBrainiac()
	{
		
		var playerUnder:AdversaryPersonnage = this;
		var id:Number = this.idClip;
		var restedTime:Number;
		var filterC:ColorMatrixFilter = this.colorFilter;
			   	  	    
	    var intervalIDEndBrain = setInterval(etapeEndBrain, 1000, playerUnder);	
		
		function etapeEndBrain():Void
		{    
		   var image:MovieClip = playerUnder.obtenirImage(); 
		   playerUnder.decreaseBrainiacTime();
		   restedTime = playerUnder.getBrainiacTime(); 
		             		  
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
			target_mc.nom = playerUnder.obtenirNom();
			target_mc.gotoAndPlay("bored");
			target_mc._visible = true;						
        };
		myLoader.addListener(mclListener);
		
	}//end endOnBrainiac
	
	private function cancelBanana()
	{
		bananaState = false;
	    bananaRestedTime = 1;		
	}
	
	public function correctStateBeforeBanane(adverName:String)
	{
		//???
	}
	
	// used for Banana action on the game
	public function tossBananaShell(playerTo:IPersonnage )
	{
	    // phase 1 - remove old shell_mc
	   //_level0.loader.contentHolder.referenceLayer.shell_mc.removeMovieClip();
	   // phase 1 - player toss banana
	   
	   playerTo.correctStateBeforeBanane(this.nom);
	   tossBanana();
	   
	   // phase 2 - banana shell fly to the player that support the action
	   var intervalId:Number;
	   var num:Number =  playerTo.getIdPersonnage();
	   var coorByX:Number =  obtenirX() - 10;// - getPersonnageByName(nameBy).obtenirImage()._width;
	   var coorByY:Number =  obtenirY() - obtenirImage()._height;
	      
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
		 
		 trace("fin attendre?");
	   }// end attendre
	 trace("fin toss banana shell?");
	 
	 // player is slipping 
	 var intervalIdToss:Number;
  	 intervalIdToss = setInterval(tossIt, 5000, playerTo);	
     function tossIt(){
		 clearInterval(intervalIdToss);
		  playerTo.slippingBanana();       
	 } 
	  	   
	}// end function
	
	
}