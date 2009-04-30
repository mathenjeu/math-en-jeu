/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Générale Affero (AGPL), telle que publiée par
Affero Inc. ; soit la version 1 de la Licence, ou (à
votre discrétion) une version ultérieure quelconque.

Ce programme est distribué dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans même une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Générale Affero pour plus de détails.

Vous devriez avoir reçu un exemplaire de la Licence Publique
Générale Affero avec ce programme; si ce n'est pas le cas,
écrivez à Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/

class Case
{	
	private var type:Number;				// Type (number)
	private var l:Number;					// Row
	private var c:Number;					// Column
	private var clipCase:MovieClip;
	private var listeDesPersonnages:Array;	// List of characters on the cell
	private var obj:ObjetSurCase;			// Item on cell?
	private var piece:Piece;				// Coin on cell?
	private var magasin:Magasin;			// Shop on cell?
	private var miniGame:Boolean;
	private var winTheGame:WinTheGame;		// WinTheGame on cell?
	private var casePossible:MovieClip;		// Cell accessible?
	private var numMagasin:Number;


	////////////////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////////////////
	
	function Case(num:Number, ll:Number, cc:Number, nombreDeLignes:Number, nombreDeColonnes:Number)
	{
		var nb:Number;		// For the cell's color
		var temp:Number;	// To avoid conflicting layers
		var nombreDeCases:Number = nombreDeLignes * nombreDeColonnes;	// Total number of cells on the board

		this.type = num;
		this.l = ll;
		this.c = cc;
		this.listeDesPersonnages = new Array();
		this.casePossible = null;
		this.numMagasin = -1;
		
		temp = (this.l+1)*nombreDeColonnes - c;
		/*
		if ((this.l == nombreDeLignes - 1) && (this.c == nombreDeColonnes - 2 || this.c == nombreDeColonnes - 3 || this.c == nombreDeColonnes - 4))
		{
			num = 41000;
			this.type = 41000;
		} 
		*/
		nb = Math.abs(num%100);  // The cell's color is determined by num (its type)'s last 2 digits	
		
		
		if((nb >= 91)&&(nb <= 95))
		{
			this.clipCase = _level0.loader.contentHolder.referenceLayer.attachMovie("case91", "case"+temp, temp);
			this.miniGame = true;
		}
		else
		{
			this.clipCase = _level0.loader.contentHolder.referenceLayer.attachMovie("case0", "case"+temp, temp); 
			this.miniGame = false;
		}
		
		if(this.clipCase != null)
		{
			this.clipCase._visible = false;
		}
		
		//////////////////////////////////////////////////////////
		//  Values of num == this.type	||	What's on the cell	||
		//¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯||¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯||
		//				-  10 000		||		Nothing			||
		//  	10 000  -  20 000		||		Shop			||
		//  	20 000  -  30 000		||		Coin			||
		//  	30 000  -  40 000		||		Item			||
		//  	40 000  -  50 000		||		WinTheGame		||
		//		50 000	-				||		Nothing			||
		//								||						||
		//////////////////////////////////////////////////////////

		// Make sure there's nothing on the cell...
		this.piece = null;
		this.magasin = null;
		this.winTheGame = null;
		this.obj = null;
		
		// ... then add a shop, coin or item if necessary
		switch((num - num%10000)/10000)
		{
			case 1:	// Shop
				this.magasin = new Magasin("magasin" + String((num-(num%100))/100-100), 3*nombreDeCases+temp);
				numMagasin = num % 100;
			break;

			case 2:	// Coin
				this.piece = new Piece(2*nombreDeCases+temp);
			break;

			case 3:	// Item
				var nomObj:String = new String();
				trace("num : " + num);
				switch( ((num - 30000) - (num % 100)) / 100)
				{
					case 1:
						nomObj = "Livre";
					break;			
					case 2:
						nomObj = "Papillon";
					break;
					case 3:
						nomObj = "Telephone";
					break;
					case 4:
						nomObj = "Boule";
					break;
					case 5:
						nomObj = "PotionGros";
					break;
					case 6:
						nomObj = "PotionPetit";
					break;
					case 7:
						nomObj = "Banane";
					break;

					default:break;
				}
			
				this.obj = new ObjetSurCase(nomObj, 4*nombreDeCases+temp);	
			break;
			
			case 4: //	WinTheGame
				trace("num : " + num);
				this.winTheGame = new WinTheGame(3*nombreDeCases+temp);
				obtenirClipCase().attachMovie("winTheGame", "winTheGame1", 100);
				if(_level0.loader.contentHolder.langue == "en")
				{
					obtenirClipCase().winTheGame1.texteTorche = "Victory";
				}
			break;
		}				
	}


