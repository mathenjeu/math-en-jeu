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

last change - 2010 - Oloieri Lilian
*********************************************************************/

import flash.geom.Transform;
import flash.geom.ColorTransform;
import mx.transitions.Tween;
import mx.transitions.easing.*;
import mx.utils.Delegate;
import mx.transitions.Transition;


class PlancheDeJeu
{
	private var mat:Array;
    private var tableauDesCases:Array = new Array();
    private var hauteurDeCase:Number = -1;
    private var largeurDeCase:Number = -1;
    
	// reference from GestionnaireEvenements to our personnage
	private var perso:MyPersonnage; 
	
	private var zoom:Number = 0;
    private var rotation:Number = 0;
    private var intervalCol:Number;
	private var showCases:Boolean;  // if is true the casesPossibles is in sight
	private var repostCases:Boolean; // if is true we need to restart casesPossibles because we have changes
	private var tempoSight:Number;
	//private var manager:GestionnaireEvenements;
	
	public function setTempoSight(tempo:Number)
	{
		this.tempoSight = tempo;
		
	}
	
	public function getTempoSight():Number
	{
		return this.tempoSight;
	} 
	
	public function setRepostCases(repost:Boolean)
	{
		this.repostCases = repost;
	}
	
	public function getRepostCases():Boolean
	{
		return this.repostCases;
	} 
	
	public function getShowCases():Boolean
	{
		return this.showCases;
	}
	
	public function setShowCases(showIt:Boolean)
	{
		this.showCases = showIt;
	}
	
    
    public function obtenirPerso():MyPersonnage
    {
        return this.perso;
    }
	
	public function setPerso(p:MyPersonnage):Void
    {
        perso = p;
    }
	    
	public function getNumeroMagasin(ll:Number, cc:Number):Number
	{
		return this.tableauDesCases[ll][cc].obtenirNumMagasin();
	}
	
	public function obtenirTableauDesCases():Array
    {
        return this.tableauDesCases;
    }
	
	public function getCase(l:Number, c:Number):Case
    {
        return this.tableauDesCases[l][c];
    }
    
	public function obtenirMat():Array
    {
        return this.mat;
    }
    

    ////////////////////////////////////////////////////////////
    //  CONSTUCTEUR
    ////////////////////////////////////////////////////////////
    function PlancheDeJeu(tab:Array , gest:GestionnaireEvenements)
    {
        var i:Number;
		this.mat = new Array(tab.length);
		
        for(i in tab)
        {
			this.mat[i] = new Array(tab[i].length);
            this.tableauDesCases.push(new Array());
        }
        definirMat(tab, null);
        //var idDessin:Number =((num - 10000)-(num - 10000) % 100)/100;
		//var idPers:Number = num - 10000 - idDessin * 100;
        //monPersonnage = idPers;        
       	this.tempoSight = 3;
		//this.manager = gest;		
    }	
	
    public function obtenirNombreDeColonnes():Number
    {
        return this.mat[0].length;
    }

   
    // a revoir, pour tout de suite ca sert quand on enleve une piece
    public function modifierNumeroCase(l:Number, c:Number, num:Number)
    {
	    // si on vient d'enlever un objet il faut mettre comme nouvelle valeur juste la couleur de la case (deux derniers chiffres)
	    if(num == -30000 || num == -10000)
	    {
			this.mat[l][c] = this.mat[l][c]%100;    
	    }
	    else
	    {
		    this.mat[l][c] += num;
	    }
    }
    
       
    public function enleverPiece(l:Number, c:Number)
    {
        this.tableauDesCases[l][c].effacerPiece();
    }
	
	
	///////////////////////////////////////////////////////////////////////////
	public function obtenirObjet(l:Number, c:Number):ObjetSurCase
    {
        return this.tableauDesCases[l][c].obtenirObjet();
    }
	
	
	public function enleverObjet(l:Number, c:Number)
    {
        this.tableauDesCases[l][c].effacerObjet();
    }
	

    public function obtenirNombreDeLignes():Number
    {
        return this.mat.length;
    }
   
   	// cette fonction affiche la planche de jeu initiale,
    // idealement il faudrait que les 4 coins soient toujours presents et pas plus de 9998 cases
    public function afficher()
    {
        var i:Number;
        var j:Number;
        var nouvelleCase:Case;
        var pt:Point = new Point(0, 0);
        var clipTest:MovieClip;
        var x:Number;
        var y:Number;
        var temp:Number;
        // ici on veut juste determiner la hauteur et la largeur des cases  //////////////////////////
        clipTest = _level0.loader.contentHolder.referenceLayer.attachMovie("case0", "case", 0);
        clipTest._x = -100;
        clipTest._y = -100;
        largeurDeCase = clipTest._width;
        hauteurDeCase = clipTest._height * 0.850111857;
        clipTest.removeMovieClip();
		
        ////////////////////////////////////////////////////////////////////////////////////////
		i = 0;
        for(var l in this.mat)
        {
			x = i*largeurDeCase/2 + largeurDeCase/2;
            y = 200 + i*hauteurDeCase/2;
			j = 0;
            for(var s in this.mat[0])
            {
                pt.definirX(x);
                pt.definirY(y);
                if(this.mat[i][j] != 0)
                {
            		//trace("dans afficher la planche de jeu, i   j   numero de la case :  "+i+"   "+j+"   "+this.mat[i][j]);
                    nouvelleCase = new Case(this.mat[i][j], i, j, mat.length, this.mat[0].length);
                    this.tableauDesCases[i][j] = nouvelleCase;
                    nouvelleCase.afficher(pt);
                }
                else
                {
                    if((i==0 && j==0) || (i==this.mat.length-1 && j==0) || (i==0 && j==this.mat[0].length-1) || (i==this.mat.length-1 && j==this.mat[0].length-1))
                    {
                        //mat[i][j] = -1;
                        nouvelleCase = new Case(-1, i, j, mat.length, this.mat[0].length);
                        this.tableauDesCases[i][j] = nouvelleCase;
                        nouvelleCase.afficher(pt);
                    }
                }
                x = x + largeurDeCase/2;
                y = y - hauteurDeCase/2;
				j++;
            }
			i++;
        }
		repostCases = true;		
    }

    
    ////////////////////////////////////////////////////////////////////////////////////
    public function definirMat(tab:Array, tdc:Array)
    {
        var i:Number;
        var j:Number;
        // on initie les tableaux 
		for(i in tab)
        {
			for(j in tab[0])
            {
                this.mat[i][j] = tab[i][j];
                //trace("ds definirMat  mat   tab i  j  mat  tab :    "+i+"   "+j+"   "+this.mat[i][j]+"     "+tab[i][j]);
                if(tdc == null)
                {
                    this.tableauDesCases[i][j] = null;
                }
                else
                {
                    this.tableauDesCases[i][j] = tdc[i][j];
                    this.tableauDesCases[i][j].definirL(i);
                    this.tableauDesCases[i][j].definirC(j);
                }
            }
        }
    }
	
    public function translater(direction:String):Boolean
    {
        var la:Number;
        var ha:Number;
		var limiteAtteinte:Boolean = false;
		
		var coinGauche = new Point(0,0);
		var coinDroit = new Point(0,0);
		var coinHaut = new Point(0,0);
		var coinBas = new Point(0,0);
		
		trace("zoom " + this.zoom);
		coinGauche.definirX(_level0.loader.contentHolder.referenceLayer._x + (10+this.zoom)/10*this.tableauDesCases[0][0].obtenirClipCase()._x);
		coinGauche.definirY(_level0.loader.contentHolder.referenceLayer._y + (10+this.zoom)/10*this.tableauDesCases[0][0].obtenirClipCase()._y);
		coinHaut.definirX(coinGauche.obtenirX() + this.largeurDeCase/2 * (this.tableauDesCases[0].length-1));
		coinHaut.definirY(coinGauche.obtenirY() - this.hauteurDeCase/2 * (this.tableauDesCases[0].length-1));
		coinDroit.definirX(coinHaut.obtenirX() + this.largeurDeCase/2 * (this.tableauDesCases.length-1));
		coinDroit.definirY(coinHaut.obtenirY() + this.hauteurDeCase/2 * (this.tableauDesCases.length-1));
		coinBas.definirX(coinGauche.obtenirX() + this.largeurDeCase/2 * (this.tableauDesCases.length-1));
		coinBas.definirY(coinGauche.obtenirY() + this.hauteurDeCase/2 * (this.tableauDesCases.length-1));
	
        switch(direction)
        {
            case "Est":
                la = largeurDeCase/2;
				if (coinDroit.obtenirX()+la < 275) //Le coin droit du tableau est au centre de l'ecran
				{
					la = 275 - coinDroit.obtenirX();
					limiteAtteinte = true;
				}
                ha = 0;
            break;
            case "Ouest":
                la = -largeurDeCase/2; 
				if (coinGauche.obtenirX()+la > 275) //Le coin gauche du tableau est au centre de l'ecran
				{
					la = 275 - coinGauche.obtenirX();
					limiteAtteinte = true;
				}
                ha = 0;
            break;
            case "Nord":
                la = 0;
                ha = -hauteurDeCase/2;
				if (coinHaut.obtenirY()+ha > 200) //Le coin haut du tableau est au centre de l'ecran
				{
					ha = 200 - coinHaut.obtenirY();
					limiteAtteinte = true;
				}
            break;
            case "Sud":
                la = 0;
                ha = hauteurDeCase/2;
				if (coinBas.obtenirY()+ha < 200) //Le coin bas du tableau est au centre de l'ecran
				{
					ha = 200 - coinBas.obtenirY();
					limiteAtteinte = true;
				}
            break;
        }
	
		// on deplace le clip sur lequel est attache tous les autres clips
		_level0.loader.contentHolder.referenceLayer._x += la;		
		_level0.loader.contentHolder.referenceLayer._y += ha;
		
		return !(limiteAtteinte);
    }

    //*************************************************
    public function zoomer(sens:String, fois:Number):Boolean
    {
		var distX:Number;
		var distY:Number;
		
		switch(sens)
		{
			case "in":
			     
				if(this.zoom < 8)
				{
					fois = 8 - this.zoom >= fois ? fois : (8 - this.zoom);
					this.zoom = this.zoom + fois; 
					//this.zoom++;
					
					// on zoom le clip sur lequel est attache tous les autres clips
					_level0.loader.contentHolder.referenceLayer._xscale += 10 * fois;
					_level0.loader.contentHolder.referenceLayer._yscale += 10 * fois;
				
					// on deplace le clip sur lequel est attache tous les autres clips
					distX = 275 - _level0.loader.contentHolder.referenceLayer._x;
					distX *= (10+this.zoom)/(9+this.zoom); 
					_level0.loader.contentHolder.referenceLayer._x = 275 - distX;
					distY = 200 - _level0.loader.contentHolder.referenceLayer._y;
					distY *= (10+this.zoom)/(9+this.zoom); 
					_level0.loader.contentHolder.referenceLayer._y = 200 - distY;
					
					// on modifie largeurDeCase et hauteurDeCase
					this.largeurDeCase *= (10+this.zoom)/(9+this.zoom);
					this.hauteurDeCase *= (10+this.zoom)/(9+this.zoom);
				}
			break;
				
			case "out":
				if(this.zoom > -8)
				{
					fois = Math.abs(- 8 - this.zoom) >= fois ? fois : Math.abs(- 8 - this.zoom);
					this.zoom = this.zoom - fois; 
					//this.zoom--; 
					
					// on zoom le clip sur lequel est attache tous les autres clips
					_level0.loader.contentHolder.referenceLayer._xscale -= 10 * fois;
					_level0.loader.contentHolder.referenceLayer._yscale -= 10 * fois;
					
					// on deplace le clip sur lequel est attache tous les autres clips
					distX = 275 - _level0.loader.contentHolder.referenceLayer._x;
					distX *= (10+this.zoom)/(11+this.zoom); 
					_level0.loader.contentHolder.referenceLayer._x = 275 - distX;
					distY = 200 - _level0.loader.contentHolder.referenceLayer._y;
					distY *= (10+this.zoom)/(11+this.zoom); 
					_level0.loader.contentHolder.referenceLayer._y = 200 - distY;
					
					// on modifie largeurDeCase et hauteurDeCase
					this.largeurDeCase *= (10+this.zoom)/(11+this.zoom);
					this.hauteurDeCase *= (10+this.zoom)/(11+this.zoom);
				}
			break;
		
			default:
			break;
		}
		
		if(this.zoom == -8 || this.zoom == 8) return false;
		return true;
    }
   
    public function obtenirRotation():Number
    {
        return rotation;
    }
        
