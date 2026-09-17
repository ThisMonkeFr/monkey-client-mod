from pathlib import Path
import sys,json,shutil
root=Path(__file__).resolve().parents[1];v=sys.argv[1];out=Path(sys.argv[2]).resolve();loader=sys.argv[3] if len(sys.argv)>3 else 'fabric'
for p in (root/'src/main/resources').rglob('*'):
 if p.is_file():
  rel=p.relative_to(root/'src/main/resources')
  if str(rel).replace('\\','/').startswith('META-INF/jars/'):continue
  if loader=='forge' and p.name=='fabric.mod.json':continue
  dest=out/rel;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,dest)
formats={'1.21.9':[69,0],'1.21.10':[69,0],'1.21.11':[75,0],'26.1':[84,0],'26.1.1':[84,0],'26.1.2':[84,0],'26.2':[88,0],'26.3':[97,1]}
(out/'pack.mcmeta').write_text(json.dumps({'pack':{'description':'Monkey Client resources','min_format':formats[v],'max_format':formats[v]}}))
if v=='26.3':
 # RenderPearl compiles GLSL to SPIR-V; stage interfaces need explicit locations.
 for shader in (out/'assets/monkeyclient/shaders/post').glob('*.fsh'):
  text=shader.read_text().replace('#version 330','#version 330\n#extension GL_ARB_separate_shader_objects : require')
  text=text.replace('in vec2 texCoord;', 'layout(location = 0) in vec2 texCoord;').replace('out vec4 fragColor;', 'layout(location = 0) out vec4 fragColor;')
  shader.write_text(text)
for name in ['monkeyclient.mixins.json','monkeycache.mixins.json']:
 p=out/name;j=json.loads(p.read_text());j['compatibilityLevel']='JAVA_21' if loader=='forge' or v.startswith('1.') else 'JAVA_25'
 if v in ['1.21.10','1.21.9']:
  if name=='monkeyclient.mixins.json':j['client']+=['RegionStorageAccessor','OutlineWidthMixin']
  elif loader=='fabric':j['client'].append('sodium06.LegacySodiumOptionsMixin')
 if loader=='forge':
  j['client']=[s for s in j['client'] if not s.startswith('sodium06.')]
  if name=='monkeyclient.mixins.json':j['client']+=['ForgeTickMixin','ForgePayloadCodecMixin','ForgePayloadMixin']
 p.write_text(json.dumps(j,indent=2))
if loader=='fabric':
 p=out/'fabric.mod.json';j=json.loads(p.read_text());j['version']='0.7.0';j['depends'].update(minecraft=v,java='>=21' if v.startswith('1.') else '>=25',fabricloader='>=0.19.5',sodium='*');j['jars']=[{'file':'META-INF/jars/sodium.jar'}]
 if v in ['1.21.10','1.21.9']:j['entrypoints'].pop('sodium:config_api_user',None)
 p.write_text(json.dumps(j,indent=2))
else:
 lib=root/'dependencies/mixinextras-forge-0.5.5.jar'
 dest=out/'META-INF/jarjar';dest.mkdir(parents=True,exist_ok=True);shutil.copyfile(lib,dest/lib.name)
 (dest/'metadata.json').write_text(json.dumps({'jars':[{'identifier':{'group':'io.github.llamalad7','artifact':'mixinextras-forge'},'version':{'range':'[0.5.5,)','artifactVersion':'0.5.5'},'path':'META-INF/jarjar/'+lib.name,'isObfuscated':False}]},indent=2))
 p=out/'META-INF/mods.toml';p.parent.mkdir(parents=True,exist_ok=True)
 p.write_text('''modLoader="javafml"
loaderVersion="[1,)"
license="MIT AND LGPL-3.0-or-later"
[[mods]]
modId="monkeyclient"
version="0.7.0"
displayName="Monkey Client"
authors="ThisMonkeFr"
displayTest="IGNORE_ALL_VERSION"
description='''+"'''Minecraft HUD modules, a vanilla mod menu, saved profiles and quality-of-life tools.'''"+'''
[[dependencies.monkeyclient]]
modId="minecraft"
mandatory=true
versionRange="['''+v+''']"
ordering="NONE"
side="CLIENT"
''')
 (out/'META-INF/MANIFEST.MF').write_text('Manifest-Version: 1.0\nMixinConfigs: monkeyclient.mixins.json,monkeycache.mixins.json\n\n')
print('Prepared resources',v,loader)
