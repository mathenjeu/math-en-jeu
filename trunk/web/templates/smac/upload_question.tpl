<td width="90%" valign="top" align="left">
<div class="centre">


<table>
<form method="post" action="question.php" enctype="multipart/form-data">
<input type="hidden" name="action" value="doUpload" />
<input type="hidden" name="cleQuestion" value="{$cleQuestion}" />

<tr>
	<td>Fichier Question : </td>
	<td><input name="question" type="file" /></td>
</tr>
<tr>
	<td>Fichier Rétroaction :  </td>
	<td><input name="retroaction" type="file" /></td>
</tr>
<tr>
	<td><input type="submit"></td>
</tr>
</form>
</table>




</div>
</td>