	// cette fonction prend en entree un pt du board original et retourne la ligne et la colonne dans le board tourne
	// a appliquer a tous les pt donnes par le serveur
    public function calculerPositionTourne(ll:Number, cc:Number):Point
    {
        var pt:Point;
        var i:Number;
        var a:Number;
        var b:Number;
        var temp:Number;
        a = ll;
        b = cc;
        switch(this.rotation)
        {
            case 1:
                temp = b;
                b = a;
                a = mat.length-1-temp;
            break;
            case 2:
                temp = b;
                b = a;
                a = mat[0].length-1-temp;
                temp = b;
                b = a;
                a = mat.length-1-temp;
            break;
            case 3:
                temp = b;
                b = a;
                a = mat.length-1-temp;
                temp = b;
                b = a;
                a = mat[0].length-1-temp;
                temp = b;
                b = a;
                a = mat.length-1-temp;
            break;
        }
        pt = new Point(a,b);
        return pt;
    }

    
    // cette fonction prend en entree un pt du board tourne et retourne la ligne et la colonne dans le board original
    // a appliquer a tous les points donnes au serveur
    public function calculerPositionOriginale(ll:Number, cc:Number):Point
    {
        var pt:Point;
        var i:Number;
        var a:Number;
        var b:Number;
        var temp:Number;
        a = ll;
        b = cc;
        switch(this.rotation)
        {
            case 1:
                temp = a;
                a = b;
                b = mat.length-1-temp;
            break;
            case 2:
                temp = a;
                a = b;
                b = mat.length-1-temp;
                temp = a;
                a = b;
                b = mat[0].length-1-temp;
            break;
            case 3:
                temp = a;
                a = b;
                b = mat.length-1-temp;
                temp = a;
                a = b;
                b = mat[0].length-1-temp;
                temp = a;
                a = b;
                b = mat.length-1-temp;
            break;
        }
        pt = new Point(a,b);
        return pt;
    }
   
    //function ajouterPersonnage(nom:String, ll:Number, cc:Number, idPers:Number, idClip:Number, userRole:Number, cloColor:String)
	public function ajouterPersonnage(p:IPersonnage, ll:Number, cc:Number)
    {       
        trace("ajouterPersonnage:" + p.obtenirNom() + " ll:" + ll + " cc:" + cc );
        tableauDesCases[ll][cc].ajouterPersonnage(p);
    }
    
	public function getOutPerso(p:IPersonnage)
	{
		tableauDesCases[p.obtenirL()][p.obtenirC()].retirerPersonnage(p);
	}
    
  /*
	// c'est quoi la difference entre ca et recentrerBoard ??
    function centrerPersonnage(l:Number, c:Number)
    {
        var diffX:Number;
        var diffY:Number;
        var i:Number;
        var j:Number;
		
		diffX = 275 - this.tableauDesCases[l][c].obtenirClipCase()._x;
        diffY = 200 - this.tableauDesCases[l][c].obtenirClipCase()._y;
				
		//var count:Number = this.tableauDesCases.length;
		for(i in this.tableauDesCases )
        {
			//var countS:Number = this.tableauDesCases[0].length;
            for(j in this.tableauDesCases[0])
            {
                if(this.tableauDesCases[i][j] != null)
                {
                    this.tableauDesCases[i][j].translater(diffX,diffY);
                }
            }
        }
    }*/
	
	/////////////////////////// Etape 1 de l'animation ////////////////////////////////////////////////////////////////
	public function startAnimationCourseI():Void
	{
		var dx:Number;
		var dy:Number;
		var l:Number = tableauDesCases.length - 4;
		var c:Number = tableauDesCases[0].length - 4; 
		
		dx = 275 - (_level0.loader.contentHolder.referenceLayer._x + (10+this.zoom)/10*this.tableauDesCases[l][c].obtenirClipCase()._x);
		dy = 200 - (_level0.loader.contentHolder.referenceLayer._y + (10+this.zoom)/10*this.tableauDesCases[l][c].obtenirClipCase()._y);
		
		dx = Math.round(dx);
		dy = Math.round(dy);
				
		_level0.loader.contentHolder.referenceLayer._x += dx;		
		_level0.loader.contentHolder.referenceLayer._y += dy;
								
	}
	
