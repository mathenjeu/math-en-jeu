<?php
/*******************************************************************************
Fichier : util.php
Auteur : Maxime B�gin
Description : regroupe toutes les fonctions diverse utilitaire
********************************************************************************
11-07-2006 Maxime B�gin - Ajout de la fonction pour afficher la FAQ
30-05-2006 Maxime B�gin - Version initiale
*******************************************************************************/



/*******************************************************************************
Fonction : convertirDateEnString($date)
Param�tre :
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
Param�tre :
        - $page : page web vers laquel il faut redirig� le client
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



/**
 * Return the list of school type for the current language
 *
 * @return an array containing school type id and school type name
 */
function getSchoolType() {
  $sql = "select st.school_type_id, st.name from school_type st , language lg where st.language_id=lg.language_id and " . 
      "lg.short_name='" . $_SESSION['langage'] . "'";
  
  
  $result = $_SESSION['mysqli']->query($sql);
  $nb=$result->num_rows;
  
    
  for($i=0;$i<=$nb;$i++) {
    $row = $result->fetch_array(MYSQLI_NUM);
    $arr[$i][0] = $row[0];
    $arr[$i][1] = $row[1];
    echo $row[0];
  }
      
  
  return $arr;
  
  
}

function getLevels() {
  $sql = "select lvl.level_id, lvl.name from level lvl , language lg where lvl.languague_id=lg.language_id and " . 
      "lg.short_name='" . $_SESSION['langage'] . "'";
  
  
  $result = $_SESSION['mysqli']->query($sql);
  $nb=$result->num_rows;
  
    
  for($i=0;$i<=$nb;$i++) {
    $row = $result->fetch_array(MYSQLI_NUM);
    $arr[$i][0] = $row[0];
    $arr[$i][1] = $row[1];
  }
      
  
  return $arr;
   
  
  
}


/*******************************************************************************
Fonction : genererListeEtablissement($niveau)
Param�tre :
        - $page : page web vers laquel il faut redirig� le client
        - $temps : le temps avec la redirection (0 = immediatement)
Description :
        - rediriger le client vers une autre page.
*******************************************************************************/
function genererListeEtablissement($school_type_id)
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

    $sql="select s.school_id, s.name, s.city from school s , school_type st
        where s.school_type_id=" . $school_type_id . " ORDER BY s.name";
        
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
Param�tre : 
	- &$smarty : r�f�rence � smarty
	- $dir : dossier qui contient les langages
Description : assigner � smarty les dossiers de langages disponibles
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
Param�tre : 
	- &$smarty : r�f�rence � smarty
	- $dir : dossier qui contient les fichiers css
Description : assigner � smarty les fichiers .css disponible
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
Param�tre : 
	- &$smarty : r�f�rence � smarty
	- $dir : dossier qui contient les templates
Description : assigner � smarty les dossiers de templates disponible
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

/*
function setLangage($cleLangage) {
  
  switch($cleLangage)
  {
	case LANG_FRENCH:
	  $_SESSION['langage'] = "fr";
	  break;
	case LANG_ENGLISH:
		$_SESSION['langage'] = "en";
		break;
	default:
		$_SESSION['langage'] = "fr";
		break;
  }
}
*/

/*
function getCleLangue($langage) {
  $ret=0;
  if ($langage == "en") {
    $ret = LANG_ENGLISH;
  } else {
    $ret = LANG_FRENCH;
  }
  
  return $ret;
}
*/