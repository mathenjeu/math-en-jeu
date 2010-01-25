<?php
/*******************************************************************************
Fichier : statistique.class.php
Auteur : Maxime Bégin
Description :
    classe servant à produire des statistiques et de générer des grahique
    en utilisant Image_graph (http://pear.veggerby.dk/)
    Utilisation : on appelle cette classe directement à partir d'un tag
    html <img> avec les paramêtres voulues.
    ex : <img src="statictique.class.php?graphique=1&opt=30&type=smooth">
********************************************************************************
15-06-2006 Maxime Bégin - Version initiale
21-06-2006 Maxime Bégin - ajout de quelques commentaires
*******************************************************************************/

require_once("ini.php");
set_include_path(LIB_DIR . PATH_SEPARATOR . get_include_path());
require_once(LIB_DIR . '/Image/Graph.php');

main();

function main()
{
  try
  {
    if(isset($_GET["graphique"]))
    {
        $stats = new Statistique($_SESSION["mysqli"]);

        if($_GET["opt"]=='0')
            $stats->aucunResultat();
        else
        {
            $stats->typeGraph=$_GET["type"];
        
            switch($_GET["graphique"])
            {
              case "1":
                $stats->nbPartieJouerMois($_GET["opt"]);
                break;
              case "2":
                $stats->nbPartieJouerJour($_GET["opt"]);
                break;
              case "3":
                $stats->tempsTotalPartieJour($_GET["opt"]);
                break;
              case "4":
                $stats->detailSondage($_GET["opt"]);
                break;
            }
        }
    }
  }
  catch(SQLException $e)
  {
    echo $e->exception_dump();
  }
  catch(MyException $e)
  {
    echo $e->exception_dump();
  }
}


class Statistique
{
    private $mysqli;
    public $typeGraph;
  
    function Statistique($mysqli)
    {
      PRECONDITION(get_class($mysqli)=="mon_mysqli");
      $this->mysqli=$mysqli;
    }
    

    private function afficherGraph($arrx,$arry,$intervalle,$titre,$legende)
    {

        $Graph =& Image_Graph::factory('graph', array(600, 400));
        
        $Font =& $Graph->addNew('font', 'Verdana');
        $Font->setSize(8);
        $Graph->setFont($Font);
        
        //création du layout
        $Graph->add(
            Image_Graph::horizontal(
                Image_Graph::factory('plotarea'),
                Image_Graph::horizontal(
                    Image_Graph::vertical(
                            Image_Graph::factory('title', array($titre, 11)),
                            Image_Graph::vertical(
                                $Plotarea = Image_Graph::factory('plotarea'),
                                $Legend = Image_Graph::factory('legend'),
                                90),
                            8),
                    Image_Graph::factory('plotarea'),
                    90),
                10)
        );
        $Legend->setPlotarea($Plotarea);

        $Dataset =& Image_Graph::factory('dataset');
        for($i=0;$i<count($arrx);$i++)
        {
            $Dataset->addPoint($arrx[$i],$arry[$i]);
        }
        
        switch($this->typeGraph)
        {
          case 'baton':
            $type='bar';
            break;
          case 'ligne':
            $type='line';
            break;
          case 'smooth':
            $type='Image_Graph_Plot_Smoothed_Area';
            break;
        }

        $Plot =& $Plotarea->addNew($type, $Dataset);
        $Plot->setTitle($legende);
        $Plot->width(100);
        
        $Axis =& $Plotarea->getAxis(IMAGE_GRAPH_AXIS_X);
        $Axis->setLabelInterval($intervalle);

        $Plot->setLineColor('gray');
        $Plot->setFillColor('blue@0.2');
        
        $Graph->done();
    }
    
