<td>
</td>
<td>
&nbsp;&nbsp;
</td>
<td valign="top" align="center">
<form method="post" action="sexe.php">
{$lang.question_sexe}&nbsp;
<select length="10" id="sexe" name="sexe">
	<option value="0" {if $sexe eq 0} selected {/if}>{$lang.feminin}&nbsp;&nbsp;&nbsp;</option> 
				<option value="1" {if $sexe eq 1} selected {/if}>{$lang.masculin}&nbsp;&nbsp;&nbsp;</option>
</select>
<br><br><input type="submit" />
</form>
</td>