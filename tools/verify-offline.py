#!/usr/bin/env python3
"""Check plugin JVM member references against a local SDK. Not a replacement for Plugin Verifier or IDE tests."""
import argparse, pathlib, zipfile, struct, functools, xml.etree.ElementTree as ET
p=argparse.ArgumentParser();p.add_argument('--ide',default='/Applications/PhpStorm.app/Contents');args=p.parse_args()
root=pathlib.Path(__file__).resolve().parent.parent;ide=pathlib.Path(args.ide)
if (ide/'Contents').is_dir():ide/='Contents'
artifact=root/'build/offline'/('yii2-insight-'+dict(line.split('=',1) for line in (root/'gradle.properties').read_text().splitlines() if '=' in line)['pluginVersion']+'.jar')
class Reader:
 def __init__(self,b):self.b=b;self.i=0
 def take(self,n):b=self.b[self.i:self.i+n];self.i+=n;return b
 def u1(self):return self.take(1)[0]
 def u2(self):return struct.unpack('>H',self.take(2))[0]
 def u4(self):return struct.unpack('>I',self.take(4))[0]
def parse(b):
 r=Reader(b)
 if r.u4()!=0xcafebabe:raise ValueError('Invalid class')
 minor=r.u2();major=r.u2();cp=[None]*r.u2();i=1
 while i<len(cp):
  tag=r.u1()
  if tag==1:cp[i]=(tag,r.take(r.u2()).decode('utf-8','replace'))
  elif tag in (3,4):r.take(4)
  elif tag in (5,6):r.take(8);i+=1
  elif tag in (7,8,16,19,20):cp[i]=(tag,r.u2())
  elif tag in (9,10,11,12,17,18):cp[i]=(tag,r.u2(),r.u2())
  elif tag==15:cp[i]=(tag,r.u1(),r.u2())
  else:raise ValueError(tag)
  i+=1
 def string(n):return cp[n][1]
 def cls(n):return string(cp[n][1]) if n else None
 access=r.u2();name=cls(r.u2());parent=cls(r.u2());parents=[parent] if parent else []
 parents += [cls(r.u2()) for _ in range(r.u2())]
 def attrs():
  for _ in range(r.u2()):r.u2();r.take(r.u4())
 members=[]
 for kind in ('field','method'):
  found=set()
  for _ in range(r.u2()):
   r.u2();found.add((string(r.u2()),string(r.u2())));attrs()
  members.append(found)
 refs=[];classes=set()
 for item in cp:
  if not item:continue
  if item[0]==7:classes.add(string(item[1]))
  elif item[0] in (9,10,11):
   pair=cp[item[2]];refs.append((cls(item[1]),string(pair[1]),string(pair[2]),0 if item[0]==9 else 1))
 return dict(name=name,parents=parents,members=members,refs=refs,classes=classes,major=major)
archives=[];index={}
for path in [artifact]+sorted((ide/'lib').rglob('*.jar'))+sorted((ide/'plugins').rglob('*.jar')):
 if path.name.endswith(('-src.jar','-sources.jar')):continue
 z=zipfile.ZipFile(path);archives.append(z)
 for name in z.namelist():
  if name.endswith('.class') and not name.startswith('META-INF/'):index.setdefault(name[:-6],(z,name))
@functools.lru_cache(None)
def get(name):
 item=index.get(name)
 return parse(item[0].read(item[1])) if item else None
platform_prefixes=('java/','javax/','jdk/','sun/','com/sun/','org/w3c/','org/xml/')
def jdk(name):return name.startswith(platform_prefixes)
def exists(owner,name,desc,kind,seen=None):
 if owner.startswith('[') or jdk(owner):return True
 seen=set() if seen is None else seen
 if owner in seen:return False
 seen.add(owner);target=get(owner)
 if target is None:return False
 if (name,desc) in target['members'][kind]:return True
 if name=='<init>':return False
 # JDK Object methods commonly inherited by every SDK type.
 if owner=='java/lang/Object':return True
 for base in target['parents']:
  if base=='java/lang/Object':
   if name in ('toString','equals','hashCode','getClass','clone','finalize','wait','notify','notifyAll'):return True
  elif exists(base,name,desc,kind,seen):return True
 return False
issues=set();count=0
with zipfile.ZipFile(artifact) as z:
 for name in z.namelist():
  if not name.endswith('.class'):continue
  data=parse(z.read(name));count+=1
  if data['major']!=69:issues.add(f'{name}: expected Java 25 class, got {data["major"]}')
  for cls in data['classes']:
   if not cls.startswith('[') and not jdk(cls) and cls not in index:issues.add(f'{name}: missing class {cls}')
  for owner,member,desc,kind in data['refs']:
   if not exists(owner,member,desc,kind):issues.add(f'{name}: missing {owner}.{member}{desc}')
 descriptor=ET.fromstring(z.read('META-INF/plugin.xml'))
 for node in descriptor.iter():
  for attr in ('implementation','implementationClass','instance','serviceImplementation','factoryClass','class'):
   value=node.get(attr)
   if value and value.startswith('io.github.byscrimm.yii2insight.') and value.replace('.','/')+'.class' not in z.namelist():issues.add('Descriptor class missing: '+value)
 if descriptor.findtext('id')!='io.github.byscrimm.yii2insight':issues.add('Plugin ID changed unexpectedly')
 if descriptor.find('idea-version').attrib!={'since-build':'262','until-build':'262.*'}:issues.add('Wrong compatibility range')
for issue in sorted(issues):print(issue)
print(f'{"FAIL" if issues else "PASS"}: {count} plugin classes; JVM class/member references and descriptor checked against {len(archives)-1} SDK jars')
for z in archives:z.close()
raise SystemExit(bool(issues))
