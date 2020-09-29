@echo off

for /f "usebackq tokens=*" %%i in (`vswhere -products * -latest -find **/Auxiliary/Build/vcvarsall.bat`) do (
  set Vcvarsall=%%i
)

echo "%Vcvarsall%"

if exist "%Vcvarsall%" (
  "%Vcvarsall%" amd64
  nmake /F Makefile.nmake clean all
)