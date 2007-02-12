<?php
/*******************************************************************************
Fichier : exceptions.class.php
Auteur : Maxime Bégin
Description : Classes pour la gestion des exceptions et la théorie du contrat
********************************************************************************
21-06-2006 Maxime Bégin - Ajout de commentaires.
02-06-2006 Maxime Bégin - Version initiale
*******************************************************************************/


class MyException extends Exception
{
    function exception_dump()
    {
     	
       $res = "";
       $res .= "<h4>" . $this->getMessage() . "</h3>\n\n";
       $res .= "fichier: {$this->file}<br/>\n";
       $res .= "ligne: {$this->line}<br/>\n";
       $res .= "<PRE>";
       $res .= $this->getTraceAsString();
       $res .= "</PRE><br>";
       
       return $res;
    }
}

class FatalException extends MyException {};
class WarningException extends MyException {};
class CourrielException extends MyException {};

//
//Théorie du contrat
//
class ContratException extends MyException
{
    function __construct($message)
    {
      parent::__construct($message);
    }
}
class AssertionException extends ContratException
{
    function __construct($message)
    {
      parent::__construct($message);
    }
}
class PreconditionException extends ContratException
{
    function __construct($message)
    {
      parent::__construct($message);
    }
}
class PostconditionException extends ContratException
{
    function __construct($message)
    {
      parent::__construct($message);
    }
}
class InvariantException extends ContratException
{
    function __construct($message)
    {
      parent::__construct($message);
    }
}

//
//fonctions globale pour la théorie du contrat
//
function ASSERTION($bool)
{
    if($bool==false && defined("CONTRAT_DEBUG"))
        throw new PreconditionException("Erreur d'assertion!");
}

function PRECONDITION($bool)
{
    if($bool==false && defined("CONTRAT_DEBUG"))
        throw new PreconditionException("Erreur de précondition!");
}

function POSTCONDITION($bool)
{
  if($bool==false && defined("CONTRAT_DEBUG"))
    throw new PostconditionException("Erreur de postcondition!");
}

function INVARIANT($bool)
{
  if($bool==false && defined("CONTRAT_DEBUG"))
    throw new InvariantException("Erreur d'invariant!");
}



class BDException extends MyException{};

class SQLException extends BDException
{

    private $sql;
    private $sqlerrmsg;

    function __construct($msg, $code, $sql)
    {
           $this->sqlerrmsg = $msg;
           if (defined('SQL_DEBUG')) {
               $msg .= " in '$sql'";
           }
           parent::__construct($msg, $code);
           $this->sql = $sql;
    }

    function getSQL()
    {
        return $this->sql;
    }

    function getSQLError()
    {
        return $this->sqlerrmsg;
    }
}


function exceptionErrorHandler($errno, $errstr, $errfile, $errline)
{
  switch ($errno)
  {
    case E_USER_ERROR:
        throw new FatalException($errstr, $errno);
        break;
    case E_WARNING:
        throw new WarningException($errstr, $errno);
        break;
    default:
        echo " - Unknown error - <b>$errstr</b>($errno) <br>";
   break;
  }
  //return true;
}


$old_error_handler = null;

function install_exception_errorhandler()
{
   global $old_error_handler;
   $old_error_handler = set_error_handler('exceptionErrorHandler');
}

function uninstall_exception_errorhandler()
{
   global $old_error_handler;
   set_error_handler($old_error_handler);
   $old_error_handler = null;
}

