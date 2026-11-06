@echo off
setlocal enabledelayedexpansion

set CLASSPATH=target\classes

for /r "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot-starter-web\3.2.0" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot-starter\3.2.0" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot\3.2.0" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot-autoconfigure\3.2.0" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-webmvc\6.1.1" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-web\6.1.1" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-context\6.1.1" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-core\6.1.1" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-beans\6.1.1" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-expression\6.1.1" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot-starter-data-jpa\3.2.0" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-orm\6.1.1" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-tx\6.1.1" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\hibernate\orm\hibernate-core\6.4.1.Final" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\com\mysql\mysql-connector-j\8.2.0" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot-starter-security\3.2.0" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\io\jsonwebtoken\jjwt-api\0.12.3" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\io\jsonwebtoken\jjwt-impl\0.12.3" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\io\jsonwebtoken\jjwt-jackson\0.12.3" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\org\projectlombok\lombok\1.18.30" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.15.3" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.15.3" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f
for /r "%USERPROFILE%\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.15.3" %%f in (*.jar) do set CLASSPATH=!CLASSPATH!;%%f

java -cp "%CLASSPATH%" com.example.shopping.ShoppingApplication
