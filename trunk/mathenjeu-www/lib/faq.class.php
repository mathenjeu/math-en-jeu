<?php
/*******************************************************************************
Fichier : faq.class.php
Auteur : Maxime Bégin
Description :
    classe pour la gestion de la FAQ(Foire au question)
********************************************************************************
05-07-2006 Maxime Bégin - Version initiale
*******************************************************************************/
require_once("exception.class.php");
require_once("mon_mysqli.class.php");

class FAQ
{
	private $cleFaq;		//la clé unique de la faq
	private $question;		//le texte de la question
	private $reponse;		//le texte de la réponse
	private $numero;		//le numéro ( l'ordre dans lesquelles les questions seront afficher)
	private $mysqli;		//l'objet mysqli
	
	//**************************************************************************
    // Sommaire:    Constructeur de la classe FAQ
    // Entrée:
    // Sortie:
    // Note:        initialise les données à vide
    //**************************************************************************
	function __construct($mysqli)
	{
	  PRECONDITION(get_class($mysqli)=="mon_mysqli");
	  $this->cleFaq=0;
	  $this->question="";
	  $this->reponse="";
	  $this->numero=0;
	  $this->mysqli=$mysqli;
	}
	
	//**************************************************************************
    // Sommaire:    Vérifier les invariants de la classe
    // Entrée:
    // Sortie:
    // Note:        
    //**************************************************************************
	function INVARIANTS()
	{
	  INVARIANT($this->cleFaq>=0);
	  INVARIANT($this->question!="");
	  INVARIANT($this->reponse!="");
	  INVARIANT($this->numero>=0);
	}
	
	//**************************************************************************
    // Sommaire:    assigné la clé de la faq
    // Entrée:      $cle : la clé unique de la faq
    // Sortie:
    // Note:
    //**************************************************************************
	function asgCle($cle)
	{
	  PRECONDITION($cle>=0);
	  $this->cleFaq=$cle;
	  POSTCONDITION($this->reqCle()==$cle);
	}
	
	//**************************************************************************
    // Sommaire:    assigné la question à la faq
    // Entrée:      $question : ne doit pas être vide
    // Sortie:
    // Note:
    //**************************************************************************
	function asgQuestion($question)
	{
	  PRECONDITION($question!="");
	  $this->question=$question;
	  POSTCONDITION($this->reqQuestion()==$question);
	}

	//**************************************************************************
    // Sommaire:    assigné la réponse à la faq
    // Entrée:      $reponse : ne doit pas être vide
    // Sortie:
    // Note:
    //**************************************************************************
	function asgReponse($reponse)
	{
	  PRECONDITION($reponse!="");
	  $this->reponse=$reponse;
	  POSTCONDITION($this->reqReponse()==$reponse);
	}

	//**************************************************************************
    // Sommaire:    assigné le numéro à la faq
    // Entrée:      $numero : doit être >= 0
    // Sortie:
    // Note:
    //**************************************************************************
	function asgNumero($no)
	{
	  PRECONDITION($no>=0);
	  $this->numero=$no;
	  POSTCONDITION($this->reqNumero()==$no);
	}

	//**************************************************************************
    // Sommaire:    insérer la faq dans la table
    // Entrée:      
    // Sortie:		vrai si tout va bien, faux sinon
    // Note:		la clé de la faq doit être égal à 0
    //**************************************************************************
	function insertionMySQL()
	{
	  $this->INVARIANTS();
	  if($this->cleFaq !=0)
	  	return false;
	  $sql="select max(numero) from faq";
	  $result = $this->mysqli->query($sql);
	  
	  $arr=$result->fetch_array();

	  $sql="insert into faq(question,reponse,numero) 
	  		values('" . $this->question . "','" . $this->reponse . "'," . ($arr[0]+1) . ")";
	  		
	  $this->mysqli->query($sql);
	  $this->asgCle($this->mysqli->insert_id);
	  
	  return true;
	}

	//**************************************************************************
    // Sommaire:    mettre à jour la faq dans la table
    // Entrée:      
    // Sortie:		vrai si tout va bien, faux sinon
    // Note:		la clé de la faq doit être différente de 0
    //**************************************************************************
	function miseAJourMySQL()
	{
	  $this->INVARIANTS();
	  
	  if($this->cleFaq==0)
	  	return false;
	  $sql="update faq set question='" . $this->question . "',reponse='" . $this->reponse . 
	  	"',numero=" . $this->numero . " where cleFaq=" . $this->cleFaq;
	  $this->mysqli->query($sql);
	  
	  return true;
	}

