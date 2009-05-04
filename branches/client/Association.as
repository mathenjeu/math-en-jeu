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

/******************************************************************************
Classe pour le jeu Association (version1)

09-07-2007 Alexandre Couët - Version initiale
******************************************************************************/
class Association
{

	private var noMot:Number;					//le numéro du mot en cours dans le tableau mots
	private var mots:Array;						//tableau des mots
	private var motEnCours:Array;				//les mots choisis
	private var defEnCours:Array;				//les définitions associées aux mots
	private var tabReponse:Array;				//les indices de la réponse à trouver
	private var motDejaTrouve:Array = new Array;//tableau des mots déja trouvé

	/*****************************************************************************
	Fonction : association
	Description : constructeur de la classe association
	******************************************************************************/
	public function Association()
	{
		this.motEnCours = new Array();
		this.defEnCours = new Array();
		this.tabReponse = new Array();
	}
	
	/*****************************************************************************
	Fonction : chargerMots
	Paramêtre :
		- myXML : un objet XML déjà chargé avec les valeurs
	Description : on parcours l'objet XML et on ajoute les mots
		au tableau de mot.
	******************************************************************************/
	public function chargerMots(myXML:XML)
	{
		var root = myXML.firstChild;
        var nodes = root.childNodes;
		this.mots = new Array(nodes.length);
        for(var i=0; i<nodes.length; i++)
		{  
			var subnodes = nodes[i].childNodes;
			this.mots[i] = new Array(3);
			this.mots[i][0] = subnodes[0].firstChild.nodeValue.toString().toUpperCase();
			this.mots[i][1] = 0;
		//TODO - la ligne rempli du tableau pour rien, il faudrait tout décaler la 3ème colonne du tablau
			this.mots[i][2] = subnodes[1].firstChild.nodeValue.toString();
        }
	}
		
	
	/*****************************************************************************
	Fonction : estDejaTrouve
	Paramêtre :
		- no : le numéro du mot 
	Description : on vérifie si le no du mot a déjà été trouvé
		par le joueur. Retourne vrai si c'est la cas, faux sinon
	******************************************************************************/
	private function estDejaTrouve(no:Number)
	{
		var i:Number;
		for(i=0;i<this.motDejaTrouve.length;i++)
		{
			if(this.motDejaTrouve[i]==no)
			{
				return true;
			}
		}
		return false;
	}
	
	
	/*****************************************************************************
	Fonction : nouveauxMots
	Paramêtre :
	Description : on trouve un nouveau mot pour le joueur et
		on met à jour les informations.
		S'il n'en reste pas on retourne faux.
	******************************************************************************/
	public function nouveauxMots()
	{
		
		for(var i:Number = 0; i<4; i++)
		{
			//on ne vérifie pas s'il reste des mots à trouver : besoin de juste 4
			//on trouve un mot qui n'a pas encore été trouvé
			do
			{
				this.noMot = Math.floor(Math.random()*this.mots.length);
			}while(estDejaTrouve(this.noMot))
					
			this.motEnCours[i] = this.mots[this.noMot][0];
			this.defEnCours[i] = this.mots[this.noMot][2];
			trace('mot ' + i + ' : ' + this.mots[this.noMot][0]);
		
			motTrouve();
		}
		
		melangerReponse();
		this.tabReponse = fixerBonneReponse(this.tabReponse);
		trace("bonne reponse : " + this.tabReponse);
	}
	
	
	/*****************************************************************************
	Fonction : motTrouve
	Paramêtre :
	Description : on ajoute un mot à la liste des mots déjà trouvés
	******************************************************************************/
	public function motTrouve()
	{
		this.motDejaTrouve.push(this.noMot);
	}
	
			
	/*FONCTIONS DE MÉLANGE
	**********************/
	
	/*****************************************************************************
	Fonction : melangerReponse
	Paramêtre :
		- m : un mot à mélanger
	Description : on le met dans un tableau, on mélange et on récupère dans un string
	******************************************************************************/
	function melangerReponse()
	{
		for(var i:Number = 0; i<4; i++)
		{
			this.tabReponse[i] = i;
		}

		for(var i:Number = 0; i<4; i++)
		{
			switch(Math.round(Math.random()*6))
			{
				case 0:
				trace("0");
					inverse(0, 1);
				break;
				
				case 1:
				trace("1");
					inverse(1, 2);
				break;
				
				case 2:
				trace("2");
					inverse(2, 3);
				break;
				
				case 3:
				trace("3");
					inverse(3, 0);
				break;
				
				case 4:
				trace("4");
					inverse(0, 2);
				break;
				
				case 5:
				trace("5");
					inverse(1, 3);
				break;
				case 6:
				
				trace("6");
					inverse(3, 0);
				break;
				
				default:
					trace("???");
				break;
			}
		}
		
	}

	
	/*****************************************************************************
	Fonction : inverse
	Paramêtre :	i, j : les 2 rangs
	Description : on invsere le contenu des tableaux aux rangs indiqués
	******************************************************************************/
	public function inverse(i:Number, j:Number)
	{
		var temp:Number;
		var tempDef:String;
		
		temp = this.tabReponse[i];
		this.tabReponse[i] = this.tabReponse[j];
		this.tabReponse[j] = temp;
		
		tempDef = this.defEnCours[i];
		this.defEnCours[i] = this.defEnCours[j];
		this.defEnCours[j] = tempDef;
	}

	
	/*****************************************************************************
	Fonction : fixerBonneReponse
	******************************************************************************/
	public function fixerBonneReponse(tableauReponse:Array):Array
	{
		var temp:Array = new Array(4);
		
		for(var i:Number = 0; i<4; i++)
		{
			for(var j:Number = 0; j<4; j++)
			{
				if(tableauReponse[j] == i)
				{
					temp[i] = j;
				}
	
			}
		}
		
		return temp;
	}

	
	/*****************************************************************************
	* FONCTIONS DE RETOUR
	******************************************************************************/

	public function retMot()
	{
		return this.mots[this.noMot][0];
	}
	public function retValeur()
	{
		return this.mots[this.noMot][1];
	}
	public function retDescription()
	{
		return this.mots[this.noMot][2];
	}
	public function retMotEnCours()
	{
		return this.motEnCours;
	}
	public function retDefEnCours()
	{
		return this.defEnCours;
	}
	public function retTabReponse()
	{
		return this.tabReponse;
	}
	public function obtenirMots()
	{
		return this.mots;
	}
		
}