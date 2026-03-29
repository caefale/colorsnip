Packaging Steps:
1. Run the command: `mvn clean javafx:jlink`
2. Paste the JNativeHook DLL into \target\colorsnip-image\bin\, make sure it's called 'JNativeHook.dll' (can rename)
3. Create a .vbs file, and copy this inside:
```
Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

scriptPath = fso.GetParentFolderName(WScript.ScriptFullName)

shell.Run "cmd /c cd /d """ & scriptPath & """ && colorsnip.bat", 0, False

Set shell = Nothing
Set fso = Nothing
```