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

import flash.geom.Transform;
import flash.geom.ColorTransform;

import mx.transitions.Tween;
import mx.transitions.easing.*;
import mx.utils.Delegate;
import mx.transitions.Transition;


class PlancheDeJeu
{
	// if changes here, change also in Case.as
	// colors used for the different cell (question) categories
	private var CLR_1:Number = 0x845f1d;
	private var CLR_2:Number = 0x123456;
	private var CLR_3:Number = 0xfedcba;
	private var CLR_4:Number = 0x559911;
	private var CLR_5:Number = 0x991155;
	private var CLR_6:Number = 0x115599;
	private var CLR_7:Number = 0x3579bd;
	private var CLR_8:Number = 0xd1f548;
					
    private var mat:Array;
    private var tableauDesCases:Array = new Array();
    private var hauteurDeCase:Number = -1;
    private var largeurDeCase:Number = -1;
    private var perso:Personnage;
    private var monPersonnage:Number = -1;  //  numero dans le tableau des perso du personnage en cours
    private var nomDeMonPersonnage:String; // nom de notre perso
    private var zoom:Number = 0;
    private var gestionnaireInterface:GestionnaireInterface;
    private var rotation:Number = 0;
    private var tableauDesPersonnages:Array = new Array(); // contient les personnages
	
    
    function obtenirPerso():Personnage
    {
        return this.perso;
    }
	
	function setPerso(p:Personnage):Void
    {
        perso = p;
    }
	
    function obtenirNomDeMonPersonnage():String
    {
        return this.nomDeMonPersonnage;
    }
    
	function getNumeroMagasin(ll:Number, cc:Number):Number
	{
		return this.tableauDesCases[ll][cc].obtenirNumMagasin();
	}
	
	function obtenirTableauDesCases():Array
    {
        return this.tableauDesCases;
    }
    
	function obtenirMat():Array
    {
        return this.mat;
    }
    

    ////////////////////////////////////////////////////////////
    //  CONSTUCTEUR
    ////////////////////////////////////////////////////////////
    function PlancheDeJeu(tab:Array, num:Number, p:GestionnaireInterface)
    {
        var i:Number;
		this.mat = new Array(tab.length)
        for(i=0;i<tab.length;i++)
        {
			this.mat[i] = new Array(tab[i].length);
            this.tableauDesCases.push(new Array());
        }
        definirMat(tab, null);
        var idDessin:Number =((num - 10000)-(num - 10000) % 100)/100;
		var idPers:Number = num - 10000 - idDessin * 100;
        monPersonnage = idPers;//num;
        perso = null;
        gestionnaireInterface = p;
    }
	
	function getPersonnageByName(playerName:String)
	{
		for(var i:Number = 0; i < tableauDesPersonnages.length; i++)
		{
		   if(tableauDesPersonnages[i].obtenirNom() == playerName)
		      tableauDesPersonnages[i].slippingBanana();
			  //return tableauDesPersonnages[i];
		}
	}
	
    function obtenirNombreDeColonnes():Number
    {
        return this.mat[0].length;
    }

   
    // a revoir, pour tout de suite ca sert quand on enleve une piece
    function modifierNumeroCase(l:Number, c:Number, num:Number)
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
    
       
    function enleverPiece(l:Number, c:Number)
    {
        this.tableauDesCases[l][c].effacerPiece();
    }
	
	
	///////////////////////////////////////////////////////////////////////////
	function obtenirObjet(l:Number, c:Number):ObjetSurCase
    {
        return this.tableauDesCases[l][c].obtenirObjet();
    }
	
	
	function enleverObjet(l:Number, c:Number)
    {
        this.tableauDesCases[l][c].effacerObjet();
    }
	

    function obtenirNombreDeLignes():Number
    {
        return this.mat.length;
    }
   
