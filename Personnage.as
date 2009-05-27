
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
import flash.filters.*;


class Personnage
{
	private var image:MovieClip;
	private var position:Point;
	private var numero:Number;           // ????
	private var prochainePosition:Point;
	private var l:Number;
	private var c:Number;
	private var pointage:Number;
	private var argent:Number;
	private var listeDesObjets:Array;
	private var listeDesIDObjets:Array;	 // sert pour envoyer les commandes d'utilisation des objets au serveur ( on envoye les ID)
	private var faireCollision:String;   // sert a savoir s'il y a eu collision apres un deplacement et avec quoi
	private var nom:String;               // name of user that is master of pers
	private var boardCentre:Boolean;
	private var listeSurMagasin:Array;	 // sert a recuperer la liste d'objets du magasin lorsque qu'on va sur une case magasin
	private var nouveauID:Number;
	private var vieuxID:Number;
	private var minigameLoade:Boolean;
	private var SCALE_BOX_OBJET:Number = 75;
   
	////////////////////////////////////////////////////////////
	function obtenirNom():String
	{
		return this.nom;
	}
	       
	////////////////////////////////////////////////////////////
	function acheterObjet(o:ObjetSurCase, id:Number)
	{
		listeDesObjets.push(o);
		listeDesIDObjets.push(id);
	}
	
	////////////////////////////////////////////////////////////
	function afficherObjets()
	{
		var i:Number;
		trace("debut afficherObjets\n**********")
		for(i=0;i<listeDesObjets.length;i++)
		{
			trace(listeDesObjets[i].obtenirNom() + " - id : " + listeDesIDObjets[i]);
		}
		trace("**********\nfin afficherObjets")
	}
	
	////////////////////////////////////////////////////////////
	function afficherIDObjets()
	{
		var i:Number;
		for(i=0;i<listeDesIDObjets.length;i++)
		{
			trace(listeDesIDObjets[i]);
		}
	}
	
	////////////////////////////////////////////////////////////
	function setNouveauID(n:Number)
	{
		nouveauID = n;
	}
	
	////////////////////////////////////////////////////////////
	function setVieuxID(n:Number)
	{
		vieuxID = n;
	}
	
	////////////////////////////////////////////////////////////
	function getNouveauID():Number
	{
		return nouveauID;
	}
	
