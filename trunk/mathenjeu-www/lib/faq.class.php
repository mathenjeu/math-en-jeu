<?php
/*******************************************************************************
Fichier : faq.class.php
Auteur : Maxime B�gin
Description :
    classe pour la gestion de la FAQ(Foire au question)
********************************************************************************
05-07-2006 Maxime B�gin - Version initiale
*******************************************************************************/
require_once("exception.class.php");
require_once("mon_mysqli.class.php");

class FAQ
{
	private $cleFaq;		//la cl� unique de la faq
	private $question;		//le texte de la question
	private $reponse;		//le texte de la r�ponse
	private $numero;		//le num�ro ( l'ordre dans lesquelles les questions seront afficher)
    private $cleLangue; 
	private $mysqli;		//l'objet mysqli
	
	//**************************************************************************
    // Sommaire:    Constructeur de la classe FAQ
    // Entr�e:
    // Sortie:
    // Note:        initialise les donn�es � vide
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
    // Sommaire:    V�rifier les invariants de la classe
    // Entr�e:
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
    // Sommaire:    assign� la cl� de la faq
    // Entr�e:      $cle : la cl� unique de la faq
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
    // Sommaire:    assign� la question � la faq
    // Entr�e:      $question : ne doit pas �tre vide
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
    // Sommaire:    assign� la r�ponse � la faq
    // Entr�e:      $reponse : ne doit pas �tre vide
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
    // Sommaire:    assign� le num�ro � la faq
    // Entr�e:      $numero : doit �tre >= 0
    // Sortie:
    // Note:
    //**************************************************************************
	function asgNumero($no)
	{
	  PRECONDITION($no>=0);
	  $this->numero=$no;
	  POSTCONDITION($this->reqNumero()==$no);
	}

	function asgCleLangue($cleLangue) {
	  $this->cleLangue = $cleLangue;
	}
	//**************************************************************************
    // Sommaire:    ins�rer la faq dans la table
    // Entr�e:      
    // Sortie:		vrai si tout va bien, faux sinon
    // Note:		la cl� de la faq doit �tre �gal � 0
    //**************************************************************************
	function insertionMySQL()
	{
	  $this->INVARIANTS();
	  if($this->cleFaq !=0)
	  	return false;
	  $sql="select max(numero) from faq";
	  $result = $this->mysqli->query($sql);
	  
	  $arr=$result->fetch_array();

	  $sql="insert into faq(question,reponse,numero,cleLangue) 
	  		values('" . $this->question . "','" . $this->reponse . "'," . ($arr[0]+1) . "," . $this->cleLangue . ")";
	  		
	  $this->mysqli->query($sql);
	  $this->asgCle($this->mysqli->insert_id);
	  
	  return true;
	}

	//**************************************************************************
    // Sommaire:    mettre � jour la faq dans la table
    // Entr�e:      
    // Sortie:		vrai si tout va bien, faux sinon
    // Note:		la cl� de la faq doit �tre diff�rente de 0
    //**************************************************************************
	function miseAJourMySQL()
	{
	  $this->INVARIANTS();
	  
	  if($this->cleFaq==0)
	  	return false;
	  $sql="update faq set question='" . $this->question . "',reponse='" . $this->reponse . 
	  	"',numero=" . $this->numero . ",cleLangue=" . $this->cleLangue . " where cleFaq=" . $this->cleFaq;
	  $this->mysqli->query($sql);
	  
	  return true;
	}

	//**************************************************************************
    // Sommaire:    supprimer la faq de la table
    // Entr�e:      
    // Sortie:		vrai si tout va bien, faux sinon
    // Note:		la cl� de la faq doit �tre diff�rente de 0,
    //				les num�ros des faq subs�quente sont diminu�s de 1
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
    // Sommaire:    charger une faq � partir de la cl�
    // Entr�e:      $cle : cl� de la faq � charger
    // Sortie:		vrai si tout va bien, faux si aucune faq n'est trouv�
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
	function reCleLangue() {
	  return $this->cleLangue;
	}
}

class FAQs
{
    private $nbFaq;    	//le nombre de faq
    private $faqs;     	//tableau contenant les questions
    private $mysqli;	//objet mysqli
    
    //**************************************************************************
    // Sommaire:    Constructeur de la classe FAQ
    // Entr�e:
    // Sortie:
    // Note:        initialise les donn�es � vide
    //**************************************************************************
    function __construct($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
	  $this->mysqli=$mysqli;
	}

    //**************************************************************************
    // Sommaire:    ajouter une faq au tableau de faqs
    // Entr�e:
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
    // Entr�e:		$nbFaq : le nombre de faq � charger
    // Sortie:
    // Note:        
    //**************************************************************************
	function chargerMySQL($nbFaq,$langue)
	{
	  $sql="select * from faq where cleLangue=" . $langue . " order by numero asc"; 
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
	
    function chargerTouteMySQL()
	{
	  $sql="select * from faq order by numero asc";
	  
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
        $faq->asgCleLangue($row->cleLangue);
        
        $this->ajoutFaq($faq);
	  }
	}

    //**************************************************************************
    // Sommaire:    charger une faq � partir du(des) num�ro( pas la cl� )
    // Entr�e:		$no : un tableau contenant les num�ros 
	//				d'ordre des faq � charger
    // Sortie:
    // Note:        cette fonction est utlis� lors de la modification de l'ordre
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
  