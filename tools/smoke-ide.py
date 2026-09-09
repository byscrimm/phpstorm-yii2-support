#!/usr/bin/env python3
"""Load the built plugin in an isolated headless PhpStorm profile and run real PSI checks."""
import argparse,json,os,pathlib,shutil,subprocess,zipfile
root=pathlib.Path(__file__).resolve().parent.parent
p=argparse.ArgumentParser();p.add_argument('--ide',default=os.environ.get('PHPSTORM_HOME','/Applications/PhpStorm.app/Contents'));args=p.parse_args()
ide=pathlib.Path(args.ide).resolve()
if (ide/'Contents').is_dir():ide/='Contents'
info=json.loads(next(x for x in [ide/'Resources/product-info.json',ide/'product-info.json'] if x.exists()).read_text())
java=next(x for x in [ide/'jbr/Contents/Home/bin',ide/'jbr/bin'] if (x/'javac').exists())
build=root/'build/ide-smoke';build.mkdir(parents=True,exist_ok=True)
classes=build/'classes';classes.mkdir(exist_ok=True)
# Runtime boot paths are provided by this installed IDE; compilation uses the same SDK.
jars=[x for x in list((ide/'lib').rglob('*.jar'))+list((ide/'plugins').rglob('*.jar')) if not x.name.endswith(('-src.jar','-sources.jar'))]
plugin=root/'build/offline'/('yii2-insight-'+dict(line.split('=',1) for line in (root/'gradle.properties').read_text().splitlines() if '=' in line)['pluginVersion']+'.jar')
cp=os.pathsep.join(map(str,[plugin]+jars))
argv=['-sourcepath','','--release','25','-encoding','UTF-8','-classpath',cp,'-d',str(classes)]+list(map(str,(root/'integration').rglob('*.java')))
argfile=build/'javac.args';argfile.write_text('\n'.join('"'+x.replace('\\','\\\\').replace('"','\\"')+'"' for x in argv))
subprocess.run([str(java/'javac'),'@'+str(argfile)],check=True)
plugins=build/'plugins'
for name in ['yii2-insight','yii2-test']:(plugins/name/'lib').mkdir(parents=True,exist_ok=True)
shutil.copy2(plugin,plugins/'yii2-insight/lib'/plugin.name)
with zipfile.ZipFile(plugins/'yii2-test/lib/smoke.jar','w') as z:
 for x in classes.rglob('*.class'):z.write(x,x.relative_to(classes))
 z.writestr('META-INF/plugin.xml','''<idea-plugin><id>yii2.test</id><name>Yii2 isolated tests</name><version>1</version><vendor>Local tests</vendor><depends>com.intellij.modules.platform</depends><depends>com.jetbrains.php</depends><depends>io.github.byscrimm.yii2insight</depends><extensions defaultExtensionNs="com.intellij"><appStarter id="yii2-smoke" implementation="io.github.byscrimm.yii2insight.IdeSmokeTest"/></extensions></idea-plugin>''')
launch=info['launch'][0]
boot=os.pathsep.join(str(ide/'lib'/x) for x in launch['bootClassPathJarNames'])
vm=[x.replace('$APP_PACKAGE',str(ide.parent)) for x in launch.get('additionalJvmArguments',[]) if not x.startswith('-Didea.paths.selector=')]
cmd=[str(java/'java'),'-Xmx2g','-Djava.awt.headless=true','-Didea.is.internal=true','-Didea.home.path='+str(ide),'-Didea.platform.prefix=PhpStorm','-Didea.config.path='+str(build/'config'),'-Didea.system.path='+str(build/'system'),'-Didea.log.path='+str(build/'log'),'-Didea.plugins.path='+str(plugins),'-Didea.initially.ask.config=never','-Didea.trust.all.projects=true','-Dide.no.platform.update=true','-Didea.privacy.policy.text=<!--999.999-->','-Djb.consents.confirmation.enabled=false']+vm+['-cp',boot,'com.intellij.idea.Main','yii2-smoke']
result=subprocess.run(cmd,cwd=root,timeout=180)
raise SystemExit(result.returncode)