   	// cette fonction affiche la planche de jeu initiale,
    // idealement il faudrait que les 4 coins soient toujours presents et pas plus de 9998 cases
    function afficher()
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
        hauteurDeCase = clipTest._height*0.85;
        clipTest.removeMovieClip();
        ////////////////////////////////////////////////////////////////////////////////////////
        for(i=0; i < this.mat.length; i++)
        {
            x = i*largeurDeCase/2 + largeurDeCase/2;
            y = 200 + i*hauteurDeCase/2;
            for(j=0;j<this.mat[0].length;j++)
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
                x = x+largeurDeCase/2;
                y = y-hauteurDeCase/2;
            }
        }
    }

    
    ////////////////////////////////////////////////////////////////////////////////////
    function definirMat(tab:Array, tdc:Array)
    {
        var i:Number;
        var j:Number;
        // on initie les tableaux 
        for(i=0;i<tab.length;i++)
        {
            for(j=0;j<tab[0].length;j++)
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
	
    function translater(direction:String):Boolean
    {
        var la:Number;
        var ha:Number;
		var limiteAtteinte:Boolean = false;
		
		var coinGauche = new Point(0,0);
		var coinDroit = new Point(0,0);
		var coinHaut = new Point(0,0);
		var coinBas = new Point(0,0);
		
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
                la = -largeurDeCase/2;
				if (coinDroit.obtenirX()+la < 275) //Le coin droit du tableau est au centre de l'ecran
				{
					la = 275 - coinDroit.obtenirX();
					limiteAtteinte = true;
				}
                ha = 0;
            break;
            case "Ouest":
                la = largeurDeCase/2; 
				if (coinGauche.obtenirX()+la > 275) //Le coin gauche du tableau est au centre de l'ecran
				{
					la = 275 - coinGauche.obtenirX();
					limiteAtteinte = true;
				}
                ha = 0;
            break;
            case "Nord":
                la = 0;
                ha = hauteurDeCase/2;
				if (coinHaut.obtenirY()+ha > 250) //Le coin haut du tableau est au centre de l'ecran
				{
					ha = 250 - coinHaut.obtenirY();
					limiteAtteinte = true;
				}
            break;
            case "Sud":
                la = 0;
                ha = -hauteurDeCase/2;
				if (coinBas.obtenirY()+ha < 250) //Le coin bas du tableau est au centre de l'ecran
				{
					ha = 250 - coinBas.obtenirY();
					limiteAtteinte = true;
				}
            break;
        }
	
		// on deplace le clip sur lequel est attache tous les autres clips
		_level0.loader.contentHolder.referenceLayer._x += la;		
		_level0.loader.contentHolder.referenceLayer._y += ha;
		
		return !(limiteAtteinte);
    }

       
    function zoomer(sens:String):Boolean
    {
		var distX:Number;
		var distY:Number;
		
		switch(sens)
		{
			case "in":
				if(this.zoom < 8)
				{
					this.zoom++;
					
					// on zoom le clip sur lequel est attache tous les autres clips
					_level0.loader.contentHolder.referenceLayer._xscale +=10;
					_level0.loader.contentHolder.referenceLayer._yscale +=10;
				
					// on deplace le clip sur lequel est attache tous les autres clips
					distX = 275 - _level0.loader.contentHolder.referenceLayer._x;
					distX *= (10+this.zoom)/(9+this.zoom); 
					_level0.loader.contentHolder.referenceLayer._x = 275 - distX;
					distY = 250 - _level0.loader.contentHolder.referenceLayer._y;
					distY *= (10+this.zoom)/(9+this.zoom); 
					_level0.loader.contentHolder.referenceLayer._y = 250 - distY;
					
					// on modifie largeurDeCase et hauteurDeCase
					this.largeurDeCase *= (10+this.zoom)/(9+this.zoom);
					this.hauteurDeCase *= (10+this.zoom)/(9+this.zoom);
				}
			break;
				
			case "out":
				if(this.zoom > -8)
				{
					this.zoom--; 
					
					// on zoom le clip sur lequel est attache tous les autres clips
					_level0.loader.contentHolder.referenceLayer._xscale -=10;
					_level0.loader.contentHolder.referenceLayer._yscale -=10;
					
					// on deplace le clip sur lequel est attache tous les autres clips
					distX = 275 - _level0.loader.contentHolder.referenceLayer._x;
					distX *= (10+this.zoom)/(11+this.zoom); 
					_level0.loader.contentHolder.referenceLayer._x = 275 - distX;
					distY = 250 - _level0.loader.contentHolder.referenceLayer._y;
					distY *= (10+this.zoom)/(11+this.zoom); 
					_level0.loader.contentHolder.referenceLayer._y = 250 - distY;
					
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
    
    
    function obtenirRotation():Number
    {
        return rotation;
    }
    
    
    function obtenirTableauDesPersonnages():Array
    {
        return this.tableauDesPersonnages;
    }
   
    
	// cette fonction prend en entree un pt du board original et retourne la ligne et la colonne dans le board tourne
	// a appliquer a tous les pt donnes par le serveur
    function calculerPositionTourne(ll:Number, cc:Number):Point
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
    function calculerPositionOriginale(ll:Number, cc:Number):Point
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

	
    function faireRirePersonnage(p:Personnage)
    {
        p.rire();
    }

    
    function ajouterPersonnage(nom:String, ll:Number,cc:Number,num:Number, idClip:Number, userRole:Number)
    {
        var p:Personnage;
        trace("ajouterPersonnage:" + nom + " niveau:" + (5*tableauDesCases.length*tableauDesCases[0].length+2*num) + " idPers:" + num + " idDessin:" + idClip);
        p = new Personnage(nom, userRole, 5*tableauDesCases.length*tableauDesCases[0].length+2*num,"Personnage"+idClip,ll, cc, tableauDesCases[ll][cc].obtenirClipCase()._x,tableauDesCases[ll][cc].obtenirClipCase()._y );
        p.afficher();
     	//   p.zoomer(zoom*10);
        tableauDesCases[ll][cc].ajouterPersonnage(p);
	
		this.tableauDesPersonnages.push(p);
	
        if(num == monPersonnage)
        {
            perso = p;
	    	nomDeMonPersonnage = nom;
	    	//this.recentrerBoard(ll,cc,false);
	  		//  p.zoomer(10);
        }
		//else
		//p.zoomer(-7);  // POURQUOI?
    }
    
    
    
	// c'est quoi la difference entre ca et recentrerBoard ??
    function centrerPersonnage(l:Number, c:Number)
    {
        var diffX:Number;
        var diffY:Number;
        var i:Number;
        var j:Number;
		
		// for different types of game we need different pozitions
        if((_level0.loader.contentHolder.objGestionnaireEvenements.typeDeJeu == "Tournament") )
		{
		    diffX = 180 - this.tableauDesCases[l][c].obtenirClipCase()._x;
            diffY = 250 - this.tableauDesCases[l][c].obtenirClipCase()._y;
		}else{
        	diffX = 300 - this.tableauDesCases[l][c].obtenirClipCase()._x;
            diffY = 220 - this.tableauDesCases[l][c].obtenirClipCase()._y;
		}
		
		for(i=0;i<this.tableauDesCases.length;i++)
        {
            for(j=0;j<this.tableauDesCases[0].length;j++)
            {
                if(this.tableauDesCases[i][j] != null)
                {
                    this.tableauDesCases[i][j].translater(diffX,diffY);
                }
            }
        }
    }
    
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	function recentrerBoard(l:Number, c:Number, modeGraduel:Boolean):Boolean
	{
		var dx:Number;
		var dy:Number;
		var pourcent:Number;
		
		// if different type of game we need different pozitions
		if((_level0.loader.contentHolder.objGestionnaireEvenements.typeDeJeu=="Tournament") )
		{
		   dx = 180 - (_level0.loader.contentHolder.referenceLayer._x + (10+this.zoom)/10*this.tableauDesCases[l][c].obtenirClipCase()._x);
		   dy = 250 - (_level0.loader.contentHolder.referenceLayer._y + (10+this.zoom)/10*this.tableauDesCases[l][c].obtenirClipCase()._y);
		}else{
		   dx = 300 - (_level0.loader.contentHolder.referenceLayer._x + (10+this.zoom)/10*this.tableauDesCases[l][c].obtenirClipCase()._x);
		   dy = 220 - (_level0.loader.contentHolder.referenceLayer._y + (10+this.zoom)/10*this.tableauDesCases[l][c].obtenirClipCase()._y);
		}
		
		dx = Math.round(dx);
		dy = Math.round(dy);
		
		if ((dx == 0) && (dy == 0))
		{
			return true;
		}
		//trace("dx = " + dx);
		//trace("dy = " + dy);
	
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
		_level0.loader.contentHolder.referenceLayer._x +=dx;		
		_level0.loader.contentHolder.referenceLayer._y +=dy;
		
		return false;
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	function switchColor(laCase:Case)
	{
		//trace("--- switchColor ---");
		var mClip:MovieClip = new MovieClip();
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
		}
	}

	function switchBackColor(laCase:Case)
	{
		//trace("--- switchBackColor ---");
		var mClip:MovieClip = new MovieClip();
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

	
    function afficherCasesPossibles(p:Personnage)
    {		
	 var isInWinTheGame:Boolean = true;
	 
	 if(tableauDesCases[p.obtenirL()][p.obtenirC()].obtenirType() > 41000)
        isInWinTheGame = false;
	 var moveVisibility =  _level0.loader.contentHolder.objGestionnaireEvenements.moveVisibility;
	 //trace("Move : " + moveVisibility);
	 if( !(p.getRole() == 2 && _level0.loader.contentHolder.objGestionnaireEvenements.typeDeJeu == "Tournament") && isInWinTheGame)
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
		var pointageMin:Number = _level0.loader.contentHolder.objGestionnaireEvenements.obtenirPointageMinimalWinTheGame();

		trace(pointageMin);
		
		switchColor(tableauDesCases[p.obtenirL()][p.obtenirC()]);
		
		//trace("ds afficherCasesPossibles");
        for(i = 1; i <= Math.min(mat.length-p.obtenirL()-1,moveVisibility); i++)
        {
			temp = Number(p.obtenirL());
			temp += Number(i);
			// pourquoi il concatene ??????????????
			// trace("ds premier for avant if  i  temp   mat  L   C  :  "+i+"   "+temp+"   "+this.mat[temp][p.obtenirC()]+"   "+p.obtenirL()+"   "+p.obtenirC());
		
            if(this.mat[temp][p.obtenirC()] > 0)
            {
				trace(pointageMin);
				if(tableauDesCases[temp][p.obtenirC()].obtenirType() > 40000 && _level0.loader.contentHolder.planche.obtenirPerso().obtenirPointage() < pointageMin)
				{
					trace("pas assez de points pour atteindre le WinTheGame");
				}
				else
				{
					switchColor(tableauDesCases[temp][p.obtenirC()]);
	
					level = (tableauDesCases.length*tableauDesCases[0].length) +(temp*tableauDesCases[0].length)+Number(p.obtenirC()) + Number(1);
					//trace("ds if,  level  "+level);
					if(tableauDesCases[temp][p.obtenirC()].obtenirType() > 40000)
					{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("winShine", "winShine"+level, level);
					   tableauDesCases[temp][p.obtenirC()].obtenirWinTheGame().shineWin();
					}else{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					   brille._alpha = 0;
					}
					brille._x = tableauDesCases[temp][p.obtenirC()].obtenirClipCase()._x;
					brille._y = tableauDesCases[temp][p.obtenirC()].obtenirClipCase()._y;
					//brille._alpha = 0;
					brille._width = largeurDeCase/0.55;
					brille._height = hauteurDeCase/0.55;//0.85
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = temp;
					brille._colonne = p.obtenirC();
			
					afficherValeurDeplacementColonne(p, brille, temp, p.obtenirC());

					brille.onPress = function()
					{   
						removeMovieClip(p.obtenirImage().valDeplace);
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
						_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
						tableauDesCases[temp][p.obtenirC()].obtenirWinTheGame().removeShineWin();
					};
					tableauDesCases[temp][p.obtenirC()].definirCasePossible(brille);
			
					_root.objGestionnaireInterface.ajouterBouton(brille, 3);
				}
            }
            else
            {
                break;
            }
			
        }
	
	
        for(i=1; i <= Math.min(p.obtenirL(),moveVisibility); i++)
        {
			//trace("ds deuxieme for avant if  i  mat  :  "+i+"   "+mat[p.obtenirL()-i][p.obtenirC()]);
			temp = p.obtenirL()-i;
           
			if(mat[temp][p.obtenirC()] > 0)
            {
				if(tableauDesCases[temp][p.obtenirC()].obtenirType() > 40000 && _level0.loader.contentHolder.planche.obtenirPerso().obtenirPointage() < pointageMin)
				{
					trace("pas assez de points pour atteindre le WinTheGame");
				}
				else
				{
					switchColor(tableauDesCases[temp][p.obtenirC()]);
					
					// trace("tableau des cases  vs  mat  : "+ tableauDesCases.length+"   "+mat.length);
					// trace("tableau des cases[0]  vs  mat[]  : "+ tableauDesCases[0].length+"   "+mat[0].length);
					level = (tableauDesCases.length*tableauDesCases[0].length)  +  ((temp)*tableauDesCases[0].length)  +  Number(p.obtenirC()) + Number(1);
					// trace("ds if,  level:  "+level);
					if(tableauDesCases[temp][p.obtenirC()].obtenirType() > 40000 ){
					   
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("winShine", "winShine"+level, level);
					   tableauDesCases[temp][p.obtenirC()].obtenirWinTheGame().shineWin();
					}else{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					   brille._alpha = 0;
					}
					//brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[temp][p.obtenirC()].obtenirClipCase()._x;
					brille._y = tableauDesCases[temp][p.obtenirC()].obtenirClipCase()._y;
					
					brille._width = largeurDeCase/0.55;
					brille._height = hauteurDeCase/0.55;//0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = temp;
					brille._colonne = p.obtenirC();
					
					afficherValeurDeplacementColonne(p, brille, temp, p.obtenirC());
					
					brille.onPress = function ()
					{
						removeMovieClip(p.obtenirImage().valDeplace);
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees); 
						tableauDesCases[temp][p.obtenirC()].obtenirWinTheGame().removeShineWin();
					};
					this.tableauDesCases[temp][p.obtenirC()].definirCasePossible(brille);
			
					_root.objGestionnaireInterface.ajouterBouton(brille, 3);
				}
           	}
            else
            {
                break;
            }
        }
	
        for(i=1;i<=Math.min(mat[0].length-p.obtenirC()-1,moveVisibility);i++)
        {
			temp = Number(p.obtenirC());
			temp += Number(i);
			//trace("ds troisieme for avant if  i  temp   mat  L   C  :  "+i+"   "+temp+"    "+mat[p.obtenirL()][temp]+"   "+p.obtenirL()+"   "+p.obtenirC());
            
			if(mat[p.obtenirL()][temp] > 0)
            {
				if(tableauDesCases[p.obtenirL()][temp].obtenirType() > 40000 && _level0.loader.contentHolder.planche.obtenirPerso().obtenirPointage() < pointageMin)
				{
					trace("pas assez de points pour atteindre le WinTheGame");
				}
				else
				{
					switchColor(tableauDesCases[p.obtenirL()][temp]);
			
					level = (tableauDesCases.length*tableauDesCases[0].length) + Number(p.obtenirL()*tableauDesCases[0].length) + temp + Number(1);
					//trace("ds if : level :   "+level);
					if(tableauDesCases[p.obtenirL()][temp].obtenirType() > 40000){
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("winShine", "winShine"+level, level);
					   tableauDesCases[p.obtenirL()][temp].obtenirWinTheGame().shineWin();
					}else{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					   brille._alpha = 0;
					}
					//brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[p.obtenirL()][temp].obtenirClipCase()._x;
					brille._y = tableauDesCases[p.obtenirL()][temp].obtenirClipCase()._y;
					
					brille._width = largeurDeCase/0.55;
					brille._height = hauteurDeCase/0.55;//0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = p.obtenirL();
					brille._colonne = temp;
				
					afficherValeurDeplacementLigne(p, brille, p.obtenirL(), temp);
					
					brille.onPress = function ()
					{
						removeMovieClip(p.obtenirImage().valDeplace);
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
			            tableauDesCases[p.obtenirL()][temp].obtenirWinTheGame().removeShineWin(); 
					};
					this.tableauDesCases[p.obtenirL()][temp].definirCasePossible(brille);
			
					_root.objGestionnaireInterface.ajouterBouton(brille, 3);
				}
			}
            else
            {
                break;
            }
        }
		
        for(i=1;i<=Math.min(p.obtenirC(),moveVisibility);i++)
        {
			//trace("ds dernier for avant if  i  mat  :  "+i+"   "+mat[p.obtenirL()][p.obtenirC()-i]);
			temp = p.obtenirC()-i;
			
            if(mat[p.obtenirL()][temp] > 0)
            {
				if(tableauDesCases[p.obtenirL()][temp].obtenirType() > 40000 && _level0.loader.contentHolder.planche.obtenirPerso().obtenirPointage() < pointageMin)
				{
					trace("pas assez de points pour atteindre le WinTheGame");
				}
				else
				{
					switchColor(tableauDesCases[p.obtenirL()][temp]);
			
					level = (tableauDesCases.length*tableauDesCases[0].length) + (Number(p.obtenirL())*tableauDesCases[0].length)+temp+Number(1);
					//trace("ds if,  level: "+level);
					if(tableauDesCases[p.obtenirL()][temp].obtenirType() > 40000){
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("winShine", "winShine"+level, level);
					   tableauDesCases[p.obtenirL()][temp].obtenirWinTheGame().shineWin();
					}else{
					   brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					   brille._alpha = 0;
					}
					//brille = _level0.loader.contentHolder.referenceLayer.attachMovie("caseAlpha", "caseAlpha"+level, level);
					brille._x = tableauDesCases[p.obtenirL()][temp].obtenirClipCase()._x;
					brille._y = tableauDesCases[p.obtenirL()][temp].obtenirClipCase()._y;
					
					brille._width = largeurDeCase/0.55;
					brille._height = hauteurDeCase/0.55;//0.85;
					brille._ligne = new Object();
					brille._colonne = new Object();
					brille._ligne = p.obtenirL();
					brille._colonne = temp;
					
					afficherValeurDeplacementLigne(p, brille, p.obtenirL(), temp);
			
					brille.onPress = function ()
					{
						removeMovieClip(p.obtenirImage().valDeplace);
						coordonnees.definirX(this._ligne);
						coordonnees.definirY(this._colonne);
				
						_level0.loader.contentHolder.planche.effacerCasesPossibles(_level0.loader.contentHolder.planche.obtenirPerso());
						_level0.loader.contentHolder.objGestionnaireEvenements.deplacerPersonnage(coordonnees);
						tableauDesCases[p.obtenirL()][temp].obtenirWinTheGame().removeShineWin(); 
					};
					this.tableauDesCases[p.obtenirL()][temp].definirCasePossible(brille);
			
					_root.objGestionnaireInterface.ajouterBouton(brille, 3);		
				}
            }
            else
            {
                break;
            }
        }
		trace("Fin afficher cases possibles"); 
	 }// end if for watcher
    }
    
	
	/*
	Les 2 fonctions suivantes servent a afficher le nombre de points que
	vaut chaque deplacement quand la souris est deplacee sur les cases.
	Elles sont appelees dans afficherCasesPossibles().
	*/
	function afficherValeurDeplacementColonne (p, brille:MovieClip, dx:Number, dy:Number)
	{
		var mc:MovieClip = p.obtenirImage();
		brille.onRollOver = function()
		{
			mc.createEmptyMovieClip("valDeplace", 999)
			mc.valDeplace = _level0.loader.contentHolder.referenceLayer.attachMovie("ptsTxt", "ptsTxt_mc", 999 );
			//mc.valDeplace._x = p.obtenirX();
			//mc.valDeplace._y = p.obtenirY()-150;
			mc.valDeplace._x = brille._x;
			mc.valDeplace._y = brille._y;
				
			mc.valDeplace.valeur = Math.abs(p.obtenirL() - dx);
			mc.valDeplace._alpha = 100;
				
			switch(mc.valDeplace.valeur)
			{
				case 4:
					mc.valDeplace.valeur = 5;
				break;
				case 5:
					mc.valDeplace.valeur = 8;
				break;
				case 6:
					mc.valDeplace.valeur = 13;
				break;
			}

		}

		brille.onRollOut = function()
		{
			mc.valDeplace.removeMovieClip();
		}
	}

	function afficherValeurDeplacementLigne (p, brille:MovieClip, dx:Number, dy:Number)
    {
		var mc:MovieClip = p.obtenirImage();
		brille.onRollOver = function()
		{
			mc.createEmptyMovieClip("valDeplace", 999)
			mc.valDeplace = _level0.loader.contentHolder.referenceLayer.attachMovie("ptsTxt", "ptsTxt_mc", 999 );
			//mc.valDeplace._x = p.obtenirX();
			//mc.valDeplace._y = p.obtenirY()-150;
			mc.valDeplace._x = brille._x;
			mc.valDeplace._y = brille._y;
		
			mc.valDeplace.valeur = Math.abs(p.obtenirC() - dy);
			mc.valDeplace._alpha = 100;
			
			switch(mc.valDeplace.valeur)
			{
				case 4:
					mc.valDeplace.valeur = 5;
				break;
				case 5:
					mc.valDeplace.valeur = 8;
				break;
				case 6:
					mc.valDeplace.valeur = 13;
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
	
	
    function effacerCasesPossibles(p:Personnage)
    {
        var i:Number;
		var temp:Number;
	
		trace("Debut effacerCasesPossibles");
	
		switchBackColor(tableauDesCases[p.obtenirL()][p.obtenirC()]);
	
        for(i=1;i<=Math.min(mat.length-p.obtenirL()-1,6);i++)
        {
			temp = Number(Number(p.obtenirL()) + i);   
			//trace("ds premier for    i     L    temp"+i+"    "+p.obtenirL()+"    "+temp);
		
            if(mat[temp][p.obtenirC()] > 0)
            {
		    	//trace("ds if premier for");
                tableauDesCases[temp][p.obtenirC()].effacerCasePossible();
				switchBackColor(tableauDesCases[temp][p.obtenirC()]);
            }
            else
            {
                break;
            }
        }
		
        for(i=1;i<=Math.min(p.obtenirL(),6);i++)
        {
			//trace("ds deuxieme for");
		
            if(mat[p.obtenirL()-i][p.obtenirC()] > 0)
            {
				// trace("ds if deuxieme for");
                tableauDesCases[p.obtenirL()-i][p.obtenirC()].effacerCasePossible();
				switchBackColor(tableauDesCases[p.obtenirL()-i][p.obtenirC()]);
            }
            else
            {
                break;
            }
        }
		
        for(i=1;i<=Math.min(mat[0].length-p.obtenirC()-1,6);i++)
        {
			temp = Number(Number(p.obtenirC())+i);   
			//trace("ds troisieme for    i     L    temp"+i+"    "+p.obtenirC()+"    "+temp);
		
            if(mat[p.obtenirL()][temp] > 0)
            {
		    	//trace("ds if troisieme for");
                tableauDesCases[p.obtenirL()][temp].effacerCasePossible();
				switchBackColor(tableauDesCases[p.obtenirL()][temp]);
            }
            else
            {
                break;
            }
        }
		
        for(i=1;i<=Math.min(p.obtenirC(),6);i++)
        {
			//trace("ds quatrieme for");
		
            if(mat[p.obtenirL()][p.obtenirC()-i] > 0)
            {
		    	//trace("ds if quatrieme for");
                tableauDesCases[p.obtenirL()][p.obtenirC()-i].effacerCasePossible();
				switchBackColor(tableauDesCases[p.obtenirL()][p.obtenirC()-i]);
            }
            else
            {
                break;
            }
        }
	
		_root.objGestionnaireInterface.deleterCasesSpeciales();  
		//trace("Fin effacerCasesPossibles");
		//trace("****************************");
    }	
	
    ////////////////////////////////////////////////////////////////////////////
    function effacerPiece(ll:Number, cc:Number)
    {
        tableauDesCases[ll][cc].effacerPiece();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    function effacerObjet(ll:Number, cc:Number)
    {
        tableauDesCases[ll][cc].effacerObjet();
    }
  
  
    // num est le numero du personnage   str est le type de collision
    function teleporterPersonnage(nom:String, ancienL:Number, ancienC:Number, nouveauL:Number, nouveauC:Number, str:String)
    {
	    trace(" dans teleporterPersonnage, parametres :  "+nom+"   "+ancienL+"   "+ancienC+"   "+nouveauL+"   "+nouveauC+"   "+str);
	    var listeTemporaire:Array;
	    //var p:Point = new Point(this.tableauDesCases[nouveauL][nouveauC].obtenirClipCase()._x, this.tableauDesCases[nouveauL][nouveauC].obtenirClipCase()._y);
	
	    var p:Point = new Point(nouveauL,nouveauC);
	    
	    //trace(" le point :  "+p.obtenirX()+"     "+p.obtenirY());
	    listeTemporaire = this.tableauDesCases[ancienL][ancienC].obtenirListeDesPersonnages();
	    
	    for(var i:Number = 0; i< listeTemporaire.length; i++)
	    {
		    if(listeTemporaire[i].obtenirNom() == nom)
		    {
			    trace("juste avant de definir la prochaine poisition");
			    //listeTemporaire[i].definirPosition(p, nouveauL, nouveauC);  // on le met si on veut teleportation, mais probleme avec les collisions...
			    this.tableauDesCases[ancienL][ancienC].obtenirListeDesPersonnages()[i].definirProchainePosition(p,str);
			    break;
		    }
	    }
	    
	    trace("fin de teleporterPersonnage");
    }
    
    
    function estCaseSpeciale(lig:Number, col:Number):Boolean
    {
	    //trace("est dans estCaseSpeciale   "+lig+"   "+col);
		if(tableauDesCases[lig][col].obtenirType()%100 > 90)
        {
			trace("ds le if de estCaseSpeciale");
            return true;
        }
	
		return false;
    }
        
}