<?php
/*******************************************************************************
Fichier : util.php
Auteur : Maxime Bégin
Description : regroupe toutes les fonctions diverse utilitaire
********************************************************************************
11-07-2006 Maxime Bégin - Ajout de la fonction pour afficher la FAQ
30-05-2006 Maxime Bégin - Version initiale
*******************************************************************************/



/*******************************************************************************
Fonction : convertirDateEnString($date)
Paramètre :
        - $date est une date au format aaaa-mm-jj
Description :
    - on convertir la date qui est au format aaaa-mm-jj en format jj Mois aaaa
*******************************************************************************/
function convertirDateEnString($date)
{
    $dateExp = explode("-",$date);
    global $lang;
    $mois=array($lang['janvier'],
        $lang['fevrier'],
        $lang['mars'],
        $lang['avril'],
        $lang['mai'],
        $lang['juin'],
        $lang['juillet'],
        $lang['aout'],
        $lang['septembre'],
        $lang['octobre'],
        $lang['novembre'],
        $lang['decembre']);

    return $dateExp[2] . " " . $mois[intval($dateExp[1])-1] . " " . $dateExp[0];

}

/*******************************************************************************
Fonction : redirection($page,$temps)
Paramètre :
        - $page : page web vers laquel il faut redirigé le client
        - $temps : le temps avec la redirection (0 = immediatement)
Description :
        - rediriger le client vers une autre page.
*******************************************************************************/
function redirection($page,$temps)
{
  if(!stripos($page,"?"))
  {
    echo '<meta http-equiv="refresh" content="' . $temps . '; url=' . $page . '?' . strip_tags(SID) . '">';
  }
  else
  {
  	 echo '<meta http-equiv="refresh" content="' . $temps . '; url=' . $page . '&amp;' . strip_tags(SID) . '">';
  }
}

/*******************************************************************************
Fonction : genererListeEtablissement($niveau)
Paramètre :
        - $page : page web vers laquel il faut redirigé le client
        - $temps : le temps avec la redirection (0 = immediatement)
Description :
        - rediriger le client vers une autre page.
*******************************************************************************/
function genererListeEtablissement($niveau)
{
  	 global $lang;
    if($niveau<=6){
        $typeEtablissement=$lang['primaire'];
    }
    elseif($niveau<=11){
        $typeEtablissement=$lang['secondaire'];
    }
    elseif($niveau==12){
        $typeEtablissement=$lang['collegial'];
    }
    elseif($niveau==13){
        $typeEtablissement=$lang['universitaire'];
    }
    else{
        $typeEtablissement="";
    }

    $sql="select cleEtablissement,nom,ville from etablissement,typeetablissement
        where typeetablissement.cleTypeEtablissement=etablissement.cleTypeEtablissement
        AND identificateurTypeEtablissement='" . $typeEtablissement . "' ORDER BY nom";
        
    $result = $_SESSION['mysqli']->query($sql);
    $nb=$result->num_rows;
    
    $arr[0][0] = 0;
    $arr[1][0] = $lang['autre'];
    
    for($i=1;$i<=$nb;$i++)
    {
      $row = $result->fetch_array(MYSQLI_NUM);
      $arr[0][$i] = $row[0];
      $arr[1][$i] = $row[1] . '( ' . $row[2] . ' )' ;
    }
    return $arr;
}

/*******************************************************************************
Fonction : templates_dir
Paramètre : 
	- &$smarty : référence à smarty
	- $dir : dossier qui contient les langages
Description : assigner à smarty les dossiers de langages disponibles
*******************************************************************************/
function langage_dir(&$smarty,$dir)
{
  	if (is_dir($dir))
	{
	  if ($dh = opendir($dir))
	  {
	    while (($file = readdir($dh)) !== false)
	    {
	      if($file != "." && $file!= ".." & $file!="index.htm")
	        $arr[$file] = $file;
	    }
	  }
	}
	$smarty->assign('langue',$arr);
}
/*******************************************************************************
Fonction : css_file
Paramètre : 
	- &$smarty : référence à smarty
	- $dir : dossier qui contient les fichiers css
Description : assigner à smarty les fichiers .css disponible
*******************************************************************************/
function css_file(&$smarty,$dir)
{
  	if (is_dir($dir))
	{
	  if ($dh = opendir($dir))
	  {
	    while (($file = readdir($dh)) !== false)
	    {
	      $fileEx = explode(".",$file);
	      if($fileEx[count($fileEx)-1]=="css")
	        $arr[$file] = $file;
	    }
	  }
	}
	$smarty->assign('css',$arr);
  
}

/*******************************************************************************
Fonction : templates_dir
Paramètre : 
	- &$smarty : référence à smarty
	- $dir : dossier qui contient les templates
Description : assigner à smarty les dossiers de templates disponible
*******************************************************************************/
function templates_dir(&$smarty,$dir)
{
	if (is_dir($dir))
	{
	  if ($dh = opendir($dir))
	  {
	    while (($file = readdir($dh)) !== false)
	    {
	      if($file != "." && $file!= ".." & $file!="index.htm")
	        $arr[$file] = $file;
	    }
	  }
	}
	$smarty->assign('templates',$arr);
}



