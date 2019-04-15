[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls11

[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true}
# for PS v3
if( $PSVersionTable.PSVersion.Major -ge 3 ){
    $ScriptDir = $PSScriptRoot
}
# for PS v2
else{
    $ScriptDir = Split-Path $MyInvocation.MyCommand.Path -Parent
}

Write-Host "http://dl.inventivetalent.org/download/?file=plugin/ParticleAPI_v2.1.1.jar より ParticleAPI_v2.1.1.jar をダウンロードします。"
$filename = $ScriptDir + "\\libraries\\ParticleAPI_v2.1.1.jar";
$client = new-object System.Net.WebClient
$downloaddir = New-Object System.Uri("http://dl.inventivetalent.org/download/?file=plugin/ParticleAPI_v2.1.1.jar")
$client.DownloadFile($downloaddir, $filename)
Write-Host "完了。"