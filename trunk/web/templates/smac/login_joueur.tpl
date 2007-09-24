<td valign="top">
<h2>{$lang.joueur_login_direction}</h2>
<div style="width:90%;align:center" class="hr"><p>
<div class="centre">
    <form name="login" method="post" action="login-joueur.php?action=valider">
        <table align="center" border="0" cellpadding="5" cellspacing="0" width="75%">
        <tr>
            <td colspan="2" align="center"><p></td>
        </tr>
        <tr>
        	<td colspan="2" align="left" class="blanc">
            	{$lang.joueur_login_non_inscrit}
        	</td>
        </tr>
        <tr>
        	<td><br></td>
        </tr>
        <tr>
            <td class="erreur2" colspan="2" align="center">{$erreur}</td>
		</tr>
        <tr>
            <td align="right" class="blanc">{$lang.alias} :</td>
            <td align="left"><input type="text" name="alias"></td>
        </tr>
        <tr>
            <td align="right" class="blanc">{$lang.mot_passe} :</td>
            <td align="left"><input type="password" name="motDePasse"></td>
        </tr>
        <tr>
            <td colspan="2" align="center"><p></td>
        </tr>
        <tr>
            <td align="center" colspan="2" class="blanc">{$lang.joueur_login_oublie}
                <a href="login-joueur.php?action=pass_perdu" class="lien_sur_couleur">{$lang.joueur_login_clique_ici}</a>
            </td>
        </tr>
        <tr>
            <td align="center" colspan="2"><input type="submit" value="{$lang.bouton_connexion}"></td>
        </tr>
        </table>
    </form>
    <script type="text/javascript" language="JavaScript">
        document.login.alias.focus();
    </script>
</div>
</td>