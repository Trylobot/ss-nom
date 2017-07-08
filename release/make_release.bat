
FOR /F "tokens=* USEBACKQ" %%F IN (`json -f ..\mod_info.json version`) DO (
SET version=%%F
)

echo mod_info.version: %version%

del /F *.zip
RMDIR /S /Q  ss-nom

MKDIR        ss-nom
XCOPY /S /I  ..\data            ss-nom\data
XCOPY /S /I  ..\graphics        ss-nom\graphics
XCOPY /S /I  ..\jars            ss-nom\jars
XCOPY /S /I  ..\sounds          ss-nom\sounds
XCOPY /S /I  ..\src             ss-nom\src
COPY         ..\mod_info.json   ss-nom
COPY         ..\nomads.version  ss-nom

7z.exe a     ss-nom-%version%.zip  ss-nom 

pause