    function detailSondage($cle)
    {
      global $lang;
      
      $sql="select question from pool where pool_is=" . $cle;
      $result = $this->mysqli->query($sql);
      $row=$result->fetch_array();
      $titre=$row[0];
      
      $sql="select * from pool_awnser where pool_id=" . $cle;
      $result = $this->mysqli->query($sql);
      $nb=$result->num_rows;
      $total=0;

      $Dataset1 =& Image_Graph::factory('dataset');
      //on ajoute les valeur au dataset
      for($i=0;$i<$nb;$i++)
      {
        $row = $result->fetch_object();
        $data = ($row->compteur==0) ? 0.0001:$row->compteur;

        $Dataset1->addPoint($row->reponse,$data);
        $total+=$row->compteur;
      }
      //si on a aucun résultat
      if($total==0)
      {
        $this->aucunResultat();
        return;
      }
      
      //créer le graph
      $Graph =& Image_Graph::factory('graph', array(500, 500));

      // ajoute la fonte
      $Font =& $Graph->addNew('font', 'Verdana');
      $Font->setSize(10);
      $Graph->setFont($Font);

      // creation de la zone d'affichage
      $Graph->add(
        Image_Graph::vertical(
        Image_Graph::vertical(
            Image_Graph::factory('title', array($titre, 12)),
            Image_Graph::horizontal(
                $Plotarea = Image_Graph::factory('plotarea'),
                $Legend = Image_Graph::factory('legend'),
                70
            ),
            5),
        Image_Graph::factory('title',array($lang['stat_total_repondant'] . " : " . $total,8)),95)
      );

      $Legend->setPlotarea($Plotarea);

      // ajoute le graphique à la zone
      $Plot =& $Plotarea->addNew('pie', array($Dataset1));
      $Plotarea->hideAxis();

      // creation d'un makeur avec les données de Y
      $Marker =& $Plot->addNew('Image_Graph_Marker_Value', IMAGE_GRAPH_PCT_Y_TOTAL);
      $PointingMarker =& $Plot->addNew('Image_Graph_Marker_Pointing_Angular', array(-40, &$Marker));
      $Plot->setMarker($PointingMarker);
      // format du markeur pour afficher des %
      $Marker->setDataPreprocessor(Image_Graph::factory('Image_Graph_DataPreprocessor_Formatted', '%0.1f%%'));

      $Plot->Radius = 2;

      $FillArray =& Image_Graph::factory('Image_Graph_Fill_Array');
      $Plot->setFillStyle($FillArray);
      $FillArray->addNew('gradient', array(IMAGE_GRAPH_GRAD_RADIAL, 'white', 'green'));
      $FillArray->addNew('gradient', array(IMAGE_GRAPH_GRAD_RADIAL, 'white', 'blue'));
      $FillArray->addNew('gradient', array(IMAGE_GRAPH_GRAD_RADIAL, 'white', 'yellow'));
      $FillArray->addNew('gradient', array(IMAGE_GRAPH_GRAD_RADIAL, 'white', 'red'));
      $FillArray->addNew('gradient', array(IMAGE_GRAPH_GRAD_RADIAL, 'white', 'orange'));

      $Plot->explode(5);

      $PointingMarker->setLineColor(false);
      $Marker->setBorderColor(false);
      $Marker->setFillColor(false);

      // affiche le graphique
      $Graph->done();
      
    }

    function moyenneTempsPartieMois($nbMois)
    {}
    function moyenneTempsPartieJour($nbJour)
    {
      /*
      $sql="select sum(dureePartie) as duree, count(clePartie) as total,datePartie from partie";
      $sql.="where DATE_SUB(CURDATE(),INTERVAL " . $nbJour . " DAY) <= datePartie";
      $sql.="GROUP BY datePartie ORDER by datePartie asc";
      */
      

    }
    function tempsTotalPartieMois($nbMois)
    {}
    
    function aucunResultat()
    {
        global $lang;
        $image = ImageCreate(300,100);
        $background_color = imagecolorallocate ($image, 255, 255, 255);
        $text_color = imagecolorallocate ($image, 255, 0, 0);
        imagestring($image,5,50,50,$lang['stat_aucun_result'],$text_color);
        imagepng ($image);
        imagedestroy($image);
    }
    function tempsTotalPartieJour($nbJour)
    {
      global $lang;
      
      $sql="select sum(dureePartie) as duree,datePartie from partie where ";
      $sql.="DATE_SUB(CURDATE(),INTERVAL " . $nbJour . " DAY) <= datePartie ";
      $sql.="GROUP BY datePartie ORDER by datePartie asc";
      $result = $this->mysqli->query($sql);
      $nb=$result->num_rows;
      if($nb==0)
      {
        $this->aucunResultat();
        return;
      }

      //calcul de la premier date à gauche
      $dateEx = explode("-",date("m-d-Y"));
      $mktime=mktime(0,0,0,$dateEx[0],$dateEx[1]-$nbJour,$dateEx[2]);

      $row=$result->fetch_array();
      //on boucle le nombre de jours demandés
      for($i=0;$i<$nbJour;$i++)
      {
        $arrx[$i]=date("Y-m-d",$mktime);
        //si la date est égal à celle de la requete on ajoute le nombre de parties
        if($arrx[$i]==$row[1])
        {
            $arry[$i]=$row[0];
            $row=$result->fetch_array();
        }
        else
        {
          //date pas égale on ajoute 0
          $arry[]=0;
        }
        //incrémente la date d'une journée
        $dateEx=explode("-",date("m-d-Y",$mktime));
        $mktime=mktime(0,0,0,$dateEx[0],$dateEx[1]+1,$dateEx[2]);
      }

      $intervalle=(int)($nbJour/5);
      $intervalle = ($intervalle==0) ? 1:$intervalle;

      $this->afficherGraph($arrx,$arry,$intervalle,
        $lang['stat_temps_total_au_cour'] . $nbJour . $lang['stat_dernier_jour'],
        $lang['stat_temps_jouee']);
    }
    
