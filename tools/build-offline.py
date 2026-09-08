#!/usr/bin/env python3
"""Build against an installed PhpStorm SDK. No downloads or IDE installation changes."""
import argparse, hashlib, json, os, pathlib, shutil, subprocess, sys, zipfile
root = pathlib.Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser()
parser.add_argument('--ide', default=os.environ.get('PHPSTORM_HOME', '/Applications/PhpStorm.app/Contents'))
parser.add_argument('--java-home', default=os.environ.get('JAVA_HOME'))
parser.add_argument('--test', action='store_true')
parser.add_argument('--psi-test', action='store_true')
args = parser.parse_args()
ide = pathlib.Path(args.ide).resolve()
if (ide/'Contents').is_dir(): ide /= 'Contents'
info_path = next((p for p in [ide/'Resources/product-info.json',ide/'product-info.json'] if p.exists()),None)
if not info_path: raise SystemExit('Point --ide to an installed PhpStorm 2026.2 distribution.')
info=json.loads(info_path.read_text())
if not info['buildNumber'].startswith('262.'): raise SystemExit('This build targets PhpStorm 2026.2 (262.*).')
java_home=pathlib.Path(args.java_home) if args.java_home else next((p for p in [ide/'jbr/Contents/Home',ide/'jbr'] if (p/'bin/javac').exists()),None)
if java_home is None: raise SystemExit('Set JAVA_HOME to a JDK 25 installation.')
version=subprocess.check_output([str(java_home/'bin/java'),'-version'],stderr=subprocess.STDOUT,text=True)
if '"25.' not in version: raise SystemExit('Java 25 is required.')
# Platform and declared bundled plugin dependencies only, to catch accidental undeclared API usage.
jars=sorted((ide/'lib').rglob('*.jar'))
for name in ['php','php-impl','DatabaseTools','grid-core-plugin','php-remoteInterpreter','phpstorm-remote-interpreter','twig','terminal']:
 plugin=ide/'plugins'/name
 if plugin.is_dir(): jars += sorted(plugin.rglob('*.jar'))
jars=[p for p in jars if not p.name.endswith(('-src.jar','-sources.jar'))]
build=root/'build/offline'
classes=build/'classes'
if classes.exists(): shutil.rmtree(classes)
classes.mkdir(parents=True)
cp=os.pathsep.join(map(str,jars))
def compile_sources(sources,destination,classpath):
 argv=['-sourcepath','','-encoding','UTF-8','--release','25','-d',str(destination),'-classpath',classpath]+[str(p) for p in sources]
 argfile=build/'javac.args'
 argfile.write_text('\n'.join('"'+s.replace('\\','\\\\').replace('"','\\"')+'"' for s in argv))
 subprocess.run([str(java_home/'bin/javac'),'@'+str(argfile)],check=True,cwd=root)
compile_sources(sorted((root/'src').rglob('*.java')),classes,cp)
props=dict(line.split('=',1) for line in (root/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
version=props['pluginVersion']
jar=build/f'yii2-support-extended-{version}.jar'
def zip_entry(name):
 entry=zipfile.ZipInfo(name,(1980,1,1,0,0,0));entry.compress_type=zipfile.ZIP_DEFLATED;entry.external_attr=0o100644<<16;return entry
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
 for p in sorted(classes.rglob('*.class')): z.writestr(zip_entry(p.relative_to(classes).as_posix()),p.read_bytes())
 for p in sorted((root/'resources').rglob('*')):
  if not p.is_file(): continue
  rel=p.relative_to(root/'resources').as_posix()
  data=p.read_bytes()
  if rel=='META-INF/plugin.xml':
   text=data.decode().replace('<idea-plugin>',f'<idea-plugin>\n    <version>{version}</version>\n    <idea-version since-build="262" until-build="262.*"/>',1)
   data=text.encode()
  z.writestr(zip_entry(rel),data)
if args.test:
 test_classes=build/'test-classes';test_classes.mkdir(exist_ok=True)
 compile_sources(sorted((root/'tests').rglob('*.java')),test_classes,os.pathsep.join([str(classes),cp]))
 subprocess.run([str(java_home/'bin/java'),'-ea','-Djava.awt.headless=true','-cp',os.pathsep.join([str(test_classes),str(classes),cp]),'com.nvlad.yii2support.CoreRegressionTest'],check=True)
if args.psi_test:
 subprocess.run([sys.executable,str(root/'tools/test-php-psi.py'),'--ide',str(ide)],check=True,env={**os.environ,'JAVA_HOME':str(java_home)})
dist=root/'build/distributions';dist.mkdir(parents=True,exist_ok=True)
archive=dist/f'yii2-support-extended-{version}.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z: z.writestr(zip_entry('yii2-support-extended/lib/'+jar.name),jar.read_bytes())
sha=hashlib.sha256(archive.read_bytes()).hexdigest()
archive.with_suffix('.zip.sha256').write_text(sha+'  '+archive.name+'\n')
(dist/f'build-info-{version}.json').write_text(json.dumps({'version':version,'sdkVersion':info['version'],'sdkBuild':info['buildNumber'],'sha256':sha,'coreTests':'passed' if args.test else 'not run','phpPsiTests':'passed' if args.psi_test else 'not run','ideUiTests':'not run','jetbrainsPluginVerifier':'not run'},indent=2)+'\n')
print(f'Built {archive}\nSDK: PhpStorm {info["version"]} ({info["buildNumber"]})')
