"""Build any supported game/loader with pinned, locally downloaded SDKs. Python 3.11+, JDK 25."""
from pathlib import Path
import argparse,hashlib,io,json,os,shutil,subprocess,sys,urllib.request,zipfile
p=argparse.ArgumentParser();p.add_argument('--version',default='26.2');p.add_argument('--loader',choices=['fabric','forge'],default='fabric');p.add_argument('--java',default='java');p.add_argument('--cache',default='build/sdk');a=p.parse_args()
repo=Path(__file__).resolve().parents[1];cache=Path(a.cache).resolve();cache.mkdir(parents=True,exist_ok=True)
row=next(r for r in json.loads((repo/'tools/versions.lock.json').read_text()) if r['minecraft']==a.version)
if a.loader=='forge' and not row['forge']:raise SystemExit('No official Forge build for '+a.version)
base=cache/a.version;base.mkdir(exist_ok=True);cp=base/'classpath';deps=cache/'tools-classpath';bin=cache/'tools-bin';out=repo/'build/release'/(a.version+'-'+a.loader)
out.mkdir(parents=True,exist_ok=True)
def download(url,file,sha=None,algorithm='sha1'):
 file.parent.mkdir(parents=True,exist_ok=True)
 if file.exists() and (not sha or hashlib.new(algorithm,file.read_bytes()).hexdigest()==sha):return file
 print('Download',file.name,flush=True)
 with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'MonkeyClientBuild/0.6'}),timeout=90) as r:data=r.read()
 if sha and hashlib.new(algorithm,data).hexdigest()!=sha:raise ValueError('Checksum failed: '+url)
 file.write_bytes(data);return file
def extract(file,target):
 with zipfile.ZipFile(file) as z:
  for name in z.namelist():
   if name.endswith('.class') and not name.startswith('META-INF/versions/') or name=='version.json':
    dest=target/name;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(name))
   elif name.startswith(('META-INF/jars/','META-INF/jarjar/')) and name.endswith('.jar'):extract(io.BytesIO(z.read(name)),target)
def run(args,log):
 print(log.name,flush=True)
 with log.open('w',encoding='utf-8') as f:r=subprocess.run(list(map(str,args)),stdout=f,stderr=subprocess.STDOUT)
 if r.returncode:raise RuntimeError(log.read_text(encoding='utf-8')[-8000:])
def jtool(name,args,log):run([a.java,'-Xmx3g','-cp',str(bin)+os.pathsep+str(deps),name,*args],log)
coordinates=[('net.fabricmc','fabric-loader','0.19.5'),('net.fabricmc','sponge-mixin','0.17.4+mixin.0.8.7'),('net.fabricmc','tiny-remapper','0.14.0'),('net.fabricmc','mapping-io','0.8.0'),('io.github.llamalad7','mixinextras-common','0.5.5'),('org.jetbrains','annotations','26.1.0')]+[('org.ow2.asm',name,'9.10.1') for name in ['asm','asm-tree','asm-commons','asm-util','asm-analysis']]+[('com.google.code.gson','gson','2.13.2')]
for group,name,version in coordinates:
 rel=group.replace('.','/')+'/'+name+'/'+version+'/'+name+'-'+version+'.jar';host='https://maven.fabricmc.net/' if group=='net.fabricmc' else 'https://repo.maven.apache.org/maven2/'
 jar=download(host+rel,cache/'libraries'/rel);extract(jar,deps);extract(jar,cp)
# javac's usual ZIP file manager is unavailable in some Windows sandboxes.
# CompileDriver reads exploded classes and uses the standard compiler backend.
driver=repo/'tools/java/CompileDriver.java';run([a.java,driver,deps,repo/'tools/java',bin],out/'build-tools.log')
meta=row['metadata']
for lib in meta['libraries']:
 artifact=lib.get('downloads',{}).get('artifact')
 if artifact and ':natives-' not in lib['name']:
  jar=download(artifact['url'],cache/'libraries'/artifact['path'],artifact.get('sha1'));extract(jar,cp)
artifact=meta['downloads']['client'];client=download(artifact['url'],base/'client.jar',artifact['sha1']);extract(client,cp)
for name in ['fabric-api','sodium']:
 artifact=row[name];jar=download(artifact['url'],base/(name+'.jar'),artifact['sha512'],'sha512');extract(jar,cp)
