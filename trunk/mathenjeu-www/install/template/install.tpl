<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>{$titre}</title>
<link rel="stylesheet" type="text/css" href="install.css">
</head>
<body>
<div class=wraper>
	<div class="top">
		<h1>{$lang.install_titre}</h1>
	</div>
	<div class="main">
	{if $etape eq 0}
		<form action="install.php?etape=0" method="post">
			{$lang.install_langage} :
			{html_options name=langue options=$langue selected=$sLangue}
			<br>
			<hr>
			<input type="submit" value="{$lang.install_suivant}">
		</form>
	{elseif $etape eq 2}
		{if $erreur neq ""}
			<div class="erreurbox">{$erreur}</div>
		{/if}
		<form action="install.php?etape=3" method="post">
		<h2>{$lang.install_config_db}</h2>
		<div class="sous_section">
			<table>
			<tr>
				<td>{$lang.install_db_adresse} :</td><td><input name="dbHote" value="{$dbHote}"></td>
			</tr>
			<tr>
				<td>{$lang.install_db_user} :</td><td><input name="dbUtilisateur" value="{$dbUtilisateur}"></td>
			</tr>
			<tr>
				<td>{$lang.install_db_password} :</td><td><input type="password" name="dbMotDePasse" value=""></td>
			</tr>
			<tr>
				<td>{$lang.install_db_schema} :</td><td><input name="dbSchema" value="{$dbSchema}"></td>
			</tr>
			</table>
		</div>
		<hr>
		<input onclick="window.location.href='install.php'" type="button" value="{$lang.install_precedent}">
		<input type="submit" value="{$lang.install_suivant}">
		</form>
	{elseif $etape eq 3}
		<ul>
		<li>{$lang.install_db_connect} :
		{if $info.db_connect eq 0}
			<img src="img/red_check.gif">
		{else}
			<img src="img/green_check.gif">
		{/if}
		</li>
		<li>
		{$lang.install_lecutre_sql} : 
		{if $info.lecture_fichier eq 0}
			<img src="img/red_check.gif">
		{else}
			<img src="img/green_check.gif">
		{/if}
		</li>
		<br>
		<li>
		{$lang.install_creation_table}
			<ul>
			{section name=table loop=$info.table_check}
				<li>
					{if $info.table_check[table] eq 0}
						<img src="img/red_check.gif">
						`{$info.table_name[$smarty.section.table.index]}`
						{if $info.table_exist[$smarty.section.table.index] eq 1}
							{$lang.install_table_existe}
						{/if}
					{else}
						<img src="img/green_check.gif">
						{$info.table_name[$smarty.section.table.index]}
					{/if}
				</li>
			{/section}
			</ul>
		</li>
		<li>
		{$lang.install_insertion_donnee}
		{if $info.insertion_donnee eq 1}
			<img src="img/green_check.gif">
		{else}
			<img src="img/red_check.gif">
		{/if}
		</li>
		</ul>
		{if $erreur eq 0}
			{if $erreur_table_exist eq 1}
				<strong>{$lang.install_erreur_table_existe}</strong>
			{else}
				{$lang.install_db_succes}
			{/if}
			<br>
			<input onclick="window.location.href='install.php?etape=1'" type="submit" value="{$lang.install_precedent}">
			<input onclick="window.location.href='install.php?etape=4'" type="submit" value="{$lang.install_suivant}">
		{else}
			<div class="erreurbox">{$lang.install_db_erreur}</div><p></p>
			<input onclick="window.location.href='install.php?etape=1'" type="submit" value="{$lang.install_precedent}">
		{/if}	
	{elseif $etape eq 4}
		<h2>{$lang.install_config_admin}</h2>
		<span style="color:red">{$erreur}</span>
		<form action="install.php?etape=5" method="post">
		<div class="sous_section">
			<table>
			<tr>
				<td>{$lang.install_admin_prenom} :</td><td><input size="30" name="admin_prenom" value="{$prenom}"></td>
			</tr>
			<tr>
				<td>{$lang.install_admin_nom} :</td><td><input size="30" name="admin_nom" value="{$nom}"></td>
			</tr>
			<tr>
				<td>{$lang.install_admin_courriel} :</td><td><input size="30" name="admin_courriel" value="{$courriel}"></td>
			</tr>
			<tr>
				<td>{$lang.install_admin_password} :</td><td><input size="30" type="password" name="admin_password" value="{$motDePasse}"></td>
			</tr>
			</table>
		</div>
		<hr>
		<!--<input onclick="window.location.href='install.php?etape=1'" type="button" value="{$lang.install_precedent}">-->
		<input type="submit" value="{$lang.install_suivant}">
		</form>
	{elseif $etape eq 6}
		<span style="color:red">{$erreur}</span><br>
		{$lang.install_message_fin}
	{/if}
	</div>
</div>
</body>
</html>