	////////////////////////////////////////////////////////////
	//	Getter methods
	////////////////////////////////////////////////////////////

	function obtenirL():Number
	{
		return this.l;
	}

	function obtenirC():Number
	{
		return this.c;
	}
	
	function obtenirClipCase():MovieClip
	{
		return this.clipCase;
	}
	
	function obtenirObjet():ObjetSurCase
	{
		return this.obj;
	}

	function obtenirListeDesPersonnages():Array
	{
		return this.listeDesPersonnages;
	}
	
	function obtenirPiece():Piece
	{
		return this.piece;
	}
	
	function obtenirMagasin():Magasin
	{
		return this.magasin;
	}
	
	function obtenirMiniGame():Boolean
	{
		return this.miniGame;
	}
	
	function obtenirWinTheGame():WinTheGame
	{
		return this.winTheGame;
	}
	
	function obtenirType():Number
	{
		return this.type;
	}
	
	function obtenirCasePossible():MovieClip
	{
		return this.casePossible;
	}

	function obtenirNumMagasin():Number
	{
		return this.numMagasin;
	}
	
	////////////////////////////////////////////////////////////
	//	Setter methods
	////////////////////////////////////////////////////////////
	
	function definirL(ll:Number)
	{
		this.l = ll;
	}

	function definirC(cc:Number)
	{
		this.c = cc;
	}

	function definirClipCase(clip:MovieClip)
	{
		this.clipCase = clip;
	}
	
	function definirListeDesPersonnages(liste:Array)
	{
		this.listeDesPersonnages = liste;
	}

	function definirObjet(o:ObjetSurCase)
	{
		this.obj = o;
	}

	function definirPiece(p:Piece)
	{
		this.piece = p;
	}

	function definirMagasin(m:Magasin)
	{
		this.magasin = m;
	}
		
	function definirWinTheGame(w:WinTheGame)
	{
		this.winTheGame = w;
	}

	function definirType(t:Number)
	{
		this.type = t;
	}

	function definirCasePossible(cp:MovieClip)
	{
		this.casePossible = cp;
	}
	
	function definirNumMagasin(n:Number)
	{
		this.numMagasin = n;
	}
	

	////////////////////////////////////////////////////////////
	//	Class functions
	////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// Make the cell not accessible
	function effacerCasePossible()
	{
		this.casePossible.removeMovieClip();
		this.casePossible = null;
	}


	////////////////////////////////////////////////////////////
	// Remove a coin from the cell
	function effacerPiece()
	{
		this.piece.effacer();
		this.piece = null;
		
		// il faudrait changer le numéro de la case !!
		this.type -= 10000;   // c'est ok???
	}
	
	// Remove a WinTheGame from the cell
	function effacerWinTheGame()
	{
		this.winTheGame.effacer();
		this.winTheGame = null;
		
		// il faudrait changer le numéro de la case !!
		this.type -= 41000;   // c'est ok???
	}
	

	////////////////////////////////////////////////////////////
	// Remove an item from the cell
	function effacerObjet()
	{
		this.obj.effacer();
		this.obj = null;
		
		// il faudrait changer le numéro de la case !!
		this.type -= 30000;	 // c'est ok???
	}


