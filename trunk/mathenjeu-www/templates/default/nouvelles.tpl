<td align="left" valign="top">
<h2>{$lang.nouvelles}</h2>
<div style="width:90%;align:center" class="hr"><p>
{section name=nouvelles loop=$nouvelle}
	<table>
		<tr>
			<td class="titrenouvelle">{$nouvelle[nouvelles].titre}</td>
		</tr>
		<tr>
			<td class="datenouvelle">{$nouvelle[nouvelles].date}</td>
		</tr>
		<tr>
			<td colspan="2" class="textenouvelle">
				{if $nouvelle[nouvelles].image neq " "}
					<img alt="" src="{$nouvelle[nouvelles].image}" border="0" hspace="15" align="left">
				{/if}
				{$nouvelle[nouvelles].nouvelle}
			</td>
		
		</tr>
	</table>
	<hr>
	<br>
{/section}
</div>
</td>

