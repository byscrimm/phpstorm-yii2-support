#!/usr/bin/env python3
"""Run PHP PSI tests with IntelliJ CoreApplicationEnvironment and the local PhpStorm parser."""
import argparse,os,pathlib,subprocess,zipfile,xml.etree.ElementTree as ET
root=pathlib.Path(__file__).resolve().parent.parent
p=argparse.ArgumentParser();p.add_argument('--ide',default=os.environ.get('PHPSTORM_HOME','/Applications/PhpStorm.app/Contents'));args=p.parse_args()
ide=pathlib.Path(args.ide)
if (ide/'Contents').is_dir():ide/='Contents'
java=pathlib.Path(os.environ['JAVA_HOME'])/'bin' if os.environ.get('JAVA_HOME') else next(p for p in [ide/'jbr/Contents/Home/bin',ide/'jbr/bin'] if (p/'javac').exists())
build=root/'build/php-psi-tests';build.mkdir(parents=True,exist_ok=True)
jars=[p for d in ['lib','plugins/php-impl','plugins/php'] for p in (ide/d).rglob('*.jar') if not p.name.endswith(('-src.jar','-sources.jar'))]
cp=os.pathsep.join(map(str,[root/'build/offline/classes']+jars))
argv=['-sourcepath','','--release','25','-encoding','UTF-8','-cp',cp,'-d',str(build)]+list(map(str,(root/'psi-tests').rglob('*.java')))
argfile=build/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','\\\\').replace('"','\\"')+'"' for s in argv))
subprocess.run([str(java/'javac'),'@'+str(argfile)],check=True)
# Core environment does not load plugin.xml; use the actual SDK's parser registry defaults.
props=[]
for jar in jars:
 if jar.name!='php.jar':continue
 with zipfile.ZipFile(jar) as z:
  descriptor=ET.fromstring(z.read('META-INF/plugin.xml'))
  props=['-D'+e.attrib['key']+'='+e.attrib['defaultValue'] for e in descriptor.iter('registryKey') if e.attrib['key'] in ['php.allowed.parser.advancement.count','php.use.proper.incremental.psi.builder']]
result=subprocess.run([str(java/'java'),'-ea','-Djava.awt.headless=true','-Didea.home.path='+str(ide)]+props+['-cp',str(build)+os.pathsep+cp,'com.nvlad.yii2support.PhpPsiRegressionTest'],cwd=root,timeout=60)
raise SystemExit(result.returncode)