	////////////////////////////// Etape 2 de l'animation //////////////////////////////////////////////////////////////////
	public function startAnimationCourseII():Void
	{
		var coorByX:Number = _level0.loader.contentHolder.referenceLayer._x;
		var coorByY:Number = _level0.loader.contentHolder.referenceLayer._y;
		var coorToX:Number = 275 - (10+this.zoom)/10*this.tableauDesCases[2][3].obtenirClipCase()._x;
		var coorToY:Number = 200 - (10+this.zoom)/10*this.tableauDesCases[2][3].obtenirClipCase()._y;
		
		var twMoveX:Tween = new Tween(_level0.loader.contentHolder.referenceLayer, "_x", Regular.easeInOut, coorByX, coorToX, 2.8, true);
		var twMoveY:Tween = new Tween(_level0.loader.contentHolder.referenceLayer, "_y", Regular.easeInOut, coorByY, coorToY, 2.8, true);
				
	}
    
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public function recentrerBoard(l:Number, c:Number, modeGraduel:Boolean):Boolean
	{
		var dx:Number;
		var dy:Number;
		var pourcent:Number;
		
		dx = 275 - (_level0.loader.contentHolder.referenceLayer._x + (10+this.zoom)/10*this.tableauDesCases[l][c].obtenirClipCase()._x);
		dy = 200 - (_level0.loader.contentHolder.referenceLayer._y + (10+this.zoom)/10*this.tableauDesCases[l][c].obtenirClipCase()._y);
				
		dx = Math.round(dx);
		dy = Math.round(dy);
		
		if ((dx == 0) && (dy == 0))
		{
			return true;
		}
		
	
		if (modeGraduel)
		{
			if(Math.abs(dx) > Math.abs(dy))
			{
				if(Math.abs(dx) > 20)
				{
					pourcent = 20/Math.abs(dx);
					dx *= pourcent;
					dy *= pourcent;
				}
			}
			else
			{
				if(Math.abs(dy) > 20)
				{
					pourcent = 20/Math.abs(dy);
					dx *= pourcent;
					dy *= pourcent;
				}
			}
		}

		// on deplace le clip sur lequel est attache tous les autres clips
		_level0.loader.contentHolder.referenceLayer._x += dx;		
		_level0.loader.contentHolder.referenceLayer._y += dy;
		
		return false;
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Method is not used now
	public function switchColor(laCase:Case)
	{
		//trace("--- switchColor ---");
		var mClip:MovieClip;// = new MovieClip();
		mClip = laCase.obtenirClipCase().interieur;
		
		var trans:Transform = new Transform(mClip);
		var colorTrans:ColorTransform = new ColorTransform();
		

				
		if(laCase.obtenirMiniGame())
		{
			colorTrans.rgb = 0xd8ae00;
			trans.colorTransform = colorTrans;
			
			trans = new Transform(laCase.obtenirClipCase().bord);
			trans.colorTransform = colorTrans;
		}
		else
		{
			colorTrans.rgb = 0xd8ae00;
			trans.colorTransform = colorTrans;
						 			 
			//colorTween(mClip, colorTrans, trans, 2.4, 0x00, 0xFF, 0xEB, 0x5B, Strong.easeOut, laCase, intervalCol);
				   
		}

        
	}///////// end methode
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////// Method used to put flash on the cases
	public function switchColorFlash(laCase:Case)
	{
		//trace("--- switchColor ---");
		var mClip:MovieClip;// = new MovieClip();
		
		
		if(laCase.obtenirMiniGame())
		{
		   mClip = laCase.obtenirClipCase().minigame.interieur;
		   mClip.attachMovie("flashCase", "Alpha", mClip.getNextHighestDepth());
		   //mClip.Alpha._alpha = 75;
		}
		else
		{
		   mClip = laCase.obtenirClipCase().interieur;
		   mClip.attachMovie("flashCase", "Alpha", mClip.getNextHighestDepth());
		}
        
	}///////// end methode
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////// Method used to put flash on the Braniac cases
	public function switchColorBran(laCase:Case)
	{
		//trace("--- switchColor ---");
		var mClip:MovieClip;// = new MovieClip();
		
		
		if(laCase.obtenirMiniGame())
		{
		   mClip = laCase.obtenirClipCase().minigame.interieur;
		   mClip.attachMovie("flashCaseBran", "Alpha", mClip.getNextHighestDepth());
		   //mClip.Alpha._alpha = 75;
		}
		else
		{
		   mClip = laCase.obtenirClipCase().interieur;
		   mClip.attachMovie("flashCaseBran", "Alpha", mClip.getNextHighestDepth());
		}
        
	}///////// end methode
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////// Method used to put flash on the Banana cases
	public function switchColorFlashBanana(laCase:Case)
	{
		//trace("--- switchColor ---");
		var mClip:MovieClip;// = new MovieClip();
		
		
		if(laCase.obtenirMiniGame())
		{
		   mClip = laCase.obtenirClipCase().minigame.interieur;
		   mClip.attachMovie("flashCaseBanana", "Alpha", mClip.getNextHighestDepth());
		   //mClip.Alpha._alpha = 75;
		}
		else
		{
		   mClip = laCase.obtenirClipCase().interieur;
		   mClip.attachMovie("flashCaseBanana", "Alpha", mClip.getNextHighestDepth());
		}
        
	}///////// end methode
	
	
	
	/////////// recent add  --- changed from www.kirupa.com 
	/*
	*  Used to apply tween to color transformation - can be used in different situations
	*  where you need graduate color transform 
	*/
	public function colorTween(mc:MovieClip, ct:ColorTransform, t:Transform, seconds:Number, a:Number, r:Number, g:Number, b:Number, ease:Function, laCase:Case, intervalCol:Number):Void {
      
	 
	   intervalCol = setInterval(executeColor, 2400, mc, ct, t, seconds, a, r, g, b, ease);
	   function executeColor(){
		  ct.rgb =  0xd8ae00;
		  t.colorTransform = ct;
	      var alphaTween:Tween = new Tween(ct, "alphaOffset", ease, ct.alphaOffset, a, seconds, true);
          var redTween:Tween = new Tween(ct, "redOffset", ease, ct.redOffset, r, seconds, true);
          var greenTween:Tween = new Tween(ct, "greenOffset", ease, ct.greenOffset, g, seconds, true);
          var blueTween:Tween = new Tween(ct, "blueOffset", ease, ct.blueOffset, b, seconds, true);
     
          greenTween.onMotionChanged = function() {
           t.colorTransform = ct;
          };
		  
		  if(laCase.obtenirCasePossible() == null){
		      clearInterval(intervalCol);
			
		      var alphaTween:Tween = new Tween(ct, "alphaOffset", ease, ct.alphaOffset,0x00, 2.4, true);
              var redTween:Tween = new Tween(ct, "redOffset", ease, ct.redOffset, 0x8a, 2.4, true);
              var greenTween:Tween = new Tween(ct, "greenOffset", ease, ct.greenOffset, 0xb2, 2.4, true);
              var blueTween:Tween = new Tween(ct, "blueOffset", ease, ct.blueOffset, 0x1d, 2.4, true);
     
              greenTween.onMotionChanged = function() {
                 t.colorTransform = ct;
		         ct.rgb = 0x8ab21d;
			     t.colorTransform = ct;
			  };
		  
		  }// end if
		 
	  }
    }
	
	//////////////////////
	public function switchBackColor(laCase:Case)
	{
		//trace("--- switchBackColor ---");
		var mClip:MovieClip;// = new MovieClip();
		mClip = laCase.obtenirClipCase().interieur;
		
		var trans:Transform = new Transform(mClip);
		var colorTrans:ColorTransform = new ColorTransform();

		if(laCase.obtenirMiniGame())
		{
			colorTrans.rgb = 0xC61717;
			trans.colorTransform = colorTrans;
			
			trans = new Transform(laCase.obtenirClipCase().bord);
			trans.colorTransform = colorTrans;
		}
		else
		{
			colorTrans.rgb = 0x8ab21d;
			trans.colorTransform = colorTrans;
			
		}
	}
	
	/////////////////////////////////////
	/// method used to remove flash of cases
	public function switchBackColorFlash(laCase:Case)
	{
	    var mClip:MovieClip = new MovieClip();
		
		if(laCase.obtenirMiniGame())
		{
		   mClip = laCase.obtenirClipCase().minigame.interieur;
		   mClip.Alpha.removeMovieClip();
		}
		else
		{
		   mClip = laCase.obtenirClipCase().interieur;
		   mClip.Alpha.removeMovieClip();
		}
	} // end method


	////////////////////////////////////////////////////////////////////////////////////////////
    public function afficherCasesPossibles()
    {		
	 var isInWinTheGame:Boolean = true;
	 if(tableauDesCases[perso.obtenirL()][perso.obtenirC()].obtenirType() > 41000)
        isInWinTheGame = false;
	 
	 this.tempoSight = 7;
	 var tempo = perso.getMoveSight(); //_level0.loader.contentHolder.objGestionnaireEvenements.getMoveSight(); //
	 trace("Move : " + tempo + " " + perso.getBrainiac());
	 if( !(perso.getRole() == 2 && _level0.loader.contentHolder.objGestionnaireEvenements.getOurTable().compareType("Tournament")) && isInWinTheGame)
	 { 
		//trace("Debut afficherCasesPossibles");
		
        var i:Number;
        var nb:Number = 0;
        var brille:MovieClip;
		var coordonnees:Point = new Point(0,0);
        var level:Number;
		var temp:Number;
		var twMove:Tween;
		var twMove2:Tween;
						
		//trace("ds afficherCasesPossibles");
        for(i = 1; i <= Math.min(mat.length - perso.obtenirL() - 1, tempo); i++)
        {
			temp = Number(perso.obtenirL());
			temp += Number(i);
			
			 //trace("ds premier for avant if  i  temp   mat  L   C  :  "+i+"   "+temp+"   "+this.mat[temp][perso.obtenirC()]+"   "+perso.obtenirL()+"   "+perso.obtenirC());
		
            if(this.mat[temp][perso.obtenirC()] > 0)
            {				
					//switchColor(tableauDesCases[temp][p.obtenirC()]);
					if(i == tempo && perso.getBrainiac())
					{
						switchColorBran(tableauDesCases[temp][perso.obtenirC()]);
					
					}else
					{
					   switchColorFlash(tableauDesCases[temp][perso.obtenirC()]);
					}
					
					level = (tableauDesCases.length*tableauDesCases[0].length) +(temp*tableauDesCases[0].length)+Number(perso.obtenirC()) + Number(1);
					//trace("ds if,  level  "+level);
					if(tableauDesCases[temp][perso.obtenirC()].obtenirType() > 40000)
					{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("winShine", "winShine"+level, level, {_width:116, _height:36.25});
					   tableauDesCases[temp][perso.obtenirC()].obtenirWinTheGame().shineWin();
					}else{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level, {_width:116, _height:36.25});
					   brille._alpha = 0;
					}
					brille._x = tableauDesCases[temp][perso.obtenirC()].obtenirClipCase()._x;
					brille._y = tableauDesCases[temp][perso.obtenirC()].obtenirClipCase()._y;
					//brille._width = largeurDeCase;///0.55;
					//brille._height = hauteurDeCase;///0.55;//0.85
					
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = temp;
					brille._colonne = perso.obtenirC();
			
					afficherValeurDeplacementColonne(perso, brille, temp, perso.obtenirC());

                   /* brille.onRollOver = function()
					{ 
					  _level0.loader.contentHolder.mouseHand.removeMovieClip();
					  Mouse.show();
					};*/
					brille.onPress = function()
					{   
						removeMovieClip(perso.obtenirImage().valDeplace);
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
						_level0.loader.contentHolder.planche.effacerCasesPossibles();
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
						tableauDesCases[temp][perso.obtenirC()].obtenirWinTheGame().removeShineWin();
					};
					tableauDesCases[temp][perso.obtenirC()].definirCasePossible(brille);
												
            }
            else
            {
                break;
            }
			
        }
	
	
        for(i=1; i <= Math.min(perso.obtenirL(),tempo); i++)
        {
			//trace("ds deuxieme for avant if  i  mat  :  "+i+"   "+mat[perso.obtenirL()-i][perso.obtenirC()]);
			temp = perso.obtenirL() - i;
           
			if(mat[temp][perso.obtenirC()] > 0)
            {
					//switchColor(tableauDesCases[temp][p.obtenirC()]);
					//if we have Bran
					if(i == tempo && perso.getBrainiac())
					{
						switchColorBran(tableauDesCases[temp][perso.obtenirC()]);
					}else
					{
					   switchColorFlash(tableauDesCases[temp][perso.obtenirC()]);
					}
					// trace("tableau des cases  vs  mat  : "+ tableauDesCases.length+"   "+mat.length);
					// trace("tableau des cases[0]  vs  mat[]  : "+ tableauDesCases[0].length+"   "+mat[0].length);
					level = (tableauDesCases.length * tableauDesCases[0].length)  +  ((temp) * tableauDesCases[0].length)  +  Number(perso.obtenirC()) + Number(1);
					// trace("ds if,  level:  "+level);
					if(tableauDesCases[temp][perso.obtenirC()].obtenirType() > 40000 ){
					   
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("winShine", "winShine"+level, level, {_width:116, _height:36.25});
					   tableauDesCases[temp][perso.obtenirC()].obtenirWinTheGame().shineWin();
					}else{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					   brille._alpha = 0;
					}
					//brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[temp][perso.obtenirC()].obtenirClipCase()._x;
					brille._y = tableauDesCases[temp][perso.obtenirC()].obtenirClipCase()._y;
					//brille._width = largeurDeCase;///0.55;
					//brille._height = hauteurDeCase;//0.55;//0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = temp;
					brille._colonne = perso.obtenirC();
					
					afficherValeurDeplacementColonne(perso, brille, temp, perso.obtenirC());
					
					brille.onPress = function ()
					{
						removeMovieClip(perso.obtenirImage().valDeplace);
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles();
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees); 
						tableauDesCases[temp][perso.obtenirC()].obtenirWinTheGame().removeShineWin();
					};
					this.tableauDesCases[temp][perso.obtenirC()].definirCasePossible(brille);
			
					//_root.objGestionnaireInterface.ajouterBouton(brille, 3);
				
           	}
            else
            {
                break;
            }
        }
	
        for(i=1; i <= Math.min(mat[0].length-perso.obtenirC()-1, tempo); i++)
        {
			temp = Number(perso.obtenirC());
			temp += Number(i);
			//trace("ds troisieme for avant if  i  temp   mat  L   C  :  "+i+"   "+temp+"    "+mat[p.obtenirL()][temp]+"   "+p.obtenirL()+"   "+p.obtenirC());
            
			if(mat[perso.obtenirL()][temp] > 0)
            {
					//switchColor(tableauDesCases[p.obtenirL()][temp]);
					if(i == tempo && perso.getBrainiac())
					{
					   switchColorBran(tableauDesCases[perso.obtenirL()][temp]);
					}else
				    {
					   switchColorFlash(tableauDesCases[perso.obtenirL()][temp]);
					}
					
					level = (tableauDesCases.length * tableauDesCases[0].length) + Number(perso.obtenirL()* tableauDesCases[0].length) + temp + Number(1);
					//trace("ds if : level :   "+level);
					if(tableauDesCases[perso.obtenirL()][temp].obtenirType() > 40000){
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("winShine", "winShine"+level, level, {_width:116, _height:36.25});
					   tableauDesCases[perso.obtenirL()][temp].obtenirWinTheGame().shineWin();
					}else{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					   brille._alpha = 0;
					}
					//brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[perso.obtenirL()][temp].obtenirClipCase()._x;
					brille._y = tableauDesCases[perso.obtenirL()][temp].obtenirClipCase()._y;
					//brille._width = largeurDeCase;///0.55;
					//brille._height = hauteurDeCase;//0.55;//0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = perso.obtenirL();
					brille._colonne = temp;
				
					afficherValeurDeplacementLigne(perso, brille, perso.obtenirL(), temp);
					
					brille.onPress = function ()
					{
						removeMovieClip(perso.obtenirImage().valDeplace);
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles();
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
			            tableauDesCases[perso.obtenirL()][temp].obtenirWinTheGame().removeShineWin(); 
					};
					this.tableauDesCases[perso.obtenirL()][temp].definirCasePossible(brille);
												
			}
            else
            {
                break;
            }
        }
		
        for(i = 1; i <= Math.min(perso.obtenirC(),tempo); i++)
        {
			//trace("ds dernier for avant if  i  mat  :  "+i+"   "+mat[p.obtenirL()][p.obtenirC()-i]);
			temp = perso.obtenirC() - i;
			
            if(mat[perso.obtenirL()][temp] > 0)
            {
					//switchColor(tableauDesCases[p.obtenirL()][temp]);
					if(i == tempo && perso.getBrainiac())
					{
     				   switchColorBran(tableauDesCases[perso.obtenirL()][temp]);
					}else
					{
					   switchColorFlash(tableauDesCases[perso.obtenirL()][temp]);
					}
					
					level = (tableauDesCases.length*tableauDesCases[0].length) + (Number(perso.obtenirL())*tableauDesCases[0].length)+temp+Number(1);
					//trace("ds if,  level: "+level);
					if(tableauDesCases[perso.obtenirL()][temp].obtenirType() > 40000){
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("winShine", "winShine"+level, level, {_width:116, _height:36.25});
					   tableauDesCases[perso.obtenirL()][temp].obtenirWinTheGame().shineWin();
					}else{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					   brille._alpha = 0;
					}
					//brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[perso.obtenirL()][temp].obtenirClipCase()._x;
					brille._y = tableauDesCases[perso.obtenirL()][temp].obtenirClipCase()._y;
					//brille._width = largeurDeCase;//0.55;
					//brille._height = hauteurDeCase;//0.55;//0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = perso.obtenirL();
					brille._colonne = temp;
					
					afficherValeurDeplacementLigne(perso, brille, perso.obtenirL(), temp);
			
					brille.onPress = function ()
					{
						removeMovieClip(perso.obtenirImage().valDeplace);
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles();
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
						tableauDesCases[perso.obtenirL()][temp].obtenirWinTheGame().removeShineWin(); 
					};
					this.tableauDesCases[perso.obtenirL()][temp].definirCasePossible(brille);				
            }
            else
            {
                break;
            }
        }
		trace("Fin afficher cases possibles"); 
		
		// to put Banana cases 
		//trace("in the GE " + _level0.loader.contentHolder.objGestionnaireEvenements.bananaState);
		if(perso.getBananaState())
		   afficherCasesPossiblesBanane();
	 }// end if for watcher
	 
