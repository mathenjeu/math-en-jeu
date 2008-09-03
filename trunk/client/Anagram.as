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
Classe pour le jeu Anagram (version1)

02-07-2007 Alexandre Couët - Version initiale
******************************************************************************/
class Anagram
{

	private var noMot:Number;					//le numéro du mot en cours dans le tableau mots
	private var motEnCours:Array;				//le mot dans l'état actuel selon les lettres découvertes
	private var pointage:Number;				//le pointage total du joueur
	private var mots:Array;						//tableau des mots
	private var motDejaTrouve:Array = new Array;//tableau des mots déja trouvé
	private var nbMotTrouve:Number;				//le nombre de de mots déjà trouvé
	private var tempsTimer:Number;				//le temps qu'il reste pour répondre
	

	/*****************************************************************************
	Fonction : anagram
	Description : constructeur de la classe anagram
	******************************************************************************/
	public function Anagram()
	{
		this.pointage=0;
		this.nbMotTrouve=0;
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
			this.mots[i][1] = this.calculerValeurMot(this.mots[i][0]);
			this.mots[i][2] = subnodes[1].firstChild.nodeValue.toString();
        }
	}

	
	/*****************************************************************************
	Fonction : calculerValeurMot
	Paramêtre :
		- mot : le mot pour lequel il faut calculer la valeur
	Description : on calcule la valeur du mot selon sa longueur,
		, le nombre de caractère moin commun qu'il contient,
		et le nombre de lettre qui revienne plus d'une fois
	******************************************************************************/
	private function calculerValeurMot(mot:String)
	{
		var valeur:Number=mot.length;
		var lettrePlus:Array = ['à','ô','è','é','ê','û','z','y','x','h','k'];
				
		//on parcours le mot pour ajouter une valeur
		var l:String;
		var i:Number;
		var j:Number;
		mot = mot.toLowerCase();
		for(i=0;i<mot.length;i++)
		{
			l = mot.substr(i,1);
			
			//on vérifier si on a une lettre "bizare"
			for(j=0;j<lettrePlus.length;j++)
			{
				if(l==lettrePlus[j])
				{
					valeur+=1;
				}
			}
			
			//on vérifie si la lettre revient plus d'une fois
			var cpt:Number=0;
			for(j=0;j<mot.length;j++)
			{
				if(mot.substr(j,1)==l)
				{
					cpt++;
				}				
			}
			if(cpt>1) valeur-=0.5; //on enleve un demi point pour les lettres qui revinnent plus d'une fois

		}
		if(mot.length<5) valeur--;
		if(mot.length>4) valeur++;
		valeur = int(valeur/2+0.5);
		
		return valeur;
	}
	
	
	/*****************************************************************************
	Fonction : calculerMauvaiseReponse
	Description :On met le pointage à jour après un mauvaise réponse
	******************************************************************************/
	public function calculerMauvaiseReponse()
	{
		this.pointage = int((this.pointage/2)+0.5);
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
	Fonction : nouveauMot
	Paramêtre :
	Description : on trouve un nouveau mot pour le joueur et
		on met à jour les informations.
		S'il n'en reste pas on retourne faux.
	******************************************************************************/
	public function nouveauMot()
	{
		//on vérifier s'il reste des mots à trouver
		if(this.motDejaTrouve.length == this.mots.length)
		{
			trace('Aucun mot restant');
			return false;
		}
		
		//on trouve un mot qui n'a pas encore été trouvé
		do
		{
			this.noMot = Math.floor(Math.random()*this.mots.length);
		}while(estDejaTrouve(this.noMot))
				
		
		//on charge le mot en cours avec des points d'intérogations
		var i:Number;
		var len:Number = this.mots[this.noMot][0].length;
		this.motEnCours=new Array(len);
		for(i=0;i<len;i++)
		{
			this.motEnCours[i]='?';
		}

		trace('Le mot à trouvé est : ' + this.mots[this.noMot][0]);
		
		return true;
	}
	
	
	
	
	/*****************************************************************************
	Fonction : convertirLettreAccent
	Paramêtre :
		- l : la lettre à convertir
	Description : on vérifie si la lettre passé en paramêtre est 
		une lettre avec accent ou cédille. Si c'est le cas on la 
		convertie en lettre normale. Sinon on retourne simplement 
		la lettre
	******************************************************************************/
	private function convertirLettreAccent(l)
	{
		l = l.toLowerCase();
		if(l == 'é' || l == 'ê' || l == 'è')
		{
			return 'e';			
		}
		else if(l == 'à' || l == 'â')
		{
			return 'a';
		}
		else if(l == 'î' || l == 'ï')
		{
			return 'i';
		}
		else if(l == 'ò' || l == 'ô')
		{
			return 'o';
		}
		else if(l == 'ç')
		{
			return 'c';
		}
		else if(l == 'ù' || l == 'û')
		{
			return 'u';
		}
		else
		{
			return l;
		}
		
	}
	
	/*****************************************************************************
	Fonction : motTrouve
	Paramêtre :
	Description : on vérifie si le mot a été trouvé en entier, si
		oui on ajoute les points,aoute le mot au mot déjà trouvé 
		et retourne vrai, sinon on retourne faux
	******************************************************************************/
	public function motTrouve()
	{
		this.pointage += this.mots[this.noMot][1];
		trace('Le pointage : ' + this.pointage);
		this.motDejaTrouve.push(this.noMot);
		this.nbMotTrouve++;
		return true;
	}

	
	/*****************************************************************************
	* FONCTIONS DE RETOUR
	******************************************************************************/

	public function retPointage()
	{
		return this.pointage;
	}
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
	
	public function obtenirMots()
	{
		return this.mots;
	}

		
	/*FONCTIONS DE MÉLANGE
	**********************/
	
	/*****************************************************************************
	Fonction : melangerMot
	Paramêtre :
		- m : un mot à mélanger
	Description : on le met dans un tableau, on mélange et on récupère dans un string
	******************************************************************************/
	function melangerMot(m:String):String
	{
		var tab:Array = new Array();
		var motRetour:String = new String();
		
		tab = motVersArray(m);
		tab = melangeTableau(tab);
		motRetour = arrayVersMot(tab, m.length);
		
		return motRetour;
	}
	
	
	/*****************************************************************************
	Fonction : melangeTableau
	Paramêtre :
		- t : un tableau à mélanger
	Description : un mélange en 2 étapes : on tri ou inverse le mot selon un 
				  nombre aléatoire. Ensuite, on tri au hasard
	******************************************************************************/	
	function melangeTableau(t:Array):Array
	{
		var mix1:Number = Math.ceil(Math.random()*2);

		switch(mix1)
		{
			case 1:
				t.sort();
			break;
			case 2:
				t.reverse();
			break;
		}
		
		//t.sort(hasard = function(){return random(2)});
		t.sort(hasard);
				function hasard():Number
				{
					return random(2);
				}
				
		return t;
	}
	
	/*****************************************************************************
	Fonction : motVersArray
	Paramêtre :
		- m : un mot que l'on veut stocker dans un tableau
	Description : on push les lettres dans le tableau une à une
	******************************************************************************/
	function motVersArray(m:String):Array
	{
		var t:Array = new Array();
		
		for(var i:Number = 0; i < m.length; i++)
		{
			t.push(m.charAt(i));
		}
		return t;
	}
	
	/*****************************************************************************
	Fonction : arrayVersMot
	Paramêtre :
		- t : un tableau contenant un mot que l'on veut transformer en String
		- len : la longueur du mot
	Description : on parcours le tableau et on le range dans un String
	******************************************************************************/	
	function arrayVersMot(t:Array, len:Number):String
	{
		var m:String = new String();
		
		for(var i:Number = 0; i < len; i++)
		{
			m += t[i];
		}
		return m;
	}
	
		
}