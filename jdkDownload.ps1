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

$filename = $ScriptDir + "\\jdk-x32.exe";
$client = new-object System.Net.WebClient
$cookie = "oraclelicense=accept-securebackup-cookie"
$client.Headers.Add([System.Net.HttpRequestHeader]::Cookie, $cookie)
$downloaddir = New-Object System.Uri("https://download.oracle.com/otn-pub/java/jdk/8u201-b09/42970487e3af4f5aa5bca3f542482c60/jdk-8u201-windows-i586.exe")
$client.DownloadFile($downloaddir, $filename)
