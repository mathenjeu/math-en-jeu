<?php

  require_once("lib/ini.php");

  $dao = new Dao($_SESSION["mysqli"]);

  $result = $dao->getSubjects($_SESSION['langage']);
  $count = count($result);
  
  for ($i = 0; $i < $count; $i++) {
    echo "\nChecking $i : \n";
    echo "ID : " . $result[$i][0] . "\r\n";
    echo "nom : " . $result[$i][1] . "\r\n";
    echo "\t Level : <br>";
    $levels = $dao->getLevelsForSubject($result[$i][0], $_SESSION['langage']);
    $countLevel = count($levels);
    for ($j = 0; $j < $countLevel; $j++) {
      echo "<br> " . $levels[$j][0] . " " . $levels[$j][1];
    }

  }
  



?>