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
  <title>Math en jeu - Inscriptions</title>
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

<h1>Répartition des inscriptions par date</h1>

<p>Rapport généré le <?php echo date("Y-m-d"); ?></p>

<p><a href="#graphique">Aller au graphique</a></p>

<table>
<tr>
  <th>Date</th>
  <th>Nombre</th>
</tr>

<?php
// Gets number of registrations
$result = db_query($db, "SELECT dateInscription,COUNT(*) as nombre FROM joueur GROUP BY dateInscription DESC;");
if (!$result)
{
  echo ("No result; maybe key does not exist?  See below if MySQL says something.");
  echo db_error($db);
  exit();
}
$i = 0;
while ($row = db_fetch_assoc_array($result))
{
  echo "<tr class=\"modulo-$i\">\n";
  echo "<td>".$row["dateInscription"]."</td>\n";
  echo "<td>".$row["nombre"]."</td>\n";
  echo "</tr>\n";
  $i = ($i == 0 ? 1 : 0);
}
?>
</table>

<a name="graphique" id="graphique"></a><h2>Graphique</h2>

<p>60 derniers jours</p>

<?php
$a = array();
$max_value = -1;
$result = db_query($db, "SELECT dateInscription,COUNT(*) as nombre FROM joueur WHERE dateInscription > '".date("Y-m-d", strtotime("-60 days"))."' GROUP BY dateInscription ASC;");
if (!$result)
{
  echo ("No result; maybe key does not exist?  See below if MySQL says something.");
  echo db_error($db);
  exit();
}
$i = 0;
while ($row = db_fetch_assoc_array($result))
{
  array_push($a, $row["nombre"]);
  if ($row["nombre"] > $max_value)
    $max_value = $row["nombre"];
}
?>

<div id="graphique">

<table>
<tr>
<td class="axe" valign="top"><?php echo $max_value; ?></td>
<?php
foreach($a as $v)
{
  echo "<td valign=\"bottom\"><img style=\"height:".($v / $max_value * 300)."px;\" src=\"blue.png\"/></td>\n";
}
?>
</tr>
</table>

</div>

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
