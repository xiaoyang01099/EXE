package net.xiaoyang010.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.xiaoyang010.ex_enigmaticlegacy.Item.OmegaCore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class TimeStop {
    @Mixin(LivingEntity.class)
    public static class LivingMixin {
        @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
        public void tick(CallbackInfo ci) {
            if (!((LivingEntity) ((Object) this) instanceof Player) && OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }
    }

    @Mixin(Entity.class)
    public static class EntityMixin {
        @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
        public void tick(CallbackInfo ci) {
            if (!((Entity) ((Object) this) instanceof Player) && OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }

        @Inject(method = "canUpdate()Z", at = @At("HEAD"), cancellable = true, remap = false)
        public void canTicking(CallbackInfoReturnable<Boolean> cir) {
            if (!((Entity) ((Object) this) instanceof Player) && OmegaCore.isTimeStopped()) {
                cir.setReturnValue(true);
            }
        }

        @Inject(method = "baseTick", at = @At("HEAD"), cancellable = true)
        public void basetick(CallbackInfo ci) {
            if (!((Entity) ((Object) this) instanceof Player) && OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }

        @Inject(method = "rideTick", at = @At("HEAD"), cancellable = true)
        public void ridetick(CallbackInfo ci) {
            if (!((Entity) ((Object) this) instanceof Player) && OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }

        @Inject(method = "isAlwaysTicking", at = @At("HEAD"), cancellable = true)
        public void isTicking(CallbackInfoReturnable<Boolean> cir) {
            if (!((Entity) ((Object) this) instanceof Player) && OmegaCore.isTimeStopped()) {
                cir.setReturnValue(false);
            }
        }

        @Inject(method = "isNoGravity", at = @At("HEAD"), cancellable = true)
        public void isNoGravity(CallbackInfoReturnable<Boolean> cir) {
            if (!((Entity) ((Object) this) instanceof Player) && OmegaCore.isTimeStopped()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Mixin(ClientLevel.class)
    public static class ClientMixin {
        @Inject(method = "tickTime",at = @At("HEAD"),cancellable = true)
        public void tickTime(CallbackInfo ci) {
            if (OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }
        @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
        public void tickEntities(Entity pMount, Entity pRider, CallbackInfo ci) {
            if (OmegaCore.isTimeStopped()) {
                if (!(pMount instanceof Player)) {
                    ci.cancel();
                }
                if (!(pRider instanceof Player)) {
                    ci.cancel();
                }
            }
        }

        @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
        public void tickEntities1(Entity p_104640_, CallbackInfo ci) {
            if (OmegaCore.isTimeStopped()) {
                if (!(p_104640_ instanceof Player)) {
                    ci.cancel();
                }
            }
        }

        @Inject(method = "doAnimateTick", at = @At("HEAD"), cancellable = true)
        public void doAnimateTick(CallbackInfo ci) {
            if (OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }
        @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
        public void AnimateTick(CallbackInfo ci) {
            if (OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }
    }

    @Mixin(ServerLevel.class)
    public static class ServerLevelMixin {
        @Inject(method = "tickBlock",at = @At("HEAD"),cancellable = true)
        public void tickBlock(BlockPos p_184113_, Block p_184114_, CallbackInfo ci){
            if (OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }
        @Inject(method = "tickFluid",at = @At("HEAD"),cancellable = true)
        public void tickFluid(BlockPos p_184077_, Fluid p_184078_, CallbackInfo ci){
            if (OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }
        @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
        public void tickEntities(Entity p_8648_, CallbackInfo ci) {
            if (OmegaCore.isTimeStopped()) {
                if (!(p_8648_ instanceof Player)) {
                    ci.cancel();
                }
            }
        }
        @Inject(method = "tickTime",at = @At("HEAD"),cancellable = true)
        public void tickTime(CallbackInfo ci) {
            if (OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }
    }
    @Mixin(GameRenderer.class)
    public static class GameRendererMixin{
        @Inject(method = "tick",at = @At("HEAD"),cancellable = true)
        public void tick(CallbackInfo ci) {
            if (OmegaCore.isTimeStopped()) {
                ci.cancel();
            }
        }
    }
    /*@Mixin(value = Util.class, priority = 0x7fffffff)
    public abstract static class UtilMixin {
        @Inject(method = "getMillis", at = @At("RETURN"), cancellable = true)
        private static void getMillis(@NotNull CallbackInfoReturnable<Long> cir) {
            cir.setReturnValue(OmegaCore.isTimeStopped() ? 0L : cir.getReturnValue());
        }
    }*/
   /* @Mixin(value = IntegratedServer.class, priority = 0x7fffffff)
    public static abstract class IntegratedServerMixin extends MinecraftServer {
        public IntegratedServerMixin(Thread pServerThread, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, WorldStem pWorldStem, Proxy pProxy, DataFixer pFixerUpper, @Nullable MinecraftSessionService pSessionService, @Nullable GameProfileRepository pProfileRepository, @Nullable GameProfileCache pProfileCache, ChunkProgressListenerFactory pProgressListenerFactory) {
            super(pServerThread, pStorageSource, pPackRepository, pWorldStem, pProxy, pFixerUpper, pSessionService, pProfileRepository, pProfileCache, pProgressListenerFactory);
        }*/
        /*@Shadow
        private boolean paused;

        @Shadow @Final private static Logger LOGGER;

        @Shadow @Final private Minecraft minecraft;

        @Shadow private int previousSimulationDistance;

        public IntegratedServerMixin(Thread pServerThread, LevelStorageSource.LevelStorageAccess pStorageSource, PackRepository pPackRepository, WorldStem pWorldStem, Proxy pProxy, DataFixer pFixerUpper, @Nullable MinecraftSessionService pSessionService, @Nullable GameProfileRepository pProfileRepository, @Nullable GameProfileCache pProfileCache, ChunkProgressListenerFactory pProgressListenerFactory) {
            super(pServerThread, pStorageSource, pPackRepository, pWorldStem, pProxy, pFixerUpper, pSessionService, pProfileRepository, pProfileCache, pProgressListenerFactory);
        }

        @Shadow protected abstract void tickPaused();

        @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
        public void tick(BooleanSupplier p_120049_, CallbackInfo ci) {
            boolean flag = this.paused;
            this.paused = Minecraft.getInstance().isPaused();
            //inject start
            if (OmegaCore.isTimeStopped())this.paused = false;
            //end
            ProfilerFiller profilerfiller = this.getProfiler();
            if (!flag && this.paused) {
                profilerfiller.push("autoSave");
                LOGGER.info("Saving and pausing game...");
                this.saveEverything(false, false, false);
                profilerfiller.pop();
            }

            boolean flag1 = Minecraft.getInstance().getConnection() != null;
            if (flag1 && this.paused) {
                this.tickPaused();
            } else {
                super.tickServer(p_120049_);
                int i = Math.max(2, this.minecraft.options.renderDistance);
                if (i != this.getPlayerList().getViewDistance()) {
                    LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
                    this.getPlayerList().setViewDistance(i);
                }

                int j = Math.max(2, this.minecraft.options.simulationDistance);
                if (j != this.previousSimulationDistance) {
                    LOGGER.info("Changing simulation distance to {}, from {}", j, this.previousSimulationDistance);
                    this.getPlayerList().setSimulationDistance(j);
                    this.previousSimulationDistance = j;
                }
            }
            ci.cancel();*/

    }
    /*@Mixin(MinecraftServer.class)
    public static abstract class MinecraftServerMixin{
        @Shadow protected abstract boolean initServer() throws IOException;

        @Shadow private long lastOverloadWarning;

        @Shadow protected long nextTickTime;

        @Shadow @Final private ServerStatus status;

        @Shadow @Nullable private String motd;

        @Shadow protected abstract void updateStatusIcon(ServerStatus pResponse);

        @Shadow private volatile boolean running;

        @Shadow @Final private static Logger LOGGER;

        @Shadow private boolean debugCommandProfilerDelayStart;

        //@Shadow @Nullable private MinecraftServer.TimeProfiler debugCommandProfiler;

        @Shadow protected abstract void startMetricsRecordingTick();

        @Shadow private ProfilerFiller profiler;

        @Shadow public abstract void tickServer(BooleanSupplier pHasTimeLeft);

        @Shadow protected abstract boolean haveTime();

        @Shadow private boolean mayHaveDelayedTasks;

        @Shadow private long delayedTasksMaxNextTickTime;

        @Shadow protected abstract void waitUntilNextTick();

        @Shadow protected abstract void endMetricsRecordingTick();

        @Shadow private volatile boolean isReady;

        @Shadow private float averageTickTime;

        @Shadow protected abstract void onServerCrash(CrashReport pReport);

        @Shadow
        private static CrashReport constructOrExtractCrashReport(Throwable pCause) {
            return null;
        }

        @Shadow public abstract SystemReport fillSystemReport(SystemReport pSystemReport);

        @Shadow public abstract File getServerDirectory();

        @Shadow private boolean stopped;

        @Shadow public abstract void stopServer();

        @Shadow @Final @Nullable private GameProfileCache profileCache;

        @Shadow public abstract void onServerExit();

        //@Shadow @Nullable private MinecraftServer.TimeProfiler debugCommandProfiler;

        @Inject(method = "runServer",at = @At("HEAD"), cancellable = true)
        public void runServer(CallbackInfo ci){
            MinecraftServer this_ = (MinecraftServer)(Object)this;
            try {

                if (this.initServer()) {
                    ServerLifecycleHooks.handleServerStarted(this_);
                    this.nextTickTime = Util.getMillis();
                    this.status.setDescription(new TextComponent(this.motd));
                    this.status.setVersion(new ServerStatus.Version(SharedConstants.getCurrentVersion().getName(), SharedConstants.getCurrentVersion().getProtocolVersion()));
                    this.updateStatusIcon(this.status);

                    while(this.running) {
                        long i = Util.getMillis() - this.nextTickTime;
                        if (i > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
                            long j = i / 50L;
                            LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
                            this.nextTickTime += j * 50L;
                            this.lastOverloadWarning = this.nextTickTime;
                        }

                        if (this.debugCommandProfilerDelayStart) {
                            this.debugCommandProfilerDelayStart = false;
                            //this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
                        }

                        this.nextTickTime += 50L;
                        this.startMetricsRecordingTick();
                        this.profiler.push("tick");
                        this.tickServer(this::haveTime);
                        this.profiler.popPush("nextTickWait");
                        this.mayHaveDelayedTasks = true;
                        this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                        this.waitUntilNextTick();
                        this.profiler.pop();
                        this.endMetricsRecordingTick();
                        this.isReady = true;
                        JvmProfiler.INSTANCE.onServerTick(this.averageTickTime);
                    }

                    ServerLifecycleHooks.handleServerStopping(this_);
                    ServerLifecycleHooks.expectServerStopped();
                } else {
                    ServerLifecycleHooks.expectServerStopped();
                    this.onServerCrash((CrashReport)null);
                }
            } catch (Throwable var44) {
                Throwable throwable1 = var44;
                LOGGER.error("Encountered an unexpected exception", throwable1);
                CrashReport crashreport = constructOrExtractCrashReport(throwable1);
                this.fillSystemReport(crashreport.getSystemReport());
                File var10002 = new File(this.getServerDirectory(), "crash-reports");
                SimpleDateFormat var10003 = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
                Date var10004 = new Date();
                File file1 = new File(var10002, "crash-" + var10003.format(var10004) + "-server.txt");
                if (crashreport.saveToFile(file1)) {
                    LOGGER.error("This crash report has been saved to: {}", file1.getAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }

                ServerLifecycleHooks.expectServerStopped();
                this.onServerCrash(crashreport);
            } finally {
                try {
                    this.stopped = true;
                    this.stopServer();
                } catch (Throwable var42) {
                    Throwable throwable = var42;
                    LOGGER.error("Exception stopping the server", throwable);
                } finally {
                    if (this.profileCache != null) {
                        this.profileCache.clearExecutor();
                    }

                    ServerLifecycleHooks.handleServerStopped(this_);
                    this.onServerExit();
                }
            }
            ci.cancel();
        }
    }*/