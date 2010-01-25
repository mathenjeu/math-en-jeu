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
Classe pour le jeu du pendu (version2)
16-02-2008 Hugo Drouin-Vaillancourt Ajout de "Hint", i.e. d'indices (ne pas confondre avec 
          les 2-3 lettres qui sont fournies au départ)																	 
16-08-2006 Maxime Bégin - Ajout du paramêtre de nombre d'essaie max dans le
	constructeur
15-08-2006 Maxime Bégin - optimisation ,clean-up et ajout de commentaire
07-08-2006 Maxime Bégin - Ajout d'un indice au début(une lettre est donnée)
03-08-2006 Maxime Bégin - Version initiale
******************************************************************************/
class pendu
{

	private var noMot:Number;					//le numéro du mot en cours dans le tableau mots
	private var motEnCours:Array;				//le mot dans l'état actuel selon les lettres découvertes
	private var lettreEssaye:Array = new Array; //tableau de lettre déja essayée
	private var nbLettreEssaye:Number;			//le nombre de lettre essayée
	private var nbLettreTrouve:Number;			//le nombre de lettre trouvée
	private var pointage:Number;				//le pointage total du joueur
	private var nbEssaieRestant:Number;			//le nombre d'essaie restant pour ce mot
	private var nbEssaieMax:Number=8;			//le nombre d'essai maximal pour ce mot
	private var mots:Array;						//tableau des mots
	private var motDejaTrouve:Array = new Array;//tableau des mots déja trouvé
	private var nbMotTrouve:Number;				//le nombre de de mots déjà trouvé
	

	/*****************************************************************************
	Fonction : pendu
	Paramêtre : le nombre maximal d'essaie
	Description : constructeur de la classe pendu
	******************************************************************************/
	public function pendu(nbEssaieMax:Number)
	{
		this.pointage=0;
		this.nbMotTrouve=0;
		this.nbEssaieMax=nbEssaieMax;
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
			this.mots[i][0] = subnodes[0].firstChild.nodeValue.toString();  //mot
			this.mots[i][1] = this.calculerValeurMot(this.mots[i][0]);  //valeur
			this.mots[i][2] = subnodes[1].firstChild.nodeValue.toString();   //description
			this.mots[i][3] = subnodes[2].firstChild.nodeValue.toString();  //hint
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
			
			//si c'est un espace on enlêve un point
			if(l==" ") valeur--;
			
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
		if(mot.length<6) valeur++;
		if(mot.length>10) valeur--;
		valeur = int(valeur/2+0.5);
		
		return valeur;
		
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
		//TODO : possiblement modifier cette partie si c'est trop lent 
		// 			avec beaucoup de mots
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
				
		//on réinitialise les valeurs
		this.nbEssaieRestant=this.nbEssaieMax;
		this.nbLettreTrouve=0;
		this.nbLettreEssaye=0;
		this.lettreEssaye = new Array;
		trace('Le mot à trouvé est : ' + this.mots[this.noMot][0]);
		trace('La longueur du mot est : ' + (this.mots[this.noMot][0]).length);
		
		return true;
	}
	
	/*****************************************************************************
	Fonction : indice
	Paramêtre :
	Description : obtenir une des lettres au hasard et 
		la donnée gratuitement aux joueurs. On retourne la lettre.
	******************************************************************************/
	public function indice()
	{
		var pos:Number;
		pos = Math.floor(Math.random()*this.mots[this.noMot].length);
		return convertirLettreAccent(this.mots[this.noMot][0].substr(pos,1));
	}
	

	/*****************************************************************************
	Fonction : verifierLettre
	Paramêtre :
		- lettre:String : la lettre à vérifier
	Description : on parcours le mot pour vérifier si la lettre
		en fait partie. Si c'est le cas on ajouter cette lettre
		dans le tableau qui contient les lettres trouvés.
		On retourne le nombre de lettre trouvé.
	******************************************************************************/
	public function verifierLettre(lettre:String)
	{
		//ajoute la lettre au tableau des lettres déjà essayées
		this.lettreEssaye[this.nbLettreEssaye] = lettre;
		this.nbLettreEssaye++;
		
		var len:Number = length(this.mots[this.noMot][0]);
		var i:Number;
		var tab:Array =new Array;
		var nbTrouve:Number=0;
		//on boucle pour la longeur du mot
		for(i=0;i<len;i++)
		{
			var l:String;
			l = this.mots[this.noMot][0].substr(i,1);
			//si la lettre correspond, on ajoute la position au tableau
			if(convertirLettreAccent(l)==lettre.toLowerCase())
			{
				tab[nbTrouve] = i;
				nbTrouve++;
				//ajoute la lettre dans le mot en cours
				this.motEnCours[i] = l;
			}
		}
		//si on a trouvé aucune lettre on diminu le nombre d'essai restant
		if(nbTrouve==0 && lettre != " " && lettre != "-")
		{
			this.nbEssaieRestant--;
		}
		this.nbLettreTrouve+=nbTrouve;
		return nbTrouve;
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
		if(this.nbLettreTrouve==length(this.mots[this.noMot][0]))
		{
			this.pointage+=this.mots[this.noMot][1];
			this.motDejaTrouve.push(this.noMot);
			this.nbMotTrouve++;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/*****************************************************************************
	Fonction : estMort
	Paramêtre : 
	Description : on vérifie si le joueur est "mort". si c'est le cas
		on coupe le pointage en 2 et retourne vrai, sinon on retourne
		faux.
	******************************************************************************/
	public function estMort()
	{
		if(this.nbEssaieRestant==0)
		{
			this.pointage = int((this.pointage/2)+0.5);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/*****************************************************************************
	Fonction : tousMotsTrouve
	Paramêtre : 
	Description : on vérifie si le joueur a trouvé tous les mots. si c'est le cas
		on retourne vrai, sinon on retourne faux.
	******************************************************************************/
	public function tousMotsTrouve()
	{
		return (this.motDejaTrouve.length == this.mots.length)?true:false;
	}
	
	//fonctions de retour
	public function retNbEssaieRestant()
	{
		return this.nbEssaieRestant;
	}
	public function retNbEssaieMax()
	{
		return this.nbEssaieMax;
	}
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
		public function retHint()
	{
		return this.mots[this.noMot][3];
	}
	public function retMotEnCours()
	{
		return this.motEnCours;
	}
		
}