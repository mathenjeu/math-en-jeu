<td align="left" valign="top">
<H1>{$lang.mod_joueur_profil_de} <u>{$alias} </u></h1>
<div style="width:90%" class="hr"></div>
<div class="centre">
	    <form action="portail-joueur.php?action=doModificationPerso#s1" method=post>
	    <h2 id="s1">{$lang.mod_joueur_donnee_personnel}</h2>
	    <span class="erreur">{$erreur1}</span>
	    <span class="message">{$message1}</span>
	    <table>
	    <tr>
	        <td>{$lang.prenom}</td>
	        <td><input type="text" name="prenom" size="20" value="{$prenom}"></td>
	    </tr>
	    <tr>
	        <td>{$lang.nom}</td>
	        <td><input type="text" name="nom" size="20" value="{$nom}"></td>
	    </tr>
	    <tr>
	        <td>{$lang.ville}</td>
	        <td><input type="text" name="ville" size="20" value="{$ville}"></td>
	    </tr>
	    <tr>
	        <td>{$lang.province}</td>
	        <td><input type="text" name="province" size="20" value="{$province}"></td>
	    </tr>
	    <tr>
	        <td>{$lang.pays}</td>
	        <td><input type="text" name="pays" size="20" value="{$pays}"></td>
	    </tr>
	    <tr>
	        <td>{$lang.courriel}</td>
	        <td><input type="text" name="courriel" size="30" value="{$courriel}"></td>
	    </tr>
	    <tr>
	    	<td></td>
	    	<td colspan="3" class="commentaire">{$lang.info_courriel}</td>
	    </tr>
	    <tr>
	        <td colspan="3" align="left"><input type=submit value="{$lang.bouton_modifier}"></td>
	    </tr>
	    </table>
	    </form>
	<br>
	    <form action="portail-joueur.php?action=doModificationPass#s2" method=post>
	    <h2 id="s2">{$lang.mod_joueur_mot_passe}</h2>
	    <span class="erreur">{$erreur2}</span>
	    <span class="message">{$message2}</span>
	    <table>
	    <tr>
	        <td>{$lang.mod_joueur_ancien_pass}</td>
	        <td><input type="password" name="oldpass" size="20"></td>
	    </tr>
	    <tr>
	        <td>{$lang.mod_joueur_nouveau_pass}</td>
	        <td><input type="password" name="newpass" size="20"></td>
	    </tr>
	    <tr>
	    	<td></td>
	    	<td colspan="3" class="commentaire">{$lang.inscription_mot_de_passe_info}</td>
	    </tr>
	    <tr>
	        <td>{$lang.mot_passe_confirm}</td>
	        <td><input type="password" name="newpass2" size="20"></td>
	    </tr>
	    <tr>
	        <td align="left" colspan="3"><input type=submit value="{$lang.bouton_modifier}"></td>
	    </tr>
	    </table>
	    </form>
	<br>
		<form name="scolaire" action="portail-joueur.php?action=doModificationScolaire#s3" method=post>
	   <h2>{$lang.mod_joueur_profil_scolaire}</h2>
	   <span class="erreur">{$erreur3}</span>
	   <span style="color:green" class="message">{$message3}</span>
	   <table id="s3">
	   <tr>
	   	  <td>{$lang.niveau_scolaire}</td>
	        <td>
	            <select name="niveau"
	            onChange='document.scolaire.action="portail-joueur.php?action=etablissement#3";
	            document.scolaire.submit()'>
	            {html_options values=$niveauID output=$niveauTexte selected=$niveau}
	            </select>
	        </td>
	   </tr>
	   {if $niveau ne 14}
	   <tr>
	        <td>{$lang.etablissement}</td>
	        <td>
	            <select name="etablissement" STYLE="width: 400px">
	            {html_options values=$etablissementID output=$etablissementTexte selected=$etablissement}
	            </select>
	        </td>
	   </tr>
	   {/if}
	   <!--
	   <tr>
	        <td>{$lang.mod_joueur_alias_prof}</td>
	        <td><input type="text" name="aliasProf" value="{$aliasProf}"/></td>
	   </tr>
	   <tr>
	    	  <td></td>
	        <td colspan="3" class="commentaire">{$lang.mod_joueur_alias_prof_info}<td>
	   </tr>
	   -->
	   <tr>
	        <td align="left" colspan="3"><input type=submit value="{$lang.bouton_modifier}"></td>
	   </tr>
	  	</table>
	  	</form>
</div>
</td>

