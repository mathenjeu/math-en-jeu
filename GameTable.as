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

class GameTable
{
	private var tableId:Number;   //   numero de la table dans laquelle on est
	private var tableName:String;     // name of the created table
    private var tableTime:Number;   //  temps que va durer la partie, en minutes
	private var gameType:String;        // gameType in our table
    
	// constructor//
	public function GameTable(orderId:Number)
	{
		this.tableId = orderId;
	}
	
	//////////////////////////////////////
	public function getTableId():Number
	{
		return this.tableId
	}
	
	public function setTableId(nb:Number)
	{
		this.tableId = nb;
	}
	
}// end class