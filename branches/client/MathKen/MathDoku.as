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

class MathDoku
{	
	private var groupsArray:Array;
	private var caseArray:Array;
	////////////////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////////////////
	
	function MathDoku()
	{
		groupsArray = new Array();
		caseArray = new Array();
	}


	////////////////////////////////////////////////////////////
	//	Getter methods
	////////////////////////////////////////////////////////////

	function getGroup(id:String):DokuGroup
	{
		//trace(" test getGroup");
		for(var i in groupsArray)
		{
			if(groupsArray[i].hasCase(id))
			  return groupsArray[i];			
		}
		return null;
	}
	
	////////////////////////////////////////////////////////////
	//	Setter methods
	////////////////////////////////////////////////////////////
	function addGroup(group:DokuGroup)
	{ 
	   groupsArray.push(group);
	}
	
	function addCase(mCase:DokuCase)
	{
		trace("addCase - " + mCase.getValue());
		caseArray.push(mCase);
	}
	
	function setCaseValue(id:String, val:String)
	{
		var correct:Boolean = true; 
		if(!isNaN(val))
		   correct = verifyLineColumn(id, val);
		getGroup(id).setCaseValue(id, val, correct);
		verifyCasesWrongColor();
	}
	
	function verifyLineColumn(id:String, val:String):Boolean
	{
		var l:Number = Number(id.substr(1,1));
		var c:Number = Number(id.substr(2,1));
				
		for(var i in caseArray)
		{
			if(caseArray[i].getId() != id)
			{
			   //var mCase:DokuCase = caseArray[i];
			   //trace("case ver : l - " + caseArray[i].obtenirL() + " val - " + caseArray[i].getValue());
			   if((caseArray[i].obtenirL() == l)&&(caseArray[i].getValue() == Number(val)))
			      return false;	
			   if((caseArray[i].obtenirC() == c)&&(caseArray[i].getValue() == Number(val)))
			      return false;
			}
		}		
		return true;
	}
	
	function verifyCasesWrongColor()
	{		
	   	for(var i in caseArray)
		{
			if(caseArray[i].isRedColor())
			{
				trace(" verifyCasesWrongColor 2 ");
				if(verifyLineColumn( caseArray[i].getId(),  String(caseArray[i].getValue())))
				   caseArray[i].setGreen();				  
			}			
		}		
	}
	
	function verifyIfDid():Boolean
	{
		///var correct:Boolean = false;
		if(isComplete())
		{
			if(verifyLinesColumns())
			{
			   if(verifyGroups())
			      return true;
			}
		}		
		return false;
	}
	
	function isComplete():Boolean
	{
		for(var i in caseArray)
		{
			//var mCase:DokuCase = caseArray[i];
			if(!caseArray[i].hasValue())
			  return false;	
			
		}		
		return true;
	}
	
	function verifyGroups():Boolean
	{
		for(var i in groupsArray)
		{
			if(!groupsArray[i].verifyGroup())
			   return false;			
		}
		return true;
	}
	
	function verifyLinesColumns():Boolean
	{
		for(var i in caseArray)
		{
			var l:Number = caseArray[i].obtenirL();
		    var c:Number = caseArray[i].obtenirC();
			var val:Number = caseArray[i].getValue()
			
			for(var j in caseArray)
		    {
				if(i != j)
				{
			       if((caseArray[j].obtenirL() == l)&&(caseArray[j].getValue() == val))
			          return false;	
			       if((caseArray[j].obtenirC() == c)&&(caseArray[j].getValue() == val))
			          return false;
				}
		    }			   
		}
		return true;
	}
	
}	// End of Case class