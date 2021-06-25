#define MyAppName "MINDS-i Dashboard"
#define MyAppVersion "1.0.2"
#define MyAppPublisher "MINDS-i Education"
#define MyAppURL "https://mindsieducation.com/"
#define MyAppExeName "Dashboard.exe"
#define TelemDrivers "RadioDiversv2.12.06WHQL_Centified.exe"

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
OutputDir=C:\Archives\Working Directory\MINDS-i\Dashboard Installer
OutputBaseFilename=DashboardSetup
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}";

[Files]
Source: "C:\Archives\Working Directory\MINDS-i\Dashboard Release\Dashboard.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Archives\Working Directory\MINDS-i\Dashboard Release\DashBoard.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Archives\Working Directory\MINDS-i\Dashboard Release\RadioDiversv2.12.06WHQL_Centified.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Archives\Working Directory\MINDS-i\Dashboard Release\resources\*"; DestDir: "{app}\resources"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#TelemDrivers}"; Description: "{cm:LaunchProgram, Radio Telemetry Drivers}"; Flags: hidewizard
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent