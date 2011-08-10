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
	private var nLines:Number;
	private var nColumns:Number;
    
	// constructor 1//
	public function GameTable(orderId:Number, temps:Number, nameTable:String, gameType:String, nb_lines:Number, nb_columns:Number)
	{
	   this.tableId = orderId;
	   this.tableTime = temps;
	   this.tableName = nameTable;
	   this.gameType = gameType;
	   this.nLines = nb_lines;
	   this.nColumns = nb_columns;
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
	
	public function getTableName():String
	{
		return this.tableName
	}
	
	public function setTableName(nom:String)
	{
		this.tableName = nom;
	}
	
	public function getTableTime():Number
	{
		return this.tableTime
	}
	
	public function setTableTime(duration:Number)
	{
		this.tableTime = duration;
	}
	
	public function getGameType():String
	{
		return this.gameType
	}
	
	public function setGameType(type:String)
	{
		this.gameType = type;
	}
	
	public function compareType(type:String):Boolean
	{
		return type == gameType;
	}
	
}// end class