	////////////////////////////////////////////////////////////
	function getVieuxID():Number
	{
		return vieuxID;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirRangObjet(i:Number)
	{
		return listeDesObjets[i].obtenirNom();
	}
	
	////////////////////////////////////////////////////////////
	// retourne l'ID d'un objet au rang entre en parametre
	function obtenirRangIDObjet(i:Number)
	{
		return listeDesIDObjets[i];
	}
	
	////////////////////////////////////////////////////////////
	function obtenirNombreObjet()
	{
		return listeDesObjets.length;
	}
	
	////////////////////////////////////////////////////////////
	function ajouterObjet(o:ObjetSurCase, id:Number)
	{
		listeDesObjets.push(o);
		listeDesIDObjets.push(id);
	}
	
	////////////////////////////////////////////////////////////
	function obtenirObjets():Array
	{
		return this.listeDesObjets;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirListeDesIDObjets():Array
	{
		return this.listeDesIDObjets;
	}
	
	////////////////////////////////////////////////////////////
	// - sert a ajouter une image a la banque d'objets du personnage
	// - 10 tags sont places sur la frame de mathemaquoi afin de 
	// modifier la disposition des objets plus facilement
	// 
	function ajouterImageBanque(i:Number, nomObj:String, profondeur:Number, scale:Number)
	{		
		trace("--- ds ajouterImageBanque ! ---");
		trace(i);
		trace(nomObj);
		trace(profondeur);
		trace(scale);
	
		// dans mathemaquoi, les indices des tags sont de 1 a 10 : ils ne commencent pas a 0.
		_level0.loader.contentHolder.menuObjets.createEmptyMovieClip("objCase" + i, profondeur);
		//_level0.loader.contentHolder.menuObjets["objCase" +i].attachMovie(nomObj, nomObj, profondeur, {_x:_level0.loader.contentHolder["tag" + i]._x, _y:_level0.loader.contentHolder["tag" +i]._y, _xscale:scale, _yscale:scale});
		_level0.loader.contentHolder.menuObjets["objCase"+i].attachMovie(nomObj, nomObj, profondeur, {_x:_level0.loader.contentHolder.menuObjets["tag"+i]._x, _y:_level0.loader.contentHolder.menuObjets["tag"+i]._y, _xscale:scale, _yscale:scale});
		_level0.loader.contentHolder.menuObjets["objCase"+i]._visible = false;
		//_level0.loader.contentHolder.menuObjets["x" + i]._visible = false;
		
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
		
		var listeObjets:Array = this.obtenirObjets();
		var listeIDObjets:Array = this.obtenirListeDesIDObjets();
		_level0.loader.contentHolder.menuObjets["objCase"+i].onRelease = function()
		{
			if(peutUtiliserObjet(nomObj))
			{
				var j:Number;
				for(j=0; j<listeObjets.length && nomObj != listeObjets[j].nom; j++);
				
				if(j<listeObjets.length)
				{
					_level0.loader.contentHolder.objGestionnaireEvenements.utiliserObjet(listeIDObjets[j]);
					_level0.loader.contentHolder.planche.obtenirPerso().enleverObjet(listeObjets[j].nom);
				}
			}
		};
		_level0.loader.contentHolder.menuObjets["objCase"+i].onRollOver = function()
		{
			if(peutUtiliserObjet(nomObj)) _level0.loader.contentHolder.menuObjets["objCase"+i]._alpha = 60;
		};
		_level0.loader.contentHolder.menuObjets["objCase"+i].onRollOut = function()
		{
			if(peutUtiliserObjet(nomObj)) _level0.loader.contentHolder.menuObjets["objCase"+i]._alpha = 100;
		};
		
		trace("--- FIN ajouterImageBanque ! ---");
	}
	
	////////////////////////////////////////////////////////////
	// on enleve l'image d'objet au rang indique a l'appel de la fonction
	//
	function enleverImageBanque(i:Number)
	{
		_level0.loader.contentHolder.menuObjets["objCase" + i].removeMovieClip();
		//_level0.loader.contentHolder.menuObjets["x" + this.obtenirNombreObjet()+1]._visible = true;
	}
	
	////////////////////////////////////////////////////////////
	// -sert a enlever un objet a un personnage :
	// on enleve les donnees relatives a cet objet dans les listes
	// du personnage. on enleve ensuite l'image de l'objet
	//
	function enleverObjet(n:String)
	{
		var i:Number;
		
		trace("liste d'objets avant avoir enlever 1 obj :");
		for(i=0;i<listeDesObjets.length;i++)
		{
			trace("i : " + i + " => " + listeDesObjets[i].obtenirNom());
		}
		
		for(i=0;i<listeDesObjets.length;i++)
		{
			if(listeDesObjets[i].obtenirNom() == n)
			{
				trace("dans le if " + n + " = " + listeDesObjets[i].obtenirNom() + " et i : " + i);
				listeDesObjets.splice(i,1);
				listeDesIDObjets.splice(i,1);
				
				enleverImageBanque(i+1);
		
				// pour que la disposition des objets reste correcte,
				// on decale les objets.
				// ex : on a 3 objets et on enleve celui du milieu
				// alors l'objet3 va a la position 2 et on decale les image
				//for (var j:Number = i+1; j <= this.obtenirNombreObjet()+1; j++)
				for (var j:Number = i+1; j <= this.obtenirNombreObjet(); j++)
				{
					var k:Number = j+1;
					//trace(this.obtenirRangObjet(j-1));
					//ajouterImageBanque(j, this.obtenirRangObjet(j-1), this.obtenirRangIDObjet(j-1)+j, SCALE_BOX_OBJET);
					ajouterImageBanque(j, this.obtenirRangObjet(j-1), j, SCALE_BOX_OBJET);
					enleverImageBanque(k);
				}
				
				// une fois qu'on a trouve l'objet, on quitte la fonction
				n = "---";
				break;
			}
		}
		//enleverImageBanque(k+1);
		
		trace("liste d'objets apres avoir enlever 1 obj :");
		for(i=0;i<listeDesObjets.length;i++)
		{
			trace("i : " + i + " => " + listeDesObjets[i].obtenirNom());
		}
	}
	
	////////////////////////////////////////////////////////////
	function zoomer(valeur:Number)
	{		
		image._xscale += valeur;
		image._yscale += valeur;
	}
	
	////////////////////////////////////////////////////////////
	function translater(la:Number, ha:Number)
	{
		image._x += la;
		image._y += ha;
		
		this.position.definirX(position.obtenirX()+la);
		this.position.definirY(position.obtenirY()+ha);
		this.prochainePosition.definirX(prochainePosition.obtenirX()+la);
		this.prochainePosition.definirY(prochainePosition.obtenirY()+ha);
	}
	
	////////////////////////////////////////////////////////////
	function obtenirL():Number
	{
		return l;
	}
	
	////////////////////////////////////////////////////////////
	function definirL(ll:Number)
	{
		l = ll;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirC():Number
	{
		return c;
	}
	
	////////////////////////////////////////////////////////////
	function definirC(cc:Number)
	{
		c = cc;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirNumero():Number
	{
		return numero;
	}
	
	////////////////////////////////////////////////////////////
	function definirNumero(n:Number)
	{
		numero = n;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirPointage():Number
	{
		return pointage;
	}
	
	////////////////////////////////////////////////////////////
	function modifierPointage(x:Number)
	{
		pointage = x;
		_level0.loader.contentHolder.pointageJoueur = x;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirArgent():Number
	{
		return argent;
	}
	
	////////////////////////////////////////////////////////////
	function modifierArgent(x:Number)
	{
		argent = x;
		_level0.loader.contentHolder.argentJoueur = x;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirImage():MovieClip
	{
		return image;
	}
	
	////////////////////////////////////////////////////////////
	function definirImage(i:MovieClip)
	{
		image = i;
	}
	
	////////////////////////////////////////////////////////////
	function obtenirPosition():Point
	{
		return position;
	}
	
	////////////////////////////////////////////////////////////
	function definirPosition(p:Point, ll:Number, cc:Number)
	{
		image._x = p.obtenirX();
		image._y = p.obtenirY();
		this.position.definirX(p.obtenirX());
		this.position.definirY(p.obtenirY());
		this.l = ll;
		this.c = cc;
	}

	////////////////////////////////////////////////////////////
	function obtenirProchainePosition():Point
	{
		return prochainePosition;
	}
	
	////////////////////////////////////////////////////////////
	// retourne la liste des objets que le magasin ou on se trouve contient
	//
	function obtenirMagasin():Array
	{
		return listeSurMagasin;
	}
	
	////////////////////////////////////////////////////////////
	// fixe les objets contenus dans un magasin
	//
	function definirMagasin(mag:Array)
	{
		listeSurMagasin = mag;
	}
	
	////////////////////////////////////////////////////////////
	// defini le ID d'un object. appele apres l'achat
	//
	function definirCodeIDobjet(nouveauID:Number, vieuxID:Number)
	{
		trace(listeDesIDObjets);
		
		for(var i:Number = 0; i < listeDesIDObjets.length; i++)
		{
			if(listeDesIDObjets[i] == vieuxID)
			{
				listeDesIDObjets[i] = nouveauID;
			}
		}
		trace(listeDesIDObjets);
	}
	
	////////////////////////////////////////////////////////////
	// cette fonction change l'echelle d'une image lorsqu'on utilise
	// une potion ( bleue ou rouge : grandit ou rapetisse )
	// la potion fait effet pour 30 secondes. apres, on rappelle la
	// la fonction qui retourne a la normale.
	// on utilise des tween pour une transition plus amusante !
	//
	function shrinkBonhommeSpecial(mClip1:MovieClip, x:Number, y:Number)
	{
		var twMove1:Tween;
		var twMove2:Tween;
		var intervalId:Number;
		var cpt:Number = 0;
		var fctPerso:Object = this;	// on garde en memoire qu'on est dans la classe personnage pour etre capable d'utiliser ses fonctions

		intervalId = setInterval(attendre, 1000, cpt);	// sert pour attendre la jusqu'a la fin de l'effet de la potion
		
		twMove1 = new Tween(mClip1, "_xscale", Bounce.easeIn, 100, x, 2, true);
		twMove2 = new Tween(mClip1, "_yscale", Bounce.easeIn, 100, y, 2, true);
		
		// cette fonction attend jusqu'au signal du compteur
		// et appelle le retour au format normal
		function attendre():Void
		{
			var maxCpt:Number = 30;
			
			if(cpt >= maxCpt) 
			{
				clearInterval(intervalId);
				fctPerso.shrinkBonhommeNormal(mClip1, x, y);
			} 
			cpt++;
		}
	}

	////////////////////////////////////////////////////////////
	// Cette fonction ramene la taille du mClip a sa taille normale
	// prend les valeurs x et y pour l'echelle
	// - le tween permet une transition plus amusante !
	//
	function shrinkBonhommeNormal(mClip1:MovieClip, x:Number, y:Number)
	{
		var twMove1:Tween;
		var twMove2:Tween;
		
		twMove1 = new Tween(mClip1, "_xscale", Bounce.easeIn, x, 100, 2, true);
		twMove2 = new Tween(mClip1, "_yscale", Bounce.easeIn, y, 100, 2, true);
	}

	////////////////////////////////////////////////////////////
	// cette fonction transforme des donnees stockees dans un array
	// dans un XMLNode.
	// ca permet de garder en memoire les objets contenus dans un magasin,
	// leur ID et leur cout
	//
	function genererListeMagasinXML(lstObjMagasin:Array):Object
	{
		var objMagasin:Object = new Object();
		
		for(var j:Number = 0; j<lstObjMagasin.length; j++)
		{
			var objNoeudObjMagasin:XMLNode = lstObjMagasin[j];
									
			objMagasin["objet"+j] = new Object();
			objMagasin["objet"+j].cout = objNoeudObjMagasin.attributes.cout;
			objMagasin["objet"+j].id = objNoeudObjMagasin.attributes.id;
			objMagasin["objet"+j].type = objNoeudObjMagasin.attributes.type;
		
			trace("id : " + objMagasin["objet"+j].id);
		}
		return objMagasin;
	}
	
	function genererListeMagasinXML2(lstObjMagasin:Array, nouveauID:Number, vieuxID:Number):Object
	{
		var objMagasin:Object = new Object();
		
		//trace("newID : " + nouveauID);
		//trace("vieuxID : " + vieuxID);
		
		for(var j:Number = 0; j<lstObjMagasin.length; j++)
		{
			var objNoeudObjMagasin:XMLNode = lstObjMagasin[j];
									
			objMagasin["objet"+j] = new Object();
			objMagasin["objet"+j].cout = objNoeudObjMagasin.attributes.cout;
			objMagasin["objet"+j].type = objNoeudObjMagasin.attributes.type;
			
			if(vieuxID == objNoeudObjMagasin.attributes.id)
			{
				objMagasin["objet"+j].id = nouveauID;
			}
			else
			{
				objMagasin["objet"+j].id = objNoeudObjMagasin.attributes.id;
			}
		
			//trace("id ds generer #2 : " + objMagasin["objet"+j].id);
		}
		return objMagasin;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	//	CONSTRUCTEUR
	//////////////////////////////////////////////////////////////////////////////////////
	function Personnage(nom:String, niveau:Number, nomClip:String, ll:Number, cc:Number, xx:Number, yy:Number, mag:Array)
	{
		this.l = ll;
		this.c = cc;
		this.numero = niveau;
		this.position = new Point(xx,yy);                       
		this.prochainePosition = new Point(xx + 1,yy + 1);
		
		
		//trace("Personnage "+nom+" nomClip="+nomClip+"  niveau="+niveau);
		if(!(_level0.loader.contentHolder.objGestionnaireEvenements.controlForMaster(nom))) 
		   this.image = _level0.loader.contentHolder.referenceLayer.attachMovie(nomClip, "Personnage"+niveau, niveau);
		
		this.image.dText.nom = nom;
		this.image.nom = nom;
		this.image._visible = false;
		this.pointage = 0;
		this.argent = 0;
		this.listeDesObjets = new Array();
		this.listeDesIDObjets = new Array();
		this.faireCollision = null;
		this.nom = nom;
		this.listeSurMagasin = mag;
		this.minigameLoade = false;
	    
		
		
		
		}
	
	
	////////////////////////////////////////////////////////////
	function deplacePersonnage()
	{
		var pourcent:Number;
		var dx:Number;
		var dy:Number;
		var reafficher1:Boolean = true;
		var reafficher2:Boolean = false;
		
		//trace("ds deplacePersonnage");
		
		dx = this.prochainePosition.obtenirX() - this.position.obtenirX();  
		dy = this.prochainePosition.obtenirY() - this.position.obtenirY();
		
		if( boardCentre) //dx == 0 && dy == 0 && image._currentFrame == 1) 
		{
			return;
		}
		
		if ((dx == 0) && (dy == 0))
		{
			if(image._currentFrame != 1)
			{
				// place le personnage au repos et de face
				this.image.gotoAndStop(1);
		
				switch(this.faireCollision)
				{
					case "piece":
						// l'argent est ajoute au personnage dans les fichiers gestEve et gestComm
						// en gros, ici, on enleve l'image du board
				
						/////////////////////////////////////////
						//fuck : si c'est pas moi qui tombe dessus la piece
						//aussi, est-ce que les ordis utilisent des pieces ?
						/////////////////////////////////////////
					
						/*if(this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							if(this.obtenirNombreObjet() < 11)
							{					
								ajouterImageBanque(this.obtenirNombreObjet(), "pieceFixe", this.obtenirNombreObjet(), SCALE_BOX_OBJET);
							}
						}*/
	
						_level0.loader.contentHolder.planche.enleverPiece(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -10000);
						this.faireCollision = null;
					break;
				
					case "magasin":
						//si notre personnage tombe sur un magasin, on charge le GUI_magasin
						if(this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							this.minigameLoade = true;
							reafficher1 = false;
							_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
							_root.objGestionnaireInterface.effacerBoutons(1);			
							
							// on charge le magasin : on ajuste sa position a l'ecran
							// on indique qu'il ne s'agit pas d'un mini-game.
						
							var minigame:MovieClip;   
							minigame = _level0.loader.contentHolder.miniGameLayer.attachMovie("GUI_magasin", "magasin", 9997);
							minigame._x = -6;
							minigame._y = -20;
							minigame._xscale = 115;
							minigame._yscale = 115;
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
								_level0.loader.contentHolder.enteteHolder._visible = false;
								_level0.loader.contentHolder.horloge._visible = false;
								minigame._visible = true;
							}
						
							this.faireCollision = null;
						}
						else
						{
							faireCollision = null;
						}
					break;
				
					// Pour tous les objets, on se comporte de la meme maniere :
					// on enleve l'objet (meme si on en a 10 : parce que le serveur nous dit qu'on l'enleve)
					// si necessaire, on l'ajoute a nos objets
					
					case "Livre":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;
					
						if(this.obtenirNombreObjet() < 11 && this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							ajouterImageBanque(this.obtenirNombreObjet(), "Livre", this.obtenirNombreObjet(), SCALE_BOX_OBJET);
						}
					break;
				
					case "Telephone":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;

						if(this.obtenirNombreObjet() < 11 && this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							ajouterImageBanque(this.obtenirNombreObjet(), "Telephone", this.obtenirNombreObjet(), SCALE_BOX_OBJET);
						}
					break;
				
					case "Papillon":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;

						if(this.obtenirNombreObjet() < 11 && this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							ajouterImageBanque(this.obtenirNombreObjet(), "Papillon", this.obtenirNombreObjet(), SCALE_BOX_OBJET);
						}
					break;
				
					case "Boule":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;

						if(this.obtenirNombreObjet() < 11 && this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							ajouterImageBanque(this.obtenirNombreObjet(), "Boule", this.obtenirNombreObjet(), SCALE_BOX_OBJET);
						}
					break;
				
					case "PotionGros":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;

						if(this.obtenirNombreObjet() < 11 && this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							ajouterImageBanque(this.obtenirNombreObjet(), "PotionGros", this.obtenirNombreObjet(), SCALE_BOX_OBJET);
						}
					break;
				
					case "PotionPetit":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;

						if(this.obtenirNombreObjet() < 11 && this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							ajouterImageBanque(this.obtenirNombreObjet(), "PotionPetit", this.obtenirNombreObjet(), SCALE_BOX_OBJET);
						}
					break;
				
					case "Banane":
						_level0.loader.contentHolder.planche.enleverObjet(this.l, this.c);
						_level0.loader.contentHolder.planche.modifierNumeroCase(this.l, this.c, -30000);
						this.faireCollision = null;

						if(this.obtenirNombreObjet() < 11 && this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
						{
							ajouterImageBanque(this.obtenirNombreObjet(), "Banane", this.obtenirNombreObjet(), SCALE_BOX_OBJET);
						}
					break;
				
					default :
						//trace("pas de collision");	
					break;
				}
			
				if(this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
				{
					if(_level0.loader.contentHolder.planche.estCaseSpeciale(this.l, this.c) &&  _level0.loader.contentHolder.sortieDunMinigame == false)
					{
						this.minigameLoade = true;
						 _level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_root.objGestionnaireInterface.effacerBoutons(1);
					
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
								minigame._x = 8;
								minigame._y = -30;
								minigame._width = 800;
								minigame._height= 550;
							break;
							
							case "balleAuMur2.swf":
								minigame._x = 0;
								minigame._y = 40;
								minigame._width = 570;
								minigame._height= 410;
							break;
							
							default:
								minigame._x = 0;
								minigame._y = 0;
								minigame._width = 550;
								minigame._height= 400;
							break;
						}
						
						twMove = new Tween(mClip1, "_alpha", Regular.easeOut, 0, 100, 1, true);
						twMove.addListener(oListener);
	
						oListener.onMotionFinished = function():Void 
						{ 
							twMove2 = new Tween(mClip1, "_alpha", Regular.easeOut, 100, 0, 1, true);
							minigame._visible = true;
						}
					}
					else reafficher2 = true;
				}
			}
			if(reafficher1 && reafficher2) this.minigameLoade = false;
			//Si le perso est le mien et qu'il est au repos, mais que le board n'est pas centre
			
			if(this.nom == _level0.loader.contentHolder.planche.obtenirNomDeMonPersonnage())
			{
				if(_level0.loader.contentHolder.planche.recentrerBoard(this.l, this.c, true))
				{
					if(!this.minigameLoade) _level0.loader.contentHolder.planche.afficherCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
					boardCentre = true;
					trace(_level0.loader.contentHolder.horlogeNum + "temps restant");
					
				}
			} 
			
			return; // pour ne pas faire le reste des verifications inutilement si dx == dy == 0
		}
		
		//pour le reste, ((dx == 0)&&(dy == 0) != 1)
		
		if(Math.abs(dx) > Math.abs(dy))
		{
			if(Math.abs(dx) > 10)
			{
				pourcent = 10/Math.abs(dx);
				dx *= pourcent;
				dy *= pourcent;
			}
		}
		else
		{
			if(Math.abs(dy) > 10)
			{
				pourcent = 10/Math.abs(dy);
				dx *= pourcent;
				dy *= pourcent;
			}
		}

		if (dy < 0)
		{
			if(this.image._currentFrame<70)
			{
				this.image.gotoAndPlay(70);
			}
			
		}
		else
		{
			if(this.image._currentFrame>69)
			{
				this.image.gotoAndPlay(10);
			}
		}
				
		if (dx >= 0)
		{
			// assure que le clip a la bonne orientation
			image._xscale = - Math.abs(image._xscale);
			image.dtNom._xscale = - Math.abs(image._xscale);
			image.dtNom._x = 42;
		}
		else
		{
			// flip le clip pour aller vers la gauche
			image._xscale =  Math.abs(image._xscale);
			image.dtNom._xscale =   Math.abs(image._xscale);
			image.dtNom._x = - 42;
		}
		
		
		// deplace le clip
		this.image._x += dx;  
		this.image._y += dy;
		position.definirX(position.obtenirX()+dx);
		position.definirY(position.obtenirY()+dy);
	
		// Si le deplacement voulu n'est pas nul mais que le personnage est au repos
		//trace("avant le if le frame  :   "+this.image._currentFrame);
		if (((dx != 0) || (dy != 0)) && (this.image._currentFrame == 1))
		{
			// place le clip du personnage au debut de la sequence de deplacement
			this.image.gotoAndPlay(10);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	function afficher()
	{
		image._visible = true;
		image._x = position.obtenirX();  
		image._y = position.obtenirY();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	function cachePersonnage()
	{
		image._visible = false;
	}	
	
	//////////////////////////////////////////////////////////////////////////////////////
	////  pt contient la ligne et la colonne PAS LES X et Y
	function definirProchainePosition(pt:Point, str:String)
	{
		_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].retirerPersonnage(this);
		
		this.l = pt.obtenirX();
		this.c = pt.obtenirY();

		_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].ajouterPersonnage(this);
		
		this.prochainePosition.definirX(_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].obtenirClipCase()._x);
		this.prochainePosition.definirY(_level0.loader.contentHolder.planche.tableauDesCases[this.l][this.c].obtenirClipCase()._y);
		
		this.faireCollision = str;
		this.boardCentre = false;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	function obtenirX():Number
	{
		return position.obtenirX();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	function obtenirY():Number
	{
		return position.obtenirY();
	}
		
	//////////////////////////////////////////////////////////////////////////////////////
	function rire()
	{
		image.gotoAndPlay("rire");
	}
	
}