<td class="centre" width="85%" align="left" valign="top">
<div class="centre">
	<h2>{$lang.config_site}</h2>
	<span class="direction_critique">{$lang.config_direction}</span>
	<form name="configuration" action="admin_config.php?action=doConfig" method="post">
	<table width="100%" border="0">
	<tr>
		<td colspan="2"><br><b>{$lang.config_general}</b></td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_langage} :</td>
		<td>{html_options name=langue options=$langue selected=$sLangue}</td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_adresse_site} :</td>
		<td><input type="text" name="adresseWeb" size="40" value="{$adresseWeb}"></td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_nb_nouvelles} :</td>
		<td><input type="text" name="nbNouvelles" size="10" value="{$nbNouvelles}"></td>
	</tr>
	<tr>
		<td>{$lang.config_nb_joueurs_palmares} :</td>
		<td><input type="text" name="nbJoueurs" size="10" value="{$nbJoueurs}"></td>
	</tr>
	<tr>
		<td>{$lang.config_min_parties_palmares} :</td>
		<td><input type="text" name="minParties" size="10" value="{$minParties}"></td>
	</tr>
	<tr>
		<td>{$lang.config_nb_jours_palmares} :</td>
		<td><input type="text" name="nbJours" size="10" value="{$nbJours}"></td>
	</tr>
	<tr>
		<td>{$lang.config_choix_template} :</td>
		<td><select name="template" 
				onChange='document.configuration.action="admin_config.php?action=templates";
            				document.configuration.submit()'>
			{html_options options=$templates selected=$sTemplate}
			</select>
		</td>
	</tr>
	<tr>
		<td>{$lang.config_fichier_css_defaut} :</td>
		<td>{html_options name=css options=$css selected=$scss}</td>
	</tr>
	
	<!-- Configuration des courriels -->
	<tr>
		<td colspan="2"><br><b>{$lang.config_courriel}</b></td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_nom_courriel} :</td>
		<td><input type="text" name="nomCourriel" size="40" value="{$nomCourriel}"></td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_adresse_courriel} :</td>
		<td><input type="text" name="courriel" size="40" value="{$courriel}"></td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_serveur_SMTP} :</td>
		<td><input type="text" name="serveurSMTP" size="40" value="{$serveurSMTP}"></td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_port_SMTP} :</td>
		<td><input type="text" name="portSMTP" size="40" value="{$portSMTP}"></td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_utilisateur_SMTP} :</td>
		<td><input type="text" name="utilisateurSMTP" size="40" value="{$utilisateurSMTP}"></td>
	</tr>
	<tr>
		<td width="40%">{$lang.config_motDePasse_SMTP} :</td>
		<td><input type="text" name="motDePasseSMTP" size="40" value="{$motDePasseSMTP}"></td>
	</tr>
	
	<tr>
		<td>
			<u>{$lang.config_tag_dispo}</u>
			<ul>
				<li>[ALIAS] : {$lang.config_tag_alias}</li>
				<li>[MOT_DE_PASSE] : {$lang.config_tag_mot_de_passe}</li>
				<li>[NOM] : {$lang.config_tag_nom}</li>
				<li>[PRENOM] : {$lang.config_tag_prenom}</li>
				<li>[CLE_CONFIRMATION] : {$lang.config_tag_cle_confirmation}</li>
				<li>[ADRESSE_SITE_WEB] : {$lang.config_tag_adresse_site_web}</li>
			</ul>
		</td>
	</tr>
	<tr>
		<td valign="top">{$lang.config_courriel_inscription} :</td>
		<td>
			<br>{$lang.config_sujet} : <br><input name="sujet_courriel_inscription" value="{$sujet_courriel_inscription}"size="60" type="text"><br>
			<textarea name="courriel_inscription" cols="50" rows="8">{$courriel_inscription}</textarea>
		</td>
	</tr>
	<tr>
		<td valign="top">{$lang.config_courriel_pass_perdu} :</td>
		<td>
			<br>{$lang.config_sujet} : <br><input name="sujet_courriel_pass_perdu" value="{$sujet_courriel_pass_perdu}"size="60" type="text"><br>
			<textarea name="courriel_pass_perdu" cols="50" rows="8">{$courriel_pass_perdu}</textarea>
		</td>
	</tr>
	<!-- Fin de la configuration des courriels -->
	
	<!-- configuration de la base de données -->
	<tr>
		<td colspan="2"><br><b>{$lang.config_db}</b></td>
	</tr>
	<tr>
		<td>{$lang.config_adresse} :</td>
		<td><input type="text" name="dbHote" value="{$dbHote}"></td>
	</tr>
	<tr>
		<td>{$lang.config_utilisateur} :</td>
		<td><input type="text" name="dbUtilisateur" value="{$dbUtilisateur}"></td>
	</tr>
	<tr>
		<td>{$lang.config_mot_de_passe} :</td>
		<td><input type="text" name="dbMotDePasse" value="{$dbMotDePasse}"></td>
	</tr>
	<tr>
		<td>{$lang.config_schema} :</td>
		<td><input type="text" name="dbSchema" value="{$dbSchema}"></td>
	</tr>
	<tr>
		<td colspan="2" align="left"><input type="submit" value="{$lang.bouton_enregistrer}"></td>
	</tr>
	<!-- FIN de la configuration de la base de données -->
	</table>
	</form>
</div>
</td>