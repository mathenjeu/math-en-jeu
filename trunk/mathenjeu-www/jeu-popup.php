<?php
/*******************************************************************************
Fichier : jeu-popup.php
Auteur : Sylvain Hallé
Description :   Affiche le jeu si le joueur est bien connecté
********************************************************************************
TODO : modification pour spécifiaction HTML 4.01/XHTML
10-06-2006 Maxime Bégin - Modification.
02-06-2006 Maxime Bégin - Sylvain Hallé
*******************************************************************************/
require_once("lib/ini.php");

// Vérifie si une session est en cours
if (!isset($_SESSION["joueur"]))
{
  // Sinon, on ferme la fenêtre
  echo "<script>window.close()</script>";
}
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" style="margin:0px;padding:0px;height:100%;">
<head>
<meta http-equiv="Expires" content="Thu, 01 Dec 1994 16:00:00 GMT"/>
<meta http-equiv="Cache-Control" content="no-cache, max-age=0"/>
<script type="text/javascript">
<!-- Begin
function Launch(page) {
 OpenWin = window.open(page, "Aide", "toolbar=no,menubar=no,location=no,scrollbars=auto,resizable=yes,width=350,height=250");
}
// End -->
</script> 
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Math en jeu</title>
</head>
<body bgcolor="#000000" style="margin:0px;padding:0px;height:100%;width:100%;">
<div style="height:100%">

<OBJECT 
	classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
	codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0"
	WIDTH="100%" HEIGHT="100%" id="mathenjeu">
	<PARAM NAME=movie VALUE="flash/chargement_beta.swf?nomUtilisateur=<?php echo $_GET["alias"]; ?>&amp;motDePasse=<?php echo $_GET["motDePasse"]; ?>">
	<PARAM NAME=quality VALUE=high>
	<param name="bgcolor" value="#000000" />
	<param name="allowFullScreen" value="true" />
	<EMBED src="flash/chargement_beta.swf?nomUtilisateur=<?php echo $_GET["alias"]; ?>&amp;motDePasse=<?php echo $_GET["motDePasse"]; ?>" 
		quality=high bgcolor=#FFFFFF WIDTH="100%" HEIGHT="100%"
		NAME="mathenjeu" TYPE="application/x-shockwave-flash"
		PLUGINSPAGE="http://www.macromedia.com/go/getflashplayer">
	</EMBED>
</OBJECT>

<!--
<object
		codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0"
        type="application/x-shockwave-flash"
        data="flash/chargement_beta.swf?nomUtilisateur=<?php echo $_GET["alias"]; ?>&amp;motDePasse=<?php echo $_GET["motDePasse"]; ?>"
        width="100%"
        height="100%">
    	<param name="movie"	value="flash/chargement_beta.swf?nomUtilisateur=<?php echo $_GET["alias"]; ?>&amp;motDePasse=<?php echo $_GET["motDePasse"]; ?>" />
    	<param name="allowScriptAccess" value="always" />
		<param name="quality" value="high" />
		<param name="bgcolor" value="#000000" />
		<EMBED WIDTH="550" HEIGHT="400"
			TYPE="application/x-shockwave-flash"
			pluginspage="http://www.macromedia.com/go/getflashplayer"></EMBED>

</object>
-->
</div>
</body>
</html>