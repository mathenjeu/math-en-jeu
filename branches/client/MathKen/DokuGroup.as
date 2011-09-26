/*******************************************************************
MathDoku Copyright (C) 2011 Projet Mathamaze

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

class DokuGroup
{	
	private var groupCases:Array;
	private var groupLaw:String;
	private var groupLawValue:Number;
	// id of textField in fla that show group law
	private var groupLawId:String;
	private var groupText:TextField;
	private var master:MathDoku;
	//private var iD:String;                 
	////////////////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////////////////
	
	function DokuGroup(math:MathDoku)
	{
		groupCases = new Array();
		master = math;
	}
	
	function addCase(caseString:String, caseTxt:TextField)
	{
		var gameCase:DokuCase = new DokuCase(caseString, caseTxt);
		groupCases.push(gameCase);
		master.addCase(gameCase);
	}
	
	function getCase(id:String):DokuCase
	{
		for(var i in groupCases)
		{
			if(groupCases[i].isThisCase(id))
			   return groupCases[i];
		}
		return null;		  
	}
	
	function getGroupId()
	{
		return groupLawId;
	}
	
	// change case value ... if group is full verify it and
	// inform the main about corectness by boolean value
	function setCaseValue(id:String, val:String, correct:Boolean)
	{
		var test:Boolean = isNaN(val);
		trace("in setCaseValue " + val + "*");
		if(test)
		{  
		   getCase(id).setValue(0, true);
		   groupText.textColor = 0x000000;
		}else
		{
		   getCase(id).setValue(Number(val), correct);
		   if(verifyGroupFullness())
		   {
			 if(verifyGroup())
			 {
				 groupText.textColor = 0x000000;
			 }else{
				 groupText.textColor = 0xCC0033; 
			 }
			
		   }		   
		}
		
	}// end method
	
	// verify if all group cases is completed
	function verifyGroupFullness():Boolean
	{
		for(var i in groupCases)
		{
			var isEmptyCase:Boolean = (groupCases[i].getValue() == 0);
			trace("is empty - " + isEmptyCase);
			if(isEmptyCase)
			   return false;
		}
		return true;
	}
	
	function setGroupLaw(law:String, id:String, group_txt:TextField)
	{
		if(law.length == 1)
		{
			groupLaw = "";
			groupLawValue = Number(law);
		}else {
		    groupLaw = law.substr(-1,1);
			groupLawValue = Number(law.substring(0, law.length - 1));		    
		}
		
		groupLawId = id;
		
		groupText = group_txt;
		
		trace(groupLaw + " " + groupLawValue + " " + groupLawId);		
	}
	
	function setGroupLawId(id:String)
	{
		groupLawId = id;
	}
	
	function hasCase(id:String):Boolean
	{
		for(var i in groupCases)
		{
			if(groupCases[i].isThisCase(id))
			   return true;
		}
		return false;		  
	}
	
	function verifyGroup():Boolean
	{
		var allowed:Boolean = false;
		switch(groupLaw)
		{  
		   case "":		      
		      allowed = verifySingleCase();
			  trace("groupLaw  ? " + groupLaw + " " + allowed);
		      break;
		   case "+":
		      allowed = verifyAddCases();
			  trace("groupLaw  + " + groupLaw + " " + allowed);
		      break;
		   case "-":
		      allowed = verifySubCases();
			  trace("groupLaw  - " + groupLaw + " " + allowed);
		      break;
		   case "*":
		      allowed = verifyMultiplyCases();
			  trace("groupLaw  * " + groupLaw + " " + allowed);
		      break;
		   case "/":
		      allowed = verifyDivCases();
			  trace("groupLaw  / " + groupLaw + " " + allowed);
		      break;
		}	
		return allowed;
	} // end verify Group
	
	function verifySingleCase():Boolean
	{
		var test:Number = groupCases[0].getValue();
		return test == groupLawValue;
	}
	
	function verifyAddCases():Boolean
	{
		var resultat:Number = 0;
		for(var i in groupCases)
		  resultat += groupCases[i].getValue();
		return resultat == groupLawValue;
	}
	
	function verifySubCases():Boolean
	{
		groupCases.sort(compareByValueDescending);
		//for(var i = 0; i < groupCases.length; i++)
		   //trace(groupCases[i].getValue() + " test sub");
		var resultat:Number = groupCases[0].getValue();
		for(var i = 1; i < groupCases.length; i++)
		   resultat -= groupCases[i].getValue();
		return resultat == groupLawValue;
	}
	
	function verifyMultiplyCases():Boolean
	{
		var resultat:Number = 1;
		for(var i in groupCases)
		  resultat *= groupCases[i].getValue();
		return resultat == groupLawValue;
	}
	
	function verifyDivCases():Boolean
	{
		groupCases.sort(compareByValueDescending);
		//for(var i = 0; i < groupCases.length; i++)
		   //trace(groupCases[i].getValue() + " test sub");
		var resultat:Number = groupCases[0].getValue();
		for(var i = 1; i < groupCases.length; i++)
		   resultat /= groupCases[i].getValue();
		return resultat == groupLawValue;
	}
	
	// methode used as compare function to sort our cases by their value
	public function compareByValueDescending(element1, element2)
	{
		return element2.getValue() - element1.getValue();
	}
}
