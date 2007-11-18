<td class="menu" width="20%" valign="top">
<div class="menu">
	<!--
	<div id="lang">
		<a href="set_lang.php?lang=fr"><img alt="fr" border="0" src="{$template}img/flag_fr.gif"></a>
		<a href="set_lang.php?lang=en"><img alt="en" border="0" src="{$template}img/flag_en.gif"></a>
	</div>
	-->
	{if $connecter eq 1}
		<h4 class="titre_menu">{$lang.titre_menu_joueur}</h4>
		<div>
			<ul>
				<li><a style="font-size: 150%" href="#" class="menu" onclick="window.open('jeu-popup.php?{$sid}','jeu','fullscreen=yes,channelmode=yes,menubar=no,toolbar=no,location=no,resizable=yes,scrollbars=no,status=no');">{$lang.menu_jouer}</a><p/></li>
				<!--<li><a target="_blank" href="jeu-popup.php" class="menu">{$lang.menu_jouer}</a><p/></li>-->
				<li><a href="index.php" class=menu>{$lang.menu_accueil}</a></li>
				<li><a href="nouvelles.php" class="menu">{$lang.menu_nouvelles}</a></li>
				<li><a href="demo.php" class="menu">{$lang.menu_demo}</a></li>
				<li><a href="instructions.php" class="menu">{$lang.menu_instructions}</a></li>
				<li><a href="portail-joueur.php?action=stat" class="menu">{$lang.menu_stat_joueur}</a></li>
				<li><a href="palmares.php" class="menu">{$lang.menu_palmares}</a></li>
				<li><a href="portail-joueur.php?action=profil" class="menu">{$lang.menu_profil}</a></li>
				<li><a href="faq.php" class="menu">{$lang.menu_faq}</a></li>
				<li><a href="contact.php" class="menu">{$lang.menu_contact}</a></li>
			</ul>
		</div>
		{if $acces >= 1}
		<p>
		<div>
			<ul>
				<li><a href="question.php">{$lang.menu_ajout_question}</a></li>
				<li><a href="question.php?action=find">{$lang.menu_chercher_question}</a></li>
				<li><a href="question.php?action=liste_courant">{$lang.menu_mes_question}</a></li>
			</ul>
		</div>
		</p>
		{/if}
		{if $acces == 5}
		<p>
		<div>
			<h4 class="titre_menu">{$lang.titre_menu_super_admin}</h4>
			<ul>
				<!--<li><a href="admin_config.php" class="menu">{$lang.menu_configuration}</a></li>-->
				<li><a target="_blank" href="/phpMyAdmin/" class="menu">{$lang.menu_php_my_admin}</a></li>
				<li><a href="admin_nouvelles.php" class="menu">{$lang.menu_gestion_nouvelles}</a></li>
				<li><a href="admin_sondage.php" class="menu">{$lang.menu_gestion_sondages}</a></li>
				<li><a href="admin_faq.php">{$lang.menu_gestion_faq}</a></li>
				<li><a target="_blank" href="/awstats/awstats.pl?config=mathenjeu" class="menu">{$lang.menu_stats_web}</a></li>
				<li><a href="admin_statistiques.php" class="menu">{$lang.menu_statistiques}</a></li>
				<li><a href="stats/" target="_blank" class="menu">{$lang.menu_statistique2}</a></li>
			</ul>
		</div>
		</p>
		{/if}
	{else}
		<h4 class="titre_menu">{$lang.titre_menu_principal}</h4>
		<div>
			<ul>
				<li><a style="font-size: 150%" href="inscription-joueur.php" class=menu>{$lang.menu_inscription_joueur}</a><br><br></li>
			    <li><a href="index.php" class=menu>{$lang.menu_accueil}</a></li>
			    <li><a href="nouvelles.php" class="menu">{$lang.menu_nouvelles}</a></li>
			    <li><a href="demo.php" class="menu">{$lang.menu_demo}</a></li>
			    <li><a href="instructions.php" class="menu">{$lang.menu_instructions}</a></li>
			    <li><a href="login-joueur.php" class=menu>{$lang.menu_connexion}</a></li>
			    <li><a href="palmares.php" class="menu">{$lang.menu_palmares}</a></li>
			    <li><a href="faq.php" class=menu>{$lang.menu_faq}</a></li>
			    <li><a href="contact.php" class="menu">{$lang.menu_contact}</a></li>
			</ul>
		</div>
	{/if}
	<br>
	<hr/>
	<br>
	<div class="menu" style="padding:10px">
	{if $connecter eq 1}
		Bonjour <i>{$alias}</i>. <p>
		<a href="logout.php" class="menu">{$lang.menu_deconnexion}</a>
	{else}
		<form method="post" action="login-joueur.php?action=valider">
			{$lang.alias} : <input type="text" size="15" name="alias"><p>
			{$lang.mot_passe} : <input type="password" size="15" name="motDePasse"><p>
			<a href="inscription-joueur.php" class=menu>{$lang.menu_inscription_joueur}</a>
			<a href="#" onClick="window.open('../aide/aide-popup-inscription.html', 'Aide', 'toolbar=no,menubar=no,location=no,scrollbars=auto,resizable=yes,width=350,height=350');">
				<img alt="{$lang.aide}" width="20%" src="{$template}img/btnInterrogation.gif"/>
			</a><p>
			<input type="submit" value="{$lang.bouton_connexion}">
		</form>
	{/if}
	</div>
</div>

</td>
<td>
&nbsp;&nbsp;

</td>



