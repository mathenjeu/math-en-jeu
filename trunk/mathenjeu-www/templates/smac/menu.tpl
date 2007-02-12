<td class="menu" width="20%" valign="top">
<div class="menu">
	{if $connecter eq 1}
		<h4 class="titre_menu">{$lang.titre_menu_joueur}</h4>
		<div>
			<ul>
				<li><a style="font-size: 150%" href="nouvelles.php" class="menu" onclick="window.open('jeu-popup.php','jeu','fullscreen=yes,channelmode=yes,menubar=no,toolbar=no,location=no,resizable=yes,scrollbars=no,status=no');">{$lang.menu_jouer}</a><p/></li>
				<!--<li><a target="_blank" href="jeu-popup.php" class="menu">{$lang.menu_jouer}</a><p/></li>-->
				<li><a href="index.php" class=menu>{$lang.menu_accueil}</a></li>
				<li><a href="nouvelles.php" class="menu">{$lang.menu_nouvelles}</a></li>
				<li><a href="instructions.php" class="menu">{$lang.menu_instructions}</a></li>
				<li><a href="portail-joueur.php?action=stat" class="menu">{$lang.menu_stat_joueur}</a></li>
				<li><a href="palmares.php" class="menu">{$lang.menu_palmares}</a></li>
				<li><a href="portail-joueur.php?action=profil" class="menu">{$lang.menu_profil}</a></li>
				<li><a href="faq.php" class="menu">{$lang.menu_faq}</a></li>
				<li><a href="contact.php" class="menu">{$lang.menu_contact}</a></li>
			</ul>
		</div>
	{else}
		<h4 class="titre_menu">{$lang.titre_menu_principal}</h4>
		<div>
			<ul>
				<li><a style="font-size: 150%" href="inscription-joueur.php" class=menu>{$lang.menu_inscription_joueur}</a><br><br></li>
			    <li><a href="index.php" class=menu>{$lang.menu_accueil}</a></li>
			    <li><a href="nouvelles.php" class="menu">{$lang.menu_nouvelles}</a></li>
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
<td>&nbsp;&nbsp;</td>

