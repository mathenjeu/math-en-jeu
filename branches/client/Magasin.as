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


// la classe magasin est utilisée pour l'image de magasin sur la planche de jeu
// pour tout ce qui est relatif à l'utilisation du magasin, voir monMagasin.fla

class Magasin
{
	private var image:MovieClip;
	private var nom:String = new String();


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
	function obtenirImage():MovieClip
	{
		return image;
	}


	////////////////////////////////////////////////////////////
	function obtenirNom():String
	{
		return nom;
	}


	////////////////////////////////////////////////////////////
	function definirImage(i:MovieClip)
	{
		image = i;
	}

	// constructeur
	function Magasin(n:String, niveau:Number)
	{
		image = _level0.loader.contentHolder.referenceLayer.attachMovie(n, "magasin" + niveau, niveau);  
		image._visible = false;
		nom = n;
	}

	function afficher(pt:Point)
	{	
		image._visible = true;
		image._x = pt.obtenirX();
		image._y = pt.obtenirY();
	}

} 