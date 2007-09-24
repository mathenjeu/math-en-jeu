<?php

/*******************************************************************************
Fichier : clog.class.php
Auteur : Maxime Bégin
Description :
    classe simple pour le log des erreurs.
********************************************************************************
12-02-2007 Maxime Bégin - Version initiale
*******************************************************************************/

class clog
{
 	private $handleFichier;
 	private $nomFichier;
	
	function clog($fichier)
	{
	 	if(!$this->handleFichier=fopen($fichier,"a+"))
		{
			echo "erreur";
		}	
	 	$this->nomFichier=$fichier;
	}

	function ecrire($message)
	{
	 	fputs($this->handleFichier,"\r\n" . str_repeat("=",80)."\r\n");
		fputs($this->handleFichier,date("r"). "\r\n");
	 	fputs($this->handleFichier,$message . "\r\n");
	}
	
	function __destruct()
	{
		fclose($this->handleFichier);
	}


}

?>