	//**************************************************************************
    // Sommaire:    supprimer la faq de la table
    // Entrée:      
    // Sortie:		vrai si tout va bien, faux sinon
    // Note:		la clé de la faq doit être différente de 0,
    //				les numéros des faq subséquente sont diminués de 1
    //**************************************************************************
	function deleteMySQL()
	{
	  if($this->cleFaq==0)
	  	return false;
	  	
	  //on doit ajuster la position des autres questions
	  $sql="update faq set numero=(numero-1) where numero>" . $this->numero;
	  $this->mysqli->query($sql);
	  
	  $sql="delete from faq where cleFaq=" . $this->cleFaq;
	  $this->mysqli->query($sql);
	  
	  $this->__construct($this->mysqli);
	  return true;
	}

	//**************************************************************************
    // Sommaire:    charger une faq à partir de la clé
    // Entrée:      $cle : clé de la faq à charger
    // Sortie:		vrai si tout va bien, faux si aucune faq n'est trouvé
    // Note:		
    //**************************************************************************
	function chargerMySQL($cle)
	{
	  PRECONDITION($cle>0);
	  $sql="select * from faq where cleFaq=" . $cle;
	  $result = $this->mysqli->query($sql);
	  if($result->num_rows==0)
	  	return false;
	  
	  $row=$result->fetch_object();
	  
	  $this->asgCle($row->cleFaq);
	  $this->asgQuestion($row->question);
	  $this->asgReponse($row->reponse);
	  $this->asgNumero($row->numero);
	  
	  return true;
	}
	
	//
	//fonction de retour
	//
	function reqCle(){
	  return $this->cleFaq;
	}
	function reqQuestion(){
	  return $this->question;
	}
	function reqReponse(){
	  return $this->reponse;
	} 
	function reqNumero(){
	  return $this->numero;
	}
}

class FAQs
{
    private $nbFaq;    	//le nombre de faq
    private $faqs;     	//tableau contenant les questions
    private $mysqli;	//objet mysqli
    
    //**************************************************************************
    // Sommaire:    Constructeur de la classe FAQ
    // Entrée:
    // Sortie:
    // Note:        initialise les données à vide
    //**************************************************************************
    function __construct($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
	  $this->mysqli=$mysqli;
	}

    //**************************************************************************
    // Sommaire:    ajouter une faq au tableau de faqs
    // Entrée:
    // Sortie:
    // Note:        
    //**************************************************************************
	function ajoutFaq($faq)
	{
	  PRECONDITION(get_class($faq)=="FAQ");
	  $this->faqs[]=$faq;
      $this->nbFaq++;
      POSTCONDITION(count($this->faqs)==$this->nbFaq);
	}

    //**************************************************************************
    // Sommaire:    charger un certain nombre de faq ou toutes les charger si
    //				$nbFaq = 0
    // Entrée:		$nbFaq : le nombre de faq à charger
    // Sortie:
    // Note:        
    //**************************************************************************
	function chargerMySQL($nbFaq)
	{
	  $sql="select * from faq order by numero asc";
	  if($nbFaq!=0)
        $sql.= " limit $nbFaq";
        
      $result = $this->mysqli->query($sql);
      $nbFaq = $result->num_rows;
      
      for($i=0;$i<$nbFaq;$i++)
      {
        $row=$result->fetch_object();
        $faq=new FAQ($this->mysqli);
        
		$faq->asgCle($row->cleFaq);
        $faq->asgQuestion($row->question);
        $faq->asgReponse($row->reponse);
        $faq->asgNumero($row->numero);
        
        $this->ajoutFaq($faq);
	  }
	}

    //**************************************************************************
    // Sommaire:    charger une faq à partir du(des) numéro( pas la clé )
    // Entrée:		$no : un tableau contenant les numéros 
	//				d'ordre des faq à charger
    // Sortie:
    // Note:        cette fonction est utlisé lors de la modification de l'ordre
	//				des faqs.
    //**************************************************************************
	function chargerMySQLNumero($no)
	{
	  $sql="select * from faq where ";
	  for($i=0;$i<count($no);$i++)
	  {
	  	$sql.= "numero=" . $no[$i];
		if($i+1 < count($no))
			$sql.=" OR "; 
	  }
	  $sql.=" order by numero asc";
	  $result = $this->mysqli->query($sql);
      $nbFaq = $result->num_rows;
      
      for($i=0;$i<$nbFaq;$i++)
      {
        $row=$result->fetch_object();
        $faq=new FAQ($this->mysqli);
        
		$faq->asgCle($row->cleFaq);
        $faq->asgQuestion(addslashes($row->question));
        $faq->asgReponse(addslashes($row->reponse));
        $faq->asgNumero($row->numero);
        
        $this->ajoutFaq($faq);
	  }
	  
	}
	
	//
	//fonction de retour
	//
	function reqFaq($noFaq)
	{
	  PRECONDITION($noFaq>=1 && $noFaq<= $this->nbFaq);
	  return $this->faqs[$noFaq-1];
	}
	
	function reqNbFaq(){
	  return $this->nbFaq;
	}
    
}
  