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
<script type="text/javascript">
<!-- Begin
function Launch(page) {
 OpenWin = this.open(page, "Aide", "toolbar=no,menubar=no,location=no,scrollbars=auto,resizable=yes,width=350,height=250");
}
// End -->
</script> 
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Math en jeu</title>
</head>
<body bgcolor="#000000" style="margin:0px;padding:0px;height:100%;width:100%;">
<div style="height:100%">
<object
        type="application/x-shockwave-flash"
        data="flash/chargement_beta.swf?nomUtilisateur=<?php echo $_GET["alias"]; ?>&amp;motDePasse=<?php echo $_GET["motDePasse"]; ?>"
        width="700"
        height="510">
    	<param name="movie"	value="flash/chargement_beta.swf?nomUtilisateur=<?php echo $_GET["alias"]; ?>&amp;motDePasse=<?php echo $_GET["motDePasse"]; ?>" />
    	<param name="allowScriptAccess" value="always" />
		<param name="quality" value="high" />
		<param name="bgcolor" value="#000000" />
		<param name="scale" value="exactfit" />
</object>
</div>
</body>
</html>