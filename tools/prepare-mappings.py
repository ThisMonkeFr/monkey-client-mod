from pathlib import Path
import re,zipfile,sys
root=Path(sys.argv[1])
lines=(root/'client-mappings.txt').read_text().splitlines();classes={};members={}
for line in lines:
 if line and not line.startswith((' ','#')):
  named,obf=line[:-1].split(' -> ');classes[named.replace('.','/')]=obf.replace('.','/')
def desc(name):
 n=0
 while name.endswith('[]'):n+=1;name=name[:-2]
 prim={'void':'V','boolean':'Z','byte':'B','char':'C','short':'S','int':'I','long':'J','float':'F','double':'D'}
 return '['*n+(prim[name] if name in prim else 'L'+classes.get(name.replace('.','/'),name.replace('.','/'))+';')
owner=None
for line in lines:
 if line.startswith('#') or not line:continue
 if not line.startswith(' '):owner=classes[line[:-1].split(' -> ')[0].replace('.','/')];continue
 data,obf=line.strip().split(' -> ');data=re.sub(r'^\d+:\d+:','',data);data=re.sub(r':\d+(?::\d+)?$','',data)
 typ,entry=data.split(' ',1)
 if '(' in entry:
  name,args=entry.split('(',1);sig='('+''.join(desc(t) for t in args[:-1].split(',') if t)+')'+desc(typ);kind='m'
 else:name=entry;sig=desc(typ);kind='f'
 if '.' in name:continue
 members[(owner,kind,obf,sig)]=name
with zipfile.ZipFile(root/'intermediary.jar') as jar:tiny=jar.read('mappings/mappings.tiny').decode().splitlines()
ic={};im={};owner=None
for line in tiny[1:]:
 p=line.split('\t')
 if p[0]=='c':owner=p[1];ic[owner]=p[2]
 elif len(p)>4 and p[1] in ('m','f'):im[(owner,p[1],p[3],p[2])]=p[4]
out=['tiny\t2\t0\tofficial\tnamed\tintermediary'];by={}
for key,name in members.items():by.setdefault(key[0],[]).append((key,name))
for named,obf in classes.items():
 out.append('\t'.join(['c',obf,named,ic.get(obf,obf)]))
 for key,name in by.get(obf,[]):out.append('\t'.join(['',key[1],key[3],key[2],name,im.get(key,'')]))
(root/'mappings.tiny').write_text('\n'.join(out)+'\n',encoding='utf-8')
print(len(classes),'classes',len(members),'members mapped for',sys.argv[1])
