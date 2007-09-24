<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<!-- Layout essentially stolen from
     http://alistapart.com/d/holygrail/example_4.html -->

<?php
// Imports
require_once("db-connection.lib.php");  // Connection info to the database

// Connects to the database 
$db = db_connect();
if (!$db)
{
  die("Error connecting to the MySQL database");
}
?>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="fr" lang="fr">
<head>
  <title>Math en jeu - Statistiques</title>
  <meta name="robots" content="noindex,nofollow" />
  <?php
  if (isset($_GET["style"]))
    $css_style = $_GET["style"];
  else
    $css_style = "default";
  echo "<link rel=\"stylesheet\" type=\"text/css\" href=\"".$css_style.".css\" title=\"Current style\"/>";
  ?>
  <!-- Alternate stylesheets -->
  <link rel="alternate stylesheet" type="text/css" href="printer-friendly.css" title="Printer friendly"/>
  <!-- End of alternate stylesheets -->
</head>
<body>

<div id="header">
  <div class="nw">
  <!-- Nothing for the moment -->
  </div>
</div>

<div id="center" class="column">

<h1>Résumé</h1>

<p>Rapport généré le <?php echo date("Y-m-d"); ?></p>

<ul>

<li>Nombre d'inscriptions: 
  <?php
  // Gets number of registrations
  $result = db_query($db, "SELECT COUNT(*) FROM joueur;");
  if (!$result)
  {
    echo ("No result; maybe key does not exist?  See below if MySQL says something.");
    echo db_error($db);
    exit();
  }
  $row = db_fetch_assoc_array($result);
  echo $row["COUNT(*)"];
  ?>
  <ul>
  <li>7 derniers jours:
  <?php
  // Gets number of registrations
  $result = db_query($db, "SELECT COUNT(*) FROM joueur WHERE dateInscription <= '".date("Y-m-d")."' AND dateInscription >= '".date("Y-m-d",strtotime("-7 days"))."';");
  if (!$result)
  {
    echo ("No result; maybe key does not exist?  See below if MySQL says something.");
    echo db_error($db);
    exit();
  }
  $row = db_fetch_assoc_array($result);
  echo $row["COUNT(*)"];
  ?>
  </li>
  </ul>
</li>

<li>Nombre d'établissements:
  <?php
  // Gets number of registrations
  $result = db_query($db, "SELECT COUNT(*) FROM joueur GROUP BY cleEtablissement;");
  if (!$result)
  {
    echo ("No result; maybe key does not exist?  See below if MySQL says something.");
    echo db_error($db);
    exit();
  }
  $row = db_num_rows($result);
  echo $row;
  ?>
</li>

<li>Nombre de parties jouées: 
  <?php
  // Gets number of registrations
  $result = db_query($db, "SELECT COUNT(*) FROM partie;");
  if (!$result)
  {
    echo ("No result; maybe key does not exist?  See below if MySQL says something.");
    echo db_error($db);
    exit();
  }
  $row = db_fetch_assoc_array($result);
  echo $row["COUNT(*)"];
  ?>
  <ul>
  <li>Dernière semaine:
  <?php
  // Gets number of registrations
  $result = db_query($db, "SELECT COUNT(*) FROM partie WHERE datePartie <= '".date("Y-m-d")."' AND datePartie >= '".date("Y-m-d",strtotime("-7 days"))."';");
  if (!$result)
  {
    echo ("No result; maybe key does not exist?  See below if MySQL says something.");
    echo db_error($db);
    exit();
  }
  $row = db_fetch_assoc_array($result);
  echo $row["COUNT(*)"];
  ?>
  </li>
  </ul>
</li>

<li>Temps de jeu: 
  <?php
  // Gets number of registrations
  $result = db_query($db, "SELECT SUM(dureePartie) FROM partie;");
  if (!$result)
  {
    echo ("No result; maybe key does not exist?  See below if MySQL says something.");
    echo db_error($db);
    exit();
  }
  $row = db_fetch_assoc_array($result);
  echo number_format($row["SUM(dureePartie)"] / 60, 1, ",", " ")." h";
  ?>
  <ul>
  <li>Dernière semaine:
  <?php
  // Gets number of registrations
  $result = db_query($db, "SELECT SUM(dureePartie) FROM partie WHERE datePartie <= '".date("Y-m-d")."' AND datePartie >= '".date("Y-m-d",strtotime("-7 days"))."';");
  if (!$result)
  {
    echo ("No result; maybe key does not exist?  See below if MySQL says something.");
    echo db_error($db);
    exit();
  }
  $row = db_fetch_assoc_array($result);
  echo number_format($row["SUM(dureePartie)"] / 60, 1, ",", " ")." h";
  ?>
  </li>
  </ul>
</li>

</ul>

</div>

<div id="left" class="column">
  <div id="left-inside">
    <div id="around-h2">
      <h2>Menu</h2>
    </div>
    <ul>
    <li><a href="./">Résumé</a></li>
    <li><a href="ecoles.php">Répartition par école</a></li>
    <li><a href="inscriptions.php">Nombre d'inscriptions</a></li>
    </ul>
    <?php
    if ($css_style === "default")
      echo "<p><a class=\"toggle-style\" href=\"?style=printer-friendly\">Version imprimable</a></p>";
    else
      echo "<p><a class=\"toggle-style\" href=\"?style=default\">Version graphique</a></p>";
    ?>
  </div>
</div>

<div id="right" class="column">
  <!-- Nothing for the moment -->
</div>

<div id="footer">
  <hr/>
  &copy; 2007 SMAC
</div>

</body>
</html>