    function nbPartieJouerJour($nbJour)
    {
      global $lang;

      $sql="select count(date) as nbPartie,date from game where
            DATE_SUB(CURDATE(),INTERVAL " . $nbJour . " DAY) <= date
            GROUP BY date ORDER by date asc";

      $result = $this->mysqli->query($sql);
      $nb=$result->num_rows;
      if($nb==0)
      {
        $this->aucunResultat();
        return;
      }
      
      //calcul de la premier date à gauche
      $dateEx = explode("-",date("m-d-Y"));
      $mktime=mktime(0,0,0,$dateEx[0],$dateEx[1]-$nbJour,$dateEx[2]);
      
      $row=$result->fetch_array();
      //on boucle le nombre de jours demandés
      for($i=0;$i<$nbJour;$i++)
      {
        $arrx[$i]=date("Y-m-d",$mktime);
        //si la date est égal à celle de la requete on ajoute le nombre de parties
        if($arrx[$i]==$row[1])
        {
            $arry[$i]=$row[0];
            $row=$result->fetch_array();
        }
        else
        {
          //date pas égale on ajoute 0
          $arry[]=0;
        }
        //incrémente la date d'une journée
        $dateEx=explode("-",date("m-d-Y",$mktime));
        $mktime=mktime(0,0,0,$dateEx[0],$dateEx[1]+1,$dateEx[2]);
      }


      $intervalle=(int)($nbJour/5);
      $intervalle = ($intervalle==0) ? 1:$intervalle;

      $this->afficherGraph($arrx,$arry,$intervalle,
        $lang['stat_nb_partie_au_cour'] . $nbJour . $lang['stat_dernier_jour'],
        $lang['stat_nb_partie_jouee']);

    }
    
    function nbPartieJouerMois($nbMois)
    {
      global $lang;
      
      $mois=array($lang['jan'],$lang['fev'],$lang['mar'],
        $lang['avr'],$lang['mai'],$lang['jun'],$lang['jul'],
        $lang['aou'],$lang['sep'],$lang['oct'],
        $lang['nov'],$lang['dec']);

      $sql="select count(date) as nbPartie, Month(date) as mois,Year(date) as annee from game where
            DATE_SUB(CURDATE(),INTERVAL $nbMois MONTH) <= date
            GROUP BY Month(date) ORDER by date asc";
      $result = $this->mysqli->query($sql);
      
	  if(!$result)
	  {
		$this->aucunResultat();
        return;
	  }
		
      $nb=$result->num_rows;

      if($nb==0)
      {
        $this->aucunResultat();
        return;
      }
      
      //calcul du premier mois à gauche avec correction du nombre de résultat
      $dateEx = explode("-",date("m-d-Y"));
      if($nb>$nbMois)
      {
	  	$row=$result->fetch_array();
	  	$mktime=mktime(0,0,0,$dateEx[0]-($nbMois-1),1,$dateEx[2]);
	  }
	  else
	  {
	    $mktime=mktime(0,0,0,$dateEx[0]-$nbMois,1,$dateEx[2]);
	  }
	        
      
      $row=$result->fetch_array();

      //on boucle le nombre de jours demandés
      for($i=0;$i<$nbMois;$i++)
      {
        $dateEx=explode("-",date("m-d-Y",$mktime));
        $arrx[$i]=$mois[(int)$dateEx[0]-1] . " " . $dateEx[2];
        
        //si le mois et l'année sont égales on ajoute la valeur
        if($dateEx[0] == $row[1] && $dateEx[2]==$row[2])
        {
            $arry[$i]=$row[0];
            $row=$result->fetch_array();
        }
        else
        {
          //date pas égale on ajoute 0
          $arry[$i]=0;
        }

        //incrémente la date d'une journée
        $mktime=mktime(0,0,0,$dateEx[0]+1,1,$dateEx[2]);
      }
      
      //calcul de l'intervalle pour ne pas encombrer l'axe des x
      $intervalle = (int)($nbMois/5);
      $intervalle = ($intervalle==0) ? 1:$intervalle;
        
      $titre=$lang['stat_nb_partie_au_cour'] . $nbMois  . $lang['stat_dernier_mois'];
      $legende = $lang['stat_nb_partie_jouee'];
      $this->afficherGraph($arrx,$arry,$intervalle,$titre,$legende);
        
    }

}

