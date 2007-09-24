<td align="left" valign="top">
	<h2>{$lang.joueur_login_recuperation_mot_passe}</h2>
	<div style="width:90%;align:center" class="hr"></div><p><br>
	<form name="recup_login" action="login-joueur.php?action=envoi_info" method="post">
    <table align="left" width="100%" border="0">
    <tr>
        <td colspan="2">
        {$lang.joueur_login_pass_perdu_direction}<p>
        </td>
    </tr>
    <tr>
    	<td colspan="2" class="erreur2">{$erreur}</td>
    </tr>
    <tr>
        <td colspan="2" width="50%"><input size="40" type="text" name="courriel"/></td>
    </tr>
    <tr>
        <td colspan="2"><br><input type="submit" value="{$lang.bouton_envoyer}" /></td>
    </tr>
    </table>
    </form>
    <script type="text/javascript" language="JavaScript">
        document.recup_login.courriel.focus();
    </script>

</td>