	 //
	 this.setShowCases(true);
    }
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///  Method used to put on the board the movies of the cases cauted by banana //////////////
	////////////////////////////////////////////////////////////////////////////////////////////
    public function afficherCasesPossiblesBanane()
    {	  
	    trace("start afficher cases possibles Banane");
	    var i:Number;
		var j:Number;
		var tempo:Number = perso.getMoveSight();
		//trace("tempo !!!!!!!!!!!!!" + tempo);
		var maxSight:Number = 6;
		
        var nb:Number = Math.min(maxSight - tempo,2);
		
        var coordonnees:Point = new Point(0,0);
        var temp:Number;
		var tempB:Number;
		var hasHoles:Boolean = false;
		
		for(j = 1; j <= Math.min(mat.length-perso.obtenirL()-1, tempo + 1); j++)  // moveVisi + 1
		{                                                                       // because first cases Banana can be on the hole
				tempB = Number(perso.obtenirL());
			    tempB += Number(j);
			    if(this.mat[tempB][perso.obtenirC()] == 0) hasHoles = true;
		}		
		
        for(i = 0; i < nb; i++)//Math.min(mat.length-p.obtenirL()-1,moveVisi + 2); i++)
        {
			temp = Number(perso.obtenirL());
			temp += Number(tempo + i + 1);
						
            if(this.mat[temp][perso.obtenirC()] > 0 && !hasHoles)
            {
				//trace("Test L: " + temp + " " + (mat.length - p.obtenirL()-1));
				switchColorFlashBanana(tableauDesCases[temp][perso.obtenirC()]);
			}
            
        }
	    //***************************************************************
	    hasHoles = false;
		
		for(j = 1; j <= Math.min(perso.obtenirL() - 1, tempo + 1); j++)
		{
				tempB = Number(perso.obtenirL());
			    tempB -= Number(j);
			    if(this.mat[tempB][perso.obtenirC()] == 0) hasHoles = true;
		}		
	
        for(i = 0; i < nb; i++)//i <= Math.min(p.obtenirL(),moveVisi + 2); i++)
        {
			//trace("ds deuxieme for avant if  i temp mat  :  " + i + "   " + temp + "    "  + mat[p.obtenirL()-i][p.obtenirC()]);
			temp = perso.obtenirL()-(tempo + i + 1);
           
			if(mat[temp][perso.obtenirC()] > 0 && !hasHoles)
            {
				switchColorFlashBanana(tableauDesCases[temp][perso.obtenirC()]);
			}
            
        }
	    //**************************************************************
	    hasHoles = false;
		
		for(j = 1; j <= Math.min(mat[0].length-perso.obtenirC() - 1, tempo + 1); j++)
		{
				tempB = Number(perso.obtenirC());
			    tempB += Number(j);
			    if(this.mat[perso.obtenirL()][tempB] == 0) hasHoles = true;
		}		
	
	    
        for(i = 0; i < nb; i++)//for(i = moveVisi + 1; i <= Math.min(mat[0].length-p.obtenirC()-1, moveVisi + 2);i++)
        {
			temp = Number(perso.obtenirC());
			temp += Number(tempo + i + 1);
			//trace("ds troisieme for avant if  i  temp   mat  L   C  :  " + i + "   " + temp + "    " + mat[p.obtenirL()][temp] + "   " + p.obtenirL() + "   " + p.obtenirC());
            
			if(mat[perso.obtenirL()][temp] > 0 && !hasHoles )
            {
				switchColorFlashBanana(tableauDesCases[perso.obtenirL()][temp]);
			}
           
        }
		
		//******************************************************************************
		 hasHoles = false;
		
		for(j = 1; j <= Math.min(perso.obtenirC(),tempo + 1); j++)
		{
				tempB = Number(perso.obtenirC());
			    tempB -= Number(j);
			    if(this.mat[perso.obtenirL()][tempB] == 0) hasHoles = true;
		}		
		
        for(i = 0; i < nb; i++)//for(i= moveVisi + 1; i <= Math.min(p.obtenirC(), moveVisi + 2);i++)
        {
			//trace("ds dernier for avant if  i  mat  :  " + i + "   " + mat[p.obtenirL()][p.obtenirC()-i]);
			temp = perso.obtenirC()-(tempo + i + 1);
			
            if(mat[perso.obtenirL()][temp] > 0 && !hasHoles)
            {
				switchColorFlashBanana(tableauDesCases[perso.obtenirL()][temp]);
            }
           
        }
		trace("Fin afficher cases possiblesBanana"); 
			 
    }// end function afficherCasesPossiblesBanane
    
	
	/**
	Les 2 fonctions suivantes servent a afficher le nombre de points que
	vaut chaque deplacement quand la souris est deplacee sur les cases.
	Elles sont appelees dans afficherCasesPossibles().
	*/
	public function afficherValeurDeplacementColonne (p, brille:MovieClip, dx:Number, dy:Number)
	{
		var mc:MovieClip = p.obtenirImage();
		brille.onRollOver = function()
		{
			mc.createEmptyMovieClip("valDeplace", 999)
			mc.valDeplace = _level0.loader.contentHolder.referenceLayer.attachMovie("ptsTxt", "ptsTxt_mc", _level0.loader.contentHolder.referenceLayer.getNextHighestDepth());
			
			mc.valDeplace._x = brille._x;
			mc.valDeplace._y = brille._y;
				
			mc.valDeplace.valeur = Math.abs(p.obtenirL() - dx);
							
			switch(mc.valDeplace.valeur)
			{
				case 1:
					mc.valDeplace.valeur = 2;
				break;
				case 2:
					mc.valDeplace.valeur = 3;
				break;
				case 3:
					mc.valDeplace.valeur = 5;
				break;
				case 4:
					mc.valDeplace.valeur = 8;
				break;
				case 5:
					mc.valDeplace.valeur = 13;
				break;
				case 6:
					mc.valDeplace.valeur = 21;
				break;
				case 7:
					mc.valDeplace.valeur = 34;
				break;
				
			}

		}

		brille.onRollOut = function()
		{
			mc.valDeplace.removeMovieClip();
		}
	}

	public function afficherValeurDeplacementLigne (p, brille:MovieClip, dx:Number, dy:Number)
    {
		var mc:MovieClip = p.obtenirImage();
		brille.onRollOver = function()
		{
			mc.createEmptyMovieClip("valDeplace", 999)
			mc.valDeplace = _level0.loader.contentHolder.referenceLayer.attachMovie("ptsTxt", "ptsTxt_mc", _level0.loader.contentHolder.referenceLayer.getNextHighestDepth() );
			//mc.valDeplace._x = p.obtenirX();
			//mc.valDeplace._y = p.obtenirY()-150;
			mc.valDeplace._x = brille._x;
			mc.valDeplace._y = brille._y;
		
			mc.valDeplace.valeur = Math.abs(p.obtenirC() - dy);
			//mc.valDeplace._alpha = 100;
			
			switch(mc.valDeplace.valeur)
			{
				case 1:
					mc.valDeplace.valeur = 2;
				break;
				case 2:
					mc.valDeplace.valeur = 3;
				break;
				case 3:
					mc.valDeplace.valeur = 5;
				break;
				case 4:
					mc.valDeplace.valeur = 8;
				break;
				case 5:
					mc.valDeplace.valeur = 13;
				break;
				case 6:
					mc.valDeplace.valeur = 21;
				break;
				case 7:
					mc.valDeplace.valeur = 34;
				break;
			}
				
			//trace(mc.valDeplace.valeur);
			//trace("ligne"+dx);
			//trace("collo"+dy);
		}

		brille.onRollOut = function()
		{
			mc.valDeplace.removeMovieClip();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////
    public function effacerCasesPossibles()
    {
        var i:Number;
		var temp:Number;
	    	    
		//trace("Debut effacerCasesPossibles");
	    _level0.loader.contentHolder.referenceLayer.ptsTxt_mc.removeMovieClip();  
		//switchBackColor(tableauDesCases[p.obtenirL()][p.obtenirC()]);
		//another version more light
		switchBackColorFlash(tableauDesCases[perso.obtenirL()][perso.obtenirC()]);
	
        for(i = 1; i <= Math.min(mat.length-perso.obtenirL() - 1, this.tempoSight); i++)
        {
			temp = Number(Number(perso.obtenirL()) + i);   
			//trace("ds premier for    i     L    temp"+i+"    "+p.obtenirL()+"    "+temp);
		
            if(mat[temp][perso.obtenirC()] > 0)
            {
		    	//trace("ds if premier for");
                tableauDesCases[temp][perso.obtenirC()].effacerCasePossible();
				//switchBackColor(tableauDesCases[temp][p.obtenirC()]);
				switchBackColorFlash(tableauDesCases[temp][perso.obtenirC()]);
            }
            else
            {
                break;
            }
        }
		
        for(i = 1; i <= Math.min(perso.obtenirL(), this.tempoSight);i++)
        {
			//trace("ds deuxieme for");
		
            if(mat[perso.obtenirL()-i][perso.obtenirC()] > 0)
            {
				// trace("ds if deuxieme for");
                tableauDesCases[perso.obtenirL()-i][perso.obtenirC()].effacerCasePossible();
				//switchBackColor(tableauDesCases[perso.obtenirL()-i][perso.obtenirC()]);
				switchBackColorFlash(tableauDesCases[perso.obtenirL()-i][perso.obtenirC()]);
            }
            else
            {
                break;
            }
        }
		
        for(i = 1; i <= Math.min(mat[0].length-perso.obtenirC()-1, this.tempoSight);i++)
        {
			temp = Number(Number(perso.obtenirC())+i);   
			//trace("ds troisieme for    i     L    temp"+i+"    "+perso.obtenirC()+"    "+temp);
		
            if(mat[perso.obtenirL()][temp] > 0)
            {
		    	//trace("ds if troisieme for");
                tableauDesCases[perso.obtenirL()][temp].effacerCasePossible();
				//switchBackColor(tableauDesCases[perso.obtenirL()][temp]);
				switchBackColorFlash(tableauDesCases[perso.obtenirL()][temp]);
            }
            else
            {
                break;
            }
        }
		
        for(i = 1; i <= Math.min(perso.obtenirC(), this.tempoSight);i++)
        {
			//trace("ds quatrieme for");
		
            if(mat[perso.obtenirL()][perso.obtenirC()-i] > 0)
            {
		    	//trace("ds if quatrieme for");
                tableauDesCases[perso.obtenirL()][perso.obtenirC()-i].effacerCasePossible();
				//switchBackColor(tableauDesCases[p.obtenirL()][p.obtenirC()-i]);
				switchBackColorFlash(tableauDesCases[perso.obtenirL()][perso.obtenirC()-i]);
            }
            else
            {
                break;
            }
        }
	
		//_root.objGestionnaireInterface.deleterCasesSpeciales(); 
		
		//trace("efface banana : " + _level0.loader.contentHolder.objGestionnaireEvenements.bananaState );
		if(_level0.loader.contentHolder.objGestionnaireEvenements.bananaState)
		  effacerCasesPossiblesBanane();
		  
		this.setShowCases(false);
		//trace("Fin effacerCasesPossibles");
		//trace("****************************");
    }	
	
	
	//////////////////////////////////////////////////////////////////////////////
    public function effacerCasesPossiblesBanane()
    {
        var i:Number;
		var temp:Number;
	    		
		var nb:Number = Math.min(6 - this.tempoSight, 2);
	    
		for(i = 0; i < nb; i++)//for(i = this.tempoSight + 1; i <= Math.min(mat.length-p.obtenirL()-1, this.tempoSight + 2); i++) // +2 because Banana cut 2 cases 
        {
			temp = Number(Number(perso.obtenirL()) + (this.tempoSight + i + 1));   
					
            if(mat[temp][perso.obtenirC()] > 0)
            {
		    	switchBackColorFlash(tableauDesCases[temp][perso.obtenirC()]);
            }
            else
            {
                break;
            }
        }
		
        for(i = 0; i < nb; i++)//for(i = this.tempoSight + 1; i <= Math.min(p.obtenirL(), this.tempoSight + 2); i++)
        {
			if(mat[perso.obtenirL()-(this.tempoSight + i + 1)][perso.obtenirC()] > 0)
            {
				switchBackColorFlash(tableauDesCases[perso.obtenirL()-(this.tempoSight + i + 1)][perso.obtenirC()]);
            }
            else
            {
                break;
            }
        }
		
        for(i = 0; i < nb; i++)//for(i= this.tempoSight + 1; i <= Math.min(mat[0].length-p.obtenirC()-1, this.tempoSight + 2); i++)
        {
			temp = Number(Number(perso.obtenirC())+(this.tempoSight + i + 1));   
			
            if(mat[perso.obtenirL()][temp] > 0)
            {
		    	switchBackColorFlash(tableauDesCases[perso.obtenirL()][temp]);
            }
            else
            {
                break;
            }
        }
		
        for(i = 0; i < nb; i++)//for(i= this.tempoSight + 1; i <= Math.min(p.obtenirC(), this.tempoSight + 2); i++)
        {
			if(mat[perso.obtenirL()][perso.obtenirC()-(this.tempoSight + i + 1)] > 0)
            {
		    	switchBackColorFlash(tableauDesCases[perso.obtenirL()][perso.obtenirC()-(this.tempoSight + i + 1)]);
            }
            else
            {
                break;
            }
        }
	
	    trace("fin afficher cases poss Banane");
		//_root.objGestionnaireInterface.deleterCasesSpeciales();   // ????
		
    }	
	
    ////////////////////////////////////////////////////////////////////////////
    public function effacerPiece(ll:Number, cc:Number)
    {
        tableauDesCases[ll][cc].effacerPiece();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    public function effacerObjet(ll:Number, cc:Number)
    {
        tableauDesCases[ll][cc].effacerObjet();
    }
  
  
    // num est le numero du personnage   str est le type de collision
    public function teleporterPersonnage(objEvenement:Object)//nom:String, ancienL:Number, ancienC:Number, nouveauL:Number, nouveauC:Number, str:String)
    {
		var pt_initial:Point = calculerPositionTourne(objEvenement.anciennePosition.x, objEvenement.anciennePosition.y);
    	var pt_final:Point = calculerPositionTourne(objEvenement.nouvellePosition.x, objEvenement.nouvellePosition.y);
						
	    //trace(" dans teleporterPersonnage, parametres :  "+nom+"   "+ancienL+"   "+ancienC+"   "+nouveauL+"   "+nouveauC+"   "+str);
	    var listeTemporaire:Array;
	    listeTemporaire = this.tableauDesCases[pt_initial.obtenirX()][pt_initial.obtenirY()].obtenirListeDesPersonnages();
		var count:Number = listeTemporaire.length;	    
	    for(var i:Number = 0; i< count; i++)
	    {
		    if(listeTemporaire[i].obtenirNom() == objEvenement.nomUtilisateur)
		    {
			    //trace("juste avant de definir la prochaine poisition");
			    listeTemporaire[i].definirProchainePosition(pt_final, objEvenement.collision);
			    break;
		    }
	    }	    
    }// end method
    
    
    public function estCaseSpeciale(lig:Number, col:Number):Boolean
    {
	    //trace("est dans estCaseSpeciale   "+lig+"   "+col);
		if(tableauDesCases[lig][col].obtenirType()%100 > 90)
        {
			//trace("ds le if de estCaseSpeciale");
            return true;
        }
	
		return false;
    }	
	
} // end class