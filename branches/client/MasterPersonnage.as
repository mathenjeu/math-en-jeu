
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

/**
 * MasterPersonnage is a phantom. He don't have a picture
 * Used to create the table in the Tournament type of game, he
 * don't participate on the game directly. He is creator and observer
 * of the game. 
 */
class MasterPersonnage implements IPersonnage
{
	// role of user if 1 - simple user , if 2 - master(admin)
	private var role:Number;
	// name of user that is master of pers
	private var nom:String;
	private var numero;
		
	////////////////////////////////////////////////////////////
	public function getIdPersonnage():Number
	{
		return numero;
	}
	
	////////////////////////////////////////////////////////////
	public function setIdPersonnage(n:Number)
	{
		numero = n;
	}
		
	public function setRole(n:Number)
	{
		role = n;
	}
	
	public function getRole():Number
	{
		return role;
	}
	
   	////////////////////////////////////////////////////////////
	public function obtenirNom():String
	{
		return this.nom;
	}
		
		
	
	////////////////////////////////////////////////////////////
	public function obtenirNumero():Number
	{
		return numero;
	}
	
	////////////////////////////////////////////////////////////
	public function definirNumero(n:Number)
	{
		numero = n;
	}
	
			
	//////////////////////////////////////////////////////////////////////////////////////
	//	CONSTRUCTEUR
	//////////////////////////////////////////////////////////////////////////////////////
	function MasterPersonnage(idPers:Number, nom:String, role:Number)
	{
		this.numero = idPers;							
		this.nom = nom;		
		this.role = role;		
	}// end constr
				
}