<td align="left" valign="top">
<h2>{$lang.nouvelles}</h2>
<div style="width:90%;align:center" class="hr"><p>
{section name=nouvelles loop=$nouvelle}
	<div class="nouvelleEntete">
		<span class="titrenouvelle">{$nouvelle[nouvelles].titre}</span><br>
		<span class="dateNouvelle">{$nouvelle[nouvelles].date}</span><br>
	</div>
	<div style="width:90%" class="nouvelle">
	{if $nouvelle[nouvelles].image neq " "}
		<img alt="" src="{$nouvelle[nouvelles].image}" border="0" hspace="15" align="left">
	{/if}
	<pre style="min-height:100px;width:90%" class="textenouvelle">{$nouvelle[nouvelles].nouvelle}</pre>
	<!--<span class="textenouvelle">{$nouvelle[nouvelles].nouvelle}</span>-->
	<hr>
	<br>
	</div>
{/section}
</div>
</td>

