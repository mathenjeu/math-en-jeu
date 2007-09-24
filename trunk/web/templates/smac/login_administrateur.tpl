<td class="centre" align="center" width="100%">
<div class="centre">
	<form name="login" method="post" action="login-admin.php?action=valider">
    <table cellpadding="2" cellspacing="0" border="0" align="center">
    <tr>
    	<td align="center" colspan="2"><b>{$lang.admin_login_direction}</b><p></td>
    </tr>
    <tr>
      	<td class="erreur2" align="center" colspan="2">{$erreur}</td>
    </tr>
    <tr>
        <td align="left">{$lang.alias}</td>
        <td align="left"><input type="text" name="alias"></td>
    </tr>
    <tr>
        <td align="left">{$lang.mot_passe}</td>
        <td align="left"><input type="password" name="motDePasse"></td>
    </tr>
    <tr>
      	<td colspan="2">{$lang.admin_login_oublie}
            <a class="lien_sur_couleur" href="login-admin.php?action=pass_perdu">{$lang.cliquer_ici}</a>
      	</td>
    </tr>
    <tr>
      	<td colspan="2" align="center"><input type="submit" value="{$lang.bouton_envoyer}"></td>
    </tr>
    </table>
    <script type="text/javascript" language="JavaScript">
        document.login.alias.focus();
    </script>
    </form>
</div>
</td>
