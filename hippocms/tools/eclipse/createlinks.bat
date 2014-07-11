@ECHO OFF

REM Copyright 2008-2013 Hippo B.V. (http://www.onehippo.com)
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM  http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.

REM This script assumes that it's located in and executed from the
REM hippo-ecm\tools\eclipse directory.

cd ..\..\

cd package\war\src\main\webapp
junction skin ..\..\..\..\skin\src\main\webapp
junction layout ..\..\..\..\layout\src\main\webapp

cd ..\..\..\..\..\quickstart\war\src\main\webapp
junction skin ..\..\..\..\..\package\skin\src\main\webapp
junction layout ..\..\..\..\..\package\layout\src\main\webapp
