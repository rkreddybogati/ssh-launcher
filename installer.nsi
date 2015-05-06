# Name of the resulting file
OutFile "Scalr-SSH-Launcher-Installer.exe"

SetCompressor /SOLID lzma
ShowInstDetails show
ShowUninstDetails show
SetDateSave on

# Imports
!include "WordFunc.nsh"
!insertmacro WordFind
!insertmacro un.WordFind

# set desktop as install directory
InstallDir "$PROGRAMFILES\Scalr SSH Launcher"

# default section start
Section
    SetOutPath $INSTDIR

    # App
    File build/launch4j/ssh-launcher.exe

    # Uninstaller
    WriteUninstaller $INSTDIR\uninstaller.exe

    # Registry
    Push "scalr+bar"
    Call RegisterURIHandler

SectionEnd

# create a section to define what the uninstaller does.
# the section will always be named "Uninstall"
Section "Uninstall"

    # Always delete uninstaller first  # TODO - Why?
    Delete $INSTDIR\uninstaller.exe

    # Remove protocol handler
    Push "scalr+bar"
    Call un.UnregisterURIHandler

    # now delete installed file
    Delete $INSTDIR\ssh-launcher.exe

    # Finally, delete install directory
    RMDir "$INSTDIR"

SectionEnd



# From: http://nsis.sourceforge.net/Pidgin

;--------------------------------
;Functions
 
; Default the URI handler checkboxes if Scalr is the current handler or if there is no handler
 
; Check if Scalr is the current handler
; Returns a boolean on the stack
!macro CheckIfScalrIsCurrentURIHandlerMacro UN
Function ${UN}CheckIfScalrIsCurrentURIHandler
  Exch $R0
  ClearErrors
 
  ReadRegStr $R0 HKCR "$R0\shell\Open\command" ""
  IfErrors 0 +3
    IntOp $R0 0 + 0
    Goto done
 
  !ifdef __UNINSTALL__
  ${un.WordFind} "$R0" "ssh-launcher.exe" "E+1{" $R0
  !else
  ${WordFind} "$R0" "ssh-launcher.exe" "E+1{" $R0
  !endif
  IntOp $R0 0 + 1
  IfErrors 0 +2
    IntOp $R0 0 + 0
 
  done:
  Exch $R0
FunctionEnd
!macroend
!insertmacro CheckIfScalrIsCurrentURIHandlerMacro ""
!insertmacro CheckIfScalrIsCurrentURIHandlerMacro "un."
 
; If Scalr is the current URI handler for the specified protocol, remove it.
Function un.UnregisterURIHandler
  Exch $R0
  Push $R1
 
  Push $R0
  Call un.CheckIfScalrIsCurrentURIHandler
  Pop $R1
 
  ; If Scalr isn't the current handler, leave it as-is
  IntCmp $R1 0 done
 
  ;Unregister the URI handler
  DetailPrint "Unregistering $R0 URI Handler"
  DeleteRegKey HKCR "$R0"
 
  done:
  Pop $R1
  Pop $R0
FunctionEnd
 
Function RegisterURIHandler
  Exch $R0
  DetailPrint "Registering $R0 URI Handler"
  DeleteRegKey HKCR "$R0"
  WriteRegStr HKCR "$R0" "" "URL:$R0"
  WriteRegStr HKCR "$R0" "URL Protocol" ""
  WriteRegStr HKCR "$R0\DefaultIcon" "" "$INSTDIR\ssh-launcher.exe"
  WriteRegStr HKCR "$R0\shell" "" ""
  WriteRegStr HKCR "$R0\shell\Open" "" ""
  WriteRegStr HKCR "$R0\shell\Open\command" "" '$INSTDIR\ssh-launcher.exe "%1"'
  Pop $R0
FunctionEnd
 