	////////////////////////////////////////////////////////////
	// Translate (move) the cell (and everything on it)
	function translater(la:Number, ha:Number)
	{
		var i:Number;

		this.clipCase._x += la;
		this.clipCase._y += ha;

		if(this.piece != null)
		{
			this.piece.translater(la, ha);
		}

		if(this.magasin != null)
		{
			this.magasin.translater(la, ha);
		}
		
		if(this.winTheGame != null)
		{
			this.winTheGame.translater(la, ha);
		}

		if(this.obj != null)
		{
			this.obj.translater(la, ha);
		}

		if(this.casePossible != null)
		{
			this.casePossible._x += la;
			this.casePossible._y += ha;
		}

		for(i = 0; i < this.listeDesPersonnages.length; i++)
		{
			this.listeDesPersonnages[i].translater(la, ha);
		}
	}

	
	////////////////////////////////////////////////////////////
	// Add a character on the cell
	function ajouterPersonnage(p:Personnage)
	{
		
		// Bloc of code to treat the username
        var firstDel = p.obtenirNom().indexOf("-");                 // find first delimiter
        var secondDel = p.obtenirNom().indexOf(".",firstDel + 1);   // find second delimiter
        var master;

        //Now extract the 'master' from username
        if (firstDel != -1 && secondDel != -1)
           master = p.obtenirNom().substring(firstDel + 1, secondDel);
        else
           master = "";
		if(master != "master")
		{ 
		   this.listeDesPersonnages.push(p);
		}
	}


	////////////////////////////////////////////////////////////
	// Remove a character from the cell
	// ca n'affecte pas le num de la case???
	function retirerPersonnage(p:Personnage)
	{
		var i:Number;

		for(i = 0; i < this.listeDesPersonnages.length; i++)
		{
			if(this.listeDesPersonnages[i].obtenirNumero() == p.obtenirNumero())
			{
				this.listeDesPersonnages.splice(i, 1);
			}
		}
	}


	////////////////////////////////////////////////////////////
	// Zoom in/out of the cell (and everything on it)
	function zoomer(valeur:Number)
	{
		var i:Number;

		if((this.clipCase._xscale+valeur) > 20 && (this.clipCase._xscale+valeur) < 200)
		{
			this.clipCase._xscale += valeur;
			this.clipCase._yscale += valeur;
			
			
			if(this.piece != null)
			{
				this.piece.zoomer(valeur);
			}
	
			if(this.magasin != null)
			{
				this.magasin.zoomer(valeur);
			}
			
			if(this.winTheGame != null)
			{
				this.winTheGame.zoomer(valeur);
			}

			if(this.obj != null)
			{
				this.obj.zoomer(valeur);
			}

			if(this.casePossible != null)
			{
				this.casePossible._xscale += valeur;
				this.casePossible._yscale += valeur;
			}
		

			for(i = 0; i < this.listeDesPersonnages.length; i++)
			{
				this.listeDesPersonnages[i].zoomer(valeur);
			}
		}
	}
	
	
	////////////////////////////////////////////////////////////
	// Make the cell (and everything on it) visible
	function afficher(pt:Point)
	{	
		var i:Number;

		if(this.type != -1)
		{
			this.clipCase._visible = true;
		}

		this.clipCase._x = pt.obtenirX();
		this.clipCase._y = pt.obtenirY();

		if(this.magasin != null)
		{
			this.magasin.afficher(pt);
		}

		if(this.winTheGame != null)
		{
			this.winTheGame.afficher(pt);
		}

		if(this.obj != null)
		{
			this.obj.afficher(pt);
		}

		if(this.piece != null)
		{
			this.piece.afficher(pt);
		}

		if(this.casePossible != null )
		{
			this.casePossible._x = pt.obtenirX();
			this.casePossible._y = pt.obtenirY();
		}

		for(i = 0; i < this.listeDesPersonnages.length; i++)
		{
			var pt2:Point = new Point(l,c);
			this.listeDesPersonnages[i].definirPosition(pt, l, c);
			this.listeDesPersonnages[i].definirProchainePosition(pt2,"rien");  
			
			// Bloc of code to treat the username
            var firstDel = this.listeDesPersonnages[i].obtenirNom().indexOf("-");                 // find first delimiter
            var secondDel = this.listeDesPersonnages[i].obtenirNom().indexOf(".",firstDel + 1);   // find second delimiter
            var master;

            //Now extract the 'master' from username
            if (firstDel != -1 && secondDel != -1)
                master = this.listeDesPersonnages[i].obtenirNom().substring(firstDel + 1, secondDel);
            else
                master = "";
			
			
			if(!(master == "master")) 
			 this.listeDesPersonnages[i].afficher();
				   
			if( master == "master")
			   this.listeDesPersonnages[i].cachePersonnage();
		}
	}
	
}	// End of Case class