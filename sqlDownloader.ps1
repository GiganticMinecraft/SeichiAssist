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

Write-Host "https://red.minecraftserver.jp/attachments/download/895/gachadata.sql より gachadata.sql をダウンロードします。"
$filename = $ScriptDir + "\\gachadata.sql";
$client = new-object System.Net.WebClient
$downloaddir = New-Object System.Uri("https://red.minecraftserver.jp/attachments/download/895/gachadata.sql")
$client.DownloadFile($downloaddir, $filename)
Write-Host "完了。"
Write-Host "https://red.minecraftserver.jp/attachments/download/894/msgachadata.sql より msgachadata.sql をダウンロードします。"
$msfilename = $ScriptDir + "\\msgachadata.sql";
$msdownloaddir = New-Object System.Uri("https://red.minecraftserver.jp/attachments/download/895/gachadata.sql")
$client.DownloadFile($msdownloaddir, $msfilename)
Write-Host "完了。"