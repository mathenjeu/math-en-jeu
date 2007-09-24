<?php 
/*******************************************************************************
Fichier : palmares.php
Auteur : Maxime Bégin
Description : pour afficher le palmares des meilleurs joueurs
********************************************************************************
11-26-2006 Maxime Bégin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");

main();

function main()
{
  try
  {
	$smarty = new MonSmarty($_SESSION['langage']);
	global $lang;
	
	$smarty->assign('titre',$lang['titre_contact']);
	$smarty->cache_lifetime = 0;
	$smarty->display('header.tpl');

	if(isset($_SESSION['css']))
		$smarty->assign('css',$_SESSION['css']);
	
	if(isset($_SESSION["joueur"]))
	{
	 	$smarty->assign('connecter',1);
	 	$smarty->assign('alias',$_SESSION["joueur"]->reqAlias());
	 	$smarty->assign('motDePasse',$_SESSION["joueur"]->reqMotDePasse());
	 	$smarty->assign('acces',$_SESSION["joueur"]->reqCategorie());
	}
	
	$smarty->cache_lifetime = 0;
	$smarty->display('menu.tpl');
	
	//on vérifie si on a une copie valide dans la cache
	//sinon on recalcule le palmarès
	if(!$smarty->is_cached("palmares.tpl"))
	{
		meilleurMoy($smarty);
    	joueurPlusJouee($smarty);
    	joueurPlusGagner($smarty);
    	$smarty->assign('nbJour',NB_JOUR_PALMARES);
    }
    
    $smarty->cache_lifetime = 3600;
    $smarty->display('palmares.tpl');
    
    $smarty->cache_lifetime = -1;
    $smarty->display('footer.tpl');
	
  }
  catch(MyException $e)
  {
  	echo $e->exception_dump();  
  }
}



/*******************************************************************************
Fonction : joueurPlusGagner
Paramètre :
    &$smarty : adresse de l'objet smarty
Description : obtenir les joueurs qui ont la meilleures moyenne de victoire.
*******************************************************************************/
function joueurPlusGagner(&$smarty)
{
  global $lang;

  $sql ="SELECT DISTINCT alias,ville,
            (sum( partiejoueur.gagner ) / count( pointage )) AS moyVictoire,
            count( pointage ) as nbPartie
            FROM partiejoueur, partie, joueur
            WHERE partiejoueur.clePartie = partie.clePartie AND joueur.cleJoueur = partiejoueur.cleJoueur
            AND DATE_SUB(CURDATE(),INTERVAL " . NB_JOUR_PALMARES . " DAY) <= datePartie
            GROUP BY partiejoueur.cleJoueur
            HAVING nbPartie>=" . MIN_NB_PARTIE_PALMARES . "
            ORDER BY moyVictoire DESC, nbPartie desc
            LIMIT " . MAX_NB_JOUEURS_PALMARES;

  $result = $_SESSION['mysqli']->query($sql);

  $smarty->assign('titre3', $lang['palmares_meilleur_moyenne_victoire']);

  $arr[0]="";
  
  for($i=0;$i<$result->num_rows;$i++)
  {
    $row=$result->fetch_object();
    $arr[$i]['alias'] = $row->alias;// . ' (' . $row->ville . ')';
    $arr[$i]['nbPartie'] = $row->nbPartie;
    $arr[$i]['poucentage_victoire'] = (int)(($row->moyVictoire)*100);
  }

  $smarty->assign('palmares3',$arr);

}

/*******************************************************************************
Fonction : joueurPlusJouee
Paramètre :
    &$smarty : adresse de l'objet smarty
Description : obtenir les joueurs qui ont jouée le plus
*******************************************************************************/
function joueurPlusJouee(&$smarty)
{
  global $lang;

  $sql ="SELECT DISTINCT alias,ville,
            sum( partie.dureePartie ) AS totalTemps,
            count( pointage ) AS nbPartie
            FROM partiejoueur, partie, joueur
            WHERE partiejoueur.clePartie = partie.clePartie AND joueur.cleJoueur = partiejoueur.cleJoueur
            AND DATE_SUB(CURDATE(),INTERVAL " . NB_JOUR_PALMARES . " DAY) <= datePartie
            GROUP BY partiejoueur.cleJoueur
            HAVING nbPartie>=" . MIN_NB_PARTIE_PALMARES . "
            ORDER BY totalTemps DESC
            LIMIT " . MAX_NB_JOUEURS_PALMARES;

  $result = $_SESSION['mysqli']->query($sql);

  $smarty->assign('titre2', $lang['palmares_joueur_plus_jouer']);
  
  $arr[0]="";
  for($i=0;$i<$result->num_rows;$i++)
  {
    $row=$result->fetch_object();
    $arr[$i]['alias'] = $row->alias;// . ' (' . $row->ville . ')';
    $arr[$i]['nbPartie'] = $row->nbPartie;
    $arr[$i]['totalTemps'] = $row->totalTemps;

  }
  $smarty->assign('palmares2',$arr);

}

/*******************************************************************************
Fonction : meilleurMoy
Paramètre :
    &$smarty : adresse de l'objet smarty
Description : obtenir les joueurs qui ont la meilleure moyenne
    de points par minutes
*******************************************************************************/
function meilleurMoy(&$smarty)
{
  global $lang;

  $sql ="SELECT DISTINCT alias,ville,
            (sum( partiejoueur.pointage ) / sum( partie.dureePartie )) AS moy,
            count( pointage ) AS nbPartie
            FROM partiejoueur, partie, joueur
            WHERE partiejoueur.clePartie = partie.clePartie AND joueur.cleJoueur = partiejoueur.cleJoueur
            AND DATE_SUB(CURDATE(),INTERVAL " . NB_JOUR_PALMARES . " DAY) <= datePartie
            GROUP BY partiejoueur.cleJoueur
            HAVING nbPartie>=" . MIN_NB_PARTIE_PALMARES . "
            ORDER BY moy DESC
            LIMIT " . MAX_NB_JOUEURS_PALMARES;

  $result = $_SESSION['mysqli']->query($sql);

  $smarty->assign('titre1', $lang['palmares_meilleur_moy_point']);

  $arr[0]="";

  for($i=0;$i<$result->num_rows;$i++)
  {
    $row=$result->fetch_object();
    $arr[$i]['alias'] = $row->alias;// . ' (' . $row->ville . ')';
    $arr[$i]['moy'] = $row->moy;
    $arr[$i]['nbPartie'] = $row->nbPartie;
  }

  $smarty->assign('palmares1',$arr);

}