classpath=[cp]
if a.version.startswith('1.'):
 m=meta['downloads']['client_mappings'];download(m['url'],base/'client-mappings.txt',m['sha1']);download('https://maven.fabricmc.net/net/fabricmc/intermediary/'+a.version+'/intermediary-'+a.version+'-v2.jar',base/'intermediary.jar')
 run([sys.executable,repo/'tools/prepare-mappings.py',base],out/'mappings.log');extract(client,base/'official')
 for name in ['fabric-api','sodium']:extract(base/(name+'.jar'),base/'api-intermediary')
 for src,dst,inp,target,roots in [('official','named','official','named',[cp]),('official','intermediary','official','intermediary',[cp]),('intermediary','named','api-intermediary','api-named',[base/'intermediary',cp])]:
  jtool('RemapDriver',[base/'mappings.tiny',src,dst,base/inp,base/target,';'.join(map(str,roots))],out/('remap-'+target+'.log'))
 classpath=[base/'named',base/'api-named',cp]
if a.loader=='forge':
 forgeRoot=cache/'forge';coordinate=a.version+'-'+row['forge'];id=a.version+'-forge-'+row['forge'];game=forgeRoot/'versions'/a.version;game.mkdir(parents=True,exist_ok=True)
 shutil.copyfile(client,game/(a.version+'.jar'));(game/(a.version+'.json')).write_text(json.dumps(meta));(forgeRoot/'launcher_profiles.json').write_text('{"profiles":{},"version":3}')
 installer=download('https://maven.minecraftforge.net/net/minecraftforge/forge/'+coordinate+'/forge-'+coordinate+'-installer.jar',base/'forge-installer.jar')
 run([a.java,'-Djava.awt.headless=true','-jar',installer,'--installClient',forgeRoot],out/'forge-install.log')
 sdk=base/'forge-sdk'
 for component in ['fmlcore','fmlloader','javafmllanguage','forge']:
  for jar in (forgeRoot/'libraries/net/minecraftforge'/component/coordinate).glob('*.jar'):
   if not any(s in jar.name for s in ['installer','sources']):extract(jar,sdk)
 classpath=[sdk]+classpath
# Fresh generated outputs prevent a removed mixin from leaking into a release.
for folder in ['source','compiled','resources','runtime']:
 target=(out/folder).resolve()
 if not target.is_relative_to((repo/'build/release').resolve()):raise ValueError('Unsafe build output')
 if target.exists():shutil.rmtree(target)
for kind in ['sources','resources']:run([sys.executable,repo/('tools/port-'+kind+'.py'),a.version,out/('source' if kind=='sources' else 'resources'),a.loader],out/(kind+'.log'))
joined=';'.join(map(str,classpath));jtool('CompileDriver',[joined,out/'source',out/'compiled',21 if a.loader=='forge' else row['java']],out/'compile.log')
jtool('MixinAudit',[out/'compiled',joined,out/'resources'],out/'audit.log');classes=out/'compiled'
if a.loader=='fabric' and a.version.startswith('1.'):
 classes=out/'runtime';jtool('RemapDriver',[base/'mappings.tiny','named','intermediary',out/'compiled',classes,joined,'mixins'],out/'remap-client.log')
 jtool('MixinAudit',[classes,';'.join(map(str,[base/'intermediary',base/'api-intermediary',cp])),out/'resources'],out/'runtime-audit.log')
if a.loader=='fabric':
 dest=out/'resources/META-INF/jars/sodium.jar';dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(base/'sodium.jar',dest)
jar=out/('monkeyclient-0.6.0+'+a.version+'-'+a.loader+'.jar')
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
 for folder in [classes,out/'resources']:
  for file in sorted(folder.rglob('*')):
   if file.is_file():z.write(file,file.relative_to(folder).as_posix())
 for file in repo.glob('*LICENSE*'):z.write(file,file.name)
 z.write(repo/'THIRD-PARTY-NOTICES.md','THIRD-PARTY-NOTICES.md')
print('Built',jar,'SHA256',hashlib.sha256(jar.read_bytes()).hexdigest())
