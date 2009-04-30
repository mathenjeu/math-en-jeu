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

//NOTE: la variable valeur semble totalement inutile


class ObjetSurCase
{
	private var image:MovieClip;
	private var nom:String = new String();
	private var valeur:Number;

	function effacer()
	{
		image.removeMovieClip();
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
	}


	////////////////////////////////////////////////////////////
	function obtenirNom():String
	{
		return nom;
	}



	////////////////////////////////////////////////////////////
	function definirNom(n:String)
	{
		nom = n;
	}


	////////////////////////////////////////////////////////////
	function obtenirValeur():Number
	{
		return valeur;
	}



	////////////////////////////////////////////////////////////
	function definirValeur(v:Number)
	{
		valeur = v;
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
	// la fonction permet de d'afficher sur la grille les objets qui
	// peuvent s'y trouver
	// reçoit le nom de l'objet à placer
	//
	// la valeur ne sert à rien
	//
	function ObjetSurCase(n:String, niveau:Number)
	{
		nom = n;
		switch(nom)
		{
			case "Livre":
				image = _level0.loader.contentHolder.referenceLayer.attachMovie("Livre", "Livre", niveau);  
				image._visible = false;
				valeur = 1;
			break;			
			case "Papillon":
				image = _level0.loader.contentHolder.referenceLayer.attachMovie("Papillon", "Papillon", niveau);  
				image._visible = false;
				valeur = 2;
			break;

			case "Telephone":
				image = _level0.loader.contentHolder.referenceLayer.attachMovie("Telephone", "Telephone", niveau);  
				image._visible = false;
				valeur = 3;
			break;

			case "Boule":
				image = _level0.loader.contentHolder.referenceLayer.attachMovie("Boule", "Boule", niveau);  
				image._visible = false;
				valeur = 4;
			break;

			case "PotionGros":
				image = _level0.loader.contentHolder.referenceLayer.attachMovie("PotionGros", "PotionGros", niveau);  
				image._visible = false;
				valeur = 5;
			break;
			
			case "PotionPetit":
				image = _level0.loader.contentHolder.referenceLayer.attachMovie("PotionPetit", "PotionPetit", niveau);  
				image._visible = false;
				valeur = 6;
			break;
			
			case "Banane":
				image = _level0.loader.contentHolder.referenceLayer.attachMovie("Banane", "Banane", niveau);  
				image._visible = false;
				valeur = 7;
			break;
			
			default:break;
		}
	}

	////////////////////////////////////////////////////////////
	function afficher(pt:Point)
	{
		image._visible = true;
		image._x = pt.obtenirX();
		image._y = pt.obtenirY();
	}
}