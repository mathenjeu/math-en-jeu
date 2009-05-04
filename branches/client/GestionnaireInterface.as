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


class GestionnaireInterface
{
	private var listeBoutonsFrame0:Array;
	private var listeBoutonsFrame1:Array;
	private var listeBoutonsFrame2:Array;
	private var listeBoutonsCasesSpeciales:Array;

	
	public function GestionnaireInterface()
	{
		listeBoutonsFrame0 = new Array();
		listeBoutonsFrame1 = new Array();
		listeBoutonsFrame2 = new Array();
		listeBoutonsCasesSpeciales = new Array();
	}


	public function ajouterBouton(b:MovieClip, n:Number)
	{
		switch(n)
		{
			case 0:
				listeBoutonsFrame0.push(b);
			break;
			
			case 1:
				listeBoutonsFrame1.push(b);
			break;
			
			case 2:
				listeBoutonsFrame2.push(b);
			break;
			
			case 3:
				listeBoutonsCasesSpeciales.push(b);
			break;
			
			default:
			
		}
	}


	public function effacerBoutons(n:Number)
	{
		var i:Number;
		
		switch(n)
		{
			case 0:
			//trace("on efface les boutons   0");
				for(i=0; i<listeBoutonsFrame0.length; i++)
				{
					listeBoutonsFrame0[i]._visible = false;
				}
			break;
			
			case 1:
				for(i=0; i<listeBoutonsFrame1.length; i++)
				{
					listeBoutonsFrame1[i]._visible = false;
				}
			break;
			
			case 2:
				for(i=0; i<listeBoutonsFrame2.length; i++)
				{
					listeBoutonsFrame2[i]._visible = false;
				}
			break;
			
			case 3:
				for(i=0; i<listeBoutonsCasesSpeciales.length; i++)
				{
					listeBoutonsCasesSpeciales[i]._visible = false;
				}
			break;
			
			default:
			
		}
	}



	public function afficherBoutons(n:Number)
	{
		var i:Number;

		switch(n)
		{
			case 0:
				for(i=0; i<listeBoutonsFrame0.length; i++)
				{
					listeBoutonsFrame0[i]._visible = true;
				}
			break;
			
			case 1:
				for(i=0; i<listeBoutonsFrame1.length; i++)
				{
					listeBoutonsFrame1[i]._visible = true;
				}
			break;
			
			case 2:
				for(i=0; i<listeBoutonsFrame2.length; i++)
				{
					listeBoutonsFrame2[i]._visible = true;
				}
			break;
			
			case 3:
				for(i=0; i<listeBoutonsCasesSpeciales.length; i++)
				{
					listeBoutonsCasesSpeciales[i]._visible = true;
				}
			break;
			
			default:
		}
	}
	
	
	public function deleterCasesSpeciales()
	{
		delete this.listeBoutonsCasesSpeciales;
		
		this.listeBoutonsCasesSpeciales = new Array();
	}

}