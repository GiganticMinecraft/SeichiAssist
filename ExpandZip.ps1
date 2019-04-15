# for PS v3
if( $PSVersionTable.PSVersion.Major -ge 3 ){
    echo "Data from `$PSScriptRoot"
    $ScriptDir = $PSScriptRoot
}
# for PS v2
else{
    echo "Data from `$MyInvocation.MyCommand.Path"
    $ScriptDir = Split-Path $MyInvocation.MyCommand.Path -Parent
}


# search and expand zip archives
$basedir = $ScriptDir;
 
Write-Host "Search zip files..."
$zipfiles = Get-ChildItem $basedir -Recurse | Where-Object {$_.Extension -eq ".zip"}
 
Write-Host "Expand zip files..."
foreach ($item in $zipfiles){
    Write-Host $item.Name -ForegroundColor Green
    # create directory
    $destination = $item.FullName;
    $destination = $destination.Substring(0, $destination.Length - ($item.Extension).Length);
    $buffer = New-Item -Path $destination -ItemType Directory
 
    # expand into created directory
    Expand-Archive -Path $item.FullName -DestinationPath $destination
}