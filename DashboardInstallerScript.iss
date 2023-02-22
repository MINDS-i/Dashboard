#define MyAppName "MINDS-i Dashboard"
#define MyAppVersion "1.5.2"
#define MyAppPublisher "MINDS-i Education"
#define MyAppURL "https://mindsieducation.com/"
#define MyAppExeName "Dashboard.exe"
#define TelemDrivers "RadioDiversv2.12.06WHQL_Centified.exe"
#define ArduinoCLI "arduino-cli.exe"
#define ReleaseDir "C:\Archives\Working Directory\MINDS-i\_Release Builds"

[Setup]
AppId={{C4B2ECC1-960A-4137-BFD2-23CD33DBC5B1}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
OutputDir=C:\Archives\Working Directory\MINDS-i\_Release Builds\_Installers
OutputBaseFilename=DashboardSetup
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}";

[Files]
Source: "{#ReleaseDir}\Dashboard.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#ReleaseDir}\DashBoard.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#ReleaseDir}\RadioDiversv2.12.06WHQL_Centified.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#ReleaseDir}\resources\*"; DestDir: "{app}\resources"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#ReleaseDir}\arduino-cli.exe"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[InstallDelete]                                   
Type: files; Name: "{localappdata}\MINDS-i Dashboard\persist.properties"

[Run]
Filename: "{app}\{#TelemDrivers}"; Description: "{cm:LaunchProgram, Radio Telemetry Drivers}"; Flags: hidewizard
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent