<?php 
/*******************************************************************************
Fichier : sondage.php
Auteur : Maxime B�gin
Description : pour afficher les sondages
********************************************************************************
11-24-2006 Maxime B�gin - Version initiale
*******************************************************************************/

require_once("lib/ini.php");
try
{
 	$sondage=new Sondage($_SESSION["mysqli"]);
	if(!$sondage->chargerPlusRecentSondageMySQL(array(0,1),getCleLangue($_SESSION['langage'])))
	{
		return;
	}
 	if(isset($_SESSION["joueur"]))
	{
		$joueur=$_SESSION["joueur"];
			    
	    //si le joueurs choisie de r�pondre au sondage
	    if(isset($_POST["reponseSondage"]))
        {
            $sondage->ajoutChoixJoueur($joueur->reqCle(),$_POST["reponseSondage"]);
            $sondage=new Sondage($_SESSION["mysqli"]);
            $sondage->chargerPlusRecentSondageMySQL(array(0,1),getCleLangue($_SESSION['langage']));
            redirection("nouvelles.php",0);
        }
        else
        {
	 	 
		 	if($sondage->joueurDejaRepondu($joueur->reqCle())==false)
			{
		        afficherSondage($sondage);
		    }
		    else
			{
		        afficherResultatSondage($sondage);
		    }
		}
	}
	else
	{
	 	afficherResultatSondage($sondage);
	}
 	 
}
catch(MyException $e)
{
echo $e->exception_dump();  
}
catch(SQLException $e)
{
    echo $e->exception_dump();
}


/*******************************************************************************
Fonction : afficherSondage
Param�tre : $sondage : le sondage � afficher
Description : affiche le sondage pass� en param�tre
*******************************************************************************/
function afficherSondage($sondage)
{
    $smarty = new MonSmarty($_SESSION['langage']);
    if(!$smarty->is_cached("sondage.tpl"))
    {
	    $smarty->assign('titre',$sondage->reqTitre());
	    
	    for($i=1;$i<=$sondage->reqNbReponse();$i++)
	    {
	        $reponse=$sondage->reqReponse($i);
	        $arr[$i] = $reponse->reqReponse();
	    }
	    if($sondage->reqNbReponse()>0)
	    {
		    $smarty->assign('reponseID',array_keys($arr));
		    $smarty->assign('reponseTexte',$arr);
		    $smarty->assign('page','sondage.php');
		    $smarty->cache_lifetime = -1;
		    $smarty->display('sondage.tpl');
		}
	}
	else
	{
	 	$smarty->cache_lifetime = -1;
		$smarty->display('sondage.tpl');
		
	}

}

/*******************************************************************************
Fonction : afficherResultatSondage
Param�tre : $sondage : le sondage � afficher
Description : affiche le r�sultat du sondage pass� en param�tre
*******************************************************************************/
function afficherResultatSondage($sondage)
{
    $smarty = new MonSmarty($_SESSION['langage']);
    global $lang;
    $smarty->assign('lang', $lang);
    $smarty->assign('titre',$sondage->reqTitre());
    

    $total=$sondage->reqTotal();
    $smarty->assign('total',$total);
    
    for($i=1;$i<=$sondage->reqNbReponse();$i++)
    {
        $reponse=$sondage->reqReponse($i);
        $arrTex[] = $reponse->reqReponse();
        if($total!=0)
            $arrVal[] = ((int)((($reponse->reqCompteur() / $total)*100)+0.5));
        else
            $arrVal[] = 0;
    }

    $smarty->assign('reponseTexte',$arrTex);
    $smarty->assign('reponseValeur',$arrVal);
    $smarty->cache_lifetime = 0;
    $smarty->display('sondage_resultat.tpl');

}

