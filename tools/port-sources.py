"""Materialize auditable per-version sources from common code and small API adapters."""
from pathlib import Path
import sys,re,shutil
root=Path(__file__).resolve().parents[1]
version=sys.argv[1]
out=Path(sys.argv[2]).resolve()
loader=sys.argv[3] if len(sys.argv)>3 else 'fabric'
if out==root or root/'src'==out: raise ValueError('Output must be a separate build directory')
for p in (root/'src/main/java').rglob('*.java'):
 text=p.read_text(encoding='utf-8-sig');rel=p.relative_to(root/'src/main/java')
 if version in ['1.21.10','1.21.9'] and p.name=='SodiumConfigEntrypoint.java':continue
 if loader=='forge':
  if p.name=='SodiumConfigEntrypoint.java' or 'sodium06' in str(rel):continue
  text=text.replace('net.fabricmc.loader.api.FabricLoader','gg.monkeyclient.platform.PlatformLoader').replace('FabricLoader','PlatformLoader').replace('net.fabricmc.fabric.api.event.Event','gg.monkeyclient.platform.Event').replace('net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource','gg.monkeyclient.platform.ClientCommandSource').replace('FabricClientCommandSource','ClientCommandSource')
  if p.name=='MonkeyClient.java':text=text.replace('import net.fabricmc.api.ClientModInitializer;','').replace('import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;','').replace(' implements ClientModInitializer','').replace('    @Override\n','').replace('ClientTickEvents.END_CLIENT_TICK.register(MonkeyClient::onTick);','').replace('private static void onTick','public static void onTick')
  if p.name=='AppleSkin.java':text=text.replace('import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;','');text=re.sub(r'  if\(!external\)\{.*?\n  \}', '',text,flags=re.S)
 if version.startswith('26.1') or version.startswith('1.21'):
  for old,new in [('net.minecraft.client.gui.Hud','net.minecraft.client.gui.Gui'),('@Mixin(Hud.class)','@Mixin(Gui.class)'),('Hud.getMobEffectSprite','Gui.getMobEffectSprite'),('.gui.hud','.gui'),('.gui.screen()','.screen'),('.gameRenderer.gameRenderState()','.gameRenderer.getGameRenderState()'),('.gameRenderer.mainRenderTarget()','.getMainRenderTarget()'),('.gameRenderer.mainCamera()','.gameRenderer.getMainCamera()'),('.levelExtractor.setSectionDirtyWithNeighbors','.levelRenderer.setSectionDirtyWithNeighbors'),('light,overlay,null,-1,null,outline','light,overlay,null,false,false,-1,null,outline')]:text=text.replace(old,new)
  if str(rel).endswith('mixin/HudMixin.java') or str(rel).endswith('mixin\\HudMixin.java'):
   text=text.replace('@Shadow public boolean isHidden() { throw new AssertionError(); }','private boolean isHidden() { return Minecraft.getInstance().options.hideGui; }')
  if p.name=='FakeChunkManager.java':
   start=text.index('    public void markAsLoaded(');end=text.index('    public boolean unload(',start)
   text=text[:start]+'''    public void markAsLoaded(int x,int z,LevelChunk chunk) {
        client.levelRenderer.onChunkReadyToRender(new ChunkPos(x,z));
        for(int i=0;i<chunk.getSections().length;i++) {
            int y=chunk.getSectionYFromSectionIndex(i);
            clientChunkManager.onSectionEmptinessChanged(x,y,z,chunk.getSections()[i].hasOnlyAir());
        }
    }
    private void markAsUnloaded(int x,int z,LevelChunk chunk) {
        for(int i=0;i<chunk.getSections().length;i++) {
            int y=chunk.getSectionYFromSectionIndex(i);
            clientChunkManager.getLoadedEmptySections().remove(SectionPos.asLong(x,y,z));
            client.levelRenderer.setSectionDirtyWithNeighbors(x,y,z);
        }
    }

'''+text[end:]
  if p.name=='Waypoints.java':
   text=text.replace('collector.submitShapeOutline(pose,Shapes.box(-.003,-.003,-.003,1.003,1.003,1.003),RenderTypes.linesTranslucent(),0xC0000000|(tint&0xFFFFFF),2f,false);','''collector.submitCustomGeometry(pose,RenderTypes.linesTranslucent(),(matrix,vertices)->{
    int c=0xC0000000|(tint&0xFFFFFF);
    for(int axis=0;axis<3;axis++)for(int a=0;a<2;a++)for(int b=0;b<2;b++){
     float x=axis==0?0:a,y=axis==1?0:axis==0?a:b,z=axis==2?0:b;
     vertices.addVertex(matrix,x,y,z).setColor(c).setNormal(axis==0?1:0,axis==1?1:0,axis==2?1:0).setLineWidth(2);
     vertices.addVertex(matrix,x+(axis==0?1:0),y+(axis==1?1:0),z+(axis==2?1:0)).setColor(c).setNormal(axis==0?1:0,axis==1?1:0,axis==2?1:0).setLineWidth(2);
    }
   });''')
  if p.name=='BlockOutlineMixin.java':text=text.replace('submitBlockOutline','renderBlockOutline').replace('submitHitOutline','renderHitOutline')
  if p.name=='NameScaleMixin.java':text=text.replace('@Mixin(SubmitNodeCollection.class)','@Mixin(net.minecraft.client.renderer.feature.NameTagFeatureRenderer.Storage.class)').replace('method="submitNameTag"','method="add"')
  if p.name=='LevelRendererMixin.java' and 'integration' in str(rel):text=text.replace('@Shadow @Final private GameRenderer gameRenderer;','').replace('method = "render"','method = "renderLevel"').replace('((GameRendererExt) gameRenderer)','((GameRendererExt) net.minecraft.client.Minecraft.getInstance().gameRenderer)')
  if p.name=='WaypointWorldMixin.java':
   text=text.replace('method="submitFeatures"','method="submitEntities"').replace('LevelRenderState state,SubmitNodeCollector collector,boolean outline,CallbackInfo ci','com.mojang.blaze3d.vertex.PoseStack pose,LevelRenderState state,SubmitNodeCollector collector,CallbackInfo ci')
 if version.startswith('1.21'):
  if p.name=='ScreenshotRenderMixin.java':text=text.replace('method="renderFrame"','method="runTick"')
  for old,new in [('GuiGraphicsExtractor','GuiGraphics'),('net.minecraft.client.renderer.state.gui.','net.minecraft.client.gui.render.state.'),('net/minecraft/client/renderer/state/gui/','net/minecraft/client/gui/render/state/'),('net.minecraft.client.renderer.state.level.','net.minecraft.client.renderer.state.'),('net/minecraft/client/renderer/state/level/','net/minecraft/client/renderer/state/'),('extractWidgetRenderState','renderWidget'),('extractRenderState(GuiGraphics','render(GuiGraphics'),('super.extractRenderState','super.render'),('.setScreenAndShow(','.setScreen('),('.centeredText(','.drawCenteredString('),('.text(','.drawString(')]:text=text.replace(old,new)
  if version!='1.21.11':text=text.replace('Identifier','ResourceLocation')
  for old,new in [('Icons.drawString(','Icons.text('),('.itemDecorations(','.renderItemDecorations('),('g.item(', 'g.renderItem('),('.outline(','.renderOutline('),('g.tooltip(', 'g.renderTooltip('),('extractImage(', 'renderImage('),('ChunkPos.pack(', 'ChunkPos.asLong('),('ChunkPos.unpack(', 'new ChunkPos('),('.pack()', '.toLong()'),('.addClientSystemMessage(','.addMessage('),('source.getLevel()','source.getWorld()'),('PayloadTypeRegistry.clientboundPlay()','PayloadTypeRegistry.playS2C()'),('mc.getWindow().updateFullscreenIfChanged();',''),('contents.allItemsCopyStream()','contents.stream().map(ItemStack::copy)'),('.resizeGui()', '.resizeDisplay()')]:text=text.replace(old,new)
  if 'cache' in str(rel):
   for name in ['chunkPos','playerChunkPos','pos','getPos()','packet.pos()']:text=text.replace(name+'.x()',name+'.x').replace(name+'.z()',name+'.z')
  if p.name=='FakeChunkStorage.java':text=text.replace('Optional<Identifier> generatorKey =','var generatorKey =').replace('Optional<ResourceLocation> generatorKey =','var generatorKey =').replace('.map(ResourceKey::identifier)','').replace('.map(ResourceKey::location)','').replace('nbt, -1, contextNbt, SharedConstants.getCurrentVersion().dataVersion().version()','nbt, -1, contextNbt')
  if p.name=='GameRendererMixin.java' and 'integration' in str(rel):
   text=text.replace(')Lnet/minecraft/client/renderer/fog/FogData;',')Lorg/joml/Vector4f;').replace('private FogData updateSkyFogRenderer','private org.joml.Vector4f updateSkyFogRenderer').replace('Operation<FogData> operation','Operation<org.joml.Vector4f> operation').replace('skyFogRenderer.updateBuffer(skyFogRenderer.setupFog(camera, 32, tickCounter, skyDarkness, world));','skyFogRenderer.setupFog(camera, 32, tickCounter, skyDarkness, world);')
  if p.name=='LevelRendererMixin.java' and 'integration' in str(rel):text=text.replace('levelRenderState.cameraRenderState.fogData.renderDistanceEnd >= 32 * 16','net.minecraft.client.Minecraft.getInstance().options.renderDistance().get() >= 32')
  if p.name=='FogEditor.java':text=text.replace('apply(FogData f,FogType type)','apply(FogData f,FogType type,org.joml.Vector4f fogColor)').replace('f.color.set(','fogColor.set(')
  if p.name=='Waypoints.java':text=text.replace('camera.getViewRotationProjectionMatrix(matrix);','matrix.set(mc.gameRenderer.getProjectionMatrix(MonkeyClient.modules().get(Zoom.class).zoomFov(mc.options.fov().get()))).rotate(camera.rotation().conjugate(new org.joml.Quaternionf()));')
  if p.name=='MotionBlurMixin.java':text=text.replace('Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V','Lnet/minecraft/client/renderer/CachedPerspectiveProjectionMatrixBuffer;getBuffer(IIF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;')
  text=text.replace('MonkeyMenuScreen.renderOutline(','MonkeyMenuScreen.outline(')
  if p.name=='Worlds.java':text=text.replace('"r." + pos.x + "." + pos.z + ".meta"','"r." + pos.x() + "." + pos.z() + ".meta"')
  if p.name in ['HudMixin.java','ContainerMixin.java','DebugOverlayMixin.java','TabPingMixin.java']:
   for old,new in [('"extractRenderState"','"render"'),('"extractEffects"','"renderEffects"'),('"extractCrosshair"','"renderCrosshair"'),('"extractTooltip"','"renderTooltip"'),('"extractPingIcon"','"renderPingIcon"'),('"extractFood"','"renderFood"'),('"extractHearts"','"renderHearts"'),(', "showLightmapTexture"','')]:text=text.replace(old,new)
  if p.name=='NametagMixin.java':text=text.replace('submitNameDisplay','submitNameTag')
  if p.name=='GameRendererMixin.java' and 'integration' in str(rel):text=text.replace('@WrapOperation(method = "extractCamera"','@WrapOperation(method = "renderLevel"')
  if p.name=='LevelRendererMixin.java' and 'integration' in str(rel):text=text.replace(';Lnet/minecraft/client/renderer/state/CameraRenderState;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;', ';Lnet/minecraft/client/Camera;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;')
  if version in ['1.21.10','1.21.9']:
   for old,new in [('net.minecraft.util.Util','net.minecraft.Util'),('net.minecraft.client.renderer.rendertype.RenderTypes','net.minecraft.client.renderer.RenderType'),('RenderTypes.','RenderType.'),('RenderType.linesTranslucent()','RenderType.lines()'),('import net.minecraft.world.level.MoonPhase;',''),('MoonPhase phase','int phase'),('.identifier()','.location()'),('camera.yRot()','camera.getYRot()'),('camera.xRot()','camera.getXRot()'),('isOverlayVisible','isF3Visible'),('.setLineWidth(2)',''),('player.level().getGameRules().get(net.minecraft.world.level.gamerules.GameRules.NATURAL_HEALTH_REGENERATION)','player.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_NATURAL_REGENERATION)')]:text=text.replace(old,new)
   if p.name=='ZoomVertexConsumer.java':text=re.sub(r' public VertexConsumer setLineWidth[^\n]+\n','',text)
   if p.name=='SkyEditorMixin.java':text=text.replace('float partial,Camera camera,SkyRenderState state','float partial,net.minecraft.world.phys.Vec3 camera,SkyRenderState state')
   if p.name=='GameRendererMixin.java' and 'integration' in str(rel):text=text.replace(';ILnet/minecraft/client/DeltaTracker;',';IZLnet/minecraft/client/DeltaTracker;').replace('int viewDistance, DeltaTracker','int viewDistance, boolean thickFog, DeltaTracker').replace('camera, 32, tickCounter','camera, 32, thickFog, tickCounter').replace('instance, camera, viewDistance, tickCounter','instance, camera, viewDistance, thickFog, tickCounter')
   if p.name=='FakeChunkStorage.java':text=text.replace('public class FakeChunkStorage extends SimpleRegionStorage {','public class FakeChunkStorage extends SimpleRegionStorage {\n    public IOWorker chunkScanner(){return ((gg.monkeyclient.mixin.RegionStorageAccessor)this).monkey$worker();}').replace('CompoundTag contextNbt = ChunkMap.getChunkDataFixContextTag(worldKey, generatorKey);','CompoundTag contextNbt = new CompoundTag();contextNbt.putString("dimension",worldKey.location().toString());generatorKey.ifPresent(k->contextNbt.putString("generator",k.location().toString()));').replace('nbt = upgradeChunkTag(nbt, -1, contextNbt);','nbt.put("__context",contextNbt);nbt = upgradeChunkTag(nbt, -1);nbt.remove("__context");')
 if version=='26.3':
  if p.name=='LauncherScreen.java':text=text.replace('e.button()','(e.button()==1?0:e.button()==3?1:2)')
  if p.name in ['ColorPickerScreen.java','CrosshairEditorScreen.java']:text=text.replace('e.button()==0','e.button()==1')
  if p.name=='ClickMixin.java':text=text.replace('m.click(info.button())','m.click(info.button()==1?0:info.button()==3?1:-1)')
  if p.name=='ScreenshotRenderMixin.java':text=text.replace('GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V','GameRenderer;render()V')
  text=text.replace('com.mojang.blaze3d.pipeline.RenderPipeline','com.mojang.renderpearl.api.pipeline.RenderPipeline').replace('com/mojang/blaze3d/pipeline/RenderPipeline','com/mojang/renderpearl/api/pipeline/RenderPipeline')
  if p.name=='CloudEditorMixin.java':text=text.replace('@ModifyVariable(method="render"','@ModifyVariable(method="prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V"').replace('@Inject(method="render"','@Inject(method={"render","renderOit"}')
  for old,new in [('org.lwjgl.glfw.GLFW','gg.monkeyclient.platform.LegacyKeys'),('GLFW.','LegacyKeys.'),('com.mojang.blaze3d.buffers.GpuBuffer','com.mojang.renderpearl.api.buffers.GpuBuffer'),('com/mojang/blaze3d/buffers/GpuBuffer','com/mojang/renderpearl/api/buffers/GpuBuffer'),('com.mojang.blaze3d.systems.RenderPass','com.mojang.renderpearl.api.commands.RenderPass'),('e.key()','gg.monkeyclient.platform.LegacyKeys.fromNative(e.key())'),('InputConstants.isKeyDown(window, ','LegacyKeys.isKeyDown('),('pose.mulPose(Axis.','pose.rotate(Axis.'),('light,overlay,null,-1,null,outline','light,overlay,null,-1,outline'),('DefaultTooltipPositioner.INSTANCE,null)','DefaultTooltipPositioner.INSTANCE,null,false)')]:text=text.replace(old,new)
  if p.name=='ZoomVertexConsumer.java':text=text.replace(' public VertexConsumer setUv1',' public VertexConsumer setUv3(float u,float v){delegate.setUv3(u,v);return this;}\n public VertexConsumer setUv1')
  if p.name=='ContainerPreview.java':text=text.replace('contents.allItemsCopyStream()','contents.itemCopies()')
  if p.name=='SkyEditorMixin.java':text=text.replace('state.skyColor=m.skyColor.resolve()&0xFFFFFF','state.skyColor=net.minecraft.util.ARGB.vector3fFromRGB24(m.skyColor.resolve())').replace('state.sunriseAndSunsetColor=0','state.sunriseAndSunsetColor=new org.joml.Vector4f()')
  if p.name=='ClientPacketListenerMixin.java':
   for old,new in [('packet.getX()','packet.x()'),('packet.getZ()','packet.z()'),('packet.getLightData()','packet.lightData()')]:text=text.replace(old,new)
   text=text.replace('Lnet/minecraft/client/multiplayer/ClientPacketListener;updateLevelChunk(IILnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData;)V','Lnet/minecraft/client/multiplayer/ClientChunkCache;replaceWithPacketData(IILnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData;)Lnet/minecraft/world/level/chunk/LevelChunk;')
  if p.name=='ClientChunkCacheMixin.java':text=text.replace('FriendlyByteBuf buf, Map<Heightmap.Types, long[]> heightmaps, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer','ClientboundLevelChunkPacketData data')
  if p.name=='MotionBlurMixin.java':text=text.replace('@ModifyArg(method="renderLevel"','@ModifyArg(method="render3dHud"')
  if p.name=='SkyEditorMixin.java':text=text.replace('monkey$sun(float brightness','monkey$sun(com.mojang.renderpearl.api.commands.RenderPass pass,float brightness').replace('monkey$moon(MoonPhase phase','monkey$moon(com.mojang.renderpearl.api.commands.RenderPass pass,MoonPhase phase').replace('original.call(brightness,pose)','original.call(pass,brightness,pose)').replace('original.call(phase,brightness,pose)','original.call(pass,phase,brightness,pose)')
  if p.name=='ChunkSerializer.java':
   for s in ['BlockUpdates','SkyUpdates','BlockYMask','EmptyBlockYMask','SkyYMask','EmptySkyYMask']:text=text.replace('initialLightData.get'+s+'()','initialLightData.'+s[0].lower()+s[1:]+'()')
 dest=out/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(text,encoding='utf-8')
for overlay in ([root/'ports'/'1.21'] if version.startswith('1.21') else [])+([root/'ports'/'1.21-legacy'] if version in ['1.21.10','1.21.9'] else [])+[root/'ports'/version]+([root/'ports'/'forge'] if loader=='forge' else []):
 if overlay.exists():
  for p in overlay.rglob('*.java'):
   dest=out/p.relative_to(overlay);dest.parent.mkdir(parents=True,exist_ok=True);text=p.read_text(encoding='utf-8');
   if loader=='forge' and version.startswith('1.21'):text=text.replace('getLevel()','getWorld()')
   if loader=='forge' and p.name=='LegacySodiumOptionsMixin.java':continue
   dest.write_text(text,encoding='utf-8')
print('Prepared',version,'sources in',out)
