<html>
<body>
<table cellspacing="10">
{
for $m in //measurementsclear/measurement
order by $m/@tvsid, $m/@timeofday
return <tr>
<td> {data($m/@timeofday)}</td>
<td> {data($m/@tvsid)} </td>
<td> {data($m/@delay)}</td>
<td> {data($m/@correction)}</td>
<td> {data($m/@uncertainty)}</td>
<td> {data($m/@tblind)}</td>
<td> {fn:round(data($m/@speed)*0.036,1)}</td>
</tr>
}
</table>
</body>
</html>
