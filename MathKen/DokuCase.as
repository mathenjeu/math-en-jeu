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

class DokuCase
{	
	private var l:Number;					// Row
	private var c:Number;					// Column
	private var iD:String;                  // id to identify our case in array
	                                        // little bit duplicate for row and column?
	//private var clipCase:MovieClip;
	private var caseValue:Number;
	private var caseText:TextField;
	private var caseColor:String;
	
	////////////////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////////////////
	
	function DokuCase(id:String, caseTxt:TextField)
	{
		iD = id;
		this.caseValue = 0;
		generateLC();
		caseText = caseTxt;
		caseColor = "0x009900";
		//trace(caseText.textColor + " " + caseText.textColor);
	}


	////////////////////////////////////////////////////////////
	//	Getter methods
	////////////////////////////////////////////////////////////
    function getId():String
	{
		return iD;
	}

	function obtenirL():Number
	{
		return this.l;
	}

	function obtenirC():Number
	{
		return this.c;
	}
	
	function getValue():Number
	{
		return this.caseValue;
	}
	
	function isThisCase(id:String):Boolean
	{	
		if(iD == id)
		   return true;
		return false;		
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
	
	function setValue(val:Number, correct:Boolean)
	{
		//trace(val + " correct = " + correct);
		caseValue = val;
				
		if(correct)
		{
		   caseText.textColor = 0x009900;
		   caseColor = "0x009900";
		}else{
		   caseText.textColor = 0xCC0033;
		   caseColor = "0xCC0033";
		}
		//trace(" caseText.textColor + " + caseColor);
	}
	
	function setGreen()
	{
	   caseText.textColor = 0x009900;
 	   caseColor = "0x009900";
	   trace(" caseText.textColor in setGreeen " + caseColor);
	}
	
	function isRedColor():Boolean
	{
		trace(" caseText.textColor + " + caseColor);
		return (caseColor == "0xCC0033");
	}
	
	//function to init line and column values from id string
	function generateLC()
	{
		this.l = Number(iD.substr(1,1));
		this.c = Number(iD.substr(2,1));
		//trace("id - " + iD + " l " + l + " c " + c);
	}
	
	function hasValue():Boolean
	{
		return (caseValue == 1 || caseValue == 4 || caseValue == 3 || caseValue == 2);
	}

	
}	// End of Case class