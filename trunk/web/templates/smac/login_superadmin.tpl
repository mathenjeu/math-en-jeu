<td class="centre" align="center">
<div class="centre">
	<form method=post action="super-admin.php?action=doLogin">
	<table align="center">
		<tr>
			<td colspan="2" class="erreur2">{$erreur}</td>
		</tr>
	  <tr>
	    <td>{$lang.courriel} :</td>
	    <td><input type=text name=courriel></td>
	  </tr>
	  <tr>
	    <td>{$lang.mot_passe} :</td>
	    <td><input type=password name=motDePasse></td>
	  </tr>
	  <tr>
	    <td colspan=2 align=center><input type=submit value="{$lang.bouton_valider}"></td>
	  </tr>
	</table>
	</form>
</div>
</td>
