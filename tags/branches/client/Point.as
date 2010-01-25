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


class Point
{
	private var x:Number = -50;
	private var y:Number = -50;

 	function Point(xx:Number, yy:Number)
	{
 		x = xx;
		y = yy;
 	}


	function obtenirX():Number
	{
		return x;
	}


	function obtenirY():Number
	{
		return y;
	}


	// retourne la distance entre ce point et celui passé en paramètre
 	function distanceDe(pt:Point):Number
	{
 		var dx:Number = x - pt.obtenirX();
 		var dy:Number = y - pt.obtenirY();

 		return Math.sqrt(dx * dx + dy * dy);
 	}


	function definirX(xx:Number)
	{
		x = xx;
	}
	
	
	
	function definirY(yy:Number)
	{
		y = yy;
	}



}